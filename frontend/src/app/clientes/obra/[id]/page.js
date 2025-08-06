'use client';
import { useEffect, useState } from 'react';
import styles from "./page.module.css";

export default function Productos() {
  const [inputs, setInputs] = useState({
    nombre: '',
    email: '',
    cuit: '',
    maxDesc: '',
    maxObra: '',
  });

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
      correoElectronico: inputs.email,
      cuit: Number(inputs.cuit),
      maximoDescubierto: Number(inputs.maxDesc),
      maximoCantidadObras: Number(inputs.maxObra)
    };
    console.log(payload)

    const res = await fetch('http://localhost:6080/api/clientes', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });

    const data = await res.json();
    console.log('Cliente creado:', data);
  };

  return (
    <>
      <h1>Asignar obra Page</h1>
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
        <input
          type="number"
          placeholder="Maximo descubierto"
          name="maxDesc" 
          value={inputs.maxDesc} 
          onChange={handleChange} 
        />
        <input
          type="number"
          placeholder="Maxima cant. Obra"
          name="maxObra" 
          value={inputs.maxObra} 
          onChange={handleChange} 
        />
        <button className={styles.button} onClick={handleSave}>Guardar</button>
      </div>
    </>
  );
};
