import { useEffect, useState } from "react"
import Tarjeta_servicios from "../components/Tarjeta_servicios"
import { listarServicios } from "../services/servicioService"

function Servicios() {

    const [servicios, setServicios] = useState([])

    useEffect(()=>{
        const cargarServicios = async() => {
            setServicios(await listarServicios())
        }
        cargarServicios()
    },[])

    return(
        <>
        <section className="row m-0">
            {servicios.map((s,i)=>(
                <Tarjeta_servicios id={s.id} key={i}/>
            ))}
        </section>
        </>
    )
}

export default Servicios