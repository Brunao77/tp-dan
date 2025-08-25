'use client';
import { useEffect, useState } from 'react';
import styles from "./page.module.css";

export default function Productos() {
  const [inputs, setInputs] = useState({
    pedido: '',
    estado: '',
  });

  const [pedidos, setPedidos] = useState([]);

  useEffect(() => {
    const fetchPedidos = async () => {
      try {
        const res = await fetch('http://localhost:3080/pedidos');
        const data = await res.json();
        setPedidos(data);
      } catch (err) {
        console.error('Error al obtener categorías', err);
      }
    };

    fetchPedidos();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setInputs(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSave = async () => {
    const res = await fetch(`http://localhost:3080/pedidos/${inputs.pedido}/estado/${inputs.estado}`, {
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
      <h1>Pedidos Nuevos Page</h1>
      <div className={styles.inputContainer}>
        <select
          name="pedido"
          value={inputs.pedido}
          onChange={handleChange}
        >
          <option value="">Seleccioná un Pedido</option>
          {pedidos.map((pedido) => (
            <option key={pedido.id} value={pedido.id}>
              {pedido.id}
            </option>
          ))}
        </select>
        {inputs.pedido && `Estado actual: ${pedidos.find(p => p.id === inputs.pedido).estado}` }
        {inputs.pedido && (
          <select name="estado" onChange={handleChange}>
            <option value="">Seleccione una estado</option>
            <option value="ENTREGADO">ENTREGADO</option>
            <option value="CANCELADO">CANCELADO</option>
          </select>
        )}
        <button className={styles.button} onClick={handleSave}>Guardar</button>
      </div>
    </>
  );
};
