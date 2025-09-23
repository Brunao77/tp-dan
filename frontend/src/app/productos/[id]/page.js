'use client';
import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import styles from "./page.module.css";

export default function Productos() {
  const { id } = useParams();
  const router = useRouter();
  const [producto, setProducto] = useState(null);
  const [inputs, setInputs] = useState({
      stock: '',
      precio: '',
    });
  
    useEffect(() => {
      const fetchProducto = async () => {
        try {
          const res = await fetch(`http://localhost:3080/productos/${id}`);
          const data = await res.json();
          setProducto(data);
          setInputs({
            stock: data.stockActual,
            precio: data.precio,
          });
        } catch (err) {
          console.error('Error al obtener categorías', err);
        }
      };
      fetchProducto();
    }, [id]);
  
    const handleChange = (e) => {
      const { name, value } = e.target;
      setInputs(prev => ({
        ...prev,
        [name]: value,
      }));
    };
    
  const handleUpdate = async () => {
    const payload = {
      idProducto: Number(id),
      cantidad: Number(inputs.stock),
      precio: parseFloat(inputs.precio)
    };

    const res = await fetch('http://localhost:6180/api/productos/stock', {
      method: 'PUT',
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
      <h1>Editar producto #{id}</h1>
      <div className={styles.inputContainer}>
        <input
          type="number"
          placeholder="Stock"
          name="stock" 
          value={inputs.stock} 
          onChange={handleChange} 
        />
        <input
          type="number"
          placeholder="Precio"
          name="precio" 
          value={inputs.precio} 
          onChange={handleChange} 
        />
        <button className={styles.button} onClick={handleUpdate}>Guardar</button>
      </div>
    </>
  );
};
