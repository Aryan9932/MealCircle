const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

export async function getAllMesses() {
  const response = await fetch(`${API_BASE_URL}/mess/all`);

  if (!response.ok) {
    throw new Error("Failed to fetch mess list");
  }

  return response.json();
}

export async function getMessById(messId) {
  const response = await fetch(`${API_BASE_URL}/mess/${messId}`);

  if (!response.ok) {
    throw new Error("Failed to fetch mess details");
  }

  return response.json();
}

export async function getMessReviews(messId) {
  const response = await fetch(`${API_BASE_URL}/mess/${messId}/reviews`);

  if (!response.ok) {
    throw new Error("Failed to fetch mess reviews");
  }

  return response.json();
}

export async function getMessRating(messId) {
  const response = await fetch(`${API_BASE_URL}/mess/${messId}/rating`);

  if (!response.ok) {
    throw new Error("Failed to fetch mess rating");
  }

  return response.json();
}

export async function addOrUpdateMessReview({
  messId,
  token,
  rating,
  comment,
}) {
  const response = await fetch(`${API_BASE_URL}/mess/${messId}/reviews`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ rating, comment }),
  });

  if (!response.ok) {
    let message = "Failed to submit review";
    const rawBody = await response.text();
    if (rawBody) {
      message = rawBody;
    }
    throw new Error(message);
  }

  return response.json();
}

export async function joinMess({ messId, token }) {
  const response = await fetch(`${API_BASE_URL}/mess/${messId}/join`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    let message = "Failed to join mess";
    const rawBody = await response.text();

    if (rawBody) {
      try {
        const data = JSON.parse(rawBody);
        if (typeof data === "string") {
          message = data;
        } else if (data?.message) {
          message = data.message;
        } else {
          message = rawBody;
        }
      } catch {
        message = rawBody;
      }
    }

    throw new Error(message || "Failed to join mess");
  }

  return response.json();
}

export async function createRazorpayOrder({ messId, token }) {
  const response = await fetch(
    `${API_BASE_URL}/payment/razorpay/order/${messId}`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    },
  );

  if (!response.ok) {
    let message = "Failed to create payment order";
    const rawBody = await response.text();
    if (rawBody) {
      message = rawBody;
    }
    throw new Error(message);
  }

  return response.json();
}

export async function verifyPaymentAndJoin({
  messId,
  token,
  razorpayOrderId,
  razorpayPaymentId,
  razorpaySignature,
}) {
  const response = await fetch(
    `${API_BASE_URL}/payment/razorpay/verify-and-join`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        messId,
        razorpayOrderId,
        razorpayPaymentId,
        razorpaySignature,
      }),
    },
  );

  if (!response.ok) {
    let message = "Payment verification failed";
    const rawBody = await response.text();
    if (rawBody) {
      message = rawBody;
    }
    throw new Error(message);
  }

  return response.json();
}

export async function getMySubscriptions(token) {
  const response = await fetch(`${API_BASE_URL}/subscription/customer/me`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Failed to fetch customer subscriptions");
  }

  return response.json();
}

export async function getOwnerMessSubscriptions(token) {
  const response = await fetch(`${API_BASE_URL}/subscription/owner/my-mess`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Failed to fetch owner subscriptions");
  }

  return response.json();
}

export async function getOwnerMess(token) {
  const response = await fetch(`${API_BASE_URL}/mess/owner/my-mess`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Failed to fetch owner mess");
  }

  return response.json();
}

export async function getSubscriptionDetails({ subscriptionId, token }) {
  const response = await fetch(
    `${API_BASE_URL}/subscription/${subscriptionId}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    },
  );

  if (!response.ok) {
    throw new Error("Failed to fetch subscription details");
  }

  return response.json();
}

export async function addAbsentDate({ subscriptionId, date, token }) {
  const response = await fetch(
    `${API_BASE_URL}/subscription/${subscriptionId}/absent?date=${encodeURIComponent(date)}`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    },
  );

  if (!response.ok) {
    let message = "Failed to mark absent";
    const rawBody = await response.text();
    if (rawBody) {
      message = rawBody;
    }
    throw new Error(message);
  }

  return response.json();
}

export async function createMess({ token, formData }) {
  const response = await fetch(`${API_BASE_URL}/mess/create`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: formData,
  });

  if (!response.ok) {
    if (response.status === 413) {
      throw new Error(
        "Image is too large. Please upload an image smaller than 10MB.",
      );
    }

    let message = "Mess registration failed";
    const rawBody = await response.text();

    if (rawBody) {
      try {
        const data = JSON.parse(rawBody);
        if (typeof data === "string") {
          message = data;
        } else if (data?.message) {
          message = data.message;
        } else {
          message = rawBody;
        }
      } catch {
        message = rawBody;
      }
    }

    throw new Error(message || "Mess registration failed");
  }

  return response.json();
}

export async function updateOwnerMenuNotice({ token, todaysMenu, notices }) {
  const payload = {};

  if (typeof todaysMenu === "string" && todaysMenu.trim()) {
    payload.todaysMenu = todaysMenu.trim();
  }

  if (typeof notices === "string" && notices.trim()) {
    payload.notices = notices.trim();
  }

  const response = await fetch(`${API_BASE_URL}/mess/owner/menu-notice`, {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    let message = "Failed to update menu/notice";
    const rawBody = await response.text();
    if (rawBody) {
      message = rawBody;
    }
    throw new Error(message);
  }

  return response.json();
}

/**
 * Fetch messes sorted by nearest distance.
 * @param {number} lat  - User's latitude
 * @param {number} lng  - User's longitude
 * @param {number} radius - Search radius in km (default 5)
 * @returns {Promise<Array<{mess: object, distanceKm: number, distanceMeters: number}>>}
 */
export async function getNearbyMesses({ lat, lng, radius = 5 }) {
  const url = `${API_BASE_URL}/mess/nearby?lat=${lat}&lng=${lng}&radius=${radius}`;
  const response = await fetch(url);

  if (!response.ok) {
    const rawBody = await response.text();
    throw new Error(rawBody || "Failed to fetch nearby messes");
  }

  return response.json();
}
