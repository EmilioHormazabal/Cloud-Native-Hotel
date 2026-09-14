import { useState } from "react"
import { registrarServicio } from "../services/servicioService"

function Formulario_servicios({show}){

    const [t_servicio, setT_servicio]       = useState("")
    const [precio, setPrecio]               = useState(0)
    const [n_habitacion, setN_habitacion]   = useState(0)
    const [capacidad, setCapacidad]         = useState(0)
    const [disponible, setDisponible]       = useState(true)
    const [nivel_s, setNivel_s]             = useState("")
    const [nombre, setNombre]               = useState("")
    const [desc, setDesc]                   = useState("")

    const guardarServicio = async() => {

        if(precio>0&&capacidad>0&&n_habitacion>0&&t_servicio!==""&&nivel_s!==""){
            const servicio = {
                tipoServicio: t_servicio,
                precio: precio,
                numHabitacion: n_habitacion,
                capacidad: capacidad,
                disponible: disponible,
                nivelServicio: nivel_s,
                nombre: nombre,
                descripcion: desc
            }
            await registrarServicio(servicio)
            show(false)
        }else{
            alert("Valores numericos deben ser mayor a 0")
        }
    }

    const cerrarFormulario = () => {
        show(false)
    }

    return(
        <>
        <section className="card_black_bg">
            <form className="div_style form_card_size" onSubmit={(e)=>{e.preventDefault(); guardarServicio()}}>
                <p className="mb-1 mt-3">ID de servicio o habitacion:</p>
                <input
                    className="stuff_mwidth" type="number" required
                    value={n_habitacion}
                    onChange={(e)=>setN_habitacion(e.target.value)}
                />
                <p className="mb-1 mt-3">Tipo de servicio:</p>
                <select value={t_servicio} onChange={(e)=>setT_servicio(e.target.value)}>
                    <option value="" disabled>Seleccione tipo de servicio</option>
                    <option value="habitacion">Habitacion</option>
                    <option value="comida">Comida</option>
                    <option value="recreativo">Recreativo</option>
                </select>
                <p className="mb-1 mt-3">Nivel del servicio:</p>
                <select value={nivel_s} onChange={(e)=>setNivel_s(e.target.value)}>
                    <option value="" disabled>Seleccione nivel de servicio</option>
                    <option value="invitado">Invitado</option>
                    <option value="miembro">Miembro</option>
                    <option value="VIP">VIP</option>
                </select>
                <p className="mb-1 mt-3">Nombre:</p>
                <input
                    className="stuff_mwidth" type="text" required
                    maxLength={50} value={nombre}
                    onChange={(e)=>setNombre(e.target.value)}
                />
                <p className="mb-1 mt-3">Descripcion:</p>
                <textarea
                    className="stuff_mwidth" required
                    maxLength={150} value={desc}
                    onChange={(e)=>setDesc(e.target.value)}
                />
                <p className="mb-1 mt-3">Precio:</p>
                <input
                    className="stuff_mwidth" type="number" required
                    value={precio}
                    onChange={(e)=>setPrecio(e.target.value)}
                />
                <p className="mb-1 mt-3">Capacidad servicio:</p>
                <input
                    className="stuff_mwidth" type="number" required
                    value={capacidad}
                    onChange={(e)=>setCapacidad(e.target.value)}
                />
                <p className="mb-1 mt-3">Esta disponible?</p>
                <input
                    type="checkbox" checked={disponible}
                    onChange={(e)=>setDisponible(e.target.checked)}
                />
                <p className="mb-1 mt-3"></p>
                <button type="submit">Registrar</button>
                <button type="button" onClick={cerrarFormulario}>Cerrar</button>
            </form>
        </section>
        </>
    )
}

export default Formulario_servicios