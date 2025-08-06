import Navbar from './Navbar';
import Sidebar from './Sidebar';

const Layout = ({ children }) => {
  return (
    <div>
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
          padding: 20px;
        }
      `}</style>
    </div>
  );
};

export default Layout;
