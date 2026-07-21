import { Link } from "react-router-dom";
import { useEffect, useRef, useState } from "react";
import { getAllMesses, getMessRating, getNearbyMesses } from "../../services/messApi";

// Location state machine values
const GEO_STATE = {
  IDLE: "idle",
  REQUESTING: "requesting",
  GRANTED: "granted",
  DENIED: "denied",
  UNSUPPORTED: "unsupported",
};

// View mode: nearby results OR all messes
const VIEW_MODE = {
  NEARBY: "nearby",
  ALL: "all",
};

function ExploreMessesPage() {
  const [messes, setMesses] = useState([]);
  const [allMessesCache, setAllMessesCache] = useState([]); // cache of all messes
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Geolocation state
  const [geoState, setGeoState] = useState(GEO_STATE.IDLE);
  const [userCoords, setUserCoords] = useState(null);
  const [radiusKm, setRadiusKm] = useState(5);
  const radiusRef = useRef(radiusKm);
  radiusRef.current = radiusKm;

  // View mode toggle (only relevant when location is granted)
  const [viewMode, setViewMode] = useState(VIEW_MODE.NEARBY);

  // Filter states
  const [minPrice, setMinPrice] = useState(0);
  const [maxPrice, setMaxPrice] = useState(10000);
  const [minRating, setMinRating] = useState(0);

  // Helper: enrich a plain mess array with ratings
  async function enrichWithRatings(messesArray) {
    return Promise.all(
      messesArray.map(async (mess) => {
        try {
          const ratingData = await getMessRating(mess.id);
          return {
            ...mess,
            averageRating: ratingData.averageRating || 0,
            totalReviews: ratingData.totalReviews || 0,
          };
        } catch {
          return { ...mess, averageRating: 0, totalReviews: 0 };
        }
      })
    );
  }

  // Load all messes
  async function loadAllMesses(isMounted, updateCache = true) {
    try {
      setLoading(true);
      setError("");
      const data = await getAllMesses();
      if (!isMounted.current) return;
      const messesArray = Array.isArray(data) ? data : [];
      const enriched = await enrichWithRatings(messesArray);
      if (isMounted.current) {
        setMesses(enriched);
        if (updateCache) setAllMessesCache(enriched);
      }
    } catch (err) {
      if (isMounted.current) setError(err.message || "Failed to load messes.");
    } finally {
      if (isMounted.current) setLoading(false);
    }
  }

  // Load nearby messes
  async function loadNearbyMesses(coords, radius, isMounted) {
    try {
      setLoading(true);
      setError("");
      const results = await getNearbyMesses({
        lat: coords.lat,
        lng: coords.lng,
        radius,
      });
      if (!isMounted.current) return;
      const enriched = await Promise.all(
        results.map(async (item) => {
          try {
            const ratingData = await getMessRating(item.mess.id);
            return {
              ...item.mess,
              averageRating: ratingData.averageRating || 0,
              totalReviews: ratingData.totalReviews || 0,
              distanceKm: item.distanceKm,
              distanceMeters: item.distanceMeters,
            };
          } catch {
            return {
              ...item.mess,
              averageRating: 0,
              totalReviews: 0,
              distanceKm: item.distanceKm,
              distanceMeters: item.distanceMeters,
            };
          }
        })
      );
      if (isMounted.current) setMesses(enriched);
    } catch (err) {
      if (isMounted.current) setError(err.message || "Failed to load nearby messes.");
    } finally {
      if (isMounted.current) setLoading(false);
    }
  }

  // On mount: request geolocation
  useEffect(() => {
    const isMounted = { current: true };

    if (!navigator.geolocation) {
      setGeoState(GEO_STATE.UNSUPPORTED);
      loadAllMesses(isMounted);
      return () => { isMounted.current = false; };
    }

    setGeoState(GEO_STATE.REQUESTING);

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const coords = {
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        };
        setUserCoords(coords);
        setGeoState(GEO_STATE.GRANTED);
        loadNearbyMesses(coords, radiusRef.current, isMounted);
      },
      () => {
        setGeoState(GEO_STATE.DENIED);
        loadAllMesses(isMounted);
      },
      { enableHighAccuracy: true, timeout: 8000 }
    );

    return () => { isMounted.current = false; };
  }, []);

  // Re-fetch when radius changes (only if location granted AND in nearby mode)
  useEffect(() => {
    if (geoState !== GEO_STATE.GRANTED || !userCoords || viewMode !== VIEW_MODE.NEARBY) return;
    const isMounted = { current: true };
    loadNearbyMesses(userCoords, radiusKm, isMounted);
    return () => { isMounted.current = false; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [radiusKm]);

  // Handle toggle between Nearby and All Messes
  function handleToggleViewMode(mode) {
    if (mode === viewMode) return;
    setViewMode(mode);

    if (mode === VIEW_MODE.ALL) {
      if (allMessesCache.length > 0) {
        // Use cached data instantly — no loading spinner
        setMesses(allMessesCache);
      } else {
        const isMounted = { current: true };
        loadAllMesses(isMounted, true);
      }
    } else {
      // Switch back to nearby
      if (userCoords) {
        const isMounted = { current: true };
        loadNearbyMesses(userCoords, radiusKm, isMounted);
      }
    }
  }

  // Filter
  const filteredMesses = messes.filter((mess) => {
    const price = Number(mess.pricePerMonth || 0);
    const rating = mess.averageRating || 0;
    return price >= minPrice && price <= maxPrice && rating >= minRating;
  });

  const isNearbyMode = geoState === GEO_STATE.GRANTED && viewMode === VIEW_MODE.NEARBY;
  const locationGranted = geoState === GEO_STATE.GRANTED;

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
          <h2>{isNearbyMode ? "Nearby Messes" : "Explore All Messes"}</h2>
          <p>
            {isNearbyMode
              ? "Messes sorted by distance from your current location."
              : "Browse all listed mess options, compare price and menu, then choose what fits you best."}
          </p>

          {/* View Mode Toggle — only shown when location permission is granted */}
          {locationGranted && (
            <div className="view-mode-toggle">
              <button
                id="btn-nearby-messes"
                className={`toggle-btn${viewMode === VIEW_MODE.NEARBY ? " toggle-btn--active" : ""}`}
                onClick={() => handleToggleViewMode(VIEW_MODE.NEARBY)}
              >
                📍 Nearby Messes
              </button>
              <button
                id="btn-all-messes"
                className={`toggle-btn${viewMode === VIEW_MODE.ALL ? " toggle-btn--active" : ""}`}
                onClick={() => handleToggleViewMode(VIEW_MODE.ALL)}
              >
                🌐 All Messes
              </button>
            </div>
          )}
        </div>

        {/* Geolocation status banners */}
        {geoState === GEO_STATE.REQUESTING && (
          <div className="geo-banner geo-banner--requesting">
            <span className="geo-icon">📍</span>
            Requesting your location to find nearby messes…
          </div>
        )}

        {geoState === GEO_STATE.DENIED && (
          <div className="geo-banner geo-banner--denied">
            <span className="geo-icon">📍</span>
            Location access denied — showing all messes. To enable nearby
            search, allow location permission in your browser and refresh the
            page.
          </div>
        )}

        {geoState === GEO_STATE.UNSUPPORTED && (
          <div className="geo-banner geo-banner--denied">
            <span className="geo-icon">📍</span>
            Your browser does not support geolocation — showing all messes.
          </div>
        )}

        {/* Filters */}
        {!loading && !error && (
          <div className="filters-container">
            {/* Radius slider — only in nearby mode */}
            {isNearbyMode && (
              <div className="filter-group filter-group--radius">
                <label htmlFor="radiusSlider" className="filter-label">
                  Search Radius: <strong>{radiusKm} km</strong>
                </label>
                <input
                  id="radiusSlider"
                  type="range"
                  min="1"
                  max="20"
                  step="1"
                  value={radiusKm}
                  onChange={(e) => setRadiusKm(Number(e.target.value))}
                  className="filter-range"
                />
                <div className="filter-range-labels">
                  <span>1 km</span>
                  <span>20 km</span>
                </div>
              </div>
            )}

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
                if (isNearbyMode) setRadiusKm(5);
              }}
            >
              Reset Filters
            </button>

            <div className="filter-results-count">
              Showing {filteredMesses.length} of {messes.length} messes
            </div>
          </div>
        )}

        {loading && <p className="explore-info">Loading messes…</p>}
        {error && !loading && (
          <p className="explore-info explore-error">{error}</p>
        )}

        {!loading && !error && messes.length === 0 && (
          <p className="explore-info">
            {isNearbyMode
              ? `No messes found within ${radiusKm} km. Try increasing the radius or switch to All Messes.`
              : "No messes found yet. Check back soon."}
          </p>
        )}

        {!loading && !error && filteredMesses.length === 0 && messes.length > 0 && (
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
                      <div className="mess-card-badges">
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
                        {isNearbyMode && mess.distanceKm !== undefined && (
                          <div className="mess-distance-badge">
                            📍 {mess.distanceKm < 1
                              ? `${Math.round(mess.distanceMeters)} m`
                              : `${mess.distanceKm.toFixed(2)} km`}
                          </div>
                        )}
                      </div>
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