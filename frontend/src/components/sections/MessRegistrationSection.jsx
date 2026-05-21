import { useState } from "react";
import { createMess } from "../../services/messApi";

function MessRegistrationSection({ isAuthenticated, token, onOpenAuth }) {
  const [form, setForm] = useState({
    messName: "",
    email: "",
    address: "",
    latitude: "",
    longitude: "",
    type: "VEG",
    todaysMenu: "",
    notices: "",
    ownerPhone: "",
    pricePerMonth: "",
  });
  const [image, setImage] = useState(null);
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState({ type: "", message: "" });

  const onChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const clearForm = () => {
    setForm({
      messName: "",
      email: "",
      address: "",
      latitude: "",
      longitude: "",
      type: "VEG",
      todaysMenu: "",
      notices: "",
      ownerPhone: "",
      pricePerMonth: "",
    });
    setImage(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!isAuthenticated) {
      setStatus({
        type: "error",
        message: "Please login first to register your mess.",
      });
      return;
    }

    if (!image) {
      setStatus({ type: "error", message: "Please upload a mess image." });
      return;
    }

    setLoading(true);
    setStatus({ type: "", message: "" });

    try {
      const payload = {
        ...form,
        latitude: Number(form.latitude),
        longitude: Number(form.longitude),
        pricePerMonth: Number(form.pricePerMonth),
      };

      const formData = new FormData();
      formData.append("data", JSON.stringify(payload));
      formData.append("image", image);

      await createMess({ token, formData });
      setStatus({ type: "success", message: "Mess registered successfully." });
      clearForm();
    } catch (submitError) {
      setStatus({
        type: "error",
        message: submitError.message || "Mess registration failed.",
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="mess-register" id="mess-register">
      <div className="register-intro">
        <p className="eyebrow">Owner Zone</p>
        <h2>Register Your Mess</h2>
        <p>
          Add your mess profile in minutes and start receiving customers with
          menu updates, pricing, and transparent attendance.
        </p>

        {!isAuthenticated && (
          <button className="btn btn-outline" onClick={onOpenAuth}>
            Login To Continue
          </button>
        )}
      </div>

      <form className="register-form" onSubmit={handleSubmit}>
        <label>
          Mess Name
          <input
            name="messName"
            value={form.messName}
            onChange={onChange}
            required
          />
        </label>

        <label>
          Contact Email
          <input
            type="email"
            name="email"
            value={form.email}
            onChange={onChange}
            required
          />
        </label>

        <label className="full-width">
          Address
          <input
            name="address"
            value={form.address}
            onChange={onChange}
            required
          />
        </label>

        <label>
          Latitude
          <input
            type="number"
            step="any"
            name="latitude"
            value={form.latitude}
            onChange={onChange}
            required
          />
        </label>

        <label>
          Longitude
          <input
            type="number"
            step="any"
            name="longitude"
            value={form.longitude}
            onChange={onChange}
            required
          />
        </label>

        <label>
          Type
          <select name="type" value={form.type} onChange={onChange}>
            <option value="VEG">VEG</option>
            <option value="NON-VEG">NON-VEG</option>
            <option value="MIXED">MIXED</option>
          </select>
        </label>

        <label>
          Owner Phone
          <input
            name="ownerPhone"
            value={form.ownerPhone}
            onChange={onChange}
            required
          />
        </label>

        <label>
          Price Per Month
          <input
            type="number"
            name="pricePerMonth"
            value={form.pricePerMonth}
            onChange={onChange}
            min="0"
            required
          />
        </label>

        <label className="full-width">
          Today's Menu
          <input
            name="todaysMenu"
            value={form.todaysMenu}
            onChange={onChange}
            required
          />
        </label>

        <label className="full-width">
          Notices
          <input name="notices" value={form.notices} onChange={onChange} />
        </label>

        <label className="full-width">
          Mess Image
          <input
            type="file"
            accept="image/*"
            onChange={(event) => setImage(event.target.files?.[0] || null)}
            required
          />
        </label>

        {status.message && (
          <p
            className={`register-status ${status.type === "error" ? "status-error" : "status-success"}`}
          >
            {status.message}
          </p>
        )}

        <button className="btn btn-solid" type="submit" disabled={loading}>
          {loading ? "Submitting..." : "Register Mess"}
        </button>
      </form>
    </section>
  );
}

export default MessRegistrationSection;
