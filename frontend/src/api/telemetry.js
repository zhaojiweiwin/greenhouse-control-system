import { http } from './http'

export const fetchLatestTelemetry = () => http.get('/telemetry/latest')
export const fetchTelemetryOverview = (deviceId) => http.get('/telemetry/overview', { params: { deviceId } })
