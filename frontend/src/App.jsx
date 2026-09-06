import { useState } from "react";
import { Link, Navigate, Route, Routes } from "react-router-dom";
import Header from "./components/layout/Header";
import Footer from "./components/layout/Footer";
import HeroSection from "./components/sections/HeroSection";
import FeaturesSection from "./components/sections/FeaturesSection";
import WorkflowSection from "./components/sections/WorkflowSection";
import ReviewsSection from "./components/sections/ReviewsSection";
import CtaSection from "./components/sections/CtaSection";
import MessRegistrationSection from "./components/sections/MessRegistrationSection";
import ExploreMessesPage from "./components/sections/ExploreMessesPage";
import MessDetailPage from "./components/sections/MessDetailPage";
import DashboardPage from "./components/sections/DashboardPage";
import AuthModal from "./components/auth/AuthModal";
import { login, register } from "./services/authApi";
import {
  ctaContent,
  featureCards,
  footerContent,
  heroContent,
  navLinks,
  reviewCards,
  workflowSteps,
} from "./data/landingContent";
import "./styles/landing.css";

function LandingPage() {
  return (
    <>
      <HeroSection content={heroContent} />
      <FeaturesSection items={featureCards} />
      <WorkflowSection steps={workflowSteps} />
      <ReviewsSection reviews={reviewCards} />
      <CtaSection content={ctaContent} />
    </>
  );
}

function MessRegistrationPage({ isAuthenticated, token, onOpenAuth }) {
  return (
    <main className="register-page">
      <div className="register-page-actions">
        <Link className="btn btn-ghost" to="/">
          Back To Home
        </Link>
      </div>
      <MessRegistrationSection
        isAuthenticated={isAuthenticated}
        token={token}
        onOpenAuth={onOpenAuth}
      />
    </main>
  );
}

function App() {
  const [isAuthOpen, setIsAuthOpen] = useState(false);
  const [token, setToken] = useState(localStorage.getItem("mealcircle_token"));

  const isAuthenticated = Boolean(token);

  const handleAuthSuccess = async ({ mode, payload }) => {
    const response = mode === "register" ? await register(payload) : await login(payload);

    if (!response?.token) {
      throw new Error("Token was not returned by server");
    }

    localStorage.setItem("mealcircle_token", response.token);
    setToken(response.token);
  };

  const handleLogout = () => {
    localStorage.removeItem("mealcircle_token");
    setToken("");
  };

  return (
    <div className="landing-shell">
      <Header
        brand={footerContent.brand}
        links={navLinks}
        isAuthenticated={isAuthenticated}
        onOpenAuth={() => setIsAuthOpen(true)}
        onLogout={handleLogout}
      />
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route
          path="/register-mess"
          element={
            <MessRegistrationPage
              isAuthenticated={isAuthenticated}
              token={token}
              onOpenAuth={() => setIsAuthOpen(true)}
            />
          }
        />
        <Route path="/explore-mess" element={<ExploreMessesPage />} />
        <Route
          path="/explore-mess/:messId"
          element={
            <MessDetailPage
              isAuthenticated={isAuthenticated}
              token={token}
              onOpenAuth={() => setIsAuthOpen(true)}
            />
          }
        />
        <Route
          path="/dashboard"
          element={
            <DashboardPage
              isAuthenticated={isAuthenticated}
              token={token}
              onOpenAuth={() => setIsAuthOpen(true)}
              onLogout={handleLogout}
            />
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <Footer brand={footerContent.brand} text={footerContent.text} />

      <AuthModal
        isOpen={isAuthOpen}
        onClose={() => setIsAuthOpen(false)}
        onAuthSuccess={handleAuthSuccess}
      />
    </div>
  );
}

export default App;
