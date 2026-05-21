import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { getAllMesses, getMessRating } from "../../services/messApi";

function ExploreMessesPage() {
  const [messes, setMesses] = useState([]);
  const [messesWithRatings, setMessesWithRatings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Filter states
  const [minPrice, setMinPrice] = useState(0);
  const [maxPrice, setMaxPrice] = useState(10000);
  const [minRating, setMinRating] = useState(0);

  // Load all messes and their ratings
  useEffect(() => {
    let isMounted = true;

    async function loadMesses() {
      try {
        setLoading(true);
        setError("");
        const data = await getAllMesses();
        if (isMounted) {
          const messesArray = Array.isArray(data) ? data : [];
          setMesses(messesArray);

          // Fetch ratings for each mess
          const messesWithRatingsData = await Promise.all(
            messesArray.map(async (mess) => {
              try {
                const ratingData = await getMessRating(mess.id);
                return {
                  ...mess,
                  averageRating: ratingData.averageRating || 0,
                  totalReviews: ratingData.totalReviews || 0,
                };
              } catch (err) {
                return {
                  ...mess,
                  averageRating: 0,
                  totalReviews: 0,
                };
              }
            }),
          );
          setMessesWithRatings(messesWithRatingsData);
        }
      } catch (loadError) {
        if (isMounted) {
          setError(loadError.message || "Failed to load messes.");
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    }

    loadMesses();

    return () => {
      isMounted = false;
    };
  }, []);

  // Filter messes based on price and rating
  const filteredMesses = messesWithRatings.filter((mess) => {
    const price = Number(mess.pricePerMonth || 0);
    const rating = mess.averageRating || 0;
    return price >= minPrice && price <= maxPrice && rating >= minRating;
  });

  return (
    <main className="explore-page">
      <div className="explore-actions">
        <Link className="btn btn-ghost" to="/">
          Back To Home
        </Link>
      </div>

      <section className="explore-shell">
        <div className="explore-head">
          <p className="eyebrow">Discover</p>
          <h2>Explore All Messes</h2>
          <p>
            Browse all listed mess options, compare price and menu, then choose
            what fits you best.
          </p>
        </div>

        {/* Filters Section */}
        {!loading && !error && (
          <div className="filters-container">
            <div className="filter-group">
              <label htmlFor="minPrice" className="filter-label">
                Min Price (Rs)
              </label>
              <input
                id="minPrice"
                type="number"
                min="0"
                value={minPrice}
                onChange={(e) => setMinPrice(Number(e.target.value))}
                className="filter-input"
              />
            </div>

            <div className="filter-group">
              <label htmlFor="maxPrice" className="filter-label">
                Max Price (Rs)
              </label>
              <input
                id="maxPrice"
                type="number"
                min="0"
                value={maxPrice}
                onChange={(e) => setMaxPrice(Number(e.target.value))}
                className="filter-input"
              />
            </div>

            <div className="filter-group">
              <label htmlFor="minRating" className="filter-label">
                Min Rating
              </label>
              <select
                id="minRating"
                value={minRating}
                onChange={(e) => setMinRating(Number(e.target.value))}
                className="filter-select"
              >
                <option value={0}>All Ratings</option>
                <option value={1}>★ 1.0+</option>
                <option value={2}>★ 2.0+</option>
                <option value={3}>★ 3.0+</option>
                <option value={4}>★ 4.0+</option>
                <option value={5}>★ 5.0 (Perfect)</option>
              </select>
            </div>

            <button
              className="btn btn-secondary filter-reset"
              onClick={() => {
                setMinPrice(0);
                setMaxPrice(10000);
                setMinRating(0);
              }}
            >
              Reset Filters
            </button>

            <div className="filter-results-count">
              Showing {filteredMesses.length} of {messesWithRatings.length}{" "}
              messes
            </div>
          </div>
        )}

        {loading && <p className="explore-info">Loading messes...</p>}
        {error && !loading && (
          <p className="explore-info explore-error">{error}</p>
        )}

        {!loading && !error && messesWithRatings.length === 0 && (
          <p className="explore-info">No messes found yet. Check back soon.</p>
        )}

        {!loading &&
          !error &&
          filteredMesses.length === 0 &&
          messesWithRatings.length > 0 && (
            <p className="explore-info">
              No messes match your filter criteria. Try expanding your filters.
            </p>
          )}

        {!loading && !error && filteredMesses.length > 0 && (
          <div className="mess-grid">
            {filteredMesses.map((mess) => (
              <Link
                className="mess-card-link"
                key={mess.id}
                to={`/explore-mess/${mess.id}`}
              >
                <article className="mess-card">
                  {mess.imageUrl ? (
                    <img
                      src={mess.imageUrl}
                      alt={mess.messName || "Mess"}
                      className="mess-image"
                    />
                  ) : (
                    <div className="mess-image mess-image-fallback">
                      No Image
                    </div>
                  )}

                  <div className="mess-body">
                    <div className="mess-card-header">
                      <h3>{mess.messName || "Unnamed Mess"}</h3>
                      {mess.averageRating > 0 && (
                        <div className="mess-rating-badge">
                          <span className="rating-stars">★</span>
                          <span className="rating-value">
                            {mess.averageRating.toFixed(1)}
                          </span>
                          <span className="rating-count">
                            ({mess.totalReviews})
                          </span>
                        </div>
                      )}
                    </div>
                    <p className="mess-meta">
                      {mess.type || "N/A"} ·{" "}
                      {mess.address || "Address not available"}
                    </p>
                    <p className="mess-price">
                      Rs {Number(mess.pricePerMonth || 0).toLocaleString()} /
                      month
                    </p>
                    <p className="mess-text">
                      <strong>Today:</strong> {mess.todaysMenu || "Not updated"}
                    </p>
                    <p className="mess-text">
                      <strong>Notice:</strong> {mess.notices || "No notices"}
                    </p>
                    <p className="mess-contact">
                      Contact: {mess.ownerPhone || "N/A"}
                    </p>
                  </div>
                </article>
              </Link>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}

export default ExploreMessesPage;
