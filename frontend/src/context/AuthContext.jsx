// AuthContext used by withAuth HOC; JWT persisted in sessionStorage so a
// page refresh doesn't blow the session away (refresh-token cookie flow is
// out of scope for this trainer copy).
import React, { createContext, useContext, useState } from 'react';

const AuthContext = createContext({ user: null, isLoading: false, login: () => {}, logout: () => {} });

function loadStoredUser() {
  const token = sessionStorage.getItem('reconx-token');
  const role = sessionStorage.getItem('reconx-role');
  if (!token) return null;
  return { token, role };
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(loadStoredUser);
  const [isLoading] = useState(false);

  const login = (token, role) => {
    sessionStorage.setItem('reconx-token', token);
    sessionStorage.setItem('reconx-role', role);
    setUser({ token, role });
  };

  const logout = () => {
    sessionStorage.removeItem('reconx-token');
    sessionStorage.removeItem('reconx-role');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);