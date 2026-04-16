<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { fetchAlarms } from '../api/alarm'
import PaginationBar from '../components/PaginationBar.vue'

const alarms = ref([])
const page = ref(1)
const pageSize = ref(10)
const filters = ref({
  deviceId: '',
  alarmType: '',
  status: '',
  startTime: '',
  endTime: ''
})
const deviceOptions = ref([])
const typeOptions = ref([])
const statusOptions = ['OPEN', 'RESOLVED']

const pagedAlarms = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return alarms.value.slice(start, start + pageSize.value)
})

watch(alarms, () => {
  const maxPage = Math.max(1, Math.ceil(alarms.value.length / pageSize.value))
  if (page.value > maxPage) {
    page.value = maxPage
  }
})

const loadAlarms = async () => {
  const params = {}
  if (filters.value.deviceId) params.deviceId = filters.value.deviceId
  if (filters.value.alarmType) params.alarmType = filters.value.alarmType
  if (filters.value.status) params.status = filters.value.status
  if (filters.value.startTime) params.startTime = filters.value.startTime
  if (filters.value.endTime) params.endTime = filters.value.endTime
  const { data } = await fetchAlarms(params)
  alarms.value = data
  deviceOptions.value = Array.from(new Set(data.map(item => item.deviceId))).sort()
  typeOptions.value = Array.from(new Set(data.map(item => item.alarmType))).sort()
}

const resetFilters = async () => {
  filters.value = {
    deviceId: '',
    alarmType: '',
    status: '',
    startTime: '',
    endTime: ''
  }
  await loadAlarms()
}

onMounted(loadAlarms)
</script>

<template>
  <div class="card">
    <h3>告警中心</h3>
    <div class="filter-grid">
      <label>
        设备
        <input v-model="filters.deviceId" list="device-options" placeholder="输入或选择设备ID" />
        <datalist id="device-options">
          <option v-for="item in deviceOptions" :key="item" :value="item" />
        </datalist>
      </label>
      <label>
        类型
        <input v-model="filters.alarmType" list="type-options" placeholder="输入或选择告警类型" />
        <datalist id="type-options">
          <option v-for="item in typeOptions" :key="item" :value="item" />
        </datalist>
      </label>
      <label>
        状态
        <select v-model="filters.status">
          <option value="">全部</option>
          <option v-for="item in statusOptions" :key="item" :value="item">{{ item }}</option>
        </select>
      </label>
      <label>
        开始时间
        <input v-model="filters.startTime" type="datetime-local" />
      </label>
      <label>
        结束时间
        <input v-model="filters.endTime" type="datetime-local" />
      </label>
      <div class="filter-actions">
        <button @click="loadAlarms">查询</button>
        <button @click="resetFilters">重置</button>
      </div>
    </div>
    <table class="data-table">
      <thead>
        <tr><th>设备</th><th>类型</th><th>级别</th><th>状态</th><th>触发值</th><th>阈值</th><th>时间</th><th>消息</th></tr>
      </thead>
      <tbody>
        <tr v-for="alarm in pagedAlarms" :key="alarm.id">
          <td>{{ alarm.deviceId }}</td>
          <td>{{ alarm.alarmType }}</td>
          <td>{{ alarm.alarmLevel }}</td>
          <td>{{ alarm.status }}</td>
          <td>{{ alarm.triggerValue }}</td>
          <td>{{ alarm.thresholdValue }}</td>
          <td>{{ alarm.triggeredAt }}</td>
          <td>{{ alarm.message }}</td>
        </tr>
      </tbody>
    </table>
    <PaginationBar
      v-model:page="page"
      v-model:page-size="pageSize"
      :total="alarms.length"
    />
  </div>
</template>

<style scoped>
.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.filter-actions {
  display: flex;
  gap: 8px;
  align-items: end;
}

input,
select,
button {
  width: 100%;
  box-sizing: border-box;
  margin-top: 4px;
  padding: 8px;
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
