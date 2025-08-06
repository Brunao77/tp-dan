package isi.dan.ms_productos.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import isi.dan.ms_productos.dao.ProductoRepository;
import isi.dan.ms_productos.dto.DescuentoDTO;
import isi.dan.ms_productos.dto.StockUpdateDTO;
import isi.dan.ms_productos.exception.ProductoNotFoundException;
import isi.dan.ms_productos.modelo.Categoria;
import isi.dan.ms_productos.modelo.Producto;
import isi.dan.ms_productos.servicio.ProductoService;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;

    private StockUpdateDTO stockUpdateDTO;
    private Categoria categoria;

    private DescuentoDTO descuentoDTO;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Categoria 1");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto 1");
        producto.setDescripcion("Descripcion 1");
        producto.setPrecio(BigDecimal.valueOf(100));
        producto.setDescuento(BigDecimal.ZERO);
        producto.setStockActual(10);
        producto.setStockMinimo(5);
        producto.setCategoria(categoria);

        stockUpdateDTO = new StockUpdateDTO();
        stockUpdateDTO.setIdProducto(1L);
        stockUpdateDTO.setCantidad(5);
        stockUpdateDTO.setPrecio(BigDecimal.valueOf(150));

        descuentoDTO = new DescuentoDTO();
        descuentoDTO.setIdProducto(1L);
        descuentoDTO.setDescuento(new BigDecimal(10.00));
    }

    @Test
    void saveProducto_StockInicialCero() {
        // Arrange
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre("Nuevo Producto");
        nuevoProducto.setPrecio(new BigDecimal(499.99));
        nuevoProducto.setCategoria(categoria);
        nuevoProducto.setDescuento(null); // Para probar que se establece a 0

        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        Producto savedProducto = productoService.saveProducto(nuevoProducto);

        assertEquals(0, savedProducto.getStockActual());
        assertEquals(BigDecimal.ZERO, savedProducto.getDescuento());
        verify(productoRepository).save(nuevoProducto);
    }

    @Test
    void saveProducto_DescuentoDefinido() {
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre("Producto con descuento");
        nuevoProducto.setPrecio(new BigDecimal(999.99));
        nuevoProducto.setCategoria(categoria);
        nuevoProducto.setDescuento(new BigDecimal(10.00)); // Descuento definido por el usuario

        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        Producto savedProducto = productoService.saveProducto(nuevoProducto);

        assertEquals(0, savedProducto.getStockActual());
        assertEquals(new BigDecimal(10.00), savedProducto.getDescuento());
        verify(productoRepository).save(nuevoProducto);
    }

    @Test
    void getAllProductos() {
        when(productoRepository.findAll()).thenReturn(Arrays.asList(producto));

        List<Producto> productos = productoService.getAllProductos();

        assertEquals(1, productos.size());
        assertEquals(producto, productos.get(0));
        verify(productoRepository).findAll();
    }

    @Test
    void getProductoById_Existe() throws ProductoNotFoundException {
        when(productoRepository.findById(1L)).thenReturn(java.util.Optional.of(producto));

        Producto productoObtenido = productoService.getProductoById(1L);

        assertEquals(producto, productoObtenido);
        verify(productoRepository).findById(1L);
    }

    @Test
    void getProductoById_NoExiste() throws ProductoNotFoundException {
        when(productoRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThrows(ProductoNotFoundException.class, () -> {
            productoService.getProductoById(99L);
        });
        verify(productoRepository).findById(99L);
    }

    @Test
    void deleteProducto_Exitoso() {
        doNothing().when(productoRepository).deleteById(1L);

        productoService.deleteProducto(1L);

        verify(productoRepository).deleteById(1L);
    }

    @Test
    void updateStockAndPrice_Exitoso() throws ProductoNotFoundException {
        when(productoRepository.findById(1L)).thenReturn(java.util.Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        Producto productoActualizado = productoService.updateStockAndPrice(stockUpdateDTO);

        assertEquals(15, productoActualizado.getStockActual()); // 10 + 5
        assertEquals(BigDecimal.valueOf(150), productoActualizado.getPrecio());
        verify(productoRepository).findById(1L);
        verify(productoRepository).save(producto);
    }

    @Test
    void updateStockAndPrice_ProductoNoExiste() throws ProductoNotFoundException {
        // se debe cambiar el id del producto en el stockUpdateDTO ya que es el que se busca en la base de datos
        stockUpdateDTO.setIdProducto(99L);

        when(productoRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThrows(ProductoNotFoundException.class, () -> {
            productoService.updateStockAndPrice(stockUpdateDTO);
        });
        verify(productoRepository).findById(99L);
    }

    @Test
    void updateDescuento_Exitoso() throws ProductoNotFoundException {
        when(productoRepository.findById(1L)).thenReturn(java.util.Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        Producto productoActualizado = productoService.updateDescuento(1L, descuentoDTO);

        assertEquals(new BigDecimal(10.00), productoActualizado.getDescuento());
        verify(productoRepository).findById(1L);
        verify(productoRepository).save(producto);
    }

    @Test
    void updateDescuento_ProductoNoExiste() throws ProductoNotFoundException {
        // se debe cambiar el id del producto en el descuentoDTO ya que es el que se busca en la base de datos
        descuentoDTO.setIdProducto(99L);
        when(productoRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThrows(ProductoNotFoundException.class, () -> {
            productoService.updateDescuento(99L, descuentoDTO);
        });
        verify(productoRepository).findById(99L);
    }

}
