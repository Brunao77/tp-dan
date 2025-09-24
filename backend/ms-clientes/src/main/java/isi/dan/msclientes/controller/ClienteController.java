package isi.dan.msclientes.controller;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import isi.dan.msclientes.aop.LogExecutionTime;
import isi.dan.msclientes.exception.ClienteNotFoundException;
import isi.dan.msclientes.exception.UsuarioHabilitadoNotFoundException;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.servicios.ClienteService;


@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    Logger log = LoggerFactory.getLogger(ClienteController.class);

    @Value("${dan.clientes.instancia:ms-clientes-svc-default}")
    private String instancia;

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    @LogExecutionTime
    public List<Cliente> getAll() {
        log.info("=== INICIO getAll() === Solicitando lista de todos los clientes desde instancia: {}", instancia);
        List<Cliente> clientes = clienteService.findAll();
        log.info("=== RESULTADO getAll() === Se encontraron {} clientes", clientes.size());
        log.warn("=== DEBUG getAll() === Método getAll ejecutado correctamente");
        return clientes;
    }
    
    @GetMapping("/echo")
    @LogExecutionTime
    public String getEcho() {
        log.info("=== TEST LOG === Recibiendo un echo desde instancia: {}", instancia);
        log.warn("=== TEST WARN === Este es un log de WARNING");
        log.error("=== TEST ERROR === Este es un log de ERROR");
        return Instant.now()+" - "+instancia;
    }

    @GetMapping("/{id}")
    @LogExecutionTime
    public ResponseEntity<Cliente> getById(@PathVariable Integer id)  throws ClienteNotFoundException {
        log.info("Buscando cliente con ID: {} desde instancia: {}", id, instancia);
        Optional<Cliente> cliente = clienteService.findById(id);
        if (cliente.isEmpty()) {
            log.warn("Cliente con ID {} no encontrado", id);
        } else {
            log.info("Cliente con ID {} encontrado exitosamente", id);
        }
        return ResponseEntity.ok(cliente.orElseThrow(()-> new ClienteNotFoundException("Cliente "+id+" no encontrado")));
    }

    @PostMapping
    @LogExecutionTime
    public Cliente create(@RequestBody @Validated Cliente cliente) {
        log.info("Creando nuevo cliente desde instancia: {}", instancia);
        Cliente nuevoCliente = clienteService.save(cliente);
        log.info("Cliente creado exitosamente");
        return nuevoCliente;
    }

    @PutMapping("/{id}")
    @LogExecutionTime
    public ResponseEntity<Cliente> update(@PathVariable final Integer id, @RequestBody Cliente cliente) throws ClienteNotFoundException {
        if (!clienteService.findById(id).isPresent()) {
            throw new ClienteNotFoundException("Cliente "+id+" no encontrado");
        }
        cliente.setId(id);
        return ResponseEntity.ok(clienteService.update(cliente));
    }

    @DeleteMapping("/{id}")
    @LogExecutionTime
    public ResponseEntity<Void> delete(@PathVariable Integer id) throws ClienteNotFoundException {
        if (!clienteService.findById(id).isPresent()) {
            throw new ClienteNotFoundException("Cliente "+id+" no encontrado para borrar");
        }
        clienteService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}/usuariohabilitado/{idUsuario}")
    @LogExecutionTime
    public ResponseEntity<Cliente> addUsuario(@PathVariable final Integer id, @PathVariable final Integer idUsuario) throws ClienteNotFoundException, UsuarioHabilitadoNotFoundException {
        Cliente cliente = clienteService.asociarUsuarioHabilitado(id, idUsuario);
        return ResponseEntity.ok(cliente);
    }
    
    
}

