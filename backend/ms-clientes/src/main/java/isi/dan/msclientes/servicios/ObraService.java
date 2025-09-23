package isi.dan.msclientes.servicios;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import isi.dan.msclientes.dao.ObraRepository;
import isi.dan.msclientes.exception.ClienteNotFoundException;
import isi.dan.msclientes.exception.ObraNotFoundException;
import isi.dan.msclientes.model.Cliente;
import isi.dan.msclientes.model.EstadoObra;
import isi.dan.msclientes.model.Obra;

@Service
public class ObraService {

    @Autowired
    private ObraRepository obraRepository;

    @Autowired
    private ClienteService clienteService;


    public List<Obra> findAll() {
        return obraRepository.findAll();
    }

    public Optional<Obra> findById(Integer id) {
        return obraRepository.findById(id);
    }

    public Obra save(Obra obra) {
        if (obra.getEstado() == null) {
            obra.setEstado(EstadoObra.PENDIENTE);
        }
        return obraRepository.save(obra);
    }

    public Obra update(Obra obra) {
        return obraRepository.save(obra);
    }

    public void deleteById(Integer id) {
        obraRepository.deleteById(id);
    }

    public List<Obra> findByClienteId(Integer id) {
        return obraRepository.findByClienteId(id);
    }

    public Obra asignarCliente(Integer idObra, Integer idCliente) throws ObraNotFoundException, ClienteNotFoundException {
        Obra obra = obraRepository.findById(idObra)
                .orElseThrow(() -> new ObraNotFoundException("Obra " + idObra + " no encontrada"));

        Cliente cliente = clienteService.findById(idCliente)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente " + idCliente + " no encontrado"));

        if (obra.getCliente() != null) {
            throw new IllegalStateException("La obra ya se encuentra asignada a un cliente");
        }
        obra.setCliente(cliente);
        // se valida si la obra se puede setear como habilitada o pendiente
        obra.setEstado(
                validarHabilitacionObra(obra, cliente)
                ? EstadoObra.HABILITADA
                : EstadoObra.PENDIENTE);

        cliente.getObras().add(obra);
        return this.update(obra);
    }

    public Obra habilitar(Obra obra) {
        // solo se puede pasar de pendiente a habilitada
        if(obra.getEstado().equals(EstadoObra.FINALIZADA)){
            throw new IllegalStateException("No se puede habilitar una obra finalizada");
        }
        if(obra.getEstado().equals(EstadoObra.HABILITADA)){
            throw new IllegalStateException("La obra ya se encuentra habilitada");
        }
        if(validarHabilitacionObra(obra, obra.getCliente())){
            obra.setEstado(EstadoObra.HABILITADA);
        }
        return this.update(obra);
    }

    public Obra deshabilitar(Obra obra){
        // solo se puede pasar de habilitada a pendiente
        if(obra.getEstado().equals(EstadoObra.FINALIZADA)){
            throw new IllegalStateException("No se puede deshabilitar una obra finalizada");
        }
        if(obra.getEstado().equals(EstadoObra.PENDIENTE)){
            throw new IllegalStateException("La obra ya se encuentra pendiente");
        }
        obra.setEstado(EstadoObra.PENDIENTE);
        return this.update(obra);
    }

    @Transactional
    public Obra finalizar(Obra obra) {
        // solo se puede pasar de habilitada a finalizada
        if(obra.getEstado().equals(EstadoObra.PENDIENTE)){
            throw new IllegalStateException("No se puede finalizar una obra pendiente");
        }
        if(obra.getEstado().equals(EstadoObra.FINALIZADA)){
            throw new IllegalStateException("La obra ya se encuentra finalizada");
        }
        obra.setEstado(EstadoObra.FINALIZADA);
        obraRepository.save(obra);

        habilitarPendientesSiCorresponde(obra.getCliente());
        return obra;
    }

    public boolean validarHabilitacionObra(Obra obra, Cliente cliente) {
        // se busca las obras del cliente que ya estan habilitadas y se cuentan
        // si verifica que tiene menos obras que las permitidas se habilita la obra
        int obrasHabiliadas = obraRepository.findByClienteIdAndEstadoEquals(obra.getCliente().getId(), EstadoObra.HABILITADA).size();
        return obrasHabiliadas < cliente.getMaximoCantidadObras() 
            && clienteService.getDescubierto(cliente).add(obra.getPresupuesto()).compareTo(cliente.getMaximoDescubierto()) <= 0;
    }

    private void habilitarPendientesSiCorresponde(Cliente cliente) {
        // Obtengo las obras pendientes en orden FIFO (o por prioridad si la tuvieses)
        List<Obra> pendientes = obraRepository
                .findByClienteIdAndEstadoEquals(cliente.getId(), EstadoObra.PENDIENTE);

        for (Obra p : pendientes) {
            if (validarHabilitacionObra(p, cliente)) {
                p.setEstado(EstadoObra.HABILITADA);
                obraRepository.save(p);
                // si solo se debe habilitar UNA, salgo del bucle
                break;
            }
        }
    }

}
