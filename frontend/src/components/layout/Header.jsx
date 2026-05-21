import { Link } from "react-router-dom";

function Header({ brand, links, isAuthenticated, onOpenAuth, onLogout }) {
  return (
    <header className="topbar">
      <div className="brand">{brand}</div>
      <nav>
        {links.map((link) =>
          link.href.startsWith("/") ? (
            <Link key={link.href} to={link.href}>
              {link.label}
            </Link>
          ) : (
            <a key={link.href} href={link.href}>
              {link.label}
            </a>
          ),
        )}
      </nav>

      {!isAuthenticated ? (
        <button className="btn btn-outline" onClick={onOpenAuth}>
          Sign In
        </button>
      ) : (
        <div className="auth-status-wrap">
          <span className="auth-status">Logged In</span>
          <button className="logout-link" onClick={onLogout}>
            Logout
          </button>
        </div>
      )}
    </header>
  );
}

export default Header;
