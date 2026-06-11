<template>
  <div class="notice-detail-page">
    <header class="topbar">
      <div class="brand" @click="$router.push('/home')">
        <div class="brand-mark">C</div>
        <div>
          <h1>校园服务平台</h1>
          <p>公告详情</p>
        </div>
      </div>

      <div class="top-actions">
        <el-button :icon="ArrowLeft" @click="$router.push('/notice')">返回公告</el-button>
        <el-button :icon="ArrowLeft" @click="$router.back()">返回上一页</el-button>
        <el-button
          v-if="userStore.isAdmin && notice"
          type="warning"
          plain
          :loading="topLoading"
          @click="handleToggleTop"
        >
          {{ notice.isTop === 1 ? '取消置顶' : '置顶公告' }}
        </el-button>
      </div>
    </header>

    <main class="detail-main" v-loading="loading">
      <el-empty v-if="!loading && !notice" description="公告不存在" />

      <article v-else class="notice-article">
        <div class="notice-meta">
          <el-tag v-if="notice.isTop === 1" size="small" type="warning">置顶</el-tag>
          <span class="notice-date">{{ formatTime(notice.createTime) }}</span>
        </div>
        <h2>{{ notice.title }}</h2>
        <div class="notice-content">{{ notice.content || '暂无公告内容' }}</div>
      </article>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getNoticeDetailApi, toggleNoticeTopApi } from '../api/notice'
import { useUserStore } from '../stores/user'
import { formatBeijingDateTime } from '../utils/time'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const topLoading = ref(false)
const notice = ref(null)

const formatTime = value => {
  return formatBeijingDateTime(value)
}

const loadNotice = async () => {
  loading.value = true
  try {
    const res = await getNoticeDetailApi(route.params.id)
    notice.value = res.data || null
  } finally {
    loading.value = false
  }
}

const handleToggleTop = async () => {
  if (!notice.value?.id) return
  topLoading.value = true
  try {
    await toggleNoticeTopApi(notice.value.id)
    notice.value.isTop = notice.value.isTop === 1 ? 0 : 1
    ElMessage.success(notice.value.isTop === 1 ? '公告已置顶' : '已取消置顶')
  } finally {
    topLoading.value = false
  }
}

onMounted(loadNotice)
</script>

<style scoped>
.notice-detail-page {
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

.brand,
.top-actions {
  display: flex;
  align-items: center;
}

.brand {
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
}

.brand p {
  margin: 3px 0 0;
  color: var(--muted);
  font-size: 12px;
}

.detail-main {
  width: min(880px, calc(100% - 44px));
  margin: 0 auto;
  padding: 28px 0 44px;
}

.notice-article {
  padding: 32px;
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.notice-date {
  color: var(--muted);
  font-size: 13px;
}

.notice-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notice-article h2 {
  margin: 12px 0 20px;
  color: var(--primary-deep);
  font-size: 32px;
  line-height: 1.25;
}

.notice-content {
  color: var(--text);
  line-height: 1.9;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>

