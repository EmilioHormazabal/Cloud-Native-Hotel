import { Routes, Route } from 'react-router-dom'
import Inicio from '../pages/Inicio'
import Registrarse from '../pages/Registrarse'
import Login from '../pages/Login'
import Mi_cuenta from '../pages/Mi_cuenta'
import Servicios from '../pages/Servicios'
import Servicios_detalle from '../pages/Servicios_detalle'
import Reservas from '../pages/Reservas'
import Reservas_detalle from '../pages/Reservas_detalle'
import Panel_admin from '../pages/Panel_admin'
import RequireAuth from '../auth/RequireAuth'

const RouterConfig = () => (
    <Routes>
        <Route path="/" element={<Inicio/>}/>
        <Route path="/registro" element={<Registrarse/>}/>
        <Route path="/login" element={<Login/>}/>
        <Route path="/mi_cuenta" element={<RequireAuth><Mi_cuenta/></RequireAuth>}/>
        <Route path="/servicios" element={<RequireAuth><Servicios/></RequireAuth>}/>
        <Route path="/servicios/detalle" element={<RequireAuth><Servicios_detalle/></RequireAuth>}/>
        <Route path="/mis_reservas" element={<RequireAuth><Reservas/></RequireAuth>}/>
        <Route path="/mis_reservas/detalle" element={<RequireAuth><Reservas_detalle/></RequireAuth>}/>
        <Route path="/PanelAdministradores" element={<RequireAuth roles={['Admin']}><Panel_admin/></RequireAuth>}/>
    </Routes>
)

export default RouterConfig
