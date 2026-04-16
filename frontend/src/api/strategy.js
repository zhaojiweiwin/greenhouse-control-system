import { http } from './http'

export const fetchStrategies = () => http.get('/strategies')
export const saveStrategy = (payload) => http.post('/strategies', payload)
