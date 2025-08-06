package isi.dan.ms.pedidos.modelo;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "historialEstados")
public class HistorialEstado {

    private EstadoPedido estado;
    private Instant fechaEstado = Instant.now();
    private String userEstado;
    private String detalle;

}
