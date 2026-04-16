import { http } from './http'

export const fetchDevices = () => http.get('/devices')
export const saveDevice = (payload) => http.post('/devices', payload)
