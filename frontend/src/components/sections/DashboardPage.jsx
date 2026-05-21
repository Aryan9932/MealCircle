import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getCurrentUser } from "../../services/authApi";
import {
  addAbsentDate,
  getMySubscriptions,
  getOwnerMess,
  getOwnerMessSubscriptions,
  getSubscriptionDetails,
  updateOwnerMenuNotice,
} from "../../services/messApi";

function formatDateTime(value) {
  if (!value) {
    return "N/A";
  }

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }

  return parsed.toLocaleString();
}

function formatDate(value) {
  if (!value) {
    return "N/A";
  }

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }

  return parsed.toLocaleDateString();
}

function normalizeToDateKey(value) {
  if (!value) {
    return "";
  }

  if (typeof value === "string") {
    return value.slice(0, 10);
  }

  if (value instanceof Date) {
    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, "0");
    const day = String(value.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  }

  if (Array.isArray(value) && value.length >= 3) {
    const year = String(value[0]);
    const month = String(value[1]).padStart(2, "0");
    const day = String(value[2]).padStart(2, "0");
    return `${year}-${month}-${day}`;
  }

  if (
    typeof value === "object" &&
    value.year != null &&
    value.monthValue != null &&
    value.dayOfMonth != null
  ) {
    const year = String(value.year);
    const month = String(value.monthValue).padStart(2, "0");
    const day = String(value.dayOfMonth).padStart(2, "0");
    return `${year}-${month}-${day}`;
  }

  return String(value).slice(0, 10);
}

function includesDate(list, dateValue) {
  if (!Array.isArray(list)) {
    return false;
  }

  const target = normalizeToDateKey(dateValue);
  return list.some((entry) => normalizeToDateKey(entry) === target);
}

function DashboardPage({ isAuthenticated, token, onOpenAuth }) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [user, setUser] = useState(null);
  const [customerSubscriptions, setCustomerSubscriptions] = useState([]);
  const [ownerSubscriptions, setOwnerSubscriptions] = useState([]);
  const [ownerMess, setOwnerMess] = useState(null);
  const [ownerFilter, setOwnerFilter] = useState("ALL");
  const [nowMs, setNowMs] = useState(Date.now());
  const [cutoffTriggered, setCutoffTriggered] = useState(false);
  const [absentActionState, setAbsentActionState] = useState({});
  const [ownerUpdateForm, setOwnerUpdateForm] = useState({
    todaysMenu: "",
    notices: "",
  });
  const [ownerUpdateState, setOwnerUpdateState] = useState({
    loading: false,
    message: "",
    type: "",
  });

  const getTodayDateString = () => {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  };

  const getTodayCutoff = () => {
    const cutoff = new Date();
    cutoff.setHours(15, 0, 0, 0);
    return cutoff;
  };

  const refreshCustomerSubscriptions = useCallback(async () => {
    const customerSubs = await getMySubscriptions(token);
    setCustomerSubscriptions(Array.isArray(customerSubs) ? customerSubs : []);
  }, [token]);

  useEffect(() => {
    let isMounted = true;

    async function loadDashboard() {
      if (!isAuthenticated || !token) {
        if (isMounted) {
          setLoading(false);
        }
        return;
      }

      try {
        setLoading(true);
        setError("");

        const currentUser = await getCurrentUser(token);
        if (!isMounted) {
          return;
        }

        setUser(currentUser);

        if (currentUser?.role === "OWNER") {
          const [messData, ownerSubs] = await Promise.all([
            getOwnerMess(token),
            getOwnerMessSubscriptions(token),
          ]);

          if (!isMounted) {
            return;
          }

          setOwnerMess(messData || null);
          setOwnerUpdateForm({
            todaysMenu: messData?.todaysMenu || "",
            notices: messData?.notices || "",
          });

          let resolvedOwnerSubs = Array.isArray(ownerSubs) ? ownerSubs : [];

          // Fallback: if endpoint returns empty but mess has subscription ids,
          // fetch each subscription directly to ensure owner sees all customers.
          if (
            resolvedOwnerSubs.length === 0 &&
            Array.isArray(messData?.subscriptionIds) &&
            messData.subscriptionIds.length > 0
          ) {
            const fetched = await Promise.all(
              messData.subscriptionIds.map(async (subId) => {
                try {
                  return await getSubscriptionDetails({
                    subscriptionId: subId,
                    token,
                  });
                } catch {
                  return null;
                }
              }),
            );

            resolvedOwnerSubs = fetched.filter(Boolean);
          }

          setOwnerSubscriptions(resolvedOwnerSubs);
        } else {
          const customerSubs = await getMySubscriptions(token);
          if (!isMounted) {
            return;
          }

          setCustomerSubscriptions(
            Array.isArray(customerSubs) ? customerSubs : [],
          );
        }
      } catch (loadError) {
        if (isMounted) {
          setError(loadError.message || "Failed to load dashboard data.");
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    }

    loadDashboard();

    return () => {
      isMounted = false;
    };
  }, [isAuthenticated, token]);

  useEffect(() => {
    const timer = setInterval(() => {
      setNowMs(Date.now());
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    if (user?.role !== "CUSTOMER" || !isAuthenticated || !token) {
      return;
    }

    const cutoffTime = getTodayCutoff().getTime();
    const passedCutoff = nowMs >= cutoffTime;

    if (passedCutoff && !cutoffTriggered) {
      setCutoffTriggered(true);
      refreshCustomerSubscriptions().catch(() => {
        setError("Failed to refresh attendance after cutoff");
      });
    }

    if (!passedCutoff) {
      setCutoffTriggered(false);
    }
  }, [
    nowMs,
    user,
    isAuthenticated,
    token,
    cutoffTriggered,
    refreshCustomerSubscriptions,
  ]);

  const handleMarkAbsent = async (subscriptionId) => {
    const today = getTodayDateString();
    setAbsentActionState((prev) => ({
      ...prev,
      [subscriptionId]: { loading: true, message: "", type: "" },
    }));

    try {
      await addAbsentDate({ subscriptionId, date: today, token });
      await refreshCustomerSubscriptions();
      setAbsentActionState((prev) => ({
        ...prev,
        [subscriptionId]: {
          loading: false,
          message: "Marked absent for today.",
          type: "success",
        },
      }));
    } catch (absentError) {
      setAbsentActionState((prev) => ({
        ...prev,
        [subscriptionId]: {
          loading: false,
          message: absentError.message || "Unable to mark absent.",
          type: "error",
        },
      }));
    }
  };

  const handleOwnerUpdateChange = (event) => {
    const { name, value } = event.target;
    setOwnerUpdateForm((prev) => ({
      ...prev,
      [name]: value,
    }));
    setOwnerUpdateState((prev) => ({
      ...prev,
      message: "",
      type: "",
    }));
  };

  const handleOwnerUpdateSubmit = async (event) => {
    event.preventDefault();

    const todaysMenu = ownerUpdateForm.todaysMenu.trim();
    const notices = ownerUpdateForm.notices.trim();

    if (!todaysMenu && !notices) {
      setOwnerUpdateState({
        loading: false,
        message: "Enter menu or notice before saving.",
        type: "error",
      });
      return;
    }

    setOwnerUpdateState({ loading: true, message: "", type: "" });

    try {
      const updatedMess = await updateOwnerMenuNotice({
        token,
        todaysMenu,
        notices,
      });

      setOwnerMess(updatedMess || null);
      setOwnerUpdateForm({
        todaysMenu: updatedMess?.todaysMenu || todaysMenu,
        notices: updatedMess?.notices || notices,
      });
      setOwnerUpdateState({
        loading: false,
        message: "Menu/notice updated successfully.",
        type: "success",
      });
    } catch (updateError) {
      setOwnerUpdateState({
        loading: false,
        message: updateError.message || "Unable to update menu/notice.",
        type: "error",
      });
    }
  };

  const cutoffMsRemaining = Math.max(0, getTodayCutoff().getTime() - nowMs);
  const cutoffHours = String(
    Math.floor(cutoffMsRemaining / (1000 * 60 * 60)),
  ).padStart(2, "0");
  const cutoffMinutes = String(
    Math.floor((cutoffMsRemaining % (1000 * 60 * 60)) / (1000 * 60)),
  ).padStart(2, "0");
  const cutoffSeconds = String(
    Math.floor((cutoffMsRemaining % (1000 * 60)) / 1000),
  ).padStart(2, "0");
  const isBeforeCutoff = cutoffMsRemaining > 0;
  const todayDate = getTodayDateString();

  const customerStats = {
    totalSubscriptions: customerSubscriptions.length,
    totalBuffer: customerSubscriptions.reduce(
      (sum, sub) => sum + (sub.buffer || 0),
      0,
    ),
    totalAbsent: customerSubscriptions.reduce(
      (sum, sub) =>
        sum + (Array.isArray(sub.absentDates) ? sub.absentDates.length : 0),
      0,
    ),
    totalPresent: customerSubscriptions.reduce(
      (sum, sub) =>
        sum + (Array.isArray(sub.presentDates) ? sub.presentDates.length : 0),
      0,
    ),
    totalDue: customerSubscriptions.reduce(
      (sum, sub) => sum + Number(sub.moneyLeftToPay || 0),
      0,
    ),
  };

  const ownerStats = {
    totalCustomers: ownerSubscriptions.length,
    absentToday: ownerSubscriptions.reduce(
      (sum, sub) => sum + (includesDate(sub.absentDates, todayDate) ? 1 : 0),
      0,
    ),
    presentToday: ownerSubscriptions.reduce(
      (sum, sub) => sum + (includesDate(sub.presentDates, todayDate) ? 1 : 0),
      0,
    ),
    pendingToday: ownerSubscriptions.reduce(
      (sum, sub) =>
        sum +
        (!includesDate(sub.absentDates, todayDate) &&
        !includesDate(sub.presentDates, todayDate)
          ? 1
          : 0),
      0,
    ),
    avgBuffer:
      ownerSubscriptions.length > 0
        ? (
            ownerSubscriptions.reduce(
              (sum, sub) => sum + (sub.buffer || 0),
              0,
            ) / ownerSubscriptions.length
          ).toFixed(1)
        : "0.0",
    totalDue: ownerSubscriptions.reduce(
      (sum, sub) => sum + Number(sub.moneyLeftToPay || 0),
      0,
    ),
    totalAbsent: ownerSubscriptions.reduce(
      (sum, sub) =>
        sum + (Array.isArray(sub.absentDates) ? sub.absentDates.length : 0),
      0,
    ),
    totalPresent: ownerSubscriptions.reduce(
      (sum, sub) =>
        sum + (Array.isArray(sub.presentDates) ? sub.presentDates.length : 0),
      0,
    ),
  };

  const absentStudentsToday = ownerSubscriptions.filter((sub) =>
    includesDate(sub.absentDates, todayDate),
  );

  const getOwnerTodayStatus = (subscription) => {
    if (includesDate(subscription.absentDates, todayDate)) {
      return "ABSENT";
    }
    if (includesDate(subscription.presentDates, todayDate)) {
      return "PRESENT";
    }
    return "PENDING";
  };

  const filteredOwnerSubscriptions =
    ownerFilter === "ALL"
      ? ownerSubscriptions
      : ownerSubscriptions.filter(
          (sub) => getOwnerTodayStatus(sub) === ownerFilter,
        );

  if (!isAuthenticated) {
    return (
      <main className="dashboard-page">
        <div className="explore-actions">
          <Link className="btn btn-ghost" to="/">
            Back To Home
          </Link>
        </div>
        <section className="explore-shell">
          <p className="explore-info explore-error">
            Please login to view your dashboard.
          </p>
          <button className="btn btn-solid" onClick={onOpenAuth}>
            Login
          </button>
        </section>
      </main>
    );
  }

  return (
    <main className="dashboard-page">
      <div className="explore-actions">
        <Link className="btn btn-ghost" to="/">
          Back To Home
        </Link>
      </div>

      <section className="explore-shell">
        <div className="explore-head">
          <p className="eyebrow">Dashboard</p>
          <h2>
            {user?.role === "OWNER" ? "Owner Dashboard" : "Customer Dashboard"}
          </h2>
          <p>
            Manage subscriptions, attendance, timelines, and customer activity
            in one place.
          </p>
        </div>

        {loading && <p className="explore-info">Loading dashboard...</p>}
        {error && !loading && (
          <p className="explore-info explore-error">{error}</p>
        )}

        {!loading && !error && user?.role === "CUSTOMER" && (
          <>
            <section className="cutoff-timer-wrap">
              <p className="cutoff-label">
                Absent window for today closes at 3:00 PM
              </p>
              {isBeforeCutoff ? (
                <p className="cutoff-time">
                  {cutoffHours}:{cutoffMinutes}:{cutoffSeconds}
                </p>
              ) : (
                <p className="cutoff-time cutoff-over">
                  Cutoff passed. Today is auto-marked present if not absent.
                </p>
              )}
            </section>

            <section className="dash-stats">
              <article className="stat-tile">
                <p className="tile-label">Active Subscriptions</p>
                <p className="tile-value">{customerStats.totalSubscriptions}</p>
              </article>
              <article className="stat-tile">
                <p className="tile-label">Total Buffer Left</p>
                <p className="tile-value">{customerStats.totalBuffer}</p>
              </article>
              <article className="stat-tile">
                <p className="tile-label">Total Due Amount</p>
                <p className="tile-value">
                  Rs {customerStats.totalDue.toLocaleString()}
                </p>
              </article>
              <article className="stat-tile">
                <p className="tile-label">Attendance (P/A)</p>
                <p className="tile-value">
                  {customerStats.totalPresent} / {customerStats.totalAbsent}
                </p>
              </article>
            </section>

            {customerSubscriptions.length === 0 ? (
              <p className="explore-info">You have not joined any mess yet.</p>
            ) : (
              <div className="dashboard-grid">
                {customerSubscriptions.map((sub) => (
                  <article className="detail-card dashboard-card" key={sub.id}>
                    <div className="card-head">
                      <h3>Subscription Snapshot</h3>
                      <span className="pill">Mess {sub.messId || "N/A"}</span>
                    </div>

                    <div className="mini-grid">
                      <div>
                        <p className="mini-label">Start Date</p>
                        <p className="mini-value">
                          {formatDateTime(sub.joiningDate)}
                        </p>
                      </div>
                      <div>
                        <p className="mini-label">End Date</p>
                        <p className="mini-value">
                          {formatDate(sub.messEndingDate)}
                        </p>
                      </div>
                      <div>
                        <p className="mini-label">Buffer Left</p>
                        <p className="mini-value">{sub.buffer}</p>
                      </div>
                      <div>
                        <p className="mini-label">Amount Due</p>
                        <p className="mini-value">
                          Rs {Number(sub.moneyLeftToPay || 0).toLocaleString()}
                        </p>
                      </div>
                    </div>

                    <div className="attendance-band">
                      <span>
                        Present:{" "}
                        {Array.isArray(sub.presentDates)
                          ? sub.presentDates.length
                          : 0}
                      </span>
                      <span>
                        Absent:{" "}
                        {Array.isArray(sub.absentDates)
                          ? sub.absentDates.length
                          : 0}
                      </span>
                    </div>

                    <div className="absent-action-box">
                      <button
                        type="button"
                        className="btn btn-solid"
                        onClick={() => handleMarkAbsent(sub.id)}
                        disabled={
                          !isBeforeCutoff ||
                          absentActionState[sub.id]?.loading ||
                          (Array.isArray(sub.absentDates) &&
                            sub.absentDates.includes(todayDate))
                        }
                      >
                        {absentActionState[sub.id]?.loading
                          ? "Marking..."
                          : Array.isArray(sub.absentDates) &&
                              sub.absentDates.includes(todayDate)
                            ? "Absent Marked"
                            : isBeforeCutoff
                              ? "Mark Absent For Today"
                              : "Cutoff Over"}
                      </button>

                      {absentActionState[sub.id]?.message && (
                        <p
                          className={`join-status ${
                            absentActionState[sub.id]?.type === "error"
                              ? "status-error"
                              : "status-success"
                          }`}
                        >
                          {absentActionState[sub.id]?.message}
                        </p>
                      )}
                    </div>

                    <div className="date-list-wrap">
                      <p className="mini-label">Recent Absent Dates</p>
                      <p className="date-list">
                        {Array.isArray(sub.absentDates) &&
                        sub.absentDates.length > 0
                          ? sub.absentDates.slice(-3).join(" | ")
                          : "None"}
                      </p>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </>
        )}

        {!loading && !error && user?.role === "OWNER" && (
          <>
            <section className="dash-stats">
              <article className="stat-tile">
                <p className="tile-label">Joined Customers</p>
                <p className="tile-value">{ownerStats.totalCustomers}</p>
              </article>
              <article className="stat-tile">
                <p className="tile-label">Absent Today</p>
                <p className="tile-value">{ownerStats.absentToday}</p>
              </article>
              <article className="stat-tile">
                <p className="tile-label">Present Today</p>
                <p className="tile-value">{ownerStats.presentToday}</p>
              </article>
              <article className="stat-tile">
                <p className="tile-label">Pending Today</p>
                <p className="tile-value">{ownerStats.pendingToday}</p>
              </article>
            </section>

            <section className="owner-quick-strip">
              <p>
                <strong>Average Buffer:</strong> {ownerStats.avgBuffer}
              </p>
              <p>
                <strong>Total Due From Customers:</strong> Rs{" "}
                {ownerStats.totalDue.toLocaleString()}
              </p>
              <p>
                <strong>Cumulative Attendance (P/A):</strong>{" "}
                {ownerStats.totalPresent} / {ownerStats.totalAbsent}
              </p>
            </section>

            <section className="absent-today-panel">
              <h3>Absent Students Today ({absentStudentsToday.length})</h3>
              {absentStudentsToday.length === 0 ? (
                <p className="explore-info">
                  No absent students marked for today.
                </p>
              ) : (
                <div className="absent-student-list">
                  {absentStudentsToday.map((sub) => (
                    <span
                      className="owner-status-chip chip-absent"
                      key={`abs-${sub.id}`}
                    >
                      {sub.customerId || "Customer"}
                    </span>
                  ))}
                </div>
              )}
            </section>

            <article className="detail-card owner-mess-card dashboard-card">
              <div className="card-head">
                <h3>Your Mess</h3>
                <span className="pill">{ownerMess?.type || "N/A"}</span>
              </div>

              <div className="mini-grid">
                <div>
                  <p className="mini-label">Mess Name</p>
                  <p className="mini-value">{ownerMess?.messName || "N/A"}</p>
                </div>
                <div>
                  <p className="mini-label">Contact</p>
                  <p className="mini-value">{ownerMess?.ownerPhone || "N/A"}</p>
                </div>
              </div>

              <p className="mini-label">Address</p>
              <p className="mini-value">{ownerMess?.address || "N/A"}</p>

              <p className="mini-label">Today's Menu</p>
              <p className="mini-value">
                {ownerMess?.todaysMenu || "Not updated"}
              </p>

              <p className="mini-label">Notices</p>
              <p className="mini-value">{ownerMess?.notices || "No notices"}</p>
            </article>

            <section className="owner-update-panel">
              <h3>Update Menu / Notice</h3>
              <form className="owner-update-form" onSubmit={handleOwnerUpdateSubmit}>
                <div className="owner-update-fields">
                  <label htmlFor="owner-todaysMenu">Today's Menu</label>
                  <textarea
                    id="owner-todaysMenu"
                    name="todaysMenu"
                    value={ownerUpdateForm.todaysMenu}
                    onChange={handleOwnerUpdateChange}
                    rows={3}
                    placeholder="Example: Dal, Rice, Paneer, Salad"
                  />
                </div>

                <div className="owner-update-fields">
                  <label htmlFor="owner-notices">Notices</label>
                  <textarea
                    id="owner-notices"
                    name="notices"
                    value={ownerUpdateForm.notices}
                    onChange={handleOwnerUpdateChange}
                    rows={3}
                    placeholder="Example: Dinner service starts at 8:00 PM"
                  />
                </div>

                <button
                  type="submit"
                  className="btn btn-solid"
                  disabled={ownerUpdateState.loading}
                >
                  {ownerUpdateState.loading ? "Saving..." : "Save Update"}
                </button>

                {ownerUpdateState.message && (
                  <p
                    className={`join-status ${
                      ownerUpdateState.type === "error"
                        ? "status-error"
                        : "status-success"
                    }`}
                  >
                    {ownerUpdateState.message}
                  </p>
                )}
              </form>
            </section>

            <h3 className="sub-heading">Customers Joined To Your Mess</h3>
            <div className="owner-filter-bar">
              <button
                type="button"
                className={`owner-filter-btn ${ownerFilter === "ALL" ? "filter-active" : ""}`}
                onClick={() => setOwnerFilter("ALL")}
              >
                All ({ownerSubscriptions.length})
              </button>
              <button
                type="button"
                className={`owner-filter-btn ${ownerFilter === "ABSENT" ? "filter-active" : ""}`}
                onClick={() => setOwnerFilter("ABSENT")}
              >
                Absent Today ({ownerStats.absentToday})
              </button>
              <button
                type="button"
                className={`owner-filter-btn ${ownerFilter === "PRESENT" ? "filter-active" : ""}`}
                onClick={() => setOwnerFilter("PRESENT")}
              >
                Present Today ({ownerStats.presentToday})
              </button>
              <button
                type="button"
                className={`owner-filter-btn ${ownerFilter === "PENDING" ? "filter-active" : ""}`}
                onClick={() => setOwnerFilter("PENDING")}
              >
                Pending Today ({ownerStats.pendingToday})
              </button>
            </div>

            {filteredOwnerSubscriptions.length === 0 ? (
              <p className="explore-info">No customers joined yet.</p>
            ) : (
              <div className="dashboard-grid">
                {filteredOwnerSubscriptions.map((sub) => (
                  <article className="detail-card dashboard-card" key={sub.id}>
                    <div className="card-head">
                      <h3>{sub.customerId || "Customer"}</h3>
                      <span className="pill">{sub.id}</span>
                    </div>

                    <p className="mini-label">Today's Attendance Status</p>
                    <div className="owner-status-wrap">
                      {includesDate(sub.absentDates, todayDate) ? (
                        <span className="owner-status-chip chip-absent">
                          Absent Today
                        </span>
                      ) : includesDate(sub.presentDates, todayDate) ? (
                        <span className="owner-status-chip chip-present">
                          Present Today
                        </span>
                      ) : (
                        <span className="owner-status-chip chip-pending">
                          Pending Auto-Mark
                        </span>
                      )}
                    </div>

                    <div className="mini-grid">
                      <div>
                        <p className="mini-label">Start Date</p>
                        <p className="mini-value">
                          {formatDateTime(sub.joiningDate)}
                        </p>
                      </div>
                      <div>
                        <p className="mini-label">End Date</p>
                        <p className="mini-value">
                          {formatDate(sub.messEndingDate)}
                        </p>
                      </div>
                      <div>
                        <p className="mini-label">Buffer</p>
                        <p className="mini-value">{sub.buffer}</p>
                      </div>
                      <div>
                        <p className="mini-label">Due Amount</p>
                        <p className="mini-value">
                          Rs {Number(sub.moneyLeftToPay || 0).toLocaleString()}
                        </p>
                      </div>
                    </div>

                    <div className="attendance-band">
                      <span>
                        Present:{" "}
                        {Array.isArray(sub.presentDates)
                          ? sub.presentDates.length
                          : 0}
                      </span>
                      <span>
                        Absent:{" "}
                        {Array.isArray(sub.absentDates)
                          ? sub.absentDates.length
                          : 0}
                      </span>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </>
        )}
      </section>
    </main>
  );
}

export default DashboardPage;
