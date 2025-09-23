'use client';
import { useEffect, useState } from 'react';
import styles from "./page.module.css";

export default function Productos() {
  const [inputs, setInputs] = useState({
    nombre: '',
    descripcion: '',
    stockMin: '',
    precio: '',
    descuento_promocional: '0',
    categoriaId: ''
  });

  const [categorias, setCategorias] = useState([]);

  useEffect(() => {
    const fetchCategorias = async () => {
      try {
        const res = await fetch('http://localhost:6180/api/categorias');
        const data = await res.json();
        setCategorias(data);
      } catch (err) {
        console.error('Error al obtener categorías', err);
      }
    };

    fetchCategorias();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setInputs(prev => ({
      ...prev,
      [name]: value,
    }));
  };
  
  const handleSave = async () => {
    const payload = {
      nombre: inputs.nombre,
      descripcion: inputs.descripcion,
      stockMinimo: Number(inputs.stockMin),
      precio: Number(inputs.precio),
      descuento: Number(inputs.descuento_promocional),
      categoria: {
        id: Number(inputs.categoriaId)
      }
    };

    const res = await fetch('http://localhost:6180/api/productos', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });

    const data = await res.json();
    console.log('Producto creado:', data);
  };

  return (
    <>
      <h1>Productos Nuevos Page</h1>
      <div className={styles.inputContainer}>
        <input
          type="text"
          placeholder="Nombre"
          name="nombre" 
          value={inputs.nombre} 
          onChange={handleChange} 
        />
        <textarea
          placeholder="Descripcion"
          name="descripcion" 
          value={inputs.descripcion} 
          onChange={handleChange} 
        />
        <input
          type="number"
          placeholder="Stock minimo"
          name="stockMin" 
          value={inputs.stockMin} 
          onChange={handleChange} 
        />
        <input
          type="number"
          placeholder="Precio"
          name="precio" 
          value={inputs.precio} 
          onChange={handleChange} 
        />
        <input
          type="number"
          placeholder="Descuento Promocional"
          name="descuento_promocional" 
          value={inputs.descuento_promocional} 
          onChange={handleChange} 
        />
        <select
          name="categoriaId"
          value={inputs.categoriaId}
          onChange={handleChange}
        >
          <option value="">Seleccioná una categoría</option>
          {categorias.map((cat) => (
            <option key={cat.id} value={cat.id}>
              {cat.nombre}
            </option>
          ))}
        </select>
        <button className={styles.button} onClick={handleSave}>Guardar</button>
      </div>
    </>
  );
};
