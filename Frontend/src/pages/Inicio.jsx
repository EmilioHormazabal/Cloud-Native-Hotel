import { useNavigate } from "react-router-dom"

function Inicio() {

    const navegar = useNavigate()

    return(
        <>
            <section className="p-4 div_style">
                <h2>Bienvenido a Grand Hotel!</h2>
                <p>Consulta nuestros distintos servicios aqui:</p>
                <button onClick={()=>navegar("servicios")}>Servicios</button>
                <p>Consulta tus reservas aqui:</p>
                <button onClick={()=>navegar("mis_reservas")}>Mis reservas</button>
            </section>
        </>
    )
}

export default Inicio