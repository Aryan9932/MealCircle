function Footer({ brand, text }) {
  return (
    <footer className="footer">
      <p>{brand}</p>
      <span>{text}</span>
    </footer>
  );
}

export default Footer;
