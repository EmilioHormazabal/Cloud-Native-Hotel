import { useEffect, useState } from "react"
import Tarjeta_servicios from "../components/Tarjeta_servicios"
import { listarServicios } from "../services/servicioService"

function Servicios() {

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
    },[])

    return(
        <>
        {error && <p className="text-danger">No se pudieron cargar los servicios: {error}</p>}
        <section className="row m-0">
            {servicios.map((s)=>(
                <Tarjeta_servicios servicio={s} key={s.id}/>
            ))}
        </section>
        </>
    )
}

export default Servicios
