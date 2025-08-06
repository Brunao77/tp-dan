package isi.dan.msclientes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.EstadoObra;
import isi.dan.msclientes.model.Obra;
import isi.dan.msclientes.servicios.ObraService;

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

import isi.dan.msclientes.model.EstadoObra;

@WebMvcTest(ObraController.class)
public class ObraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ObraService obraService;

    private Obra obra;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1);
        cliente.setNombre("Cliente Test");

        obra = new Obra();
        obra.setId(1);
        obra.setDireccion("Direccion Test");
        obra.setPresupuesto(BigDecimal.valueOf(50000));
        obra.setEstado(EstadoObra.PENDIENTE);
        obra.setCliente(cliente);
    }

    @Test
    void testGetAll() throws Exception {
        Mockito.when(obraService.findAll()).thenReturn(Collections.singletonList(obra));

        mockMvc.perform(get("/api/obras"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].direccion").value("Direccion Test"));
    }

    @Test
    void testGetById() throws Exception {
        Mockito.when(obraService.findById(1)).thenReturn(Optional.of(obra));

        mockMvc.perform(get("/api/obras/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.direccion").value("Direccion Test"));
    }

    @Test
    void testGetById_NotFound() throws Exception {
        Mockito.when(obraService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/obras/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        Mockito.when(obraService.save(Mockito.any(Obra.class))).thenReturn(obra);

        mockMvc.perform(post("/api/obras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(obra)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direccion").value("Direccion Test"));
    }

    @Test
    void testUpdate() throws Exception {
        Mockito.when(obraService.findById(1)).thenReturn(Optional.of(obra));
        Mockito.when(obraService.update(Mockito.any(Obra.class))).thenReturn(obra);

        mockMvc.perform(put("/api/obras/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(obra)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direccion").value("Direccion Test"));
    }

    @Test
    void testUpdate_NotFound() throws Exception {
        Mockito.when(obraService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/obras/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(obra)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDelete() throws Exception {
        Mockito.when(obraService.findById(1)).thenReturn(Optional.of(obra));
        Mockito.doNothing().when(obraService).deleteById(1);

        mockMvc.perform(delete("/api/obras/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDelete_NotFound() throws Exception {
        Mockito.when(obraService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/obras/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAsignarCliente() throws Exception {
        Mockito.when(obraService.asignarCliente(1, 1)).thenReturn(obra);

        mockMvc.perform(put("/api/obras/1/asignar-cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direccion").value("Direccion Test"));
    }

    @Test
    void testHabilitar() throws Exception {
        Mockito.when(obraService.findById(1)).thenReturn(Optional.of(obra));
        Mockito.when(obraService.habilitar(obra)).thenReturn(obra);
        obra.setEstado(EstadoObra.HABILITADA);

        mockMvc.perform(put("/api/obras/1/habilitar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("HABILITADA"));
    }

    @Test
    void testDeshabilitar() throws Exception {
        Mockito.when(obraService.findById(1)).thenReturn(Optional.of(obra));
        Mockito.when(obraService.deshabilitar(obra)).thenReturn(obra);
        obra.setEstado(EstadoObra.PENDIENTE);

        mockMvc.perform(put("/api/obras/1/deshabilitar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void testfinalizar() throws Exception {
        Mockito.when(obraService.findById(1)).thenReturn(Optional.of(obra));
        Mockito.when(obraService.finalizar(obra)).thenReturn(obra);
        obra.setEstado(EstadoObra.FINALIZADA);

        mockMvc.perform(put("/api/obras/1/finalizar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADA"));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

