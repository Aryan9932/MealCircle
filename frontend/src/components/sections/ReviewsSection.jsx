function ReviewsSection({ reviews }) {
  return (
    <section className="reviews" id="reviews">
      <h2>People Love Meal Circle</h2>
      <div className="review-grid">
        {reviews.map((review) => (
          <blockquote key={review.author}>
            <p>{review.quote}</p>
            <cite>{review.author}</cite>
          </blockquote>
        ))}
      </div>
    </section>
  );
}

export default ReviewsSection;
