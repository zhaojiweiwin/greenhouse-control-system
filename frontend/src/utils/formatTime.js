/**
 * 格式化为 YYYY-MM-DD HH:mm:ss（浏览器本地时区）
 * 支持 ISO 字符串、Date、以及 Jackson 默认的 LocalDateTime 数组 [y, mo, d, h, mi, s, ns]
 */
export function formatDateTime(value) {
  if (value == null || value === '') return '-'
  if (Array.isArray(value) && value.length >= 6) {
    const [y, mo, d, h, mi, s] = value
    const p = (n) => String(n).padStart(2, '0')
    return `${y}-${p(mo)}-${p(d)} ${p(h)}:${p(mi)}:${p(s)}`
  }
  const d = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(d.getTime())) {
    return typeof value === 'string' ? value : '-'
  }
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}
