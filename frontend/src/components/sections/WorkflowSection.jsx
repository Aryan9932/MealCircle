function WorkflowSection({ steps }) {
  return (
    <section className="workflow" id="how-it-works">
      <h2>How Meal Circle Works</h2>
      <div className="steps">
        {steps.map((step) => (
          <div key={step.number}>
            <span>{step.number}</span>
            <h4>{step.title}</h4>
            <p>{step.description}</p>
          </div>
        ))}
      </div>
    </section>
  );
}

export default WorkflowSection;
