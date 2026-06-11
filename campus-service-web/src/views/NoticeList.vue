<template>
  <div class="notice-page">
    <header class="topbar">
      <div class="brand" @click="$router.push('/home')">
        <div class="brand-mark">C</div>
        <div>
          <h1>校园服务平台</h1>
          <p>校园公告</p>
        </div>
      </div>

      <div class="top-actions">
        <el-button :icon="ArrowLeft" @click="$router.push('/home')">返回首页</el-button>
        <el-button :icon="ArrowLeft" @click="$router.back()">返回上一页</el-button>
      </div>
    </header>

    <main class="notice-main">
      <section class="hero">
        <p class="eyebrow">Notice Board</p>
        <h2>校园公告</h2>
        <p>查看管理员发布的校园通知、活动安排和系统公告。</p>
      </section>

      <section class="notice-panel" v-loading="loading">
        <el-empty v-if="!loading && notices.length === 0" description="暂无公告" />

        <article
          v-for="notice in notices"
          :key="notice.id"
          class="notice-card"
          @click="$router.push(`/notice/${notice.id}`)"
        >
          <div>
            <div class="notice-meta">
              <el-tag v-if="notice.isTop === 1" size="small" type="warning">置顶</el-tag>
              <span class="notice-date">{{ formatTime(notice.createTime) }}</span>
            </div>
            <h3>{{ notice.title }}</h3>
            <p>{{ notice.content || '暂无公告内容' }}</p>
          </div>
          <el-button link type="primary">查看详情</el-button>
        </article>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getPublishedNoticeListApi } from '../api/notice'
import { formatBeijingDateTime } from '../utils/time'

const loading = ref(false)
const notices = ref([])

const formatTime = value => {
  return formatBeijingDateTime(value)
}

const loadNotices = async () => {
  loading.value = true
  try {
    const res = await getPublishedNoticeListApi()
    notices.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(loadNotices)
</script>

<style scoped>
.notice-page {
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

.notice-main {
  width: min(960px, calc(100% - 44px));
  margin: 0 auto;
  padding: 28px 0 44px;
}

.hero,
.notice-panel,
.notice-card {
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.hero {
  padding: 30px;
  background: var(--hero-gradient), var(--primary);
  color: #fff;
}

.eyebrow {
  margin: 0 0 10px;
  font-size: 13px;
  opacity: 0.82;
  font-weight: 700;
}

.hero h2 {
  margin: 0;
  font-size: 32px;
}

.hero p {
  margin: 10px 0 0;
  opacity: 0.86;
}

.notice-panel {
  display: grid;
  gap: 14px;
  margin-top: 20px;
  padding: 20px;
}

.notice-card {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
  padding: 20px;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease;
}

.notice-card:hover {
  transform: translateY(-2px);
  border-color: var(--primary);
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

.notice-card h3 {
  margin: 8px 0;
  color: var(--primary-deep);
  font-size: 20px;
}

.notice-card p {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: var(--muted);
  line-height: 1.7;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>

