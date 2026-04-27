<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useDashboardStore } from '../stores/dashboard'
import PaginationBar from '../components/PaginationBar.vue'
import { buildDeviceMetaMap } from '../utils/deviceMeta'
import { formatDateTime } from '../utils/formatTime'

const store = useDashboardStore()
let timer = null
let ws = null
const telemetryPage = ref(1)
const telemetryPageSize = ref(10)
const alarmPage = ref(1)
const alarmPageSize = ref(5)

const pagedItems = computed(() => {
  const start = (telemetryPage.value - 1) * telemetryPageSize.value
  return store.items.slice(start, start + telemetryPageSize.value)
})

const pagedAlarms = computed(() => {
  const start = (alarmPage.value - 1) * alarmPageSize.value
  return store.alarms.slice(start, start + alarmPageSize.value)
})

const deviceMeta = computed(() => buildDeviceMetaMap(store.devices))

watch(() => store.items.length, () => {
  const maxPage = Math.max(1, Math.ceil(store.items.length / telemetryPageSize.value))
  if (telemetryPage.value > maxPage) {
    telemetryPage.value = maxPage
  }
})

watch(() => store.alarms.length, () => {
  const maxPage = Math.max(1, Math.ceil(store.alarms.length / alarmPageSize.value))
  if (alarmPage.value > maxPage) {
    alarmPage.value = maxPage
  }
})

onMounted(async () => {
  await store.loadOverview()
  ws = new WebSocket(`ws://${window.location.hostname}:8080/ws/telemetry`)
  ws.onmessage = (event) => {
    const payload = JSON.parse(event.data)
    store.applyRealtimePayload(payload)
  }
  timer = setInterval(() => store.loadOverview(), 5000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (ws) ws.close()
})
</script>

<template>
  <div style="display:grid;gap:16px;">
    <div class="card">
      <h3>实时数据</h3>
      <p>最后刷新: {{ formatDateTime(store.lastUpdatedAt) }}</p>
      <p v-if="store.loading">加载中...</p>
      <div v-else class="stats-grid">
        <div class="stat-item">
          <span>空气温度</span>
          <strong>{{ store.snapshot?.temperature ?? 0 }} °C</strong>
        </div>
        <div class="stat-item">
          <span>空气湿度</span>
          <strong>{{ store.snapshot?.humidity ?? 0 }} %</strong>
        </div>
        <div class="stat-item">
          <span>光照度</span>
          <strong>{{ store.snapshot?.illuminance ?? 0 }} lux</strong>
        </div>
        <div class="stat-item">
          <span>土壤湿度</span>
          <strong>{{ store.snapshot?.soilMoisture ?? 0 }} %</strong>
        </div>
        <div class="stat-item">
          <span>设备数</span>
          <strong>{{ store.devices.length }}</strong>
        </div>
      </div>
    </div>

    <div class="card">
      <h3>最近趋势</h3>
      <p class="trend-hint">各指标展示当前实验温室下最近 10 次采样</p>
      <div class="series-grid">
        <div>
          <h4>空气温度</h4>
          <ul class="trend-list">
            <li v-for="item in store.temperatureSeries" :key="`temp-${item.time}`">{{ formatDateTime(item.time) }} / {{ item.value }} °C</li>
          </ul>
        </div>
        <div>
          <h4>空气湿度</h4>
          <ul class="trend-list">
            <li v-for="item in store.humiditySeries" :key="`hum-${item.time}`">{{ formatDateTime(item.time) }} / {{ item.value }} %</li>
          </ul>
        </div>
        <div>
          <h4>光照度</h4>
          <ul class="trend-list">
            <li v-for="item in store.illuminanceSeries" :key="`lux-${item.time}`">{{ formatDateTime(item.time) }} / {{ item.value }} lux</li>
          </ul>
        </div>
        <div>
          <h4>土壤湿度</h4>
          <ul class="trend-list">
            <li v-for="item in store.soilMoistureSeries" :key="`soil-${item.time}`">{{ formatDateTime(item.time) }} / {{ item.value }} %</li>
          </ul>
        </div>
      </div>
    </div>

    <div class="card">
      <h3>最新遥测</h3>
      <table class="data-table">
        <thead>
          <tr><th>设备名称</th><th>实验室</th><th>指标</th><th>值</th><th>采集时间</th></tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedItems" :key="item.id">
            <td>{{ deviceMeta[item.deviceId]?.name ?? '-' }}</td>
            <td>{{ deviceMeta[item.deviceId]?.greenhouseName ?? '-' }}</td>
            <td>{{ item.metricType }}</td>
            <td>{{ item.metricValue }}</td>
            <td>{{ formatDateTime(item.collectTime) }}</td>
          </tr>
        </tbody>
      </table>
      <PaginationBar
        v-model:page="telemetryPage"
        v-model:page-size="telemetryPageSize"
        :total="store.items.length"
      />
    </div>

    <div class="card">
      <h3>最新告警</h3>
      <table class="data-table">
        <thead>
          <tr><th>设备名称</th><th>实验室</th><th>类型</th><th>状态</th><th>消息</th><th>时间</th></tr>
        </thead>
        <tbody>
          <tr v-for="alarm in pagedAlarms" :key="alarm.id">
            <td>{{ deviceMeta[alarm.deviceId]?.name ?? '-' }}</td>
            <td>{{ deviceMeta[alarm.deviceId]?.greenhouseName ?? '-' }}</td>
            <td>{{ alarm.alarmType }}</td>
            <td>{{ alarm.status }}</td>
            <td>{{ alarm.message }}</td>
            <td>{{ formatDateTime(alarm.triggeredAt) }}</td>
          </tr>
        </tbody>
      </table>
      <PaginationBar
        v-model:page="alarmPage"
        v-model:page-size="alarmPageSize"
        :total="store.alarms.length"
      />
    </div>
  </div>
</template>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.trend-hint {
  margin: 0 0 12px;
  font-size: 13px;
  color: #5c6478;
}

.series-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

@media (min-width: 960px) {
  .series-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

.stat-item {
  background: #f5f7ff;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.trend-list {
  margin: 0;
  padding-left: 18px;
  max-height: 240px;
  overflow: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  border: 1px solid #dfe4f2;
  padding: 8px;
  text-align: left;
}
</style>
