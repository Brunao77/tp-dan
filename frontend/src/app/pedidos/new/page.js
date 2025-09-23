'use client';
import { useEffect, useState } from 'react';
import styles from "./page.module.css";

export default function Productos() {
  const [inputs, setInputs] = useState({
    cliente: '',
    obras: '',
    obraId: '',
    obvservaciones: ''
  });

  const [clientes, setClientes] = useState([]);
  const [productos, setProductos] = useState([]);
  const [items, setItems] = useState([]);
  const [nuevoItem, setNuevoItem] = useState({ productoId: '', cantidad: 1 });

  useEffect(() => {
    const fetchClientes = async () => {
      try {
        const res = await fetch('http://localhost:6080/api/clientes');
        const data = await res.json();
        setClientes(data);

        const res2 = await fetch('http://localhost:6180/api/productos')
        const productos = await res2.json()
        setProductos(productos)
      } catch (err) {
        console.error('Error al obtener categorías', err);
      }
    };

    fetchClientes();
  }, []);

  const handleChangeItem = (e) => {
    const { name, value } = e.target;
    setNuevoItem(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleAddItem = () => {
    if (!nuevoItem.productoId || nuevoItem.cantidad <= 0) return;

    console.log(nuevoItem)

    const producto = productos.find(p => p.id === parseInt(nuevoItem.productoId));

    setItems(prev => [
      ...prev,
      { ...producto, cantidad: parseInt(nuevoItem.cantidad) }
    ]);

    setNuevoItem({ productoId: '', cantidad: 1 });
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setInputs(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleChangeCliente = async (e) => {
    const { name, value } = e.target;

    const clienteSeleccionado = clientes.find(c => c.id === parseInt(value));

    setInputs(prev => ({
      ...prev,
      [name]: value,
      obras: clienteSeleccionado?.obras || []
    }));
    
  };
  
  const handleSave = async () => {
    const detalle = items.map(({ id, cantidad }) =>{
      return {
        idProducto: id,
        cantidad: cantidad
      }
    })
    const payload = {
      idObra: inputs.obraId,
      idCliente: inputs.cliente,
      obvservaciones: inputs.obvservaciones,
      detalle
    };
    console.log(payload);

    const res = await fetch('http://localhost:6280/api/pedidos', {
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
      <h1>Pedidos Nuevos Page</h1>
      <div className={styles.inputContainer}>
        <select
          name="cliente"
          value={inputs.cliente}
          onChange={handleChangeCliente}
        >
          <option value="">Seleccioná un Cliente</option>
          {clientes.map((cliente) => (
            <option key={cliente.id} value={cliente.id}>
              {cliente.nombre}
            </option>
          ))}
        </select>
        {inputs.obras && (
          <select name="obraId" onChange={handleChange}>
            <option value="">Seleccione una obra</option>
            {inputs.obras.map(o => (
              <option key={o.id} value={o.id}>{o.direccion}</option>
            ))}
          </select>
        )}
        {inputs.obraId && <textarea
          placeholder="Obvservaciones"
          name="obvservaciones" 
          value={inputs.obvservaciones} 
          onChange={handleChange} 
        />}
        {inputs.obraId && <div>
          <select
            name="productoId"
            value={nuevoItem.productoId}
            onChange={handleChangeItem}
          >
            <option value="">Seleccione un producto</option>
            {productos.map(p => (
              <option key={p.id} value={p.id}>
                {p.nombre} - ${p.precio}
              </option>
            ))}
          </select>
          <input
            type="number"
            name="cantidad"
            min="1"
            value={nuevoItem.cantidad}
            onChange={handleChangeItem}
          />
          <button type="button" onClick={handleAddItem}>
            Agregar
          </button>
        </div>}
        <ul>
          {items.map((item, i) => (
            <li key={i}>
              {item.nombre} (x{item.cantidad})
            </li>
          ))}
        </ul>
        <button className={styles.button} onClick={handleSave}>Guardar</button>
      </div>
    </>
  );
};
