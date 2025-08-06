"use client";
import Link from 'next/link';

const Navbar = () => {
  return (
    <nav>
      <Link href="/clientes">Clientes</Link>
      <Link href="/productos">Productos</Link>
      <Link href="/pedidos">Pedidos</Link>
      <style jsx>{`
        nav {
          background: #333;
          padding: 10px;
          display: flex;
          justify-content: space-between;
        }
        a {
          color: white;
          margin-right: 10px;
          text-decoration: none;
        }
      `}</style>
    </nav>
  );
};

export default Navbar;
