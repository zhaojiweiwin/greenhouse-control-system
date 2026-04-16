<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { fetchStrategies, saveStrategy } from '../api/strategy'
import PaginationBar from '../components/PaginationBar.vue'

const rules = ref([])
const message = ref('')
const page = ref(1)
const pageSize = ref(10)
const form = ref({
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

const pagedRules = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return rules.value.slice(start, start + pageSize.value)
})

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

const submit = async () => {
  await saveStrategy(form.value)
  message.value = '策略已保存'
  await loadRules()
}

onMounted(loadRules)
</script>

<template>
  <div style="display:grid;gap:16px;">
    <div class="card">
      <h3>策略管理</h3>
      <div class="form-grid">
        <label>规则名称 <input v-model="form.ruleName" /></label>
        <label>设备ID <input v-model="form.deviceId" /></label>
        <label>指标类型 <input v-model="form.metricType" /></label>
        <label>比较符 <input v-model="form.operator" /></label>
        <label>阈值 <input v-model="form.thresholdValue" type="number" /></label>
        <label>动作类型 <input v-model="form.actionType" /></label>
        <label class="full-width">动作载荷 <textarea v-model="form.actionPayload" rows="4" /></label>
        <label>冷却时间(s) <input v-model="form.cooldownSeconds" type="number" /></label>
        <label>启用 <input v-model="form.enabled" type="checkbox" /></label>
      </div>
      <button @click="submit">保存策略</button>
      <span style="margin-left:12px;">{{ message }}</span>
    </div>

    <div class="card">
      <h3>已配置策略</h3>
      <table class="data-table">
        <thead>
          <tr><th>名称</th><th>设备</th><th>规则</th><th>动作</th><th>启用</th><th>最近触发</th></tr>
        </thead>
        <tbody>
          <tr v-for="rule in pagedRules" :key="rule.id">
            <td>{{ rule.ruleName }}</td>
            <td>{{ rule.deviceId }}</td>
            <td>{{ rule.metricType }} {{ rule.operator }} {{ rule.thresholdValue }}</td>
            <td>{{ rule.actionType }}</td>
            <td>{{ rule.enabled ? '是' : '否' }}</td>
            <td>{{ rule.lastTriggeredAt || '-' }}</td>
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
</style>
