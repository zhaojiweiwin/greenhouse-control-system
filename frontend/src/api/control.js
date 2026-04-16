import { http } from './http'

export const sendControlCommand = (payload) => http.post('/control/command', payload)
export const fetchControlCommands = () => http.get('/control/commands')
