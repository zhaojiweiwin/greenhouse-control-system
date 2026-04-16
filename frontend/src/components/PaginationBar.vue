<script setup>
import { computed } from 'vue'

const props = defineProps({
  page: {
    type: Number,
    required: true
  },
  pageSize: {
    type: Number,
    required: true
  },
  total: {
    type: Number,
    required: true
  },
  pageSizeOptions: {
    type: Array,
    default: () => [5, 10, 20]
  }
})

const emit = defineEmits(['update:page', 'update:pageSize'])

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))
const start = computed(() => (props.total === 0 ? 0 : (props.page - 1) * props.pageSize + 1))
const end = computed(() => Math.min(props.total, props.page * props.pageSize))

const goPrev = () => {
  if (props.page > 1) {
    emit('update:page', props.page - 1)
  }
}

const goNext = () => {
  if (props.page < totalPages.value) {
    emit('update:page', props.page + 1)
  }
}

const onPageSizeChange = (event) => {
  emit('update:pageSize', Number(event.target.value))
  emit('update:page', 1)
}
</script>

<template>
  <div class="pagination-bar">
    <span>共 {{ total }} 条，显示 {{ start }}-{{ end }}</span>
    <label>
      每页
      <select :value="pageSize" @change="onPageSizeChange">
        <option v-for="size in pageSizeOptions" :key="size" :value="size">{{ size }}</option>
      </select>
      条
    </label>
    <button :disabled="page <= 1" @click="goPrev">上一页</button>
    <span>{{ page }} / {{ totalPages }}</span>
    <button :disabled="page >= totalPages" @click="goNext">下一页</button>
  </div>
</template>

<style scoped>
.pagination-bar {
  margin-top: 12px;
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
}

select,
button {
  padding: 6px 10px;
}
</style>
