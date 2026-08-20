function getToken() {
  return localStorage.getItem('token');
}

function getRole() {
  return localStorage.getItem('role');
}

function requireRole(expectedRole) {
  const token = getToken();
  const role = getRole();
  if (!token || role !== expectedRole) {
    window.location.href = '/web/login';
    return false;
  }
  return true;
}

async function apiFetch(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      ...(options.headers || {}),
      Authorization: 'Bearer ' + getToken(),
    },
  });
  if (response.status === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    window.location.href = '/web/login';
    throw new Error('JWT expired or invalid');
  }
  return response;
}

function showMessage(message, type) {
  const box = document.getElementById('message-box');
  if (!box) return;
  box.textContent = message;
  box.className = 'message ' + (type || 'error');
  box.style.display = 'block';
}

function clearMessage() {
  const box = document.getElementById('message-box');
  if (box) box.style.display = 'none';
}