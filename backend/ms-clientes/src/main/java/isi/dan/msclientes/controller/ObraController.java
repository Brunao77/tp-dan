package isi.dan.msclientes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import isi.dan.msclientes.aop.LogExecutionTime;
import isi.dan.msclientes.exception.ClienteNotFoundException;
import isi.dan.msclientes.exception.ObraNotFoundException;
import isi.dan.msclientes.model.Obra;
import isi.dan.msclientes.servicios.ObraService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/obras")
public class ObraController {

    @Autowired
    private ObraService obraService;

    @GetMapping
    @LogExecutionTime
    public List<Obra> getAll() {
        return obraService.findAll();
    }

    @GetMapping("/{id}")
    @LogExecutionTime
    public ResponseEntity<Obra> getById(@PathVariable Integer id) {
        Optional<Obra> obra = obraService.findById(id);
        return obra.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Obra create(@RequestBody Obra obra){
        return obraService.save(obra);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Obra> update(@PathVariable Integer id, @RequestBody Obra obra) {
        if (!obraService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        obra.setId(id);
        return ResponseEntity.ok(obraService.update(obra));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!obraService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        obraService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/asignar-cliente/{idCliente}")
    public ResponseEntity<Obra> asignarCliente(@PathVariable Integer id, @PathVariable Integer idCliente) throws ObraNotFoundException, ClienteNotFoundException{
        Obra obra = obraService.asignarCliente(id, idCliente);
        return ResponseEntity.ok(obra);
    }

    @PutMapping("/{id}/habilitar")
    public ResponseEntity<Obra> habilitar(@PathVariable Integer id) throws ObraNotFoundException {
        Obra obra = obraService.findById(id)
                .orElseThrow(() -> new ObraNotFoundException("Obra " + id + " no encontrada"));
        obraService.habilitar(obra);
        return ResponseEntity.ok(obra);
    }

    @PutMapping("/{id}/deshabilitar")
    public ResponseEntity<Obra> deshabilitar(@PathVariable Integer id) throws ObraNotFoundException {
        Obra obra = obraService.findById(id)
                .orElseThrow(() -> new ObraNotFoundException("Obra " + id + " no encontrada"));
        obraService.deshabilitar(obra);
        return ResponseEntity.ok(obra);
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<Obra> finalizar(@PathVariable Integer id) throws ObraNotFoundException {
        Obra obra = obraService.findById(id)
                .orElseThrow(() -> new ObraNotFoundException("Obra " + id + " no encontrada"));
        obraService.finalizar(obra);
        return ResponseEntity.ok(obra);
    }

}
