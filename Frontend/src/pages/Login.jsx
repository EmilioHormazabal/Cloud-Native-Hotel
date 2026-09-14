import { useEffect, useState } from 'react'
import { useMsal, useIsAuthenticated, useAccount } from '@azure/msal-react'
import { loginRequest } from '../auth/authConfig.js'
import { getRoles, getAccessTokenScopes } from '../auth/authUtils.js'

function Login() {
    const { instance } = useMsal()
    const isAuthenticated = useIsAuthenticated()
    const accounts = instance.getAllAccounts()
    const account = useAccount(accounts.length > 0 ? accounts[0] : null)
    const [scopes, setScopes] = useState([])

    const roles = getRoles(account)

    useEffect(() => {
        if (!isAuthenticated) return
        getAccessTokenScopes(instance).then(setScopes).catch(() => setScopes([]))
    }, [isAuthenticated, instance])

    const handleLogin = () => {
        instance.loginRedirect(loginRequest)
    }

    const handleLogout = () => {
        instance.logoutRedirect()
    }

    if (!isAuthenticated) {
        return (
            <>
                <h1>Iniciar sesión</h1>
                <p>Accede con tu cuenta de Microsoft Entra ID.</p>
                <button className="btn btn-primary" onClick={handleLogin}>
                    Iniciar sesión
                </button>
            </>
        )
    }

    return (
        <>
            <h1>Sesión activa</h1>
            <p>Usuario: {account?.username}</p>
            <p>Roles: {roles.length > 0 ? roles.join(', ') : '(sin roles)'}</p>
            <p>Scopes: {scopes.length > 0 ? scopes.join(', ') : '(sin scopes)'}</p>
            <button className="btn btn-secondary" onClick={handleLogout}>
                Cerrar sesión
            </button>
        </>
    )
}

export default Login
