<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchDevices } from '../api/device'
import { fetchTelemetryHistory } from '../api/telemetry'
import { formatDateTime } from '../utils/formatTime'

const devices = ref([])
const greenhouseNames = ref([])
const selectedGreenhouse = ref('')
const metricType = ref('temperature')
const hours = ref(168)
const points = ref([])
const loading = ref(false)
const error = ref('')

const metricOptions = [
  { value: 'temperature', label: '空气温度 (°C)' },
  { value: 'humidity', label: '空气湿度 (%)' },
  { value: 'illuminance', label: '光照度 (lux)' },
  { value: 'soil_moisture', label: '土壤湿度 (%)' }
]

const METRIC_NAMES = {
  temperature: '空气温度',
  humidity: '空气湿度',
  illuminance: '光照度',
  soil_moisture: '土壤湿度'
}

const METRIC_UNITS = {
  temperature: '°C',
  humidity: '%',
  illuminance: 'lux',
  soil_moisture: '%'
}

/** 图表标题用：温度 / 湿度 等 */
const metricNameShort = computed(() => METRIC_NAMES[metricType.value] || '指标')

const metricUnit = computed(() => METRIC_UNITS[metricType.value] || '')

const chartSubtitle = computed(() => {
  const lab = selectedGreenhouse.value || '-'
  return `当前曲线：${metricNameShort.value}（${metricUnit.value}） · ${lab}`
})

/** 横轴刻度略短，避免重叠 */
const chartAxisTime = (t) => {
  const s = formatDateTime(t)
  if (s === '-') return s
  return s.length >= 11 ? s.slice(5) : s
}

const formatValue = (v) => {
  const n = Number(v)
  if (Number.isNaN(n)) return String(v)
  if (metricType.value === 'illuminance') return n >= 1000 ? n.toFixed(0) : n.toFixed(1)
  return Number.isInteger(n) ? String(n) : n.toFixed(2).replace(/\.?0+$/, '')
}

const hourOptions = [
  { value: 24, label: '最近 24 小时' },
  { value: 168, label: '最近 7 天' },
  { value: 720, label: '最近 30 天' }
]

/** 每个实验室选一台代表设备，用于解析温室范围（与实时看板聚合逻辑一致） */
const anchorDeviceId = computed(() => {
  const name = selectedGreenhouse.value
  if (!name) return ''
  const inLab = devices.value.filter((d) => d.greenhouseName === name)
  if (!inLab.length) return ''
  return [...inLab].sort((a, b) => String(a.deviceId).localeCompare(String(b.deviceId)))[0].deviceId
})

/** 折线 + 刻度几何（含时间、数值轴标注） */
const chartLayout = computed(() => {
  const pts = points.value
  if (!pts.length) return null

  const W = 920
  const H = 300
  const padL = 64
  const padR = 28
  const padT = 52
  const padB = 56
  const pw = W - padL - padR
  const ph = H - padT - padB

  const vals = pts.map((p) => Number(p.value))
  const vmin = Math.min(...vals)
  const vmax = Math.max(...vals)
  const span = vmax - vmin || 1
  const n = pts.length
  const unit = metricUnit.value

  const xAt = (i) => padL + (n <= 1 ? pw / 2 : (i / (n - 1)) * pw)
  const yAt = (v) => padT + (1 - (v - vmin) / span) * ph

  const polyline = pts
    .map((p, i) => `${xAt(i).toFixed(1)},${yAt(Number(p.value)).toFixed(1)}`)
    .join(' ')

  const dots = pts.map((p, i) => {
    const v = Number(p.value)
    return {
      cx: xAt(i),
      cy: yAt(v),
      tip: `${formatDateTime(p.collectTime)}  |  ${metricNameShort.value}：${formatValue(v)} ${unit}`
    }
  })

  const mid = (vmin + vmax) / 2
  const yTicks = [
    { y: yAt(vmax), label: `${formatValue(vmax)} ${unit}`, dy: '0.35em' },
    { y: yAt(mid), label: `${formatValue(mid)} ${unit}`, dy: '0.35em' },
    { y: yAt(vmin), label: `${formatValue(vmin)} ${unit}`, dy: '0.35em' }
  ]

  const iMid = Math.floor((n - 1) / 2)
  const xTicks = [
    { x: padL, label: chartAxisTime(pts[0].collectTime), anchor: 'start' },
    { x: padL + pw / 2, label: chartAxisTime(pts[iMid].collectTime), anchor: 'middle' },
    { x: padL + pw, label: chartAxisTime(pts[n - 1].collectTime), anchor: 'end' }
  ]

  const gridYs = [0, 0.5, 1].map((r) => padT + r * ph)
  const yLabelCx = 20
  const yLabelCy = padT + ph / 2

  return {
    W,
    H,
    padL,
    padT,
    pw,
    ph,
    polyline,
    dots,
    yTicks,
    xTicks,
    gridYs,
    yLabelCx,
    yLabelCy
  }
})

const load = async () => {
  error.value = ''
  const aid = anchorDeviceId.value
  if (!aid) {
    points.value = []
    error.value = '请先选择实验温室'
    return
  }
  loading.value = true
  try {
    const { data } = await fetchTelemetryHistory({
      deviceId: aid,
      metricType: metricType.value,
      hours: hours.value
    })
    points.value = Array.isArray(data) ? data : []
  } catch (e) {
    points.value = []
    error.value = e?.response?.data?.message || e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

const initGreenhouses = () => {
  const names = [...new Set(devices.value.map((d) => d.greenhouseName).filter(Boolean))].sort()
  greenhouseNames.value = names
  if (!selectedGreenhouse.value && names.length) {
    selectedGreenhouse.value = names[0]
  }
}

onMounted(async () => {
  const { data } = await fetchDevices()
  devices.value = data || []
  initGreenhouses()
  await load()
})
</script>

<template>
  <div class="page">
    <div class="card">
      <h3>历史趋势</h3>
      <p class="hint">按实验温室聚合该温室内所有同类采样点，展示所选指标在时间范围内的变化。</p>

      <div class="filters">
        <label>
          实验温室
          <select v-model="selectedGreenhouse" @change="load">
            <option v-for="g in greenhouseNames" :key="g" :value="g">{{ g }}</option>
          </select>
        </label>
        <label>
          指标
          <select v-model="metricType" @change="load">
            <option v-for="m in metricOptions" :key="m.value" :value="m.value">{{ m.label }}</option>
          </select>
        </label>
        <label>
          时间范围
          <select v-model.number="hours" @change="load">
            <option v-for="h in hourOptions" :key="h.value" :value="h.value">{{ h.label }}</option>
          </select>
        </label>
        <div class="filter-actions">
          <button type="button" :disabled="loading" @click="load">{{ loading ? '加载中…' : '刷新' }}</button>
        </div>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <div v-if="!loading && points.length === 0 && !error" class="empty">该条件下暂无历史数据</div>

      <div v-else-if="chartLayout" class="chart-wrap">
        <p class="chart-legend">{{ chartSubtitle }}</p>
        <svg
          class="chart"
          :viewBox="`0 0 ${chartLayout.W} ${chartLayout.H}`"
          preserveAspectRatio="xMidYMid meet"
          role="img"
          :aria-label="chartSubtitle"
        >
          <line
            v-for="(gy, idx) in chartLayout.gridYs"
            :key="'g' + idx"
            class="grid-line"
            :x1="chartLayout.padL"
            :y1="gy"
            :x2="chartLayout.padL + chartLayout.pw"
            :y2="gy"
          />

          <text
            class="axis-title-y"
            :x="chartLayout.yLabelCx"
            :y="chartLayout.yLabelCy"
            :transform="`rotate(-90 ${chartLayout.yLabelCx} ${chartLayout.yLabelCy})`"
            text-anchor="middle"
          >
            数值（{{ metricUnit }}）
          </text>

          <text
            v-for="(tk, idx) in chartLayout.yTicks"
            :key="'y' + idx"
            class="axis-tick"
            :x="chartLayout.padL - 8"
            :y="tk.y"
            text-anchor="end"
            :dy="tk.dy"
          >
            {{ tk.label }}
          </text>

          <polyline class="chart-line" fill="none" :points="chartLayout.polyline" />

          <g v-for="(d, idx) in chartLayout.dots" :key="'d' + idx">
            <circle class="chart-dot" :cx="d.cx" :cy="d.cy" r="3.5">
              <title>{{ d.tip }}</title>
            </circle>
          </g>

          <text
            v-for="(tx, idx) in chartLayout.xTicks"
            :key="'x' + idx"
            class="axis-tick axis-tick-x"
            :x="tx.x"
            :y="chartLayout.padT + chartLayout.ph + 22"
            :text-anchor="tx.anchor"
          >
            {{ tx.label }}
          </text>

          <text class="axis-title-x" :x="chartLayout.padL + chartLayout.pw / 2" :y="chartLayout.H - 8" text-anchor="middle">
            采集时间
          </text>
        </svg>
      </div>
    </div>

    <div class="card">
      <h4>数据明细（{{ metricNameShort }} · 最多 2000 条）</h4>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>采集时间</th>
              <th>数值（{{ metricNameShort }} / {{ metricUnit }}）</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, idx) in points" :key="`${row.collectTime}-${idx}`">
              <td>{{ idx + 1 }}</td>
              <td>{{ formatDateTime(row.collectTime) }}</td>
              <td>{{ formatValue(row.value) }} {{ metricUnit }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.hint {
  margin: 0 0 16px;
  font-size: 13px;
  color: #5c6478;
}

.filters {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr)) auto;
  gap: 12px;
  align-items: end;
  margin-bottom: 12px;
}

@media (max-width: 900px) {
  .filters {
    grid-template-columns: 1fr;
  }
}

.filter-actions {
  display: flex;
  align-items: end;
}

label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
}

select,
button {
  padding: 8px;
  box-sizing: border-box;
  width: 100%;
}

.filter-actions button {
  min-width: 88px;
}

.error {
  color: #b42318;
  font-size: 14px;
}

.empty {
  padding: 24px;
  text-align: center;
  color: #6b7280;
}

.chart-wrap {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fafbff;
  margin-top: 8px;
  padding: 8px 12px 12px;
  box-sizing: border-box;
}

.chart-legend {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1f2a44;
}

.chart {
  display: block;
  width: 100%;
  height: min(360px, 55vw);
  min-height: 260px;
}

.grid-line {
  stroke: #e8ecf4;
  stroke-width: 1;
}

.axis-title-y {
  fill: #5c6478;
  font-size: 12px;
}

.axis-title-x {
  fill: #5c6478;
  font-size: 12px;
}

.axis-tick {
  fill: #4b5569;
  font-size: 11px;
}

.axis-tick-x {
  font-size: 10px;
}

.chart-line {
  stroke: #3b5bdb;
  stroke-width: 2;
  stroke-linejoin: round;
  stroke-linecap: round;
}

.chart-dot {
  fill: #3b5bdb;
  stroke: #fff;
  stroke-width: 1;
  cursor: default;
}

.table-wrap {
  max-height: 360px;
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
