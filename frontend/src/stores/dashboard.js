import { defineStore } from 'pinia'
import { fetchTelemetryOverview } from '../api/telemetry'
import { fetchDevices } from '../api/device'
import { fetchAlarms } from '../api/alarm'

export const useDashboardStore = defineStore('dashboard', {
  state: () => ({
    loading: false,
    items: [],
    devices: [],
    alarms: [],
    snapshot: null,
    temperatureSeries: [],
    humiditySeries: [],
    soilMoistureSeries: [],
    lastUpdatedAt: null,
    activeDeviceId: 'gh-esp32-01'
  }),
  actions: {
    async loadOverview() {
      this.loading = true
      try {
        const [{ data: overview }, { data: devices }, { data: alarms }] = await Promise.all([
          fetchTelemetryOverview(this.activeDeviceId),
          fetchDevices(),
          fetchAlarms()
        ])
        this.items = overview.latestRows
        this.snapshot = overview.latestSnapshot
        this.temperatureSeries = overview.temperatureSeries
        this.humiditySeries = overview.humiditySeries
        this.soilMoistureSeries = overview.soilMoistureSeries
        this.devices = devices
        this.alarms = alarms
        this.lastUpdatedAt = new Date().toISOString()
      } finally {
        this.loading = false
      }
    },
    applyRealtimePayload(payload) {
      const metrics = payload.metrics || {}
      this.snapshot = {
        deviceId: payload.deviceId,
        temperature: metrics.temperature ?? this.snapshot?.temperature ?? 0,
        humidity: metrics.humidity ?? this.snapshot?.humidity ?? 0,
        soilMoisture: metrics.soil_moisture ?? this.snapshot?.soilMoisture ?? 0
      }
      const collectTime = payload.timestamp
      const rows = Object.keys(metrics).map((metricType) => ({
        id: `${payload.deviceId}-${metricType}-${collectTime}`,
        deviceId: payload.deviceId,
        metricType,
        metricValue: metrics[metricType],
        collectTime
      }))
      this.items = [...rows, ...this.items].slice(0, 50)
      this.pushSeries(this.temperatureSeries, metrics.temperature, collectTime)
      this.pushSeries(this.humiditySeries, metrics.humidity, collectTime)
      this.pushSeries(this.soilMoistureSeries, metrics.soil_moisture, collectTime)
      this.lastUpdatedAt = new Date().toISOString()
    },
    pushSeries(target, value, time) {
      if (typeof value !== 'number') return
      target.unshift({ time, value })
      if (target.length > 20) {
        target.pop()
      }
    }
  }
})
