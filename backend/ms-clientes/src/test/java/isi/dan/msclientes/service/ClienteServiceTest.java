package isi.dan.msclientes.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import isi.dan.msclientes.dao.ClienteRepository;
import isi.dan.msclientes.exception.UsuarioHabilitadoNotFoundException;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.EstadoObra;
import isi.dan.msclientes.model.Obra;
import isi.dan.msclientes.model.UsuarioHabilitado;
import isi.dan.msclientes.servicios.ClienteService;
import isi.dan.msclientes.servicios.UsuarioHabilitadoService;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioHabilitadoService usuarioHabilitadoService;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;
    private Obra obraHabilitada1;
    private Obra obraHabilitada2;
    private Obra obraPendiente;
    private UsuarioHabilitado usuarioHabilitado;

    @BeforeEach
    void iniciarDatos() {
        // Configurar cliente
        cliente = new Cliente();
        cliente.setId(1);
        cliente.setNombre("Cliente Test");
        cliente.setCorreoElectronico("cliente@test.com");
        cliente.setCuit("20-12345678-9");
        cliente.setMaximoDescubierto(new BigDecimal(100000));
        cliente.setMaximoCantidadObras(5);
        cliente.setObras(new ArrayList<>());
        cliente.setUsuariosHabilitados(new ArrayList<>());

        // Configurar obras
        obraHabilitada1 = new Obra();
        obraHabilitada1.setId(1);
        obraHabilitada1.setDireccion("Dirección 1");
        obraHabilitada1.setEstado(EstadoObra.HABILITADA);
        obraHabilitada1.setPresupuesto(new BigDecimal(20000));
        obraHabilitada1.setCliente(cliente);

        obraHabilitada2 = new Obra();
        obraHabilitada2.setId(2);
        obraHabilitada2.setDireccion("Dirección 2");
        obraHabilitada2.setEstado(EstadoObra.HABILITADA);
        obraHabilitada2.setPresupuesto(new BigDecimal(30000));
        obraHabilitada2.setCliente(cliente);

        obraPendiente = new Obra();
        obraPendiente.setId(3);
        obraPendiente.setDireccion("Dirección 3");
        obraPendiente.setEstado(EstadoObra.PENDIENTE);
        obraPendiente.setPresupuesto(new BigDecimal(15000));
        obraPendiente.setCliente(cliente);

        // Configurar usuario
        usuarioHabilitado = new UsuarioHabilitado();
        usuarioHabilitado.setId(1);
        usuarioHabilitado.setCliente(cliente);
    }

     @Test
    void getDescubierto_DebeCalcularSumatoriaDeObrasHabilitadas() {
        // Arrange
        cliente.setObras(Arrays.asList(obraHabilitada1, obraHabilitada2, obraPendiente));
        BigDecimal descubiertoEsperado = new BigDecimal(50000); // 20000 + 30000

        // Act
        BigDecimal resultado = clienteService.getDescubierto(cliente);

        // Assert
        assertEquals(0, descubiertoEsperado.compareTo(resultado), 
                    "El descubierto debe ser igual a la suma de presupuestos de obras habilitadas");
    }

    @Test
    void getDescubierto_CuandoNoHayObrasHabilitadas_DebeRetornarCero() {
        cliente.setObras(Arrays.asList(obraPendiente));

        BigDecimal resultado = clienteService.getDescubierto(cliente);

        assertEquals(0, BigDecimal.ZERO.compareTo(resultado), 
                    "El descubierto debe ser cero cuando no hay obras habilitadas");
    }

    @Test
    void asociarUsuarioHabilitado_CuandoClienteYUsuarioExisten_DebeAsociarlos() throws UsuarioHabilitadoNotFoundException {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(usuarioHabilitadoService.findById(1)).thenReturn(Optional.of(usuarioHabilitado));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        Cliente resultado = clienteService.asociarUsuarioHabilitado(1, 1);

        assertEquals(cliente, resultado);
        assertTrue(cliente.getUsuariosHabilitados().contains(usuarioHabilitado));
        verify(clienteRepository).findById(1);
        verify(usuarioHabilitadoService).findById(1);
        verify(clienteRepository).save(cliente);
    }

    @Test
    void asociarUsuarioHabilitado_CuandoUsuarioYaEstaAsociado_NoDebeRepetirAsociacion() throws UsuarioHabilitadoNotFoundException {
        cliente.getUsuariosHabilitados().add(usuarioHabilitado);
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(usuarioHabilitadoService.findById(1)).thenReturn(Optional.of(usuarioHabilitado));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        Cliente resultado = clienteService.asociarUsuarioHabilitado(1, 1);

        assertEquals(cliente, resultado);
        assertEquals(1, cliente.getUsuariosHabilitados().size());
        verify(clienteRepository).findById(1);
        verify(usuarioHabilitadoService).findById(1);
        verify(clienteRepository).save(cliente);
    }

    @Test
    void asociarUsuarioHabilitado_CuandoClienteNoExiste_DebeLanzarException() {
        when(clienteRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> {
            clienteService.asociarUsuarioHabilitado(99, 1);
        });
        verify(clienteRepository).findById(99);
        verify(usuarioHabilitadoService, never()).findById(anyInt());
    }

    @Test
    void asociarUsuarioHabilitado_CuandoUsuarioNoExiste_DebeLanzarException() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(usuarioHabilitadoService.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsuarioHabilitadoNotFoundException.class, () -> {
            clienteService.asociarUsuarioHabilitado(1, 99);
        });
        verify(clienteRepository).findById(1);
        verify(usuarioHabilitadoService).findById(99);
        verify(clienteRepository, never()).save(any());
    }
}
