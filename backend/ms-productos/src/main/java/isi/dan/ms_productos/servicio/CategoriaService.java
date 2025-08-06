package isi.dan.ms_productos.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import isi.dan.ms_productos.dao.CategoriaRepository;
import isi.dan.ms_productos.exception.CategoriaNotFoundException;
import isi.dan.ms_productos.modelo.Categoria;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public Categoria saveCategoria(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public Categoria getCategoriaById(Long id) throws CategoriaNotFoundException {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
    }

    public List<Categoria> getAllCategorias() {
        return categoriaRepository.findAll();
    }

    public void deleteCategoria(Long id) {
        categoriaRepository.deleteById(id);
    }

    public Categoria updateCategoria(Long id, Categoria categoria) throws CategoriaNotFoundException {
        Categoria categoriaToUpdate = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
        categoriaToUpdate.setNombre(categoria.getNombre());
        return categoriaRepository.save(categoriaToUpdate);
    }
}
