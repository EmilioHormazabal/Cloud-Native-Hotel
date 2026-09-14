import { useEffect, useState } from 'react'
import { useMsal, useIsAuthenticated, useAccount } from '@azure/msal-react'
import { getRoles, getAccessTokenScopes } from '../auth/authUtils.js'

function Mi_cuenta() {
    const { instance } = useMsal()
    const isAuthenticated = useIsAuthenticated()
    const accounts = instance.getAllAccounts()
    const account = useAccount(accounts.length > 0 ? accounts[0] : null)
    const [scopes, setScopes] = useState([])

    const claims = account?.idTokenClaims || {}
    const roles = getRoles(account)

    useEffect(() => {
        if (!isAuthenticated) return
        getAccessTokenScopes(instance).then(setScopes).catch(() => setScopes([]))
    }, [isAuthenticated, instance])

    if (!isAuthenticated) {
        return (
            <>
                <h1>Mi cuenta</h1>
                <p>Debes iniciar sesión para ver tu cuenta.</p>
            </>
        )
    }

    return (
        <>
            <h1>Mi cuenta</h1>
            <article className="mt-3" style={{ maxWidth: '480px' }}>
                <header>
                    <h2 className="h5">{claims.name || account?.name || account?.username}</h2>
                </header>
                <dl className="row mb-0">
                    <dt className="col-sm-4">Correo</dt>
                    <dd className="col-sm-8">{account?.username}</dd>
                    <dt className="col-sm-4">Rol</dt>
                    <dd className="col-sm-8">{roles.length > 0 ? roles.join(', ') : '(sin rol)'}</dd>
                    <dt className="col-sm-4">Scopes</dt>
                    <dd className="col-sm-8">{scopes.length > 0 ? scopes.join(', ') : '(sin scopes)'}</dd>
                </dl>
            </article>
        </>
    )
}

export default Mi_cuenta