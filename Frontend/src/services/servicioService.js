import { apiRequest } from '../auth/apiClient.js'
import { environment } from '../utils/enviroment.ts'

const BASE = environment.apiBaseUrl_02

export async function listarServicios() {
    return apiRequest({ baseURL: BASE, url: '/api/v1/servicio/list' })
}

export async function obtenerServicio(id) {
    return apiRequest({ baseURL: BASE, url: `/api/v1/servicio/get/${id}` })
}

export async function registrarServicio(servicio) {
    return apiRequest({ baseURL: BASE, url: '/api/v1/servicio/post', method: 'post', data: servicio })
}

export async function modificarServicio(id, servicio) {
    return apiRequest({ baseURL: BASE, url: `/api/v1/servicio/put/${id}`, method: 'put', data: servicio })
}
