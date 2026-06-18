<template>
  <div class="publish-page">
    <header class="topbar">
      <div class="brand" @click="$router.push('/home')">
        <div class="brand-mark">C</div>
        <div>
          <h1>校园服务平台</h1>
          <p>发布内容</p>
        </div>
      </div>

      <div class="top-actions">
        <el-button :icon="ArrowLeft" @click="$router.back()">
          返回
        </el-button>
        <el-button type="primary" :loading="loading" :icon="Check" @click="handleSubmit">
          提交发布
        </el-button>
      </div>
    </header>

    <main class="publish-main">
      <section class="editor-panel">
        <div class="panel-head">
          <p class="eyebrow">Publish</p>
          <h2>发布校园内容</h2>
          <p>填写清晰的标题和正文，选择准确分类，提交后等待管理员审核。</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="publish-form"
        >
          <el-form-item label="标题" prop="title">
            <el-input
              v-model="form.title"
              size="large"
              maxlength="80"
              show-word-limit
              placeholder="例如：图书馆三楼捡到校园卡一张"
              clearable
            />
          </el-form-item>

          <el-form-item v-if="!isTradePublish" label="分类" prop="categoryId">
            <el-select
              v-model="form.categoryId"
              size="large"
              placeholder="请选择分类"
              class="full-select"
              filterable
            >
              <el-option
                v-for="item in selectableCategories"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item v-if="isActivityCategory" label="活动通知分类">
            <el-select
              v-model="form.activitySubCategoryId"
              size="large"
              placeholder="请选择活动通知分类"
              class="full-select"
              clearable
              filterable
            >
              <el-option label="不选择" :value="null" />
              <el-option
                v-for="item in activitySubCategories"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item v-if="!isTradeCategory" label="可见范围">
            <div class="privacy-field">
              <div>
                <strong>私密帖子</strong>
                <span>开启后仅你本人和管理员可以查看，不会出现在首页或二手交易列表。</span>
              </div>
              <el-switch
                v-model="form.isPrivate"
                :active-value="1"
                :inactive-value="0"
                active-text="私密"
                inactive-text="公开"
              />
            </div>
          </el-form-item>

          <el-form-item label="图片和视频">
            <input ref="mediaInput" class="hidden-input" type="file" accept="image/*,video/*" multiple @change="handleMediaChange" />
            <div class="media-uploader">
              <el-button :icon="Picture" @click="mediaInput?.click()">选择图片/视频</el-button>
              <span>支持多张图片和视频。</span>
            </div>
            <div v-if="mediaItems.length" class="media-preview-grid">
              <div v-for="(item, index) in mediaItems" :key="item.url" class="media-preview">
                <img v-if="item.type === 'image'" :src="item.url" alt="媒体预览" />
                <video v-else :src="item.url" muted controls />
                <button type="button" @click="removeMedia(index)">移除</button>
              </div>
            </div>
          </el-form-item>

          <div v-if="isTradeCategory" class="trade-fields">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" class="full-select" />
            </el-form-item>

            <el-form-item label="物品分类" prop="tradeType">
              <el-select v-model="form.tradeType" size="large" class="full-select" placeholder="请选择物品分类">
                <el-option v-for="item in tradeTypes" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>

            <el-form-item label="新旧程度" prop="tradeCondition">
              <el-select v-model="form.tradeCondition" size="large" class="full-select" placeholder="请选择新旧程度">
                <el-option v-for="item in tradeConditions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </div>

          <el-form-item label="正文内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="12"
              maxlength="3000"
              show-word-limit
              resize="none"
              placeholder="请详细描述内容，例如时间、地点、联系方式、注意事项等。"
            />
          </el-form-item>

          <div class="form-actions">
            <el-button size="large" @click="resetForm">
              重置
            </el-button>
            <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">
              提交发布
            </el-button>
          </div>
        </el-form>
      </section>

      <aside class="preview-panel">
        <div class="side-card">
          <h3>发布预览</h3>

          <div class="preview-card">
            <div class="preview-cover" v-if="mediaItems.length">
              <img v-if="mediaItems[0].type === 'image'" :src="mediaItems[0].url" alt="封面预览" />
              <video v-else :src="mediaItems[0].url" muted />
            </div>

            <div class="tag-stack">
              <el-tag :type="tagType">
                {{ selectedCategoryName || '未选择分类' }}
              </el-tag>
              <el-tag v-if="!isTradeCategory && form.isPrivate === 1" type="info">私密</el-tag>
            </div>

            <div v-if="isTradeCategory" class="preview-trade">
              <strong>￥{{ formatPrice(form.price) }}</strong>
              <span>{{ form.tradeType || '物品分类' }} · {{ form.tradeCondition || '新旧程度' }}</span>
            </div>

            <h4>{{ form.title || '这里会显示你的标题' }}</h4>
            <p>{{ form.content || '这里会显示正文摘要，帮助你确认发布后的展示效果。' }}</p>
          </div>
        </div>

        <div class="side-card">
          <h3>发布提示</h3>

          <div class="tips">
            <div>
              <el-icon><InfoFilled /></el-icon>
              <span>标题尽量具体，方便同学快速判断内容。</span>
            </div>
            <div>
              <el-icon><InfoFilled /></el-icon>
              <span>失物招领和交易内容请写清时间、地点和联系方式。</span>
            </div>
            <div>
              <el-icon><InfoFilled /></el-icon>
              <span>发布后默认进入审核流程，通过后会展示在首页。</span>
            </div>
          </div>
        </div>
      </aside>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Check,
  InfoFilled,
  Picture
} from '@element-plus/icons-vue'
import { addContentApi } from '../api/content'
import { getCategoryListApi } from '../api/category'
import { getActivitySubCategoryListApi } from '../api/activitySubCategory'
import { uploadMediaApi } from '../api/upload'

const router = useRouter()
const route = useRoute()

const formRef = ref()
const mediaInput = ref()
const loading = ref(false)
const categories = ref([])
const activitySubCategories = ref([])
const isTradePublish = computed(() => route.query.category === 'trade')
const mediaItems = ref([])

const form = reactive({
  title: '',
  content: '',
  coverImg: '',
  mediaUrls: '',
  categoryId: null,
  activitySubCategoryId: null,
  isPrivate: 0,
  price: null,
  tradeType: '',
  tradeCondition: '',
  tradeStatus: 0
})

const tradeTypes = ['教材资料', '数码电子', '生活用品', '运动户外', '服饰鞋包', '其他闲置']
const tradeConditions = ['全新', '几乎全新', '轻微使用', '明显使用', '功能正常']
const MAX_CONTENT_IMAGE_SIZE = 20 * 1024 * 1024
const MAX_CONTENT_VIDEO_SIZE = 2 * 1024 * 1024 * 1024

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 80, message: '标题长度应为 2 到 80 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入正文内容', trigger: 'blur' },
    { min: 5, max: 3000, message: '正文长度应为 5 到 3000 个字符', trigger: 'blur' }
  ],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }],
  tradeType: [{ required: true, message: '请选择物品分类', trigger: 'change' }],
  tradeCondition: [{ required: true, message: '请选择新旧程度', trigger: 'change' }]
}

const selectedCategoryName = computed(() => {
  return categories.value.find(item => item.id === form.categoryId)?.name || ''
})

const isTradeCategory = computed(() => selectedCategoryName.value === '二手交易')
const isActivityCategory = computed(() => selectedCategoryName.value === '活动通知')
const selectableCategories = computed(() => {
  if (isTradePublish.value) return categories.value
  return categories.value.filter(item => item.name !== '二手交易')
})

const tagType = computed(() => {
  const map = {
    失物招领: 'success',
    二手交易: 'warning',
    活动通知: 'primary',
    校园求助: 'danger',
    课程资料: 'info',
    其他服务: ''
  }

  return map[selectedCategoryName.value] || ''
})

const loadCategories = async () => {
  const res = await getCategoryListApi()
  categories.value = res.data || []
  if (isTradePublish.value) {
    const tradeCategory = categories.value.find(item => item.name === '二手交易')
    if (tradeCategory) form.categoryId = tradeCategory.id
  } else if (selectedCategoryName.value === '二手交易') {
    form.categoryId = null
  }
}

const loadActivitySubCategories = async () => {
  const res = await getActivitySubCategoryListApi()
  activitySubCategories.value = res.data || []
}

const resetForm = () => {
  formRef.value.resetFields()
  form.coverImg = ''
  form.activitySubCategoryId = null
  mediaItems.value = []
  form.tradeStatus = 0
}

const formatPrice = value => Number(value || 0).toFixed(2)

const getMediaType = url => /\.(mp4|webm|mov)$/i.test(url.split('?')[0]) ? 'video' : 'image'

const handleMediaChange = event => {
  const files = Array.from(event.target.files || [])
  const validFiles = files.filter(file => {
    if (file.type.startsWith('image/') && file.size > MAX_CONTENT_IMAGE_SIZE) {
      ElMessage.warning(`图片「${file.name}」超过20MB，未添加`)
      return false
    }
    if (file.type.startsWith('video/') && file.size > MAX_CONTENT_VIDEO_SIZE) {
      ElMessage.warning(`视频「${file.name}」超过2GB，未添加`)
      return false
    }
    return true
  })
  mediaItems.value.push(...validFiles.map(file => ({
    file,
    url: URL.createObjectURL(file),
    type: file.type.startsWith('video/') ? 'video' : 'image'
  })))
  event.target.value = ''
}

const removeMedia = index => {
  const item = mediaItems.value[index]
  if (item?.file && item.url) URL.revokeObjectURL(item.url)
  mediaItems.value.splice(index, 1)
}

const uploadMediaItems = async () => {
  const urls = []
  for (const item of mediaItems.value) {
    if (item.file) {
      const res = await uploadMediaApi(item.file)
      urls.push(res.data)
    } else {
      urls.push(item.url)
    }
  }
  return urls
}

const handleSubmit = async () => {
  await formRef.value.validate()

  await ElMessageBox.confirm(
    '确认提交后，内容将进入审核流程。',
    '提交发布',
    {
      confirmButtonText: '确认提交',
      cancelButtonText: '再检查一下',
      type: 'info'
    }
  )

  loading.value = true
  try {
    const mediaUrls = await uploadMediaItems()
    const coverImg = mediaUrls.find(url => getMediaType(url) === 'image') || ''
    await addContentApi({
      title: form.title,
      content: form.content,
      coverImg,
      mediaUrls: JSON.stringify(mediaUrls),
      categoryId: form.categoryId,
      activitySubCategoryId: isActivityCategory.value ? form.activitySubCategoryId : null,
      isPrivate: isTradeCategory.value ? 0 : form.isPrivate,
      price: isTradeCategory.value ? form.price : null,
      tradeType: isTradeCategory.value ? form.tradeType : '',
      tradeCondition: isTradeCategory.value ? form.tradeCondition : '',
      tradeStatus: isTradeCategory.value ? 0 : null
    })

    ElMessage.success('发布成功，等待审核')
    router.push(isTradeCategory.value ? '/trade' : '/home')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadActivitySubCategories()])
})
</script>

<style scoped>
.publish-page {
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

.publish-main {
  width: min(1180px, calc(100% - 44px));
  margin: 0 auto;
  padding: 28px 0 44px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 22px;
  align-items: start;
}

.editor-panel,
.side-card {
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.editor-panel {
  padding: 30px;
}

.panel-head {
  margin-bottom: 24px;
}

.eyebrow {
  margin: 0 0 10px;
  color: var(--primary);
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
}

.panel-head h2 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 32px;
}

.panel-head p {
  margin: 10px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.full-select {
  width: 100%;
}

.privacy-field {
  width: 100%;
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--surface-soft);
}

.privacy-field div {
  display: grid;
  gap: 4px;
}

.privacy-field strong {
  color: var(--primary-deep);
}

.privacy-field span {
  color: var(--muted);
  font-size: 13px;
  line-height: 1.6;
}

.tag-stack {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.trade-fields {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.hidden-input {
  display: none;
}

.media-uploader {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--muted);
}

.media-preview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.media-preview {
  position: relative;
  overflow: hidden;
  aspect-ratio: 1;
  border-radius: 14px;
  background: var(--surface-soft);
}

.media-preview img,
.media-preview video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-preview button {
  position: absolute;
  top: 8px;
  right: 8px;
  border: 0;
  border-radius: 999px;
  padding: 4px 8px;
  background: rgba(15, 23, 42, 0.72);
  color: #fff;
  cursor: pointer;
}

.preview-panel {
  display: grid;
  gap: 16px;
}

.side-card {
  padding: 20px;
}

.side-card h3 {
  margin: 0 0 16px;
  color: var(--primary-deep);
  font-size: 18px;
}

.preview-card {
  display: grid;
  gap: 12px;
}

.preview-cover {
  width: 100%;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  border-radius: 16px;
  background: var(--surface-soft);
}

.preview-cover img,
.preview-cover video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-card h4 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 18px;
  line-height: 1.45;
}

.preview-trade {
  display: grid;
  gap: 4px;
  padding: 12px;
  border-radius: 14px;
  background: rgba(245, 158, 11, 0.1);
}

.preview-trade strong {
  color: #dc2626;
  font-size: 24px;
}

.preview-trade span {
  color: var(--muted);
  font-size: 13px;
}

.preview-card p {
  margin: 0;
  color: var(--muted);
  line-height: 1.8;
  white-space: pre-wrap;
  display: -webkit-box;
  -webkit-line-clamp: 7;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tips {
  display: grid;
  gap: 12px;
}

.tips div {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  color: var(--muted);
  font-size: 14px;
  line-height: 1.7;
}

.tips .el-icon {
  margin-top: 3px;
  color: var(--primary);
}

@media (max-width: 980px) {
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

  .publish-main {
    grid-template-columns: 1fr;
  }

  .trade-fields {
    grid-template-columns: 1fr;
  }

  .media-preview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .publish-main {
    width: min(100% - 28px, 1180px);
    padding-top: 18px;
  }

  .editor-panel {
    padding: 22px;
  }

  .panel-head h2 {
    font-size: 26px;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions .el-button {
    width: 100%;
  }
}
</style>

