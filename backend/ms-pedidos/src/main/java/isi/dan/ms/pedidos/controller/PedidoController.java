package isi.dan.ms.pedidos.controller;
import isi.dan.ms.pedidos.dto.DetalleDTO;
import isi.dan.ms.pedidos.dto.PedidoCreateDTO;
import isi.dan.ms.pedidos.exception.ClienteNotFoundException;
import isi.dan.ms.pedidos.exception.PedidoNotFoundException;

import isi.dan.ms.pedidos.modelo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import isi.dan.ms.pedidos.servicio.PedidoService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    Logger log = LoggerFactory.getLogger(PedidoController.class);

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<Pedido> createPedido(@RequestBody PedidoCreateDTO pedidoCreateDTO) {
        Pedido pedido = mapToEntity(pedidoCreateDTO);
        Pedido saved = pedidoService.savePedido(pedido);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<Pedido> getAllPedidos() {
        return pedidoService.getAllPedidos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> getPedidoById(@PathVariable String id) throws PedidoNotFoundException {
        Pedido pedido = pedidoService.getPedidoById(id);
        if (pedido == null) {
            throw new PedidoNotFoundException("Pedido " +id +" no encontrado");
        }
        return ResponseEntity.ok(pedido);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePedido(@PathVariable String id) throws PedidoNotFoundException {
        Pedido pedido = pedidoService.getPedidoById(id);
        if (pedido == null) {
            throw new PedidoNotFoundException("Pedido " +id +" no encontrado");
        }
        log.info("pedido eliminado");
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pedido> updatePedido(@PathVariable String id, @RequestBody Pedido pedido) throws PedidoNotFoundException {
        Pedido p = pedidoService.getPedidoById(id);
        if (p == null) {
            throw new PedidoNotFoundException("Pedido " +id +" no encontrado");
        }
        return ResponseEntity.ok(pedidoService.updatePedido(pedido, id));
    }

    @GetMapping("/numeroPedido/{numero}")
    public ResponseEntity<Pedido> getPedidoByNumero(@PathVariable Integer numero) throws PedidoNotFoundException {
        Pedido pedido = pedidoService.getPedidoByNumero(String.valueOf(numero));
        if (pedido == null) {
            throw new PedidoNotFoundException("Pedido con numeroPedido " +numero +" no encontrado");
        }
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/cliente/{id}")
    public ResponseEntity<List<Pedido>> getPedidosByCliente(@PathVariable String id) throws ClienteNotFoundException {
        List<Pedido> pedidos = pedidoService.getAllPedidosByCliente(id);
        if (pedidos == null) {
            throw new ClienteNotFoundException("No hay pedidos con el cliente " + id);
        }
        log.info(pedidos.toString());
        return ResponseEntity.ok(pedidos);
    }

    @DeleteMapping("/numeroPedido/{numero}")
    public ResponseEntity<Pedido> detelePedidoByNumero(@PathVariable String numero) throws PedidoNotFoundException {
        Pedido pedido = pedidoService.getPedidoByNumero(numero);
        if (pedido == null) {
            throw new PedidoNotFoundException("Pedido con numeroPedido " +numero +" no encontrado");
        }
        pedidoService.deletePedido(pedido.getId());
        log.debug("pedido eliminado");
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/numeroPedido/{numero}")
    public ResponseEntity<Pedido> updatePedidoByNumero(@PathVariable String numero, @RequestBody Pedido pedido) throws PedidoNotFoundException {
        Pedido p = pedidoService.getPedidoByNumero(numero);
        if (p == null) {
            throw new PedidoNotFoundException("Pedido con numeroPedido " + numero + " no encontrado");
        }
        log.debug("pedido Updateado");
        return ResponseEntity.ok(pedidoService.updatePedido(pedido, pedido.getId()));
    }

    //acutalizarPedidoEstado(pedido, estado);
    @PutMapping("/{id}/estado/{estado}")
    public ResponseEntity<Pedido> updatePedidoEstado(@PathVariable String id, @PathVariable String estado) throws PedidoNotFoundException {
        Pedido pedido = pedidoService.getPedidoById(id);
        if (pedido == null) {
            throw new PedidoNotFoundException("Pedido con id " + id + " no encontrado");
        }
        EstadoPedido estadoPedido = EstadoPedido.valueOf(estado);
        //si el estado es cancelado hay que restockear los productos
        //Mensaje JMS se manda desde el metodo del Service
        if( estadoPedido == EstadoPedido.CANCELADO) {
            pedidoService.restockearProductos(pedido);
        }
        pedidoService.actualizarEstadoPedido(pedido, estadoPedido);
        log.debug("pedido Actualizado por estado");
        return ResponseEntity.ok(pedidoService.updatePedido(pedido,pedido.getId()));
    }

    @PutMapping("/numeroPedido/{numero}/estado/{estado}")
    public ResponseEntity<Pedido> updateByNumeroYEstado(@PathVariable String numero, @PathVariable String estado) throws PedidoNotFoundException {
        Pedido p = pedidoService.getPedidoByNumero(numero);
        if (p == null) {
            throw new PedidoNotFoundException("Pedido con numeroPedido " + numero + " no encontrado");
        }
        EstadoPedido estadoPedido = EstadoPedido.valueOf(estado);
        //si el estado es cancelado hay que restockear los productos
        //Mensaje JMS se manda desde el metodo del Service
        if( estadoPedido == EstadoPedido.CANCELADO) {
            pedidoService.restockearProductos(p);
        }
        pedidoService.actualizarEstadoPedido(p, estadoPedido);
        log.debug("pedido Actualizado por estado");
        return ResponseEntity.ok(pedidoService.updatePedido(p,p.getId()));
    }

    /* ---------- mapper sencillo DTO → entidad ---------- */
    private Pedido mapToEntity(PedidoCreateDTO dto) {
        Pedido p = new Pedido();
        p.setCliente(new Cliente()); p.getCliente().setId(dto.getIdCliente());
        p.setObra(new Obra());       p.getObra().setId(dto.getIdObra());
        p.setUsuario(dto.getUsuario());
        p.setObservaciones(dto.getObservaciones());

        List<DetallePedido> dets = new ArrayList<>();
        for (DetalleDTO d : dto.getDetalle()) {
            DetallePedido dp = new DetallePedido();
            dp.setProducto(new Producto());
            dp.getProducto().setId(d.getIdProducto());
            dp.setCantidad(d.getCantidad());
            dets.add(dp);
        }
        p.setDetalle(dets);
        return p;
    }

}