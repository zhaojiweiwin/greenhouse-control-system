<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { sendControlCommand, fetchControlCommands } from '../api/control'
import { fetchDevices } from '../api/device'
import PaginationBar from '../components/PaginationBar.vue'
import { buildDeviceMetaMap } from '../utils/deviceMeta'
import { formatDateTime } from '../utils/formatTime'

const deviceId = ref('gh-esp32-01')
const commandType = ref('FAN_ON')
const payload = ref('{"action":"ON","target":"fan"}')
const message = ref('')
const commands = ref([])
const devices = ref([])
const page = ref(1)
const pageSize = ref(10)

const pagedCommands = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return commands.value.slice(start, start + pageSize.value)
})

const deviceMeta = computed(() => buildDeviceMetaMap(devices.value))

watch(commands, () => {
  const maxPage = Math.max(1, Math.ceil(commands.value.length / pageSize.value))
  if (page.value > maxPage) {
    page.value = maxPage
  }
})

const submit = async () => {
  await sendControlCommand({
    deviceId: deviceId.value,
    commandType: commandType.value,
    payload: payload.value
  })
  message.value = '命令已下发'
  await loadCommands()
}

const loadCommands = async () => {
  const { data } = await fetchControlCommands()
  commands.value = data
}

onMounted(async () => {
  const [{ data: deviceRows }] = await Promise.all([
    fetchDevices(),
    loadCommands()
  ])
  devices.value = deviceRows
})
</script>

<template>
  <div style="display:grid;gap:16px;">
    <div class="card">
      <h3>设备控制</h3>
      <div style="display:grid;gap:10px;max-width:560px;">
        <label>
          设备
          <select v-model="deviceId">
            <option v-for="device in devices" :key="device.id" :value="device.deviceId">
              {{ device.name || '-' }} · {{ device.greenhouseName || '-' }}
            </option>
          </select>
        </label>
        <label>命令类型 <input v-model="commandType" /></label>
        <label>Payload(JSON字符串) <textarea v-model="payload" rows="4" /></label>
        <button @click="submit">发送命令</button>
        <span>{{ message }}</span>
      </div>
    </div>

    <div class="card">
      <h3>命令日志</h3>
      <table class="data-table">
        <thead>
          <tr><th>设备名称</th><th>实验室</th><th>命令</th><th>来源</th><th>状态</th><th>时间</th></tr>
        </thead>
        <tbody>
          <tr v-for="command in pagedCommands" :key="command.id">
            <td>{{ deviceMeta[command.deviceId]?.name ?? '-' }}</td>
            <td>{{ deviceMeta[command.deviceId]?.greenhouseName ?? '-' }}</td>
            <td>{{ command.commandType }}</td>
            <td>{{ command.source }}</td>
            <td>{{ command.status }}</td>
            <td>{{ formatDateTime(command.sentTime) }}</td>
          </tr>
        </tbody>
      </table>
      <PaginationBar
        v-model:page="page"
        v-model:page-size="pageSize"
        :total="commands.length"
      />
    </div>
  </div>
</template>

<style scoped>
select,
input,
textarea {
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
