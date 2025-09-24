package isi.dan.ms_productos.servicio;

import java.math.BigDecimal;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import isi.dan.ms_productos.conf.RabbitMQConfig;
import isi.dan.ms_productos.dao.ProductoRepository;
import isi.dan.ms_productos.dto.StockUpdateDTO;
import isi.dan.ms_productos.exception.ProductoNotFoundException;
import isi.dan.ms_productos.modelo.Producto;

import java.util.List;

import isi.dan.ms_productos.dto.DescuentoDTO;

@Service
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;
    Logger log = LoggerFactory.getLogger(ProductoService.class);

    @RabbitListener(queues = RabbitMQConfig.STOCK_UPDATE_QUEUE)
    public void handleStockUpdate(Message msg) throws ProductoNotFoundException {
        log.info("Recibido {}", msg);
        String body = new String(msg.getBody());
        // obtener el id del producto y la cantidad
        Long idProducto = Long.valueOf(body.split(";")[0]);
        Integer cantidad = Integer.valueOf(body.split(";")[1]);
        // buscar el producto
        Producto producto = productoRepository.findById(idProducto).orElseThrow(() -> new ProductoNotFoundException(idProducto));
        // actualizar el stock del producto
        producto.setStockActual(producto.getStockActual() - cantidad);
        // verificar el punto de pedido y generar un pedido
        if(producto.getStockActual() < producto.getStockMinimo()) {
            log.info("Generar pedido para el producto {}", producto);
        }
        // guardar el producto
        productoRepository.save(producto);
        
    }
    
    /*public Producto saveProducto(Producto producto) {
        producto.setStockActual(0);
        if(producto.getDescuento() == null) {
            producto.setDescuento(BigDecimal.ZERO);
        }
        return productoRepository.save(producto);
    }*/

    public Producto saveProducto(Producto producto) {
		return productoRepository.save(producto);
	}

    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    public Producto getProductoById(Long id) throws ProductoNotFoundException{
        return productoRepository.findById(id).orElseThrow(() -> new ProductoNotFoundException(id));
    }

    public void deleteProducto(Long id) {
        productoRepository.deleteById(id);
    }

    @Transactional
    public Producto updateStockAndPrice(StockUpdateDTO stockUpdateDTO) throws ProductoNotFoundException {
        Producto producto = productoRepository.findById(stockUpdateDTO.getIdProducto()).orElseThrow(() -> new ProductoNotFoundException(stockUpdateDTO.getIdProducto()));
        int nuevoStock = producto.getStockActual() + stockUpdateDTO.getCantidad();
        if (nuevoStock < producto.getStockMinimo()) throw new IllegalArgumentException("Stock resultante no puede ser menor que stock minimo");

        producto.setStockActual(nuevoStock);
        producto.setPrecio(stockUpdateDTO.getPrecio());
        log.info("Actualizado stock y precio del producto {}", producto);
        return productoRepository.save(producto);
    }

    public Producto updateDescuento(Long idProducto, DescuentoDTO descuento) throws ProductoNotFoundException {
        Producto producto = productoRepository.findById(idProducto).orElseThrow(() -> new ProductoNotFoundException(idProducto));
        producto.setDescuento(descuento.getDescuento());
        return productoRepository.save(producto);
    }
        
}

