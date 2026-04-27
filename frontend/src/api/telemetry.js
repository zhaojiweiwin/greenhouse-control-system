import { http } from './http'

export const fetchLatestTelemetry = () => http.get('/telemetry/latest')
export const fetchTelemetryOverview = (deviceId) => http.get('/telemetry/overview', { params: { deviceId } })

/** @param {{ deviceId: string, metricType: string, hours: number }} params */
export const fetchTelemetryHistory = (params) => http.get('/telemetry/history', { params })
