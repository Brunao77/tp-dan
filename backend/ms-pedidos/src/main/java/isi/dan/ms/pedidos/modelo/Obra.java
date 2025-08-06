package isi.dan.ms.pedidos.modelo;

import java.math.BigDecimal;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "obras")
public class Obra {
    private Integer id;
	private String direccion;
	private Boolean esRemodelacion;
	private float lat;
	private float lng;
	private Cliente cliente;
	private BigDecimal presupuesto;
	private EstadoObra estado = EstadoObra.PENDIENTE;
}
