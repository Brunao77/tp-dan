package isi.dan.ms.pedidos.modelo;


import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

import java.math.BigDecimal;

@Data
@Document(collection = "clientes")
public class Cliente {
    
    private Integer id;
    private String nombre;
    private String correoElectronico;
    private String cuit;
    private BigDecimal maximoDescubierto;

}
