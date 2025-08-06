package isi.dan.msclientes.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
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

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Testcontainers
@ActiveProfiles("db")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ClienteRepositoryTest {

     Logger log = LoggerFactory.getLogger(ClienteRepositoryTest.class);

     @Container
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private ClienteRepository clienteRepository;

    private Cliente clienteTest;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @BeforeEach
    void iniciarDatos(){
        // Crear un cliuente de prueba
        clienteTest = new Cliente();
        clienteTest.setNombre("Cliente Test");
        clienteTest.setCorreoElectronico("cliente@test.com");
        clienteTest.setCuit("30-12345678-9");
        clienteTest.setMaximoDescubierto(BigDecimal.valueOf(100000));
        clienteTest.setMaximoCantidadObras(3);
    }

    @BeforeEach
    void borrarDatos(){
        clienteRepository.deleteAll();
    }

    @AfterAll
    static void stopContainer() {
        mysqlContainer.stop();
    }
    
    @Test
    void testSaveAndFindById() {
        Cliente savedCliente = clienteRepository.save(clienteTest);
        Optional<Cliente> foundCliente = clienteRepository.findById(savedCliente.getId());
        log.info("ENCONTRE: {}", foundCliente);
        assertThat(foundCliente).isPresent();
        assertThat(foundCliente.get().getNombre()).isEqualTo("Cliente Test");
    }

    @Test
    void testFindClienteById() {
        // Guardar el cliente
        Cliente savedCliente = clienteRepository.save(clienteTest);

        // Buscar por ID
        Optional<Cliente> foundCliente = clienteRepository.findById(savedCliente.getId());

        // Verificar que se encontró correctamente
        assertThat(foundCliente).isPresent();
        assertThat(foundCliente.get().getNombre()).isEqualTo("Cliente Test");
    }

    @Test
    void testFindAllClientes() {
        // Guardar dos clientes
        clienteRepository.save(clienteTest);

        Cliente otroCliente = new Cliente();
        otroCliente.setNombre("Otro Cliente");
        otroCliente.setCorreoElectronico("otro@test.com");
        otroCliente.setCuit("30-87654321-9");
        otroCliente.setMaximoDescubierto(BigDecimal.valueOf(50000));
        otroCliente.setMaximoCantidadObras(2);
        clienteRepository.save(otroCliente);

        // Buscar todos los clientes
        List<Cliente> clientes = clienteRepository.findAll();

        // Verificar que se encontraron los clientes
        assertThat(clientes).isNotEmpty();
        assertThat(clientes).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testDeleteCliente() {
        // Guardar el cliente
        Cliente savedCliente = clienteRepository.save(clienteTest);

        // Eliminar el cliente
        clienteRepository.deleteById(savedCliente.getId());

        // Verificar que se eliminó correctamente
        Optional<Cliente> deletedCliente = clienteRepository.findById(savedCliente.getId());
        assertThat(deletedCliente).isEmpty();
    }

    @Test
    void testUpdateCliente() {
        // Guardar el cliente
        Cliente savedCliente = clienteRepository.save(clienteTest);

        // Modificar el cliente
        savedCliente.setNombre("Cliente Actualizado");
        savedCliente.setCorreoElectronico("actualizado@test.com");

        // Guardar los cambios
        Cliente updatedCliente = clienteRepository.save(savedCliente);

        // Verificar que se actualizó correctamente
        assertThat(updatedCliente.getNombre()).isEqualTo("Cliente Actualizado");
        assertThat(updatedCliente.getCorreoElectronico()).isEqualTo("actualizado@test.com");
    }


}
