// Central API service module with dynamic baseURL fallback
const DEFAULT_BASE_URL = window.location.origin.includes('localhost') || window.location.origin.includes('8000')
  ? `${window.location.origin}/api/v1`
  : "https://api.matmovies.com/api/v1";

export const getBaseURL = () => {
  return localStorage.getItem("matmovies_api_url") || DEFAULT_BASE_URL;
};

export const setBaseURL = (url) => {
  if (!url) {
    localStorage.removeItem("matmovies_api_url");
  } else {
    // Standardize trailing slash removal
    const cleanUrl = url.endsWith('/') ? url.slice(0, -1) : url;
    localStorage.setItem("matmovies_api_url", cleanUrl);
  }
};

const getHeaders = () => {
  const headers = {
    "Content-Type": "application/json",
  };
  const token = localStorage.getItem("matmovies_admin_token");
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  return headers;
};

export const apiCall = async (endpoint, options = {}) => {
  const baseUrl = getBaseURL();
  const url = `${baseUrl}${endpoint}`;
  
  const headers = getHeaders();
  if (options.body && !(options.body instanceof FormData)) {
    options.body = JSON.stringify(options.body);
  }
  
  const config = {
    ...options,
    headers: {
      ...headers,
      ...options.headers,
    },
  };

  try {
    const response = await fetch(url, config);
    if (response.status === 401) {
      // Clear token on unauthorized (session expired)
      localStorage.removeItem("matmovies_admin_token");
      localStorage.removeItem("matmovies_admin_user");
    }
    
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.detail || "An error occurred with your server request.");
    }
    return data;
  } catch (error) {
    console.error(`API Error [${endpoint}]:`, error);
    throw error;
  }
};

export const authService = {
  login: async (email, password) => {
    const baseUrl = getBaseURL();
    const url = `${baseUrl}/auth/token`;
    
    // OAuth2 standard uses form data
    const formData = new URLSearchParams();
    formData.append("username", email);
    formData.append("password", password);

    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: formData,
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.detail || "Incorrect administrative email or password.");
    }

    if (data.user.role !== "Admin") {
      throw new Error("Access denied! This login panel is reserved for administrators only.");
    }

    localStorage.setItem("matmovies_admin_token", data.access_token);
    localStorage.setItem("matmovies_admin_user", JSON.stringify(data.user));
    return data;
  },
  
  logout: () => {
    localStorage.removeItem("matmovies_admin_token");
    localStorage.removeItem("matmovies_admin_user");
  },

  getCurrentUser: () => {
    try {
      return JSON.parse(localStorage.getItem("matmovies_admin_user"));
    } catch {
      return null;
    }
  }
};

export const moviesService = {
  listAll: () => apiCall("/movies"),
  getById: (id) => apiCall(`/movies/${id}`),
  create: (movieData) => apiCall("/movies", {
    method: "POST",
    body: movieData,
  }),
  update: (id, movieData) => apiCall(`/movies/${id}`, {
    method: "PUT",
    body: movieData,
  }),
  delete: (id) => apiCall(`/movies/${id}`, {
    method: "DELETE",
  }),
};

export const categoriesService = {
  listAll: () => apiCall("/categories"),
  create: (categoryData) => apiCall("/categories", {
    method: "POST",
    body: categoryData,
  }),
};

export const usersService = {
  listAll: () => apiCall("/admin/users"),
  updateRole: (userId, role) => apiCall(`/admin/users/${userId}/role`, {
    method: "PUT",
    body: { role },
  }),
  deleteUser: (userId) => apiCall(`/admin/users/${userId}`, {
    method: "DELETE",
  }),
};
