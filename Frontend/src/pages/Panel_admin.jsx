import { useEffect, useState } from "react"
import Formulario_servicios from "../components/Formulario_servicios"
import Tarjeta_servicios from "../components/Tarjeta_servicios"
import { listarServicios } from "../services/servicioService"

function Panel_admin() {

    const [showFormulario, setShowFormulario] = useState(false)
    const [servicios, setServicios] = useState([])
    const [error, setError] = useState(null)

    useEffect(()=>{
        const cargarServicios = async() => {
            try {
                setServicios(await listarServicios())
            } catch (e) {
                setError(e?.response?.data?.message || e.message)
            }
        }
        cargarServicios()
    },[showFormulario])

    return(
        <>
        <section>
            <h1>Panel admin</h1>
            <button onClick={()=>setShowFormulario(true)}>Registrar servicio</button>
            {showFormulario &&
                <Formulario_servicios show={setShowFormulario}/>
            }
        </section>
        {error && <p className="text-danger">No se pudieron cargar los servicios: {error}</p>}
        <section className="row m-0">
            {servicios.map((s)=>(
                <Tarjeta_servicios servicio={s} key={s.id} admin/>
            ))}
        </section>
        </>
    )
}

export default Panel_admin
