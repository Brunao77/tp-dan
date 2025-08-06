'use client';
import { useState } from 'react';

import Link from 'next/link';
import styles from "./page.module.css";

export default function Productos() {
  const [filtros, setFiltros] = useState({
    nombre: '',
    precioMin: '',
    precioMax: '',
    stockMin: '',
    stockMax: '',
  });
  const [results, setResults] = useState([]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFiltros(prev => ({
      ...prev,
      [name]: value,
    }));
  };
  
  const handleSearch = async () => {
    const res = await fetch("http://localhost:6180/api/productos")
    const lista = await res.json()

    const filtrados = lista.filter(product => {
      const matchNombreOCodigo = filtros.nombre === '' ||
        product.nombre.toLowerCase().includes(filtros.nombre.toLowerCase()) ||
        product.id.toString() === filtros.nombre;
      const matchPrecioMin = filtros.precioMin === '' || product.precio >= parseFloat(filtros.precioMin);
      const matchPrecioMax = filtros.precioMax === '' || product.precio <= parseFloat(filtros.precioMax);
      const matchStockMin = filtros.stockMin === '' || product.stockActual >= parseInt(filtros.stockMin);
      const matchStockMax = filtros.stockMax === '' || product.stockActual <= parseInt(filtros.stockMax);

      return (
        matchNombreOCodigo &&
        matchPrecioMin &&
        matchPrecioMax &&
        matchStockMin &&
        matchStockMax
      );
    });

    setResults(filtrados);
  };

  const handleDelete = async (id) => {
    const confirmado = window.confirm("¿Estás seguro de que querés eliminar este producto?");
    if (!confirmado) return;

    const res = await fetch(`http://localhost:6180/api/productos/${id}`, {
      method: 'DELETE',
    });

    if (res.ok) {
      alert("Producto eliminado con éxito.");
      setResults(prev => prev.filter(p => p.id !== id));
    } else {
      const error = await res.json();
      alert("Error al eliminar: " + error.description);
    }
  };

  return (
    <>
      <h1>Productos Page</h1>
      <Link href="/productos/new">
        <button className={styles.button}>Crear nuevo producto</button>
      </Link>
      <div className={styles.inputContainer}>
        <input
          type="text" 
          name="nombre"
          placeholder="Número o nombre" 
          value={filtros.nombre}
          onChange={handleChange}
        />
        <input
          type="number"
          placeholder="Precio minimo"
          name="precioMin" 
          value={filtros.precioMin} 
          onChange={handleChange} 
        />
        -
        <input
          type="number"
          placeholder="Precio maximo"
          name="precioMax" 
          value={filtros.precioMax} 
          onChange={handleChange} 
        />
        <input
          type="number"
          placeholder="Stock minimo"
          name="stockMin" 
          value={filtros.stockMin} 
          onChange={handleChange} 
        />
        -
        <input
          type="number"
          placeholder="Stock maximo"
          name="stockMax" 
          value={filtros.stockMax} 
          onChange={handleChange} 
        />
        <button className={styles.button} onClick={handleSearch}>Buscar</button>
      </div>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Nombre</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {results.map(product => (
            <tr key={product.id}>
              <td>
                {product.id}
                
              </td>
              <td>{product.nombre}</td>
              <td>
                <Link href={`/productos/${product.id}`}><button>Editar</button></Link>
                <button onClick={() => handleDelete(product.id)}>Eliminar</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
};
