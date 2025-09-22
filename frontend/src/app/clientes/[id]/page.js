'use client';
import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import styles from "./page.module.css";

export default function Productos() {
  const { id } = useParams();
  const router = useRouter();
  const [cliente, setCliente] = useState(null);
  const [inputs, setInputs] = useState({
      nombre: '',
      email: '',
      cuit: ''
    });
  
    useEffect(() => {
      const fetchProducto = async () => {
        try {
          const res = await fetch(`http://localhost:6080/api/clientes/${id}`);
          const data = await res.json();
          setCliente(data);
          setInputs({
            nombre: data.nombre,
            email: data.correoElectronico,
            cuit: data.cuit
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
      nombre: inputs.nombre,
      correoElectronico: inputs.email,
      cuit: Number(inputs.cuit)
    };

    const res = await fetch(`http://localhost:6080/api/clientes/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });

    const data = await res.json();
    console.log('Cliente actualizado:', data);
  };

  return (
    <>
      <h1>Editar cliente #{id}</h1>
      <div className={styles.inputContainer}>
        <input
          type="text"
          placeholder="Nombre"
          name="nombre" 
          value={inputs.nombre} 
          onChange={handleChange} 
        />
        <input
          type="email"
          placeholder="Correo electronico"
          name="email" 
          value={inputs.email} 
          onChange={handleChange} 
        />
        <input
          type="number"
          placeholder="CUIT"
          name="cuit" 
          value={inputs.cuit} 
          onChange={handleChange} 
        />
        <button className={styles.button} onClick={handleUpdate}>Guardar</button>
      </div>
    </>
  );
};
