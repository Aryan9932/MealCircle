import chefImage from "../../assets/chef-main.png";

function HeroSection({ content }) {
  return (
    <section className="hero" id="hero">
      <div className="hero-copy">
        <p className="eyebrow hero-eyebrow">{content.eyebrow}</p>
        <h1 className="hero-title">
          {content.titleStart}
          <span className="hero-title-accent">{content.titleAccent}</span>
        </h1>
        <p className="hero-description">{content.description}</p>
        <div className="hero-actions">
          <button className="btn btn-solid hero-cta-text">
            {content.primaryAction}
          </button>
          <button className="btn btn-ghost hero-cta-text">
            {content.secondaryAction}
          </button>
        </div>
      </div>

      <div className="hero-card">
        <img src={chefImage} alt="Meal Circle chef" className="chef-mascot" />
      </div>
    </section>
  );
}

export default HeroSection;
