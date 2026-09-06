const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

async function request(path, payload) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    let errorMessage = "Authentication failed";
    const rawBody = await response.text();

    if (rawBody) {
      try {
        const data = JSON.parse(rawBody);
        if (typeof data === "string") {
          errorMessage = data;
        } else if (data?.message) {
          errorMessage = data.message;
        } else {
          errorMessage = rawBody;
        }
      } catch {
        errorMessage = rawBody;
      }
    }

    throw new Error(errorMessage || "Authentication failed");
  }

  return response.json();
}

export function login(payload) {
  return request("/auth/login", payload);
}

export function register(payload) {
  return request("/auth/register", payload);
}

export async function getCurrentUser(token) {
  const response = await fetch(`${API_BASE_URL}/auth/me`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    let errorMessage = "Session expired. Please log in again.";
    const rawBody = await response.text();
    if (rawBody) {
      try {
        const data = JSON.parse(rawBody);
        if (typeof data === "string") {
          errorMessage = data;
        } else if (data?.message) {
          errorMessage = data.message;
        } else {
          errorMessage = rawBody;
        }
      } catch {
        errorMessage = rawBody;
      }
    }
    const err = new Error(errorMessage);
    err.status = response.status;
    throw err;
  }

  return response.json();
}
