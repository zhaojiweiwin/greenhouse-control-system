import { http } from './http'

export const fetchStrategies = () => http.get('/strategies')
export const saveStrategy = (payload) => http.post('/strategies', payload)
export const updateStrategy = (id, payload) => http.put(`/strategies/${id}`, payload)
export const deleteStrategy = (id) => http.delete(`/strategies/${id}`)
