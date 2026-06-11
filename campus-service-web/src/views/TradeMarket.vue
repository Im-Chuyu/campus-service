<template>
  <div class="trade-page">
    <header class="topbar">
      <div class="brand" @click="$router.push('/home')">
        <div class="brand-mark">C</div>
        <div>
          <h1>校园服务平台</h1>
          <p>二手交易</p>
        </div>
      </div>

      <div class="top-actions">
        <el-button :icon="ArrowLeft" @click="$router.push('/home')">返回首页</el-button>
        <el-button :icon="ArrowLeft" @click="$router.back()">返回上一页</el-button>
        <el-button type="primary" :icon="Plus" @click="goPublishTrade">发布闲置</el-button>
      </div>
    </header>

    <main class="trade-main">
      <section class="hero">
        <div>
          <p class="eyebrow">Campus Market</p>
          <h2>校园二手交易</h2>
          <p>发布闲置教材、数码配件、生活用品和校园周边。当前只做信息撮合，不涉及在线支付。</p>
        </div>
        <div class="hero-stat">
          <strong>{{ tradePage.total }}</strong>
          <span>在售/展示物品</span>
        </div>
      </section>

      <section class="filter-panel">
        <el-input
          v-model="query.title"
          size="large"
          placeholder="搜索物品名称或关键词"
          clearable
          @clear="reloadFirstPage"
          @keyup.enter="reloadFirstPage"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button :icon="Search" @click="reloadFirstPage">搜索</el-button>
          </template>
        </el-input>

        <el-select v-model="query.tradeType" size="large" placeholder="物品分类" @change="reloadFirstPage">
          <el-option label="全部分类" value="" />
          <el-option v-for="item in tradeTypes" :key="item" :label="item" :value="item" />
        </el-select>

        <el-select v-model="query.tradeStatus" size="large" placeholder="交易状态" @change="reloadFirstPage">
          <el-option label="全部状态" :value="null" />
          <el-option label="在售" :value="0" />
          <el-option label="已预定" :value="1" />
          <el-option label="已售出" :value="2" />
        </el-select>

        <div class="price-range">
          <el-input-number v-model="query.minPrice" :min="0" :precision="2" placeholder="最低价" controls-position="right" />
          <span>-</span>
          <el-input-number v-model="query.maxPrice" :min="0" :precision="2" placeholder="最高价" controls-position="right" />
        </div>
        <el-button class="filter-button" size="large" type="primary" :icon="Search" @click="reloadFirstPage">筛选</el-button>
      </section>

      <section class="trade-grid" v-loading="loading">
        <el-empty v-if="!loading && tradeList.length === 0" description="暂无二手交易内容" />

        <article v-for="item in tradeList" :key="item.id" class="trade-card" @click="$router.push(`/content/${item.id}`)">
          <div class="trade-cover">
            <template v-if="item.coverMedia">
              <img v-if="item.coverMedia.type === 'image'" :src="item.coverMedia.url" :alt="item.title" />
              <video v-else :src="item.coverMedia.url" muted preload="metadata" playsinline />
              <span v-if="item.coverMedia.type === 'video'" class="video-badge">视频</span>
            </template>
            <div v-else class="cover-fallback">
              <el-icon><Goods /></el-icon>
            </div>
            <el-tag class="status-tag" :type="getTradeStatusType(item.tradeStatus)">
              {{ getTradeStatusText(item.tradeStatus) }}
            </el-tag>
            <el-tag v-if="item.isTop === 1" class="top-tag" type="warning">置顶</el-tag>
          </div>

          <div class="trade-body">
            <div class="trade-price">￥{{ formatPrice(item.price) }}</div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.content }}</p>
            <div class="trade-meta">
              <el-tag size="small" type="warning">{{ item.tradeType || '未分类' }}</el-tag>
              <span>{{ item.tradeCondition || '成色未填' }}</span>
            </div>
            <div class="trade-foot">
              <span>{{ formatTime(item.createTime) }}</span>
              <span>{{ item.likeCount || 0 }} 赞 · {{ item.favoriteCount || 0 }} 收藏</span>
            </div>
          </div>
        </article>
      </section>

      <el-pagination
        class="trade-pagination"
        background
        layout="total, prev, pager, next, jumper"
        :total="tradePage.total"
        v-model:current-page="tradePage.page"
        :page-size="tradePage.pageSize"
        @current-change="handlePageChange"
      />
    </main>

    <el-dialog v-model="authDialogVisible" title="登录后继续使用" width="360px" class="auth-dialog">
      <p>游客只能浏览第一页二手交易内容。登录或注册后可以查看更多商品、发布闲置和联系卖家。</p>
      <template #footer>
        <el-button @click="router.push('/register')">注册账号</el-button>
        <el-button type="primary" @click="router.push('/login')">去登录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ArrowLeft, Goods, Plus, Search } from '@element-plus/icons-vue'
import { getCategoryListApi } from '../api/category'
import { getContentPageApi } from '../api/content'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { formatBeijingDate } from '../utils/time'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const authDialogVisible = ref(false)
const tradeList = ref([])
const tradeCategoryId = ref(null)
const tradeTypes = ['教材资料', '数码电子', '生活用品', '运动户外', '服饰鞋包', '其他闲置']
const tradePage = reactive({ page: 1, pageSize: 52, total: 0 })

const query = reactive({
  title: '',
  tradeType: '',
  tradeStatus: 0,
  minPrice: null,
  maxPrice: null
})

const formatPrice = value => {
  const number = Number(value || 0)
  return number.toFixed(2)
}

const formatTime = value => {
  return formatBeijingDate(value)
}

const getTradeStatusText = status => {
  const map = { 0: '在售', 1: '已预定', 2: '已售出' }
  return map[Number(status ?? 0)] || '在售'
}

const getTradeStatusType = status => {
  const map = { 0: 'success', 1: 'warning', 2: 'info' }
  return map[Number(status ?? 0)] || 'success'
}

const getMediaType = url => /\.(mp4|webm|mov)$/i.test(String(url).split('?')[0]) ? 'video' : 'image'

const getCover = item => {
  if (item.coverImg) return { url: item.coverImg, type: getMediaType(item.coverImg) }
  if (!item.mediaUrls) return null
  try {
    const list = JSON.parse(item.mediaUrls)
    const mediaList = Array.isArray(list) ? list.filter(Boolean) : []
    const image = mediaList.find(url => getMediaType(url) === 'image')
    const video = mediaList.find(url => getMediaType(url) === 'video')
    return image ? { url: image, type: 'image' } : (video ? { url: video, type: 'video' } : null)
  } catch {
    return item.mediaUrls ? { url: item.mediaUrls, type: getMediaType(item.mediaUrls) } : null
  }
}

const loadCategory = async () => {
  const res = await getCategoryListApi()
  tradeCategoryId.value = (res.data || []).find(item => item.name === '二手交易')?.id || null
}

const loadTrades = async () => {
  if (!tradeCategoryId.value) return
  if (!userStore.isLogin && tradePage.page > 1) {
    tradePage.page = 1
    showAuthDialog()
    return
  }

  loading.value = true
  try {
    const res = await getContentPageApi({
      title: query.title || undefined,
      categoryId: tradeCategoryId.value,
      status: 1,
      tradeType: query.tradeType || undefined,
      tradeStatus: query.tradeStatus ?? undefined,
      minPrice: query.minPrice ?? undefined,
      maxPrice: query.maxPrice ?? undefined,
      page: tradePage.page,
      pageSize: tradePage.pageSize
    })
    const data = res.data || {}
    tradePage.total = Number(data.total || 0)
    tradeList.value = (data.records || []).map(item => ({
      ...item,
      coverMedia: getCover(item)
    }))
  } finally {
    loading.value = false
  }
}

const reloadFirstPage = () => {
  tradePage.page = 1
  loadTrades()
}

const showAuthDialog = () => {
  authDialogVisible.value = true
}

const handlePageChange = page => {
  if (!userStore.isLogin && page > 1) {
    tradePage.page = 1
    showAuthDialog()
    return
  }
  loadTrades()
}

const goPublishTrade = () => {
  if (!userStore.isLogin) {
    showAuthDialog()
    return
  }
  router.push('/publish?category=trade')
}

onMounted(async () => {
  await loadCategory()
  await loadTrades()
})
</script>

<style scoped>
.trade-page {
  min-height: 100vh;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  height: 72px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  padding: 0 32px;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  backdrop-filter: blur(18px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.brand-mark {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: var(--primary);
  color: #fff;
  font-size: 20px;
  font-weight: 800;
}

.brand h1 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 18px;
}

.brand p {
  margin: 3px 0 0;
  color: var(--muted);
  font-size: 12px;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.trade-main {
  width: min(1180px, calc(100% - 44px));
  margin: 0 auto;
  padding: 28px 0 44px;
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: center;
  padding: 32px;
  border-radius: var(--radius);
  background: var(--hero-gradient), var(--primary);
  color: #fff;
  box-shadow: var(--shadow);
}

.eyebrow {
  margin: 0 0 10px;
  font-size: 13px;
  font-weight: 700;
  opacity: 0.82;
}

.hero h2 {
  margin: 0;
  font-size: 34px;
}

.hero p {
  margin: 12px 0 0;
  max-width: 680px;
  line-height: 1.8;
  opacity: 0.88;
}

.hero-stat {
  min-width: 140px;
  padding: 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.16);
  text-align: center;
}

.hero-stat strong {
  display: block;
  font-size: 32px;
}

.hero-stat span {
  font-size: 13px;
  opacity: 0.82;
}

.filter-panel {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) 150px 130px minmax(260px, 300px) 96px;
  gap: 12px;
  align-items: center;
  margin-top: 20px;
  padding: 16px;
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.price-range {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  gap: 8px;
  align-items: center;
}

.price-range span {
  color: var(--muted);
}

.price-range :deep(.el-input-number) {
  width: 100%;
}

.price-range :deep(.el-input-number__decrease),
.price-range :deep(.el-input-number__increase) {
  background: var(--surface-soft);
  border-color: var(--border);
  color: var(--muted);
}

.price-range :deep(.el-input-number__decrease:hover),
.price-range :deep(.el-input-number__increase:hover) {
  color: var(--primary);
}

.filter-button {
  width: 96px;
}

.trade-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
  margin-top: 20px;
}

.trade-pagination {
  width: 100%;
  justify-content: center;
  margin-top: 20px;
}

.trade-pagination :deep(.el-pagination) {
  justify-content: center;
}

.trade-card {
  overflow: hidden;
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.trade-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.12);
}

.trade-cover {
  position: relative;
  aspect-ratio: 4 / 3;
  background: var(--surface-soft);
}

.trade-cover img,
.trade-cover video,
.cover-fallback {
  width: 100%;
  height: 100%;
}

.trade-cover img,
.trade-cover video {
  object-fit: cover;
}

.trade-cover video {
  display: block;
  pointer-events: none;
}

.video-badge {
  position: absolute;
  left: 12px;
  top: 12px;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.62);
  color: #fff;
  font-size: 12px;
  backdrop-filter: blur(8px);
}

.cover-fallback {
  display: grid;
  place-items: center;
  color: #d97706;
  font-size: 46px;
  background: var(--theme-tint-soft);
}

.status-tag {
  position: absolute;
  top: 12px;
  right: 12px;
}

.top-tag {
  position: absolute;
  top: 12px;
  left: 12px;
}

.trade-body {
  padding: 18px;
}

.trade-price {
  color: #dc2626;
  font-size: 24px;
  font-weight: 800;
}

.trade-body h3 {
  margin: 8px 0;
  color: var(--primary-deep);
  font-size: 18px;
  line-height: 1.45;
}

.trade-body p {
  display: -webkit-box;
  height: 50px;
  margin: 0;
  overflow: hidden;
  color: var(--muted);
  line-height: 1.65;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.trade-meta,
.trade-foot {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.trade-meta {
  margin-top: 14px;
}

.trade-meta span,
.trade-foot {
  color: var(--muted);
  font-size: 13px;
}

.trade-foot {
  margin-top: 14px;
}

@media (max-width: 1024px) {
  .topbar,
  .hero {
    height: auto;
    align-items: flex-start;
    flex-direction: column;
    padding: 16px 22px;
  }

  .top-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .filter-panel {
    grid-template-columns: 1fr;
  }

  .trade-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-button {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .trade-grid {
    grid-template-columns: 1fr;
  }
}
</style>

