import { useEffect, useState } from "react"
import Formulario_servicios from "../components/Formulario_servicios"
import Tarjeta_servicios from "../components/Tarjeta_servicios"
import { listarServicios } from "../services/servicioService"

function Panel_admin() {

    const [showFormulario, setShowFormulario] = useState(false)
    const [servicios, setServicios] = useState([])

    useEffect(()=>{
        const cargarServicios = async() => {
            setServicios(await listarServicios())
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
        <section className="row m-0">
            {servicios.map((s,i)=>(
                <Tarjeta_servicios id={s.id} key={i} admin/>
            ))}
        </section>
        </>
    )
}

export default Panel_admin