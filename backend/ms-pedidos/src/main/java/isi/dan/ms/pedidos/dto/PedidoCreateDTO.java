package isi.dan.ms.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PedidoCreateDTO {
    @NotNull Integer idObra;
    @NotNull Integer idCliente;

    @Size(min = 1)
    @Valid
    List<DetalleDTO> detalle;
    String observaciones;
    String usuario;
}
