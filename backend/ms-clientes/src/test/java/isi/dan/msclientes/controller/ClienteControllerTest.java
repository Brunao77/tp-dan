package isi.dan.msclientes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.servicios.ClienteService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
public class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1);
        cliente.setNombre("Cliente Test");
        cliente.setCorreoElectronico("cliente@test.com");
        cliente.setCuit("30-12345678-9");
        cliente.setMaximoDescubierto(BigDecimal.valueOf(100000));
        cliente.setMaximoCantidadObras(3);
    }

    @Test
    void testGetAll() throws Exception {
        Mockito.when(clienteService.findAll()).thenReturn(Collections.singletonList(cliente));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nombre").value("Cliente Test"));
    }

    @Test
    void testGetById() throws Exception {
        Mockito.when(clienteService.findById(1)).thenReturn(Optional.of(cliente));

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombre").value("Cliente Test"))
                .andExpect(jsonPath("$.cuit").value("30-12345678-9"));
    }
    @Test
    void testGetById_NotFound() throws Exception {
        Mockito.when(clienteService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/clientes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        Mockito.when(clienteService.save(Mockito.any(Cliente.class))).thenReturn(cliente);

        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cliente Test"));
    }

    @Test
    void testUpdate() throws Exception {
        Mockito.when(clienteService.findById(1)).thenReturn(Optional.of(cliente));
        Mockito.when(clienteService.update(Mockito.any(Cliente.class))).thenReturn(cliente);

        mockMvc.perform(put("/api/clientes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cliente Test"));
    }

    @Test
    void testUpdate_NotFound() throws Exception {
        Mockito.when(clienteService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/clientes/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(cliente)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDelete() throws Exception {
        Mockito.when(clienteService.findById(1)).thenReturn(Optional.of(cliente));
        Mockito.doNothing().when(clienteService).deleteById(1);

        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDelete_NotFound() throws Exception {
        Mockito.when(clienteService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/clientes/99"))
                .andExpect(status().isNotFound());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

