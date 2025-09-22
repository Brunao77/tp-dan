'use client';
import { useState } from 'react';
import Link from 'next/link';
import styles from "./page.module.css";


export default function Productos() {
  const [filtros, setFiltros] = useState({
    cliente: '',
    estado: '',
  });
  const [results, setResults] = useState([]);
  const [ESTADOS, setEstados] = useState([
    "ACEPTADO",
    "RECHAZADO",
    "CANCELADO",
    "EN_PREPARACION",
    "ENTREGADO"
  ])

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFiltros(prev => ({
      ...prev,
      [name]: value,
    }));
  };
  
  const handleSearch = async () => {
    const res = await fetch("http://localhost:6280/api/pedidos")
    const lista = await res.json()

    const filtrados = lista.filter(pedido => {
      const matchCliente = filtros.cliente === '' || pedido.cliente.toLowerCase().includes(filtros.cliente.toLowerCase())
      const matchEstado = filtros.estado === '' || pedido.estado == filtros.estado

      return (
        matchCliente &&
        matchEstado
      );
    });

    setResults(filtrados);
  };

  return (
    <>
      <h1>Pedidos Page</h1>
      <Link href="/pedidos/new">
        <button className={styles.button}>Crear nuevo pedido</button>
      </Link>
      <Link href="/pedidos/estado">
        <button className={styles.button}>Actualizar estado</button>
      </Link>
      <div className={styles.inputContainer}>
        <input
          type="text" 
          name="cliente"
          placeholder="Cliente" 
          value={filtros.cliente}
          onChange={handleChange}
        />
        <select
          name="estado"
          value={filtros.estado}
          onChange={handleChange}
        >
          <option value="">Seleccioná un estado</option>
          {ESTADOS.map((estado) => (
            <option key={estado} value={estado}>
              {estado}
            </option>
          ))}
        </select>
        <button className={styles.button} onClick={handleSearch}>Buscar</button>
      </div>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Nombre</th>
          </tr>
        </thead>
        <tbody>
          {results.map(product => (
            <tr key={product.id}>
              <td>
                {product.id}
                
              </td>
              <td>{product.nombre}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
};
