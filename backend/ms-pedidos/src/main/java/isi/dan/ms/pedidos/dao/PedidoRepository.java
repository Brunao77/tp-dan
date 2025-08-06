package isi.dan.ms.pedidos.dao;

import isi.dan.ms.pedidos.modelo.EstadoPedido;
import org.springframework.data.mongodb.repository.MongoRepository;

import isi.dan.ms.pedidos.modelo.Pedido;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends MongoRepository<Pedido, String> {

    List<Pedido> findByClienteIdAndEstadoIn(Integer id, List<EstadoPedido> aceptado);
    Optional<Pedido> findByNumeroPedido(Integer numeroPedido);
}
