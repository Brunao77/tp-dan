package isi.dan.ms.pedidos.modelo;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "productos")
public class Producto {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;

    private Integer stockActual;
    private Integer stockMinimo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getStock(){
        return stockActual;
    }
    

}
