import { Navigate } from 'react-router-dom'
import { useMsal, useIsAuthenticated } from '@azure/msal-react'
import { getRoles } from './authUtils.js'

function RequireAuth({ roles, children }) {
    const { instance } = useMsal()
    const isAuthenticated = useIsAuthenticated()
    const restoringSession = !isAuthenticated && instance.getAllAccounts().length > 0

    if (restoringSession) {
        return null
    }

    if (!isAuthenticated) return <Navigate to="/login" replace />

    const account = instance.getAllAccounts()[0]
    const userRoles = account ? getRoles(account) : []

    if (roles && roles.length > 0 && !roles.some((r) => userRoles.includes(r))) {
        return (
            <>
                <h1>Acceso denegado</h1>
                <p>No tienes el rol necesario para ver esta sección.</p>
            </>
        )
    }

    return children
}

export default RequireAuth