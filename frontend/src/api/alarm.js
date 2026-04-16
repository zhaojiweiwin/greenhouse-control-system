import { http } from './http'

export const fetchAlarms = (params = {}) => http.get('/alarms', { params })
