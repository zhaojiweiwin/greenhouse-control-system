/**
 * @param {Array<{ deviceId: string, name?: string, greenhouseName?: string }>} devices
 * @returns {Record<string, { name: string, greenhouseName: string }>}
 */
export function buildDeviceMetaMap(devices) {
  const map = Object.create(null)
  if (!devices) return map
  for (const d of devices) {
    if (!d || !d.deviceId) continue
    map[d.deviceId] = {
      name: d.name != null && String(d.name).trim() !== '' ? d.name : '-',
      greenhouseName:
        d.greenhouseName != null && String(d.greenhouseName).trim() !== '' ? d.greenhouseName : '-'
    }
  }
  return map
}
