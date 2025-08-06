package isi.dan.ms_productos.modelo;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "MS_PRD_PRODUCTO")
@Data

public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String nombre;
    private String descripcion;

    @Column(name ="STOCK_ACTUAL")
    @Min(message = "El stock actual no puede ser menor a 0", value = 0)
    private int stockActual;
    
    @Column(name ="STOCK_MINIMO")
    @Min(message = "El stock minimo  no puede ser menor a 0", value = 0)
    private int stockMinimo;

    @NotNull
    @DecimalMin(message = "El precio no puede ser menor a 0", value = "0.0")
    private BigDecimal precio;

    @NotNull
    @DecimalMin(message = "El descuento no puede ser menor a 0", value = "0.0")
    private BigDecimal descuento = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "ID_CATEGORIA")
    private Categoria categoria;

}
