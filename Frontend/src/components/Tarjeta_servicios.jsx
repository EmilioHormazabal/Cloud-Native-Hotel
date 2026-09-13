import { useEffect, useState } from "react"
import { obtenerServicio, eliminarServicio } from "../services/servicioService"
import { useNavigate } from "react-router-dom"
import Modificar_servicios from "./Modificar_servicios"

function Tarjeta_servicios({ id, admin }) {

    const [servicio, setServicio] = useState({})
    const [show, setShow] = useState(true)
    const [modificando, setModificando] = useState(false)
    const navegar = useNavigate()

    const setId = (id) => {
        localStorage.setItem("ID_S", id)
        navegar("/servicios/detalle")
    }

    const modificar = (id) => {
        localStorage.setItem("ID_S", id)
        setModificando(true)
    }

    const eliminar = async (id) => {
        if (confirm(`¿Eliminar el servicio "${servicio.nombre}"?`)) {
            await eliminarServicio(id)
            setShow(false)
        }
    }

    useEffect(()=>{
        const cargarServicio = async() => {
            const res = await obtenerServicio(id)
            if (res == null){
                setShow(false)
            }else{
                setServicio(res)
            }
        }
        cargarServicio()
    },[id, modificando])

    return(
        <>
        {show && (
        <article className="col-xl-3 col-lg-4 col-md-6 col-sm-12 col-xs-12">
            <section className="div_style p-4">
                <h3>{servicio.nombre}</h3>
                <p>{servicio.descripcion}</p>
                <p className="mb-0"><strong>Precio: </strong>{servicio.precio}</p>
                <p className="mb-1"><strong>Capacidad: </strong>{servicio.capacidad} persona(s)</p>
                <button className="mt-3" onClick={()=>setId(id)}>Ver detalles</button>
                {admin &&
                    <button className="mt-3" onClick={()=>modificar(id)}>Modificar</button>
                }
                {admin &&
                    <button className="mt-3" onClick={()=>eliminar(id)}>Eliminar</button>
                }
            </section>
        </article>
        )}
        {modificando &&
            <Modificar_servicios show={setModificando}/>
        }
        </>
    )
}

export default Tarjeta_servicios