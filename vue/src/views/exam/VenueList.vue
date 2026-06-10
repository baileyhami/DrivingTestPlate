<template>
  <el-card class="ydt-card" shadow="hover">
    <template #header>
      <div class="page-head">
        <div>
          <div class="page-title">考场与线路</div>
        </div>
        <el-button v-if="store.isAdmin" type="primary" @click="openVenue()">新增考场</el-button>
      </div>
    </template>
    <div class="toolbar-row">
      <el-input
        v-model="query.keyword"
        clearable
        placeholder="搜索考场名称或地址"
        class="search-input"
        @clear="load"
        @keyup.enter="load"
      >
        <template #append>
          <el-button @click="load">查询</el-button>
        </template>
      </el-input>
    </div>
    <el-table :data="table.records" v-loading="loading" border stripe row-key="id" @expand-change="onExpand">
      <el-table-column type="expand">
        <template #default="{ row: venueRow }">
          <div class="routes">
            <el-table :data="routesMap[venueRow.id] || []" size="small" border>
              <el-table-column prop="routeName" label="线路" min-width="100" />
              <el-table-column prop="routeDesc" label="说明" min-width="140" show-overflow-tooltip />
              <el-table-column label="示意图" width="100">
                <template #default="{ row: r }">
                  <el-image v-if="r.mapImageUrl" :src="r.mapImageUrl" style="height: 56px; width: 88px" fit="cover" preview-teleported />
                  <span v-else class="text-muted">—</span>
                </template>
              </el-table-column>
              <el-table-column label="路线地图" width="200">
                <template #default="{ row: r }">
                  <el-button link type="primary" :disabled="!hasRoutePath(r)" @click="openRouteMap(r)">
                    高德路线
                  </el-button>
                </template>
              </el-table-column>
              <el-table-column v-if="store.isAdmin" label="操作" width="200">
                <template #default="{ row: r }">
                  <el-button link type="primary" @click="openRoute(venueRow, r)">编辑</el-button>
                  <el-button link type="danger" @click="delRoute(r.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button v-if="store.isAdmin" class="mt" size="small" @click="openRoute(venueRow)">添加线路</el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="考场名称" min-width="120" />
      <el-table-column prop="address" label="地址" min-width="160" show-overflow-tooltip />
      <el-table-column prop="phone" label="电话" width="120" />
      <el-table-column prop="region" label="区县" width="100" />
      <el-table-column label="坐标" width="120">
        <template #default="{ row }">
          <span v-if="row.longitude != null && row.latitude != null" class="coord">
            {{ fmtCoord(row.longitude) }}, {{ fmtCoord(row.latitude) }}
          </span>
          <span v-else class="text-muted">未标注</span>
        </template>
      </el-table-column>
      <el-table-column label="地图" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openMap(row)">查看地图</el-button>
          <el-button v-if="store.isAdmin" type="primary" link @click="openVenue(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      class="pager"
      background
      layout="prev, pager, next, total"
      :total="table.total"
      v-model:current-page="query.current"
      :page-size="query.size"
      @current-change="load"
    />
    <el-dialog v-model="vDlg" :title="venueEditId ? '编辑考场' : '新增考场'" width="560px" destroy-on-close @closed="onVenueDlgClosed">
      <el-form :model="vForm" label-width="96px">
        <el-form-item label="名称"><el-input v-model="vForm.name" /></el-form-item>
        <el-form-item label="地址">
          <el-input v-model="vForm.address" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="坐标">
          <div class="coord-row">
            <el-input-number v-model="vForm.longitude" :precision="6" :step="0.000001" controls-position="right" placeholder="经度" />
            <el-input-number v-model="vForm.latitude" :precision="6" :step="0.000001" controls-position="right" placeholder="纬度" />
            <el-button :loading="geoLoading" @click="fillCoordsFromAddress">按地址解析</el-button>
          </div>
        </el-form-item>
        <el-form-item label="电话"><el-input v-model="vForm.phone" /></el-form-item>
        <el-form-item label="区县"><el-input v-model="vForm.region" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="vForm.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="vDlg = false">取消</el-button>
        <el-button type="primary" @click="saveVenue">保存</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="rDlg" :title="rForm.id ? '编辑线路' : '添加线路'" width="560px" destroy-on-close>
      <el-form :model="rForm" label-width="108px">
        <el-form-item label="线路名"><el-input v-model="rForm.routeName" /></el-form-item>
        <el-form-item label="示意图URL">
          <el-input v-model="rForm.mapImageUrl" placeholder="可选，静态示意图地址" />
        </el-form-item>
        <el-form-item label="路线坐标JSON">
          <el-input
            v-model="rForm.routePathJson"
            type="textarea"
            :rows="4"
            placeholder="路径规划锚点 [[lng,lat],...]，至少 2 个点；途经点将作为驾车途经点参与规划"
          />
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="rForm.routeDesc" type="textarea" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="rForm.sortOrder" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rDlg = false">取消</el-button>
        <el-button type="primary" @click="saveRoute">保存</el-button>
      </template>
    </el-dialog>
    <el-dialog
      v-model="mapDlg"
      :title="mapTarget ? `地图 · ${mapTarget.name}` : '地图'"
      width="min(920px, 96vw)"
      destroy-on-close
      class="map-dialog"
      @closed="destroyMap"
    >
      <div ref="mapContainerRef" class="amap-box" />
      <div v-if="mapHint" class="map-hint">{{ mapHint }}</div>
    </el-dialog>
    <el-dialog
      v-model="routeMapDlg"
      :title="routeMapRoute ? `路线 · ${routeMapRoute.routeName}` : '路线地图'"
      width="min(920px, 96vw)"
      destroy-on-close
      @closed="destroyRouteMap"
    >
      <div ref="routeMapContainerRef" class="amap-box" />
      <div v-if="routeMapHint" class="map-hint">{{ routeMapHint }}</div>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  venuesPage,
  venueRoutes,
  venueCreate,
  venueUpdate,
  routeCreate,
  routeUpdate,
  routeDelete,
  routePlannedPath,
  venueGeocode,
} from '@/api/biz'
import { useUserStore } from '@/stores/user'
import { loadAmapScript } from '@/utils/amapLoader'

const store = useUserStore()
const loading = ref(false)
const geoLoading = ref(false)
const query = reactive({ current: 1, size: 10, keyword: '' })
const table = reactive({ records: [], total: 0 })
const routesMap = reactive({})
const vDlg = ref(false)
const venueEditId = ref(null)
const vForm = reactive({
  name: '',
  address: '',
  phone: '',
  region: '',
  remark: '',
  longitude: undefined,
  latitude: undefined,
})
const rDlg = ref(false)
const rForm = reactive({
  id: null,
  venueId: null,
  routeName: '',
  mapImageUrl: '',
  routePathJson: '',
  routeDesc: '',
  sortOrder: 0,
})

const routeMapDlg = ref(false)
const routeMapRoute = ref(null)
const routeMapContainerRef = ref(null)
const routeMapHint = ref('')
let routeMapInstance = null
let routePolyline = null
const routeMarkers = []

const mapDlg = ref(false)
const mapTarget = ref(null)
const mapContainerRef = ref(null)
const mapHint = ref('')
let mapInstance = null
let mapMarker = null

function fmtCoord(v) {
  if (v == null || v === '') return '—'
  return Number(v).toFixed(4)
}

async function load() {
  loading.value = true
  try {
    const res = await venuesPage(query)
    table.records = res.records
    table.total = res.total
  } finally {
    loading.value = false
  }
}

async function onExpand(row, expandedRows) {
  if (expandedRows.some((r) => r.id === row.id)) {
    routesMap[row.id] = await venueRoutes(row.id)
  }
}

function onVenueDlgClosed() {
  venueEditId.value = null
}

function openVenue(row) {
  if (row && row.id) {
    venueEditId.value = row.id
    Object.assign(vForm, {
      name: row.name || '',
      address: row.address || '',
      phone: row.phone || '',
      region: row.region || '',
      remark: row.remark || '',
      longitude: row.longitude != null ? Number(row.longitude) : undefined,
      latitude: row.latitude != null ? Number(row.latitude) : undefined,
    })
  } else {
    venueEditId.value = null
    Object.assign(vForm, {
      name: '',
      address: '',
      phone: '',
      region: '',
      remark: '',
      longitude: undefined,
      latitude: undefined,
    })
  }
  vDlg.value = true
}

async function fillCoordsFromAddress() {
  if (!vForm.address || !vForm.address.trim()) {
    ElMessage.warning('请先填写地址')
    return
  }
  geoLoading.value = true
  try {
    const pt = await venueGeocode(vForm.address.trim())
    vForm.longitude = Number(pt.longitude)
    vForm.latitude = Number(pt.latitude)
    ElMessage.success(pt.formattedAddress ? `已解析：${pt.formattedAddress}` : '坐标已更新')
  } finally {
    geoLoading.value = false
  }
}

async function saveVenue() {
  if (venueEditId.value) {
    await venueUpdate(venueEditId.value, vForm)
  } else {
    await venueCreate(vForm)
  }
  ElMessage.success('已保存')
  vDlg.value = false
  load()
}

function openRoute(venue, existing = null) {
  if (existing) {
    Object.assign(rForm, {
      id: existing.id,
      venueId: venue.id,
      routeName: existing.routeName || '',
      mapImageUrl: existing.mapImageUrl || '',
      routePathJson: existing.routePathJson || '',
      routeDesc: existing.routeDesc || '',
      sortOrder: existing.sortOrder ?? 0,
    })
  } else {
    Object.assign(rForm, {
      id: null,
      venueId: venue.id,
      routeName: '',
      mapImageUrl: '',
      routePathJson: '',
      routeDesc: '',
      sortOrder: 0,
    })
  }
  rDlg.value = true
}

function hasRoutePath(r) {
  if (!r?.routePathJson?.trim()) return false
  try {
    const p = JSON.parse(r.routePathJson)
    if (!Array.isArray(p) || p.length < 2) return false
    let ok = 0
    for (const pt of p) {
      if (Array.isArray(pt) && pt.length >= 2 && Number.isFinite(Number(pt[0])) && Number.isFinite(Number(pt[1]))) ok++
    }
    return ok >= 2
  } catch {
    return false
  }
}

async function saveRoute() {
  if (rForm.routePathJson?.trim()) {
    try {
      const arr = JSON.parse(rForm.routePathJson)
      if (!Array.isArray(arr) || arr.length < 2) throw new Error('need 2+')
    } catch {
      ElMessage.error('路径规划锚点须为至少 2 个点的 JSON 数组，例如 [[lng,lat],[lng,lat]]')
      return
    }
  }
  if (rForm.id) {
    await routeUpdate(rForm.id, rForm)
  } else {
    await routeCreate(rForm.venueId, rForm)
  }
  ElMessage.success('已保存')
  rDlg.value = false
  routesMap[rForm.venueId] = await venueRoutes(rForm.venueId)
}

function destroyRouteMap() {
  routeMarkers.forEach((m) => {
    try {
      m.setMap(null)
    } catch (_) {}
  })
  routeMarkers.length = 0
  if (routePolyline) {
    try {
      routePolyline.setMap(null)
    } catch (_) {}
    routePolyline = null
  }
  if (routeMapInstance) {
    routeMapInstance.destroy()
    routeMapInstance = null
  }
  routeMapHint.value = ''
}

async function openRouteMap(route) {
  routeMapRoute.value = route
  routeMapDlg.value = true
  routeMapHint.value = '正在请求高德路径规划…'
  await nextTick()
  let plan
  try {
    plan = await routePlannedPath(route.id)
  } catch {
    routeMapHint.value = '路径规划请求失败，请检查 Web 服务 Key 与配额'
    return
  }
  const normalized = (plan.path || [])
    .map((p) => [Number(p[0]), Number(p[1])])
    .filter((x) => Number.isFinite(x[0]) && Number.isFinite(x[1]))
  if (normalized.length < 2) {
    routeMapHint.value = '未获得有效路线，请检查锚点或稍后再试'
    return
  }
  const amapKey = import.meta.env.VITE_AMAP_KEY
  const amapSec = import.meta.env.VITE_AMAP_SECURITY_JS_CODE
  try {
    await loadAmapScript(amapKey, amapSec)
  } catch (e) {
    routeMapHint.value = e.message || '地图加载失败'
    return
  }
  destroyRouteMap()
  const AMap = window.AMap
  const center = normalized[Math.floor(normalized.length / 2)]
  routeMapInstance = new AMap.Map(routeMapContainerRef.value, {
    zoom: 16,
    center,
    viewMode: '2D',
  })
  routePolyline = new AMap.Polyline({
    path: normalized,
    strokeColor: '#1450aa',
    strokeWeight: 6,
    strokeOpacity: 0.92,
    lineJoin: 'round',
    lineCap: 'round',
    map: routeMapInstance,
  })
  routeMarkers.push(
    new AMap.Marker({ position: normalized[0], title: '起点', map: routeMapInstance }),
    new AMap.Marker({ position: normalized[normalized.length - 1], title: '终点', map: routeMapInstance }),
  )
  routeMapInstance.setFitView([routePolyline], false, [56, 56, 56, 56])
  const modeLabel = {
    DRIVING: '驾车规划',
    WALKING_SEGMENT: '步行分段',
    ANCHOR_STRAIGHT: '锚点直连',
  }
  const bits = [modeLabel[plan.planMode] || plan.planMode]
  if (plan.distanceMeters != null && plan.distanceMeters > 0) {
    bits.push(plan.distanceMeters >= 1000 ? `约 ${(plan.distanceMeters / 1000).toFixed(1)} km` : `约 ${plan.distanceMeters} m`)
  }
  if (plan.durationSeconds != null && plan.durationSeconds > 0) {
    const m = Math.round(plan.durationSeconds / 60)
    bits.push(`约 ${m} 分钟`)
  }
  routeMapHint.value = bits.filter(Boolean).join(' · ')
}

async function delRoute(id) {
  await routeDelete(id)
  ElMessage.success('已删除')
  load()
}

function destroyMap() {
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }
  mapMarker = null
  mapHint.value = ''
}

async function openMap(row) {
  mapTarget.value = row
  mapDlg.value = true
  mapHint.value = '正在加载地图…'
  await nextTick()
  let lng = row.longitude != null ? Number(row.longitude) : NaN
  let lat = row.latitude != null ? Number(row.latitude) : NaN
  if ((Number.isNaN(lng) || Number.isNaN(lat)) && row.address) {
    try {
      const pt = await venueGeocode(row.address)
      lng = Number(pt.longitude)
      lat = Number(pt.latitude)
    } catch {
      mapHint.value = '无法根据地址解析坐标，请由管理员在考场信息中补全坐标。'
      return
    }
  }
  if (Number.isNaN(lng) || Number.isNaN(lat)) {
    mapHint.value = '该考场暂无坐标与可用地址，无法展示地图。'
    return
  }
  const amapKey = import.meta.env.VITE_AMAP_KEY
  const amapSec = import.meta.env.VITE_AMAP_SECURITY_JS_CODE
  try {
    await loadAmapScript(amapKey, amapSec)
  } catch (e) {
    mapHint.value = e.message || '地图加载失败'
    return
  }
  destroyMap()
  const AMap = window.AMap
  mapInstance = new AMap.Map(mapContainerRef.value, {
    zoom: 16,
    center: [lng, lat],
    viewMode: '2D',
  })
  mapMarker = new AMap.Marker({ position: [lng, lat], map: mapInstance })
  mapHint.value = ''
}

onMounted(load)
</script>

<style scoped>
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}
.page-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--ydt-text);
}
.page-desc {
  margin-top: 4px;
  font-size: 13px;
  color: var(--ydt-text-secondary);
  line-height: 1.5;
  max-width: 520px;
}
.toolbar-row {
  margin-bottom: 16px;
}
.search-input {
  max-width: 360px;
}
.routes {
  padding: 12px 24px;
  background: var(--ydt-surface-muted);
  border-radius: 8px;
}
.mt {
  margin-top: 8px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.coord {
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}
.text-muted {
  color: var(--ydt-text-muted);
  font-size: 13px;
}
.coord-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.amap-box {
  width: 100%;
  height: 420px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--ydt-border);
}
.map-hint {
  margin-top: 8px;
  font-size: 13px;
  color: var(--ydt-text-secondary);
}
</style>
