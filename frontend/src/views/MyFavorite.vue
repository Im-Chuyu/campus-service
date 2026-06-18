<template>
  <div class="favorite-page">
    <header class="topbar">
      <div class="brand" @click="$router.push('/home')">
        <div class="brand-mark">C</div>
        <div>
          <h1>校园服务平台</h1>
          <p>我的收藏</p>
        </div>
      </div>

      <div class="top-actions">
        <el-button :icon="ArrowLeft" @click="$router.push('/home')">
          返回首页
        </el-button>
        <el-button :icon="ArrowLeft" @click="$router.back()">
          返回上一页
        </el-button>
      </div>
    </header>

    <main class="favorite-main">
      <section class="hero-card">
        <p class="eyebrow">Favorite</p>
        <h2>我的收藏</h2>
        <p>统一查看你收藏过的内容，支持继续浏览和取消收藏。</p>
      </section>

      <section class="list-panel" v-loading="loading">
        <div class="panel-head">
          <h3>收藏列表</h3>
          <span>{{ list.length }} 条收藏</span>
        </div>

        <el-empty
          v-if="!loading && list.length === 0"
          description="你还没有收藏任何内容"
        />

        <div v-else class="favorite-list">
          <article
            v-for="item in list"
            :key="item.id"
            class="favorite-card"
          >
            <div class="cover" @click="goDetail(item.contentId)">
              <img
                v-if="item.coverImg"
                :src="item.coverImg"
                :alt="item.title"
              />
              <div v-else class="cover-placeholder">
                {{ getTitleText(item.title) }}
              </div>
            </div>

            <div class="card-body">
              <div class="card-top">
                <el-tag :type="getStatusType(item.status)">
                  {{ getStatusText(item.status) }}
                </el-tag>

                <span class="time">
                  收藏于 {{ formatTime(item.createTime) }}
                </span>
              </div>

              <h4 class="title" @click="goDetail(item.contentId)">
                {{ item.title }}
              </h4>

              <p class="summary">
                {{ item.content || '暂无内容摘要' }}
              </p>

              <div class="meta">
                <span>浏览 {{ item.viewCount || 0 }}</span>
                <span>点赞 {{ item.likeCount || 0 }}</span>
                <span>收藏 {{ item.favoriteCount || 0 }}</span>
                <span>评论 {{ item.commentCount || 0 }}</span>
              </div>

              <div class="actions">
                <el-button type="primary" plain @click="goDetail(item.contentId)">
                  查看详情
                </el-button>
                <el-button
                  type="danger"
                  plain
                  :loading="deletingId === item.contentId"
                  @click="handleCancelFavorite(item)"
                >
                  取消收藏
                </el-button>
              </div>
            </div>
          </article>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { deleteFavoriteApi, getMyFavoriteListApi } from '../api/favorite'
import { formatBeijingDateTime } from '../utils/time'

const router = useRouter()

const loading = ref(false)
const deletingId = ref(null)
const list = ref([])

const loadList = async () => {
  loading.value = true
  try {
    const res = await getMyFavoriteListApi()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const goDetail = contentId => {
  router.push(`/content/${contentId}`)
}

const getTitleText = title => {
  return (title || '收藏').slice(0, 2)
}

const getStatusText = status => {
  const map = {
    0: '待审核',
    1: '已通过',
    2: '已驳回',
    3: '已下架'
  }

  return map[status] || '未知状态'
}

const getStatusType = status => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info'
  }

  return map[status] || 'info'
}

const formatTime = value => {
  return formatBeijingDateTime(value)
}

const handleCancelFavorite = async item => {
  await ElMessageBox.confirm(
    `确定取消收藏《${item.title}》吗？`,
    '取消收藏',
    {
      confirmButtonText: '确认取消',
      cancelButtonText: '保留收藏',
      type: 'warning'
    }
  )

  deletingId.value = item.contentId
  try {
    await deleteFavoriteApi(item.contentId)
    list.value = list.value.filter(current => current.contentId !== item.contentId)
    ElMessage.success('已取消收藏')
  } finally {
    deletingId.value = null
  }
}

onMounted(loadList)
</script>

<style scoped>
.favorite-page {
  min-height: 100vh;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
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
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: var(--primary);
  color: #fff;
  font-weight: 800;
  font-size: 20px;
}

.brand h1 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 18px;
  line-height: 1.2;
}

.brand p {
  margin: 3px 0 0;
  color: var(--muted);
  font-size: 12px;
}

.favorite-main {
  width: min(1180px, calc(100% - 44px));
  margin: 0 auto;
  padding: 28px 0 44px;
}

.hero-card {
  padding: 28px 30px;
  border-radius: var(--radius);
  background: var(--hero-gradient), var(--primary);
  color: #fff;
  box-shadow: var(--shadow);
}

.eyebrow {
  margin: 0 0 10px;
  font-size: 13px;
  opacity: 0.82;
  font-weight: 700;
  text-transform: uppercase;
}

.hero-card h2 {
  margin: 0;
  font-size: 30px;
}

.hero-card p {
  margin: 10px 0 0;
  opacity: 0.88;
}

.list-panel {
  margin-top: 22px;
  padding: 28px;
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}

.panel-head h3 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 22px;
}

.panel-head span {
  color: var(--muted);
  font-size: 14px;
}

.favorite-list {
  display: grid;
  gap: 18px;
}

.favorite-card {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 18px;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid var(--border);
  background: var(--surface-soft);
}

.cover {
  width: 100%;
  aspect-ratio: 16 / 11;
  border-radius: 16px;
  overflow: hidden;
  background: var(--surface-soft);
  cursor: pointer;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  font-size: 28px;
  font-weight: 800;
  color: var(--primary-deep);
  background: var(--theme-tint-soft);
}

.card-body {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.time {
  color: var(--muted);
  font-size: 13px;
}

.title {
  margin: 14px 0 0;
  color: var(--primary-deep);
  font-size: 22px;
  line-height: 1.4;
  cursor: pointer;
}

.summary {
  margin: 12px 0 0;
  color: var(--muted);
  line-height: 1.8;
  white-space: pre-wrap;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: var(--muted);
  font-size: 13px;
}

.actions {
  margin-top: auto;
  padding-top: 18px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 980px) {
  .topbar {
    height: auto;
    padding: 16px 22px;
    align-items: flex-start;
    flex-direction: column;
  }

  .favorite-card {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .favorite-main {
    width: min(100% - 28px, 1180px);
    padding-top: 18px;
  }

  .hero-card,
  .list-panel {
    padding: 22px;
  }

  .panel-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .title {
    font-size: 18px;
  }

  .actions {
    flex-direction: column;
  }

  .actions .el-button {
    width: 100%;
  }
}
</style>

