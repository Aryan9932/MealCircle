import { Link, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import {
  addOrUpdateMessReview,
  createRazorpayOrder,
  getMessById,
  getMessRating,
  getMessReviews,
  verifyPaymentAndJoin,
} from "../../services/messApi";

function loadRazorpayScript() {
  return new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true);
      return;
    }

    const existingScript = document.querySelector(
      'script[src="https://checkout.razorpay.com/v1/checkout.js"]',
    );

    if (existingScript) {
      existingScript.addEventListener("load", () => resolve(true), {
        once: true,
      });
      existingScript.addEventListener("error", () => resolve(false), {
        once: true,
      });
      return;
    }

    const script = document.createElement("script");
    script.src = "https://checkout.razorpay.com/v1/checkout.js";
    script.async = true;
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
}

function MessDetailPage({ isAuthenticated, token, onOpenAuth }) {
  const { messId } = useParams();
  const [mess, setMess] = useState(null);
  const [rating, setRating] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [joinLoading, setJoinLoading] = useState(false);
  const [joinStatus, setJoinStatus] = useState({ type: "", message: "" });
  const [reviewForm, setReviewForm] = useState({ rating: 5, comment: "" });
  const [reviewLoading, setReviewLoading] = useState(false);
  const [reviewStatus, setReviewStatus] = useState({ type: "", message: "" });

  useEffect(() => {
    let isMounted = true;

    async function loadDetails() {
      try {
        setLoading(true);
        setError("");

        const [messData, ratingData, reviewsData] = await Promise.all([
          getMessById(messId),
          getMessRating(messId),
          getMessReviews(messId),
        ]);

        if (!isMounted) {
          return;
        }

        setMess(messData || null);
        setRating(ratingData || null);
        setReviews(Array.isArray(reviewsData) ? reviewsData : []);
      } catch (detailError) {
        if (isMounted) {
          setError(detailError.message || "Failed to load mess details.");
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    }

    loadDetails();

    return () => {
      isMounted = false;
    };
  }, [messId]);

  const handleJoin = async () => {
    if (!isAuthenticated) {
      setJoinStatus({
        type: "error",
        message: "Please login as customer to join this mess.",
      });
      onOpenAuth();
      return;
    }

    try {
      setJoinLoading(true);
      setJoinStatus({ type: "", message: "" });

      const sdkLoaded = await loadRazorpayScript();
      if (!sdkLoaded) {
        throw new Error("Unable to load Razorpay checkout. Please try again.");
      }

      const order = await createRazorpayOrder({ messId, token });

      const paymentResult = await new Promise((resolve, reject) => {
        const razorpay = new window.Razorpay({
          key: order.key,
          amount: order.amount,
          currency: order.currency || "INR",
          name: "MealCircle",
          description: `Payment for ${order.messName || "mess subscription"}`,
          order_id: order.orderId,
          handler: (response) => resolve(response),
          modal: {
            ondismiss: () => reject(new Error("Payment was cancelled.")),
          },
          theme: {
            color: "#126e82",
          },
        });

        razorpay.on("payment.failed", (event) => {
          const failureMessage =
            event?.error?.description || "Payment failed. Please try again.";
          reject(new Error(failureMessage));
        });

        razorpay.open();
      });

      await verifyPaymentAndJoin({
        messId,
        token,
        razorpayOrderId: paymentResult.razorpay_order_id,
        razorpayPaymentId: paymentResult.razorpay_payment_id,
        razorpaySignature: paymentResult.razorpay_signature,
      });

      setJoinStatus({
        type: "success",
        message: "Payment successful. Joined mess successfully.",
      });
    } catch (joinError) {
      setJoinStatus({
        type: "error",
        message: joinError.message || "Unable to join this mess.",
      });
    } finally {
      setJoinLoading(false);
    }
  };

  const handleSubmitReview = async (event) => {
    event.preventDefault();

    if (!isAuthenticated) {
      setReviewStatus({
        type: "error",
        message: "Please login as customer to add your review.",
      });
      onOpenAuth();
      return;
    }

    try {
      setReviewLoading(true);
      setReviewStatus({ type: "", message: "" });

      await addOrUpdateMessReview({
        messId,
        token,
        rating: Number(reviewForm.rating),
        comment: reviewForm.comment,
      });

      const [ratingData, reviewsData] = await Promise.all([
        getMessRating(messId),
        getMessReviews(messId),
      ]);

      setRating(ratingData || null);
      setReviews(Array.isArray(reviewsData) ? reviewsData : []);
      setReviewForm((prev) => ({ ...prev, comment: "" }));
      setReviewStatus({
        type: "success",
        message: "Review submitted successfully.",
      });
    } catch (submitError) {
      setReviewStatus({
        type: "error",
        message: submitError.message || "Unable to submit review.",
      });
    } finally {
      setReviewLoading(false);
    }
  };

  return (
    <main className="explore-page">
      <div className="explore-actions">
        <Link className="btn btn-ghost" to="/explore-mess">
          Back To Explore
        </Link>
      </div>

      <section className="explore-shell mess-detail-shell">
        {loading && <p className="explore-info">Loading mess details...</p>}
        {error && !loading && (
          <p className="explore-info explore-error">{error}</p>
        )}

        {!loading && !error && mess && (
          <>
            <div className="mess-detail-head">
              {mess.imageUrl ? (
                <img
                  src={mess.imageUrl}
                  alt={mess.messName || "Mess"}
                  className="mess-detail-image"
                />
              ) : (
                <div className="mess-detail-image mess-image-fallback">
                  No Image
                </div>
              )}

              <div className="mess-detail-meta">
                <p className="eyebrow">Mess Details</p>
                <h2>{mess.messName || "Unnamed Mess"}</h2>
                <p>
                  {mess.type || "N/A"} ·{" "}
                  {mess.address || "Address not available"}
                </p>
                <p className="mess-price">
                  Rs {Number(mess.pricePerMonth || 0).toLocaleString()} / month
                </p>
                <p className="mess-contact">
                  Contact: {mess.ownerPhone || "N/A"}
                </p>

                <div className="join-action-wrap">
                  <button
                    className="btn btn-solid"
                    type="button"
                    onClick={handleJoin}
                    disabled={joinLoading}
                  >
                    {joinLoading ? "Joining..." : "Join Mess"}
                  </button>
                  {joinStatus.message && (
                    <p
                      className={`join-status ${joinStatus.type === "error" ? "status-error" : "status-success"}`}
                    >
                      {joinStatus.message}
                    </p>
                  )}
                </div>
              </div>
            </div>

            <div className="detail-grid">
              <article className="detail-card">
                <h3>Today's Menu</h3>
                <p>{mess.todaysMenu || "Not updated yet"}</p>
              </article>

              <article className="detail-card">
                <h3>Notices</h3>
                <p>{mess.notices || "No notices available"}</p>
              </article>

              <article className="detail-card">
                <h3>Rating</h3>
                <p className="rating-number">
                  {rating?.averageRating != null
                    ? Number(rating.averageRating).toFixed(1)
                    : "0.0"}{" "}
                  / 5
                </p>
                <p>{rating?.totalReviews ?? 0} review(s)</p>
              </article>
            </div>

            <section className="reviews-list">
              <div className="review-form-card">
                <h3>Rate And Review This Mess</h3>
                <form className="review-form" onSubmit={handleSubmitReview}>
                  <label>
                    Your Rating
                    <select
                      value={reviewForm.rating}
                      onChange={(event) =>
                        setReviewForm((prev) => ({
                          ...prev,
                          rating: event.target.value,
                        }))
                      }
                    >
                      <option value={5}>5 - Excellent</option>
                      <option value={4}>4 - Good</option>
                      <option value={3}>3 - Average</option>
                      <option value={2}>2 - Poor</option>
                      <option value={1}>1 - Very Poor</option>
                    </select>
                  </label>

                  <label>
                    Comment
                    <textarea
                      value={reviewForm.comment}
                      onChange={(event) =>
                        setReviewForm((prev) => ({
                          ...prev,
                          comment: event.target.value,
                        }))
                      }
                      rows={3}
                      placeholder="Share your experience with food, service and quality..."
                    />
                  </label>

                  {reviewStatus.message && (
                    <p
                      className={`join-status ${reviewStatus.type === "error" ? "status-error" : "status-success"}`}
                    >
                      {reviewStatus.message}
                    </p>
                  )}

                  <button
                    className="btn btn-solid"
                    type="submit"
                    disabled={reviewLoading}
                  >
                    {reviewLoading ? "Submitting..." : "Submit Review"}
                  </button>
                </form>
              </div>

              <h3>All Reviews</h3>
              {reviews.length === 0 ? (
                <p className="explore-info">No reviews yet for this mess.</p>
              ) : (
                <div className="review-item-grid">
                  {reviews.map((review) => (
                    <article
                      className="review-item"
                      key={
                        review.id || `${review.customerId}-${review.updatedAt}`
                      }
                    >
                      <p className="review-rating">Rating: {review.rating}/5</p>
                      <p className="review-comment">
                        {review.comment || "No comment"}
                      </p>
                      <p className="review-user">
                        User: {review.customerId || "Anonymous"}
                      </p>
                    </article>
                  ))}
                </div>
              )}
            </section>
          </>
        )}
      </section>
    </main>
  );
}

export default MessDetailPage;
