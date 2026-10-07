import { useEffect, useState } from "react"
import { obtenerServicio, eliminarServicio } from "../services/servicioService"
import { useNavigate } from "react-router-dom"
import Modificar_servicios from "./Modificar_servicios"

function Tarjeta_servicios({ servicio: inicial, admin }) {

    const [servicio, setServicio] = useState(inicial)
    const [show, setShow] = useState(true)
    const [modificando, setModificando] = useState(false)
    const [recargar, setRecargar] = useState(false)
    const navegar = useNavigate()

    const id = inicial.id

    const setId = () => {
        localStorage.setItem("ID_S", id)
        navegar("/servicios/detalle")
    }

    const modificar = () => {
        localStorage.setItem("ID_S", id)
        setModificando(true)
    }

    const eliminar = async () => {
        if (confirm(`¿Eliminar el servicio "${servicio.nombre}"?`)) {
            await eliminarServicio(id)
            setShow(false)
        }
    }

    // El dato ya viene en la lista; solo se recarga al cerrar el modal de modificacion
    useEffect(()=>{
        if (!recargar) return
        const cargarServicio = async() => {
            setServicio(await obtenerServicio(id))
            setRecargar(false)
        }
        cargarServicio()
    },[recargar, id])

    const cerrarModificacion = (abierto) => {
        setModificando(abierto)
        if (!abierto) setRecargar(true)
    }

    return(
        <>
        {show && (
        <article className="col-xl-3 col-lg-4 col-md-6 col-sm-12 col-xs-12">
            <section className="div_style p-4">
                <h3>{servicio.nombre}</h3>
                <p>{servicio.descripcion}</p>
                <p className="mb-0"><strong>Precio: </strong>{servicio.precio}</p>
                <p className="mb-1"><strong>Capacidad: </strong>{servicio.capacidad} persona(s)</p>
                <button className="mt-3" onClick={setId}>Ver detalles</button>
                {admin &&
                    <button className="mt-3" onClick={modificar}>Modificar</button>
                }
                {admin &&
                    <button className="mt-3" onClick={eliminar}>Eliminar</button>
                }
            </section>
        </article>
        )}
        {modificando &&
            <Modificar_servicios show={cerrarModificacion}/>
        }
        </>
    )
}

export default Tarjeta_servicios
