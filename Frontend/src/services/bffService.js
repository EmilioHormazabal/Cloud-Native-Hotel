import { apiRequest } from '../auth/apiClient.js'
import { environment } from '../utils/enviroment.ts'

const BASE = environment.apiBaseUrl_01

export async function obtenerDashboard() {
    return apiRequest({ baseURL: BASE, url: '/api/v1/bff/dashboard' })
}
