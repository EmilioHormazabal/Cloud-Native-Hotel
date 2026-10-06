import { useEffect, useState } from 'react'
import { useMsal, useIsAuthenticated, useAccount } from '@azure/msal-react'
import { getRoles, getAccessTokenScopes } from '../auth/authUtils.js'
import { obtenerDashboard } from '../services/bffService.js'

function Mi_cuenta() {
    const { instance } = useMsal()
    const isAuthenticated = useIsAuthenticated()
    const accounts = instance.getAllAccounts()
    const account = useAccount(accounts.length > 0 ? accounts[0] : null)
    const [scopes, setScopes] = useState([])
    const [dashboard, setDashboard] = useState(null)
    const [errorDashboard, setErrorDashboard] = useState(null)

    const claims = account?.idTokenClaims || {}
    const roles = getRoles(account)

    useEffect(() => {
        if (!isAuthenticated) return
        getAccessTokenScopes(instance).then(setScopes).catch(() => setScopes([]))
        obtenerDashboard()
            .then(setDashboard)
            .catch((e) => setErrorDashboard(e?.response?.data?.message || e.message))
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
            <article className="mt-3" style={{ maxWidth: '480px' }}>
                <header>
                    <h2 className="h5">Resumen</h2>
                </header>
                {errorDashboard && <p className="text-danger">{errorDashboard}</p>}
                {!errorDashboard && !dashboard && <p>Cargando resumen...</p>}
                {dashboard && (
                    <dl className="row mb-0">
                        <dt className="col-sm-4">Usuario</dt>
                        <dd className="col-sm-8">{textoUsuario(dashboard.usuario)}</dd>
                        <dt className="col-sm-4">Reservas</dt>
                        <dd className="col-sm-8">{textoLista(dashboard.reservas, 'reservas')}</dd>
                        <dt className="col-sm-4">Servicios</dt>
                        <dd className="col-sm-8">{textoLista(dashboard.servicios, 'servicios')}</dd>
                    </dl>
                )}
            </article>
        </>
    )
}

function textoUsuario(campo) {
    if (!campo) return '(sin datos)'
    if (campo.error) return `Error: ${campo.error}`
    const u = campo.datos || {}
    return `${u.nombre || '(sin nombre)'} (id ${u.id})`
}

function textoLista(campo, etiqueta) {
    if (!campo) return '(sin datos)'
    if (campo.error) return `Error: ${campo.error}`
    const lista = Array.isArray(campo.datos) ? campo.datos : []
    return `${lista.length} ${etiqueta}`
}

export default Mi_cuenta
