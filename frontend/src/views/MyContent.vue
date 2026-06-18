<template>
  <div class="my-page">
    <header class="topbar">
      <div class="brand" @click="$router.push('/home')">
        <div class="brand-mark">C</div>
        <div>
          <h1>校园服务平台</h1>
          <p>我的发布</p>
        </div>
      </div>

      <div class="top-actions">
        <el-button :icon="ArrowLeft" @click="$router.push('/home')">
          返回首页
        </el-button>
        <el-button :icon="ArrowLeft" @click="$router.back()">
          返回上一页
        </el-button>
        <el-button type="primary" :icon="EditPen" @click="$router.push('/publish')">
          发布内容
        </el-button>
      </div>
    </header>

    <main class="my-main">
      <section class="page-head">
        <div>
          <p class="eyebrow">My Posts</p>
          <h2>我的发布</h2>
          <p>查看你提交过的校园内容，以及当前审核状态。</p>
        </div>

        <el-button :icon="Refresh" @click="loadList">
          刷新
        </el-button>
      </section>

      <section class="filter-row">
        <button
          v-for="item in statusTabs"
          :key="item.value"
          class="status-pill"
          :class="{ active: currentStatus === item.value }"
          @click="currentStatus = item.value"
        >
          <span>{{ item.label }}</span>
          <strong>{{ getStatusCount(item.value) }}</strong>
        </button>
      </section>

      <section class="content-panel" v-loading="loading">
        <el-empty
          v-if="!loading && filteredList.length === 0"
          description="暂无发布内容"
        />

        <article
          v-for="item in filteredList"
          :key="item.id"
          class="post-card"
        >
          <div class="post-main">
            <div class="post-head">
              <el-tag :type="getStatusType(item.status)">
                {{ getStatusText(item.status) }}
              </el-tag>
              <el-tag v-if="item.isPrivate === 1" type="info">
                私密
              </el-tag>
              <span>{{ formatTime(item.createTime) }}</span>
            </div>

            <h3>{{ item.title }}</h3>
            <p>{{ item.content }}</p>

            <div v-if="item.auditReason && item.status === 2" class="audit-reason">
              <span>审核原因</span>
              <strong>{{ item.auditReason }}</strong>
            </div>

            <div class="meta-row">
              <span>
                <el-icon><View /></el-icon>
                {{ item.viewCount || 0 }}
              </span>
              <span>
                <el-icon><Pointer /></el-icon>
                {{ item.likeCount || 0 }}
              </span>
              <span>
                <el-icon><Star /></el-icon>
                {{ item.favoriteCount || 0 }}
              </span>
              <span>
                <el-icon><ChatDotRound /></el-icon>
                {{ item.commentCount || 0 }}
              </span>
            </div>
          </div>

          <div class="post-actions">
            <el-button @click="goDetail(item.id)">
              查看
            </el-button>
            <el-button type="primary" plain @click="goEdit(item.id)">
              编辑
            </el-button>
            <el-button type="danger" plain :loading="deletingId === item.id" @click="handleDelete(item)">
              删除
            </el-button>
          </div>
        </article>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  ChatDotRound,
  EditPen,
  Pointer,
  Refresh,
  Star,
  View
} from '@element-plus/icons-vue'
import { deleteContentApi, getMyContentListApi } from '../api/content'
import { formatBeijingDateTime } from '../utils/time'

const router = useRouter()

const loading = ref(false)
const deletingId = ref(null)
const list = ref([])
const currentStatus = ref('all')

const statusTabs = [
  { label: '全部', value: 'all' },
  { label: '待审核', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已拒绝', value: 2 },
  { label: '已下架', value: 3 }
]

const filteredList = computed(() => {
  if (currentStatus.value === 'all') {
    return list.value
  }

  return list.value.filter(item => item.status === currentStatus.value)
})

const getStatusCount = status => {
  if (status === 'all') {
    return list.value.length
  }

  return list.value.filter(item => item.status === status).length
}

const getStatusText = status => {
  const map = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '已下架'
  }

  return map[status] || '未知'
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

const loadList = async () => {
  loading.value = true

  try {
    const res = await getMyContentListApi()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const goDetail = id => {
  router.push(`/content/${id}`)
}

const goEdit = id => {
  router.push(`/content/edit/${id}`)
}


const handleDelete = async item => {
  await ElMessageBox.confirm(
    `确定删除「${item.title}」吗？删除后不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )

  deletingId.value = item.id
  try {
    await deleteContentApi(item.id)
    ElMessage.success('删除成功')
    await loadList()
  } finally {
    deletingId.value = null
  }
}

const notReady = () => {
  ElMessage.info('编辑功能下一步接入')
}

onMounted(loadList)
</script>

<style scoped>
.my-page {
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

.top-actions {
  display: flex;
  gap: 10px;
}

.my-main {
  width: min(1180px, calc(100% - 44px));
  margin: 0 auto;
  padding: 28px 0 44px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-end;
  padding: 30px;
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

.page-head h2 {
  margin: 0;
  font-size: 32px;
}

.page-head p {
  margin: 10px 0 0;
  opacity: 0.86;
}

.filter-row {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 20px 2px 2px;
}

.status-pill {
  height: 42px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
  padding: 0 16px;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: var(--surface);
  color: var(--muted);
  cursor: pointer;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.05);
}

.status-pill strong {
  min-width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  padding: 0 7px;
  border-radius: 999px;
  background: var(--theme-tint-soft);
  color: var(--primary-deep);
  font-size: 12px;
}

.status-pill.active,
.status-pill:hover {
  background: var(--theme-tint);
  color: var(--primary-deep);
  border-color: var(--primary);
}

.content-panel {
  min-height: 320px;
  display: grid;
  gap: 16px;
  margin-top: 20px;
}

.post-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  padding: 22px;
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.post-head,
.meta-row,
.post-actions {
  display: flex;
  align-items: center;
}

.post-head {
  gap: 12px;
}

.post-head span {
  color: var(--muted);
  font-size: 13px;
}

.post-card h3 {
  margin: 14px 0 8px;
  color: var(--primary-deep);
  font-size: 20px;
}

.post-card p {
  margin: 0;
  color: var(--muted);
  line-height: 1.8;
  white-space: pre-wrap;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta-row {
  margin-top: 18px;
  flex-wrap: wrap;
  gap: 12px;
  color: var(--muted);
  font-size: 13px;
}

.meta-row span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.audit-reason {
  display: grid;
  gap: 6px;
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(245, 158, 11, 0.08);
  border: 1px solid rgba(245, 158, 11, 0.18);
}

.audit-reason span {
  color: var(--muted);
  font-size: 12px;
}

.audit-reason strong {
  color: var(--text);
  line-height: 1.7;
  white-space: pre-wrap;
}

.post-actions {
  gap: 10px;
}

@media (max-width: 900px) {
  .topbar {
    height: auto;
    padding: 16px 22px;
    align-items: flex-start;
    flex-direction: column;
  }

  .top-actions {
    width: 100%;
  }

  .top-actions .el-button {
    flex: 1;
  }

  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .post-card {
    grid-template-columns: 1fr;
  }

  .post-actions {
    justify-content: flex-end;
    flex-wrap: wrap;
  }
}

@media (max-width: 640px) {
  .my-main {
    width: min(100% - 28px, 1180px);
    padding-top: 18px;
  }

  .page-head {
    padding: 22px;
  }

  .page-head h2 {
    font-size: 26px;
  }

  .post-actions {
    display: grid;
    grid-template-columns: 1fr;
  }

  .post-actions .el-button {
    width: 100%;
    margin-left: 0;
  }
}
</style>

