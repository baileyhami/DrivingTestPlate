/**
 * 动态加载高德 JS API 2.0（VITE_AMAP_KEY + 可选 VITE_AMAP_SECURITY_JS_CODE）
 */
let loadPromise = null

export function loadAmapScript(key, securityJsCode) {
  if (!key) {
    return Promise.reject(new Error('缺少高德地图 Key'))
  }
  if (typeof window !== 'undefined' && securityJsCode) {
    window._AMapSecurityConfig = { securityJsCode }
  }
  if (typeof window !== 'undefined' && window.AMap) {
    return Promise.resolve(window.AMap)
  }
  if (loadPromise) {
    return loadPromise
  }
  loadPromise = new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${encodeURIComponent(key)}`
    script.async = true
    script.onload = () => resolve(window.AMap)
    script.onerror = () => reject(new Error('高德地图脚本加载失败'))
    document.head.appendChild(script)
  })
  return loadPromise
}
