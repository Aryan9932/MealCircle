function CtaSection({ content }) {
  return (
    <section className="cta">
      <h2>{content.title}</h2>
      <p>{content.description}</p>
      <button className="btn btn-solid">{content.action}</button>
    </section>
  );
}

export default CtaSection;
