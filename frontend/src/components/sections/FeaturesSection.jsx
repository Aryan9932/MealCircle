function FeaturesSection({ items }) {
  return (
    <section className="features" id="features">
      {items.map((item) => (
        <article key={item.title}>
          <h3>{item.title}</h3>
          <p>{item.description}</p>
        </article>
      ))}
    </section>
  );
}

export default FeaturesSection;
