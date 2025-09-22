'use client';
import { useEffect, useState } from 'react';
import styles from "./page.module.css";

export default function Productos() {
  const [clientes, setClientes] = useState([])
  const [obras, setObras] = useState([])
  const [inputs, setInputs] = useState({
    cliente: "",
    obra: ""
  })

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res1 = await fetch(`http://localhost:6080/api/clientes`);
        const clientes = await res1.json();
        setClientes(clientes);

        const res2 = await fetch(`http://localhost:6080/api/obras`);
        const obras = await res2.json();
        setObras(obras);
       
      } catch (err) {
        console.error('Error al obtener categorías', err);
      }
    };
    fetchData();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setInputs(prev => ({
      ...prev,
      [name]: value,
    }));
  };
  
  const handleSave = async () => {
    const res = await fetch(`http://localhost:6080/api/obras/${inputs.obra}/asignar-cliente/${inputs.cliente}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
    });

    const data = await res.json();
    console.log('Obra asignada:', data);
  };

  return (
    <>
      <h1>Asignar obra Page</h1>
      <div className={styles.inputContainer}>
        <select
          name="cliente"
          value={inputs.cliente}
          onChange={handleChange}
        >
          <option value="">Seleccioná un cliente</option>
          {clientes.map((cliente) => (
            <option key={cliente.id} value={cliente.id}>
              {cliente.id}
            </option>
          ))}
        </select>
        <select
          name="obra"
          value={inputs.obra}
          onChange={handleChange}
        >
          <option value="">Seleccioná una obra</option>
          {obras.map((obra) => (
            <option key={obra.id} value={obra.id}>
              {obra.id}
            </option>
          ))}
        </select>
        <button className={styles.button} onClick={handleSave}>Asignar</button>
      </div>
    </>
  );
};
