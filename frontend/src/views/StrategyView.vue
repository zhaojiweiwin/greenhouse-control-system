<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { fetchStrategies, saveStrategy, updateStrategy, deleteStrategy } from '../api/strategy'
import { fetchDevices } from '../api/device'
import PaginationBar from '../components/PaginationBar.vue'
import { buildDeviceMetaMap } from '../utils/deviceMeta'
import { formatDateTime } from '../utils/formatTime'

const rules = ref([])
const devices = ref([])
const message = ref('')
const page = ref(1)
const pageSize = ref(10)
const editingId = ref(null)

const defaultForm = () => ({
  ruleName: '高温启动风机',
  deviceId: 'gh-esp32-01',
  metricType: 'temperature',
  operator: '>',
  thresholdValue: 30,
  actionType: 'FAN_ON',
  actionPayload: '{"action":"ON","target":"fan"}',
  enabled: true,
  cooldownSeconds: 60
})

const form = ref(defaultForm())

const pagedRules = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return rules.value.slice(start, start + pageSize.value)
})

const deviceMeta = computed(() => buildDeviceMetaMap(devices.value))

watch(rules, () => {
  const maxPage = Math.max(1, Math.ceil(rules.value.length / pageSize.value))
  if (page.value > maxPage) {
    page.value = maxPage
  }
})

const loadRules = async () => {
  const { data } = await fetchStrategies()
  rules.value = data
}

const formPayload = () => ({
  ruleName: form.value.ruleName,
  deviceId: form.value.deviceId,
  metricType: form.value.metricType,
  operator: form.value.operator,
  thresholdValue: Number(form.value.thresholdValue),
  actionType: form.value.actionType,
  actionPayload: form.value.actionPayload,
  enabled: !!form.value.enabled,
  cooldownSeconds: Number(form.value.cooldownSeconds)
})

const submit = async () => {
  if (editingId.value != null) {
    await updateStrategy(editingId.value, formPayload())
    message.value = '策略已更新'
  } else {
    await saveStrategy(formPayload())
    message.value = '策略已保存'
  }
  await loadRules()
}

const startEdit = (rule) => {
  editingId.value = rule.id
  form.value = {
    ruleName: rule.ruleName,
    deviceId: rule.deviceId,
    metricType: rule.metricType,
    operator: rule.operator,
    thresholdValue: rule.thresholdValue,
    actionType: rule.actionType,
    actionPayload: rule.actionPayload,
    enabled: !!rule.enabled,
    cooldownSeconds: rule.cooldownSeconds
  }
  message.value = '正在编辑，修改后点击保存'
}

const cancelEdit = () => {
  editingId.value = null
  form.value = defaultForm()
  message.value = ''
}

const removeRule = async (rule) => {
  if (!confirm(`确定删除规则「${rule.ruleName}」吗？`)) return
  await deleteStrategy(rule.id)
  if (editingId.value === rule.id) {
    cancelEdit()
  }
  message.value = '已删除'
  await loadRules()
}

onMounted(async () => {
  const [{ data: deviceRows }] = await Promise.all([fetchDevices(), loadRules()])
  devices.value = deviceRows
})
</script>

<template>
  <div style="display:grid;gap:16px;">
    <div class="card">
      <h3>策略管理</h3>
      <div class="form-grid">
        <label>规则名称 <input v-model="form.ruleName" /></label>
        <label>
          设备
          <select v-model="form.deviceId">
            <option v-for="d in devices" :key="d.id" :value="d.deviceId">
              {{ d.name || '-' }} · {{ d.greenhouseName || '-' }}
            </option>
          </select>
        </label>
        <label>指标类型 <input v-model="form.metricType" /></label>
        <label>比较符 <input v-model="form.operator" /></label>
        <label>阈值 <input v-model="form.thresholdValue" type="number" /></label>
        <label>动作类型 <input v-model="form.actionType" /></label>
        <label class="full-width">动作载荷 <textarea v-model="form.actionPayload" rows="4" /></label>
        <label>冷却时间(s) <input v-model="form.cooldownSeconds" type="number" /></label>
        <label>启用 <input v-model="form.enabled" type="checkbox" /></label>
      </div>
      <button @click="submit">{{ editingId != null ? '更新策略' : '保存策略' }}</button>
      <button v-if="editingId != null" type="button" class="btn-secondary" @click="cancelEdit">取消编辑</button>
      <span style="margin-left:12px;">{{ message }}</span>
    </div>

    <div class="card">
      <h3>已配置策略</h3>
      <table class="data-table">
        <thead>
          <tr><th>名称</th><th>设备名称</th><th>实验室</th><th>规则</th><th>动作</th><th>启用</th><th>最近触发</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="rule in pagedRules" :key="rule.id">
            <td>{{ rule.ruleName }}</td>
            <td>{{ deviceMeta[rule.deviceId]?.name ?? '-' }}</td>
            <td>{{ deviceMeta[rule.deviceId]?.greenhouseName ?? '-' }}</td>
            <td>{{ rule.metricType }} {{ rule.operator }} {{ rule.thresholdValue }}</td>
            <td>{{ rule.actionType }}</td>
            <td>{{ rule.enabled ? '是' : '否' }}</td>
            <td>{{ rule.lastTriggeredAt ? formatDateTime(rule.lastTriggeredAt) : '-' }}</td>
            <td class="actions">
              <button type="button" class="btn-small" @click="startEdit(rule)">编辑</button>
              <button type="button" class="btn-small btn-danger" @click="removeRule(rule)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <PaginationBar
        v-model:page="page"
        v-model:page-size="pageSize"
        :total="rules.length"
      />
    </div>
  </div>
</template>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.full-width {
  grid-column: 1 / -1;
}

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

.actions {
  white-space: nowrap;
}

.btn-small {
  margin-right: 6px;
  padding: 4px 10px;
  cursor: pointer;
}

.btn-danger {
  color: #b42318;
  border-color: #fecdca;
  background: #fef3f2;
}

.btn-secondary {
  margin-left: 8px;
  padding: 8px 14px;
  cursor: pointer;
}
</style>
