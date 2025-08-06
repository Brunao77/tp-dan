package isi.dan.ms.pedidos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import isi.dan.ms.pedidos.modelo.*;
import isi.dan.ms.pedidos.servicio.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PedidoController.class)
public class PedidoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    private Pedido pedido1;
    private Pedido pedido2;
    private Producto producto;
    private Cliente cliente;
    private Obra obra;

    @BeforeEach
    void setUp() {
        // crear un cliente
        cliente = new Cliente();
        cliente.setId(1);
        cliente.setNombre("Cliente 1");
        cliente.setCorreoElectronico("cliente1@test.com");
        cliente.setCuit("20-12345678-9");
        cliente.setMaximoDescubierto(new BigDecimal(15000));

        // crear obra
        obra = new Obra();
        obra.setId(1);
        obra.setLat((float) -34.6037);
        obra.setLng((float) -58.3816);
        obra.setPresupuesto(new BigDecimal("500000.00"));
        obra.setDireccion("Calle Falsa 123");
        obra.setEstado(EstadoObra.PENDIENTE);
        obra.setEsRemodelacion(false);
        obra.setCliente(cliente);

        //Crear producto
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto 1");
        producto.setPrecio(new BigDecimal(200));
        producto.setDescripcion("Descripcion 1");
        producto.setStockActual(50);

        // crear detalle pedido
        DetallePedido detallePedido = new DetallePedido();
        detallePedido.setProducto(producto);
        detallePedido.setCantidad(5);
        detallePedido.setPrecioUnitario(new BigDecimal("200.00"));
        detallePedido.setDescuento(new BigDecimal("10.00"));
        detallePedido.setPrecioFinal(new BigDecimal("990.00"));

        List<DetallePedido> detallePedidoList = new ArrayList<>();
        detallePedidoList.add(detallePedido);

        //crear primer pedido con estado aceptado
        pedido1 = new Pedido();
        pedido1.setId("Pedido 1");
        pedido1.setNumeroPedido(123);
        pedido1.setObservaciones("Observaciones 1");
        pedido1.setUsuario("Usuario 1");
        pedido1.setCliente(cliente);
        pedido1.setEstado(EstadoPedido.ACEPTADO);
        pedido1.setDetalle(detallePedidoList);
        pedido1.setObra(obra);

        //crear segundo pedido con estado rechazado
        pedido2 = new Pedido();
        pedido2.setId("Pedido 2");
        pedido2.setNumeroPedido(123);
        pedido2.setObservaciones("Observaciones 2");
        pedido2.setUsuario("Usuario 1");
        pedido2.setCliente(cliente);
        pedido2.setEstado(EstadoPedido.RECHAZADO);
        pedido2.setDetalle(detallePedidoList);
        pedido2.setObra(obra);

    }

    @Test
    void testGetAll() throws Exception {
        Mockito.when(pedidoService.getAllPedidos()).thenReturn(Collections.singletonList(pedido1));

        mockMvc.perform(get("/api/pedidos")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].usuario").value("Usuario 1"))
                .andExpect(jsonPath("$[0].observaciones").value("Observaciones 1"))
                .andExpect(jsonPath("$[0].estado").value("ACEPTADO"));
    }

    @Test
    void testSave() throws Exception {
        Mockito.when(pedidoService.savePedido(Mockito.any(Pedido.class))).thenReturn(pedido1);

        mockMvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content(asJsonString(pedido1)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.numeroPedido").value(123))
                .andExpect(jsonPath("$.usuario").value("Usuario 1"))
                .andExpect(jsonPath("$.observaciones").value("Observaciones 1"))
                .andExpect(jsonPath("$.estado").value("ACEPTADO"));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
