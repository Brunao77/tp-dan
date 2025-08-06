package isi.dan.msclientes.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import isi.dan.msclientes.dao.ObraRepository;
import isi.dan.msclientes.exception.ClienteNotFoundException;
import isi.dan.msclientes.exception.ObraNotFoundException;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.EstadoObra;
import isi.dan.msclientes.model.Obra;
import isi.dan.msclientes.servicios.ClienteService;
import isi.dan.msclientes.servicios.ObraService;

@ExtendWith(MockitoExtension.class)
public class ObraServiceTest {

    @Mock
    private ObraRepository obraRepository;

    @Mock
    private ClienteService clienteService;

    @Spy
    private ObraService obraService = new ObraService();

    private Obra obra;
    private Cliente cliente;
    private Cliente otroCliente;

    @BeforeEach
    void iniciarDatos() {
        // Inyectar las dependencias manualmente en el spy
        ReflectionTestUtils.setField(obraService, "obraRepository", obraRepository);
        ReflectionTestUtils.setField(obraService, "clienteService", clienteService);

        cliente = new Cliente();
        cliente.setId(1);
        cliente.setNombre("Cliente Prueba");
        cliente.setCorreoElectronico("cliente@test.com");
        cliente.setCuit("20-12345678-9");
        cliente.setMaximoDescubierto(new BigDecimal(10000));
        cliente.setMaximoCantidadObras(3);
        cliente.setObras(new ArrayList<>());

        // otroCliente se usa para testear reasignaciones
        otroCliente = new Cliente();
        otroCliente.setId(2);
        otroCliente.setNombre("Otro Cliente");
        otroCliente.setCorreoElectronico("otro@test.com");
        otroCliente.setCuit("20-98765432-1");
        otroCliente.setMaximoDescubierto(new BigDecimal(20000));
        otroCliente.setMaximoCantidadObras(3);
        otroCliente.setObras(new ArrayList<>());

        obra = new Obra();
        obra.setId(1);
        obra.setDireccion("Direccion Test");
        obra.setEsRemodelacion(false);
        obra.setLat(30f);
        obra.setLng(30f);
        obra.setPresupuesto(BigDecimal.valueOf(5000));
        obra.setCliente(null);
    }

    @Test
    public void testAsignarClienteExitosoConEstadoHabilitada() throws ObraNotFoundException, ClienteNotFoundException {
        when(obraRepository.findById(1)).thenReturn(Optional.of(obra));
        when(clienteService.findById(1)).thenReturn(Optional.of(cliente));
        
        // Mock para que validarHabilitacionObra retorne true
        doReturn(true).when(obraService).validarHabilitacionObra(any(Obra.class), any(Cliente.class));
        
        // Mock del método update
        doReturn(obra).when(obraService).update(any(Obra.class));
        
        Obra resultado = obraService.asignarCliente(1, 1);
        
        // Assert
        assertNotNull(resultado);
        assertEquals(cliente, resultado.getCliente());
        assertEquals(EstadoObra.HABILITADA, resultado.getEstado());
        assertTrue(cliente.getObras().contains(obra));
        
        // Verify
        verify(obraRepository).findById(1);
        verify(clienteService).findById(1);
        verify(obraService).validarHabilitacionObra(any(Obra.class), eq(cliente));
        verify(obraService).update(obra);
    }
    
    @Test
    public void testAsignarClienteExitosoConEstadoPendiente() throws ObraNotFoundException, ClienteNotFoundException {
        when(obraRepository.findById(1)).thenReturn(Optional.of(obra));
        when(clienteService.findById(1)).thenReturn(Optional.of(cliente));
        
        // Mock para que validarHabilitacionObra retorne false
        doReturn(false).when(obraService).validarHabilitacionObra(any(Obra.class), any(Cliente.class));
        
        // Mock del método update
        doReturn(obra).when(obraService).update(any(Obra.class));
        
        Obra resultado = obraService.asignarCliente(1, 1);
        
        // Assert
        assertNotNull(resultado);
        assertEquals(cliente, resultado.getCliente());
        assertEquals(EstadoObra.PENDIENTE, resultado.getEstado());
        assertTrue(cliente.getObras().contains(obra));
    }
    
    @Test
    public void testAsignarClienteObraYaAsignada() throws ObraNotFoundException, ClienteNotFoundException {
        obra.setCliente(cliente);
        cliente.getObras().add(obra);
        
        when(obraRepository.findById(1)).thenReturn(Optional.of(obra));
        when(clienteService.findById(2)).thenReturn(Optional.of(otroCliente));
        
        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            obraService.asignarCliente(1, 2);
        });
        
        assertEquals("La obra ya se encuentra asignada a un cliente", exception.getMessage());
        
        // Verify
        verify(obraRepository).findById(1);
        verify(clienteService).findById(2);
        // Verificar que no se llamó a update
        verify(obraService, never()).update(any(Obra.class));
    }
    
    @Test
    public void testAsignarClienteObraNoEncontrada() {
        when(obraRepository.findById(999)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(ObraNotFoundException.class, () -> {
            obraService.asignarCliente(999, 1);
        });
        
        assertEquals("Obra 999 no encontrada", exception.getMessage());
    }
    
    @Test
    public void testAsignarClienteClienteNoEncontrado() {
        when(obraRepository.findById(1)).thenReturn(Optional.of(obra));
        when(clienteService.findById(999)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(ClienteNotFoundException.class, () -> {
            obraService.asignarCliente(1, 999);
        });
        
        assertEquals("Cliente 999 no encontrado", exception.getMessage());
    }

    @Test
    void testHabilitarObraYaHabilitada() {
        // obra habilitada a habilitada lanza excepción
        obra.setEstado(EstadoObra.HABILITADA);

        // Verificar excepción
        assertThrows(IllegalStateException.class, () -> {
            obraService.habilitar(obra);
        });
    }

    @Test
    void testHabilitar_ObraFinalizada() {
        // obra finalizada a habilitada lanza excepción
        obra.setEstado(EstadoObra.FINALIZADA);

        // Verificar excepción
        assertThrows(IllegalStateException.class, () -> {
            obraService.habilitar(obra);
        });
    }

    @Test
    void testDeshabilitarExitoso() {
        // Obra habilitada a pendiente exitoso
        obra.setEstado(EstadoObra.HABILITADA);

        // Configurar mocks
        when(obraRepository.save(obra)).thenReturn(obra);

        // Ejecutar método
        Obra resultado = obraService.deshabilitar(obra);

        // Verificaciones
        assertEquals(EstadoObra.PENDIENTE, resultado.getEstado());
        verify(obraRepository).save(obra);
    }

    @Test
    void testDeshabilitarObraPendiente() {
        // obra pendiente a pendiente lanza excepción
        obra.setEstado(EstadoObra.PENDIENTE);

        // Verificar excepción
        assertThrows(IllegalStateException.class, () -> {
            obraService.deshabilitar(obra);
        });
    }

    @Test
    void testDeshabilitarObraFinalizada() {
        // Obra finalizada a pendiente lanza excepción
        obra.setEstado(EstadoObra.FINALIZADA);

        // Verificar excepción
        assertThrows(IllegalStateException.class, () -> {
            obraService.deshabilitar(obra);
        });
    }

    @Test
    void testFinalizarExitoso() {
        //Obra habilitada a finalizada exitoso
        obra.setEstado(EstadoObra.HABILITADA);

        // Configurar mocks
        when(obraRepository.save(obra)).thenReturn(obra);

        Obra resultado = obraService.finalizar(obra);

        // Verificaciones
        assertEquals(EstadoObra.FINALIZADA, resultado.getEstado());
        verify(obraRepository).save(obra);
    }

    @Test
    void testFinalizarObraPendiente() {
        // Obra pendiente a finalizada lanza excepción
        obra.setEstado(EstadoObra.PENDIENTE);

        // Verificar excepción
        assertThrows(IllegalStateException.class, () -> {
            obraService.finalizar(obra);
        });
    }

    @Test
    void testFinalizarObraYaFinalizada() {
        // Obra finalizada a finalizada lanza excepción
        obra.setEstado(EstadoObra.FINALIZADA);

        // Verificar excepción
        assertThrows(IllegalStateException.class, () -> {
            obraService.finalizar(obra);
        });
    }

}
