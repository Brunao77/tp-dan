package isi.dan.ms_productos.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DescuentoDTO {

    @NotNull(message = "El id del producto no puede ser nulo")
    private Long idProducto;

    @NotNull(message = "El descuento no puede ser nulo")
    private BigDecimal descuento;
   

}
