package isi.dan.msclientes.dao;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.EstadoObra;
import isi.dan.msclientes.model.Obra;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Testcontainers
@ActiveProfiles("db")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ObraRepositoryTest {

    Logger log = LoggerFactory.getLogger(ObraRepositoryTest.class);

    @Container
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private ObraRepository obraRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private Obra obraTest;
    private Cliente clienteTest;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @BeforeEach
    void iniciarDatos(){
        // Crear un cliente de prueba
        clienteTest = new Cliente();
        clienteTest.setNombre("Cliente Test");
        clienteTest.setCorreoElectronico("cliente@test.com");
        clienteTest.setCuit("30-12345678-9");
        clienteTest.setMaximoDescubierto(BigDecimal.valueOf(100000));
        clienteTest.setMaximoCantidadObras(3);
        clienteRepository.save(clienteTest);

        // Crear una obra de prueba
        obraTest = new Obra();
        obraTest.setDireccion("Direccion Test");
        obraTest.setEsRemodelacion(false);
        obraTest.setLat(10.5f);
        obraTest.setLng(20.5f);
        obraTest.setPresupuesto(BigDecimal.valueOf(50000));
        obraTest.setEstado(EstadoObra.PENDIENTE);
        obraTest.setCliente(clienteTest);
    }

    @BeforeEach
    void borrarDatos(){
        obraRepository.deleteAll();
    }

    @AfterAll
    static void stopContainer() {
        mysqlContainer.stop();
    }

    @Test
    void testSaveAndFindById() {
        Obra savedObra = obraRepository.save(obraTest);
        Optional<Obra> foundObra = obraRepository.findById(savedObra.getId());
        log.info("ENCONTRE: {} ",foundObra);
        assertThat(foundObra).isPresent();
        assertThat(foundObra.get().getDireccion()).isEqualTo("Direccion Test");
    }

    @Test
    void testFindObrasByClienteID() {
        // Guardar dos obras de prueba para el mismo cliente
        obraRepository.save(obraTest);

        Obra otraObra = new Obra();
        otraObra.setDireccion("Otra Direccion");
        otraObra.setEsRemodelacion(true);
        otraObra.setLat(15.5f);
        otraObra.setLng(25.5f);
        otraObra.setPresupuesto(BigDecimal.valueOf(75000));
        otraObra.setEstado(EstadoObra.PENDIENTE);
        otraObra.setCliente(clienteTest);
        obraRepository.save(otraObra);

        // Buscamos obras por el ID del cliente
        List<Obra> obras = obraRepository.findByClienteId(clienteTest.getId());

        log.info("ENCONTRE: {} ",obras);
        
        assertThat(obras.size()).isEqualTo(2);
        assertThat(obras.get(0).getDireccion()).isEqualTo("Direccion Test");
        assertThat(obras.get(1).getDireccion()).isEqualTo("Otra Direccion");
        assertThat(obras.get(0).getCliente().getId()).isEqualTo(clienteTest.getId());
        assertThat(obras.get(1).getCliente().getId()).isEqualTo(clienteTest.getId());
    }

    @Test
    void testFindByPresupuesto() {
        obraRepository.save(obraTest);

        Obra otraObra = new Obra();
        otraObra.setDireccion("Otra Direccion");
        otraObra.setEsRemodelacion(true);
        otraObra.setLat(15.5f);
        otraObra.setLng(25.5f);
        otraObra.setPresupuesto(BigDecimal.valueOf(75000));
        otraObra.setEstado(EstadoObra.PENDIENTE);
        otraObra.setCliente(clienteTest);
        obraRepository.save(otraObra);
        obraRepository.save(otraObra);

        List<Obra> resultado = obraRepository.findByPresupuestoGreaterThanEqual(BigDecimal.valueOf(10000));
        log.info("ENCONTRE: {} ",resultado);
        assertThat(resultado.size()).isEqualTo(2);
        assertThat(resultado.get(0).getPresupuesto()).isGreaterThan(BigDecimal.valueOf(10000));
        assertThat(resultado.get(1).getPresupuesto()).isGreaterThan(BigDecimal.valueOf(10000));
    }

}
