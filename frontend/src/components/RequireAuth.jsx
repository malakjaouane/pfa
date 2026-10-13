import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import { useI18n } from '../i18n/I18nContext';

/**
 * Garde d'accès : authentification obligatoire + permissions éventuelles.
 */
export default function RequireAuth({ children, permissions = [] }) {
  const { user, loading } = useAuth();
  const { t } = useI18n();
  const location = useLocation();

  if (loading) {
    return <div className="center-page">{t('common.chargement')}</div>;
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  const autorise = permissions.length === 0
    || permissions.some((p) => user.permissions?.includes(p));

  if (!autorise) {
    return <Navigate to="/dashboard" replace />;
  }

  return children ?? <Outlet />;
}
