package isi.dan.ms.pedidos.servicio;

import isi.dan.ms.pedidos.modelo.*;
import isi.dan.ms.pedidos.utils.SequenceGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import isi.dan.ms.pedidos.conf.RabbitMQConfig;
import isi.dan.ms.pedidos.dao.PedidoRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PedidoService {
    
    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final SequenceGeneratorService seqService;

    private final  String gatewayUrl = "http://ms-gateway-svc:8080";

    @Transactional
    public Pedido savePedido(Pedido pedido) {

        /* ---------- 1. Validaciones básicas ---------- */
        if (pedido.getDetalle() == null || pedido.getDetalle().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El pedido debe contener al menos un ítem");
        }

        /* ---------- 2. Número y fecha ---------- */
        pedido.setNumeroPedido((int) seqService.next("pedidoSeq"));
        pedido.setFecha(Instant.now());

        /* ---------- 3. Obtener cliente por Gateway ---------- */
        Cliente cliente = restTemplate.getForObject(
                gatewayUrl + "/clientes/{id}",
                Cliente.class,
                pedido.getCliente().getId());

        if (cliente == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Cliente " + pedido.getCliente().getId() + " no encontrado");
        }
        pedido.setCliente(cliente);

        /* ---------- 4. Saldo descubierto actualmente comprometido ---------- */
        BigDecimal saldoComprometido = pedidoRepository
                .findByClienteIdAndEstadoIn(
                        cliente.getId(),
                        List.of(EstadoPedido.ACEPTADO, EstadoPedido.EN_PREPARACION))
                .stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        /* ---------- 5. Recorrer ítems, traer productos y calcular total ---------- */
        BigDecimal total = BigDecimal.ZERO;
        boolean stockSuficiente = true;

        for (DetallePedido det : pedido.getDetalle()) {

            // 5.1 Llamada al MS-PRODUCTOS vía Gateway
            Producto prd = restTemplate.getForObject(
                    gatewayUrl + "/productos/{id}",
                    Producto.class,
                    det.getProducto().getId());

            if (prd == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto " + det.getProducto().getId() + " no encontrado");
            }

            // 5.2 Completar datos del detalle
            det.setProducto(prd);
            det.setPrecioUnitario(prd.getPrecio());

            BigDecimal subTotal = prd.getPrecio()
                    .multiply(BigDecimal.valueOf(det.getCantidad()));
            det.setPrecioFinal(subTotal);

            total = total.add(subTotal);

            int disponible = prd.getStockActual() != null ? prd.getStockActual() : 0;
            if (disponible < det.getCantidad()) {
                stockSuficiente = false;
            }
        }
        pedido.setTotal(total);

        /* ---------- 6. Regla de descubierto ---------- */
        BigDecimal maxDesc = cliente.getMaximoDescubierto() != null ? cliente.getMaximoDescubierto() : BigDecimal.ZERO;
        if (saldoComprometido.add(total).compareTo(maxDesc) > 0) {
            pedido.setEstado(EstadoPedido.RECHAZADO);
            return pedidoRepository.save(pedido);
        }
        pedido.setEstado(EstadoPedido.ACEPTADO);

        /* ---------- 7. Reserva de stock si esta OK ---------- */
        if (stockSuficiente) {
            actualizarEstadoPedido(pedido, EstadoPedido.EN_PREPARACION);

            for (DetallePedido det : pedido.getDetalle()) {
                String mensaje = det.getProducto().getId() + ";" + det.getCantidad();
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.STOCK_UPDATE_QUEUE,
                        mensaje);
            }
        }

        /* ---------- 8. Persistir y devolver ---------- */
        return pedidoRepository.save(pedido);
    }


    public List<Pedido> getAllPedidos() {
        return pedidoRepository.findAll();
    }

    public Pedido getPedidoById(String id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    public void deletePedido(String id) {
        pedidoRepository.deleteById(id);
    }

    public Pedido updatePedido(Pedido pedido, String id) {
        pedido.setId(id);
        return pedidoRepository.save(pedido);
    }

    public Pedido getPedidoByNumero(String numeroPedido) {
        Integer num;
        try {
            num = Integer.valueOf(numeroPedido);
        } catch (NumberFormatException ex) {
            // número inválido → devolver null para que el controller responda 404
            return null;
        }
        return pedidoRepository.findByNumeroPedido(num).orElse(null);
    }

    public List<Pedido> getAllPedidosByCliente(String id) {
        List<Pedido> pedidos = new ArrayList<>();
        log.info("Obteniendo pedidos");
        for (Pedido p : this.getAllPedidos()) {
            log.info("Pedido con id cliente: " + p.getCliente().getId());
            if(String.valueOf(p.getCliente().getId()).equals(id)){
                pedidos.add(p);
                log.info("Pedido agregado");
            }
        }
        return pedidos;
    }

    private Integer getUltimoNumeroPedido() {
        List<Pedido> pedidos = this.getAllPedidos();
        return pedidos.isEmpty() ? 0 : pedidos.stream()
                .map(Pedido::getNumeroPedido)
                .max(Integer::compareTo).orElse(0);
    }

    //metodo para agregar al pedido el estado anterior al historial y actualizar al estado actual
    public void actualizarEstadoPedido(Pedido pedido, EstadoPedido estado) {
        HistorialEstado nuevo = new HistorialEstado();
        nuevo.setEstado(pedido.getEstado());
        nuevo.setFechaEstado(Instant.now());
        nuevo.setUserEstado(pedido.getUsuario());
        pedido.getEstados().add(nuevo);

        pedido.setEstado(estado);

        log.info("Estado actualizado a " + pedido.getEstado());
    }

    //cuando se cancela el pedido se envia un mensaje por RabbitMQ indicando el restock
    // lo llamamos desde el controller???
    @Transactional
    public void restockearProductos(Pedido pedido) {
        log.info("Restockear productos");
        for (DetallePedido dp : pedido.getDetalle()) {
            log.info("Enviando {}", dp.getProducto().getId() + ";" + dp.getCantidad());
            rabbitTemplate.convertAndSend(RabbitMQConfig.STOCK_UPDATE_QUEUE,
                    dp.getProducto().getId() + ";" + (-dp.getCantidad()));
        }
    }

}
