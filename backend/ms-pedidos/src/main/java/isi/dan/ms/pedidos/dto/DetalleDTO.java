package isi.dan.ms.pedidos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DetalleDTO {

    @NotNull
    long idProducto;
    @Min(1)
    Integer cantidad;
}
