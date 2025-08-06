'use client';

import Navbar from "@/components/Navbar";
import Sidebar from "@/components/Sidebar";
import styles from "./page.module.css";

export default function ProductoLayout({ children }) {
  return (
    <div className={styles.main}>
      <Navbar />
      <div className="container">
        <Sidebar />
        <main>{children}</main>
      </div>
      <style jsx>{`
        .container {
          display: flex;
        }
        main {
          display: flex;
          flex-direction: column;
          gap:10px;
          padding: 20px;
          flex:1;
        }
      `}</style>
    </div>
  );
}
