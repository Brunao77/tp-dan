'use client';
import { useState } from 'react';

import Link from 'next/link';
import styles from "./page.module.css";

export default function Productos() {
  const [filtros, setFiltros] = useState({
    cuit: ''
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
    const res = await fetch("http://localhost:6080/api/clientes")
    const lista = await res.json()

    const filtrados = lista.filter(product => {
      return filtros.cuit === '' || product.cuit === filtros.cuit;
    });

    setResults(filtrados);
  };

  const handleDelete = async (id) => {
    const confirmado = window.confirm("¿Estás seguro de que querés eliminar este cliente?");
    if (!confirmado) return;

    const res = await fetch(`http://localhost:6080/api/clientes/${id}`, {
      method: 'DELETE',
    });

    if (res.ok) {
      alert("Cliente eliminado con éxito.");
      setResults(prev => prev.filter(p => p.id !== id));
    } else {
      const error = await res.json();
      alert("Error al eliminar: " + error.description);
    }
  };

  return (
    <>
      <h1>Clientes Page</h1>
      <Link href="/clientes/new">
        <button className={styles.button}>Crear nuevo cliente</button>
      </Link>
      <div className={styles.inputContainer}>
        <input
          type="number" 
          name="cuit"
          placeholder="CUIT" 
          value={filtros.cuit}
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
          {results.map(client => (
            <tr key={client.id}>
              <td>{client.id}</td>
              <td>{client.nombre}</td>
              <td>
                <Link href={`/clientes/${client.id}`}><button>Editar</button></Link>
                <button onClick={() => handleDelete(client.id)}>Eliminar</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
};
