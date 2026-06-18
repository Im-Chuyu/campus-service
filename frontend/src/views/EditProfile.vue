<template>
  <div class="edit-page">
    <header class="topbar">
      <div class="brand" @click="handleCancel('/home')">
        <div class="brand-mark">C</div>
        <div>
          <h1>校园服务平台</h1>
          <p>资料修改</p>
        </div>
      </div>

      <div class="top-actions">
        <el-button size="default" :icon="ArrowLeft" @click="handleCancel('/home')">返回首页</el-button>
        <el-button size="default" :icon="ArrowLeft" @click="handleCancel()">返回上一页</el-button>
      </div>
    </header>

    <main class="edit-main" v-loading="loading">
      <section class="hero-card">
        <div class="hero-banner" :style="bannerStyle">
          <div class="banner-content">
            <div class="avatar-editor">
              <UserAvatar
                class="hero-avatar"
                :size="109"
                :src="form.avatar"
                :text="avatarText"
                :role-id="userStore.userInfo?.roleId"
              />
              <el-button size="small" plain @click="pickAvatar">修改头像</el-button>
              <span>{{ avatarFileName || '当前头像' }}</span>
            </div>
            <div>
              <p class="eyebrow">编辑资料</p>
              <h2>{{ previewName }}</h2>
              <p class="preview-signature">{{ previewSignature }}</p>
            </div>
          </div>
          <div class="banner-actions">
            <el-button size="small" plain @click="pickBackground">修改主页背景</el-button>
            <el-button size="small" plain @click="resetProfileBackground">设置默认</el-button>
            <span>{{ backgroundFileName || '当前主页背景' }}</span>
          </div>
        </div>
      </section>
      <input ref="avatarInput" class="hidden-input" type="file" accept="image/*" @change="handleAvatarChange" />
      <input ref="backgroundInput" class="hidden-input" type="file" accept="image/*" @change="handleBackgroundChange" />

      <div class="panel-grid">
        <section class="panel">
          <div class="panel-head">
            <h3>基础资料</h3>
            <p>修改昵称、性别和联系方式。</p>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <div class="form-grid">
              <el-form-item label="用户名" prop="username">
                <el-input
                  v-model="form.username"
                  size="large"
                  placeholder="请输入用户名"
                  maxlength="20"
                  show-word-limit
                  clearable
                />
                <div class="field-tip">用户名每个自然年最多可修改 3 次。</div>
              </el-form-item>

              <el-form-item label="昵称" prop="nickname">
                <el-input
                  v-model="form.nickname"
                  size="large"
                  placeholder="请输入昵称"
                  maxlength="16"
                  show-word-limit
                  clearable
                />
              </el-form-item>

              <el-form-item label="个性签名" prop="signature" class="span-2">
                <el-input
                  v-model="form.signature"
                  type="textarea"
                  :rows="3"
                  maxlength="50"
                  show-word-limit
                  placeholder="请输入个性签名"
                  clearable
                />
              </el-form-item>

              <el-form-item label="性别" prop="gender">
                <el-select v-model="form.gender" size="large" class="full-width" placeholder="请选择性别" clearable>
                  <el-option label="男" :value="1" />
                  <el-option label="女" :value="2" />
                  <el-option label="保密" :value="0" />
                </el-select>
              </el-form-item>

              <el-form-item label="手机号" prop="phone">
                <el-input v-model="form.phone" size="large" placeholder="请输入手机号" clearable />
              </el-form-item>

              <el-form-item label="手机号公开">
                <el-switch v-model="form.phoneVisible" :active-value="1" :inactive-value="0" active-text="公开" inactive-text="隐藏" />
              </el-form-item>

              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" size="large" placeholder="请输入邮箱" clearable />
              </el-form-item>

              <el-form-item label="邮箱公开">
                <el-switch v-model="form.emailVisible" :active-value="1" :inactive-value="0" active-text="公开" inactive-text="隐藏" />
              </el-form-item>

              <el-form-item label="微信号" prop="wechat">
                <el-input v-model="form.wechat" size="large" placeholder="请输入微信号" clearable />
              </el-form-item>

              <el-form-item label="微信公开">
                <el-switch v-model="form.wechatVisible" :active-value="1" :inactive-value="0" active-text="公开" inactive-text="隐藏" />
              </el-form-item>

              <el-form-item label="QQ号" prop="qq">
                <el-input v-model="form.qq" size="large" placeholder="请输入QQ号" clearable />
              </el-form-item>

              <el-form-item label="QQ公开">
                <el-switch v-model="form.qqVisible" :active-value="1" :inactive-value="0" active-text="公开" inactive-text="隐藏" />
              </el-form-item>
            </div>
          </el-form>
        </section>

        <section class="panel">
          <div class="panel-head">
            <h3>隐私与外观</h3>
            <p>可以开启或关闭私聊。头像和背景先支持本地预览。</p>
          </div>

          <div class="privacy-card">
            <div>
              <strong>私聊权限</strong>
              <span>关闭后，其他用户不能主动向你发起私聊。</span>
            </div>
            <el-switch v-model="form.privateChatEnabled" :active-value="1" :inactive-value="0" active-text="开启" inactive-text="关闭" />
          </div>

          <div class="theme-panel">
            <div class="theme-head">
              <strong>页面主题</strong>
              <span>选择后会立即应用到全站界面。</span>
            </div>
            <div class="theme-grid">
              <button
                v-for="theme in themes"
                :key="theme.key"
                type="button"
                class="theme-card"
                :class="{ active: form.themeKey === theme.key }"
                @click="selectTheme(theme.key)"
              >
                <span class="theme-swatch" :style="{ background: theme.colors.primary }"></span>
                <strong>{{ theme.name }}</strong>
                <small>{{ theme.description }}</small>
              </button>
            </div>
          </div>

          <div class="theme-panel">
            <div class="theme-head">
              <strong>全局背景</strong>
              <span>可以上传图片作为全站背景，只在当前浏览器本地生效。</span>
            </div>
            <div class="global-bg-panel">
              <input ref="globalBackgroundInput" class="hidden-input" type="file" accept="image/*" @change="handleGlobalBackgroundChange" />
              <div class="global-bg-preview" :style="globalBackgroundPreviewStyle">
                <span>{{ form.globalBackgroundUrl ? '自定义全局背景' : '系统默认纹理背景' }}</span>
              </div>
              <div class="global-bg-actions">
                <el-button size="small" plain @click="pickGlobalBackground">上传背景</el-button>
                <el-button size="small" plain @click="resetGlobalBackground">恢复默认</el-button>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <el-button @click="handleCancel('/profile')">取消</el-button>
            <el-button type="primary" :loading="saving" @click="handleSave()">保存修改</el-button>
          </div>
        </section>

        <section class="panel password-panel">
          <div class="panel-head">
            <h3>修改密码</h3>
            <p>通过已绑定手机号或邮箱接收验证码后修改登录密码。</p>
          </div>

          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top">
            <el-form-item label="接收方式" prop="channel">
              <el-segmented v-model="passwordForm.channel" :options="passwordChannelOptions" />
            </el-form-item>

            <el-form-item label="验证码" prop="code">
              <el-input v-model="passwordForm.code" size="large" maxlength="6" placeholder="请输入6位验证码" clearable>
                <template #append>
                  <el-button :loading="passwordCodeLoading" :disabled="passwordCodeCountdown > 0" @click="sendPasswordCode">
                    {{ passwordCodeCountdown > 0 ? `${passwordCodeCountdown}s` : '获取验证码' }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" size="large" type="password" show-password placeholder="8-20位，包含字母和数字" />
            </el-form-item>

            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" size="large" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>

            <div class="form-actions">
              <el-button type="primary" :loading="passwordSaving" @click="handleUpdatePassword">修改密码</el-button>
            </div>
          </el-form>
        </section>
      </div>
    </main>

    <el-dialog v-model="cropDialogVisible" :title="cropTitle" width="760px" class="crop-dialog" @closed="resetCropper">
      <div class="cropper-shell">
        <div
          class="crop-stage"
          @pointerdown="startCropDrag"
          @pointermove="onCropDrag"
          @pointerup="stopCropDrag"
          @pointercancel="stopCropDrag"
          @pointerleave="stopCropDrag"
        >
          <img
            v-if="cropImageUrl"
            class="crop-image"
            :src="cropImageUrl"
            :style="cropImageStyle"
            draggable="false"
          />
          <div class="crop-mask"></div>
          <div class="crop-frame" :style="cropFrameStyle"></div>
        </div>

        <div class="crop-controls">
          <span>缩放</span>
          <el-slider v-model="cropScale" :min="cropMinScale" :max="4" :step="0.01" />
        </div>
      </div>
      <template #footer>
        <el-button @click="cropDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCrop">确认使用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { sendCodeApi, updatePasswordApi, updateUserApi } from '../api/user'
import { uploadImageApi } from '../api/upload'
import { getProfileExtra, saveProfileExtra } from '../utils/profileAppearance'
import UserAvatar from '../components/UserAvatar.vue'
import {
  applyAppearance,
  applyBackground,
  getSavedBackgroundBlobUrl,
  getSavedBackgroundUrl,
  getSavedBackgroundKey,
  getSavedThemeKey,
  previewAppearance,
  removeBackgroundBlob,
  saveBackgroundBlob,
  themes
} from '../utils/theme'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const passwordSaving = ref(false)
const passwordCodeLoading = ref(false)
const passwordCodeCountdown = ref(0)
let passwordCountdownTimer = null

const formRef = ref()
const passwordFormRef = ref()
const avatarInput = ref()
const backgroundInput = ref()
const globalBackgroundInput = ref()

const avatarFileName = ref('')
const backgroundFileName = ref('')
const avatarFile = ref(null)
const backgroundFile = ref(null)
const originalThemeKey = ref(getSavedThemeKey())
const originalBackgroundKey = ref(getSavedBackgroundKey())
const originalGlobalBackgroundUrl = ref(getSavedBackgroundUrl())
const initialSnapshot = ref('')
const allowRouteLeave = ref(false)
const MAX_UPLOAD_SIZE = 10 * 1024 * 1024
const MAX_SOURCE_DIMENSION = 2400
const GLOBAL_BACKGROUND_MAX_DIMENSION = 1440
const AVATAR_SIZE = 512
const BACKGROUND_WIDTH = 1600
const BACKGROUND_HEIGHT = 476

const cropDialogVisible = ref(false)
const cropMode = ref('avatar')
const cropImageUrl = ref('')
const cropImageElement = ref(null)
const cropFileName = ref('')
const cropScale = ref(1)
const cropMinScale = ref(1)
const cropOffset = reactive({ x: 0, y: 0 })
const cropDrag = reactive({ active: false, x: 0, y: 0, startX: 0, startY: 0 })

const passwordChannelOptions = [
  { label: '手机号', value: 'phone' },
  { label: '邮箱', value: 'email' }
]

const form = reactive({
  id: null,
  username: '',
  nickname: '',
  signature: '',
  phone: '',
  phoneVisible: 0,
  email: '',
  emailVisible: 0,
  wechat: '',
  wechatVisible: 0,
  qq: '',
  qqVisible: 0,
  privateChatEnabled: 1,
  gender: null,
  avatar: '',
  background: '',
  themeKey: getSavedThemeKey(),
  backgroundKey: getSavedBackgroundKey(),
  globalBackgroundUrl: getSavedBackgroundUrl()
})

const passwordForm = reactive({
  channel: 'phone',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const avatarText = computed(() => (form.nickname || userStore.userInfo?.username || 'U').slice(0, 1))
const previewName = computed(() => form.nickname.trim() || '未填写昵称')
const previewSignature = computed(() => form.signature.trim() || '还没有设置个性签名')
const bannerStyle = computed(() => (form.background ? { backgroundImage: `url(${form.background})` } : {}))
const globalBackgroundPreviewStyle = computed(() => {
  if (form.globalBackgroundUrl) {
    return { backgroundImage: `linear-gradient(180deg, rgba(15, 23, 42, 0.18), rgba(15, 23, 42, 0.24)), url(${form.globalBackgroundUrl})` }
  }
  return {}
})
const cropTitle = computed(() => (cropMode.value === 'avatar' ? '裁剪头像' : '裁剪主页背景'))
const cropFrame = computed(() => {
  if (cropMode.value === 'avatar') {
    return { width: 320, height: 320 }
  }
  return { width: 560, height: 167 }
})
const cropFrameStyle = computed(() => ({
  width: `${cropFrame.value.width}px`,
  height: `${cropFrame.value.height}px`,
  borderRadius: cropMode.value === 'avatar' ? '50%' : '16px'
}))
const cropImageStyle = computed(() => ({
  width: `${(cropImageElement.value?.naturalWidth || 0) * cropScale.value}px`,
  height: `${(cropImageElement.value?.naturalHeight || 0) * cropScale.value}px`,
  transform: `translate(calc(-50% + ${cropOffset.x}px), calc(-50% + ${cropOffset.y}px))`
}))

const buildSnapshot = () =>
  JSON.stringify({
    nickname: form.nickname,
    username: form.username,
    signature: form.signature,
    phone: form.phone,
    phoneVisible: form.phoneVisible,
    email: form.email,
    emailVisible: form.emailVisible,
    wechat: form.wechat,
    wechatVisible: form.wechatVisible,
    qq: form.qq,
    qqVisible: form.qqVisible,
    privateChatEnabled: form.privateChatEnabled,
    gender: form.gender,
    avatar: form.avatar,
    background: form.background,
    themeKey: form.themeKey,
    backgroundKey: form.backgroundKey,
    globalBackgroundUrl: form.globalBackgroundUrl,
    avatarFileName: avatarFileName.value,
    backgroundFileName: backgroundFileName.value
  })

const hasUnsavedChanges = () => Boolean(initialSnapshot.value) && buildSnapshot() !== initialSnapshot.value

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9_.&-]{3,19}$/,
      message: '用户名必须以字母开头，可包含字母、数字、下划线、短横线、点号和&，长度4-20位',
      trigger: 'blur'
    }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 16, message: '昵称长度不能超过16个字符', trigger: 'blur' }
  ],
  signature: [
    { max: 50, message: '个性签名不能超过50个字符', trigger: 'blur' }
  ],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ pattern: /^[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}$/, message: '邮箱格式不正确', trigger: 'blur' }]
}

const validatePasswordConfirm = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
    return
  }
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const passwordRules = {
  channel: [{ required: true, message: '请选择接收方式', trigger: 'change' }],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d_!@#$%^&*()\-+=]{8,20}$/,
      message: '密码必须包含字母和数字，长度8-20位',
      trigger: 'blur'
    }
  ],
  confirmPassword: [{ validator: validatePasswordConfirm, trigger: 'blur' }]
}

const fileToDataUrl = file =>
  new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result || ''))
    reader.onerror = reject
    reader.readAsDataURL(file)
  })

const loadImage = src =>
  new Promise((resolve, reject) => {
    const image = new Image()
    image.onload = () => resolve(image)
    image.onerror = reject
    image.src = src
  })

const canvasToDataUrl = (canvas, quality = 0.88) => {
  return canvas.toDataURL('image/jpeg', quality)
}

const canvasToBlob = (canvas, quality = 0.88) =>
  new Promise(resolve => {
    canvas.toBlob(blob => resolve(blob), 'image/jpeg', quality)
  })

const resizeImageIfNeeded = async file => {
  const dataUrl = await fileToDataUrl(file)
  const image = await loadImage(dataUrl)
  const largestSide = Math.max(image.naturalWidth, image.naturalHeight)
  if (file.size <= MAX_UPLOAD_SIZE && largestSide <= MAX_SOURCE_DIMENSION) {
    return dataUrl
  }

  const scale = Math.min(1, MAX_SOURCE_DIMENSION / largestSide)
  const canvas = document.createElement('canvas')
  canvas.width = Math.round(image.naturalWidth * scale)
  canvas.height = Math.round(image.naturalHeight * scale)
  const ctx = canvas.getContext('2d')
  ctx.drawImage(image, 0, 0, canvas.width, canvas.height)
  return canvasToDataUrl(canvas, 0.82)
}

const resizeGlobalBackground = async file => {
  const dataUrl = await fileToDataUrl(file)
  const image = await loadImage(dataUrl)
  const largestSide = Math.max(image.naturalWidth, image.naturalHeight)
  const scale = Math.min(1, GLOBAL_BACKGROUND_MAX_DIMENSION / largestSide)
  const canvas = document.createElement('canvas')
  canvas.width = Math.round(image.naturalWidth * scale)
  canvas.height = Math.round(image.naturalHeight * scale)
  const ctx = canvas.getContext('2d')
  ctx.drawImage(image, 0, 0, canvas.width, canvas.height)
  const blob = await canvasToBlob(canvas, 0.68)
  return blob || file
}

const clampCropOffset = () => {
  if (!cropImageElement.value) return
  const imageWidth = cropImageElement.value.naturalWidth * cropScale.value
  const imageHeight = cropImageElement.value.naturalHeight * cropScale.value
  const maxX = Math.max(0, (imageWidth - cropFrame.value.width) / 2)
  const maxY = Math.max(0, (imageHeight - cropFrame.value.height) / 2)
  cropOffset.x = Math.min(maxX, Math.max(-maxX, cropOffset.x))
  cropOffset.y = Math.min(maxY, Math.max(-maxY, cropOffset.y))
}

const openCropper = async (file, mode) => {
  cropMode.value = mode
  cropFileName.value = file.name
  cropImageUrl.value = await resizeImageIfNeeded(file)
  cropImageElement.value = await loadImage(cropImageUrl.value)

  const minScaleX = cropFrame.value.width / cropImageElement.value.naturalWidth
  const minScaleY = cropFrame.value.height / cropImageElement.value.naturalHeight
  cropMinScale.value = Math.max(minScaleX, minScaleY, 0.1)
  cropScale.value = cropMinScale.value
  cropOffset.x = 0
  cropOffset.y = 0
  cropDialogVisible.value = true
}

const resetCropper = () => {
  cropImageUrl.value = ''
  cropImageElement.value = null
  cropFileName.value = ''
  cropScale.value = 1
  cropOffset.x = 0
  cropOffset.y = 0
}

const startCropDrag = event => {
  event.preventDefault()
  event.currentTarget?.setPointerCapture?.(event.pointerId)
  cropDrag.active = true
  cropDrag.x = event.clientX
  cropDrag.y = event.clientY
  cropDrag.startX = cropOffset.x
  cropDrag.startY = cropOffset.y
}

const onCropDrag = event => {
  if (!cropDrag.active) return
  event.preventDefault()
  cropOffset.x = cropDrag.startX + event.clientX - cropDrag.x
  cropOffset.y = cropDrag.startY + event.clientY - cropDrag.y
  clampCropOffset()
}

const stopCropDrag = event => {
  event?.currentTarget?.releasePointerCapture?.(event.pointerId)
  cropDrag.active = false
}

watch(cropScale, clampCropOffset)

const confirmCrop = async () => {
  if (!cropImageElement.value) return

  const outputWidth = cropMode.value === 'avatar' ? AVATAR_SIZE : BACKGROUND_WIDTH
  const outputHeight = cropMode.value === 'avatar' ? AVATAR_SIZE : BACKGROUND_HEIGHT
  const canvas = document.createElement('canvas')
  canvas.width = outputWidth
  canvas.height = outputHeight
  const ctx = canvas.getContext('2d')

  const sourceWidth = cropFrame.value.width / cropScale.value
  const sourceHeight = cropFrame.value.height / cropScale.value
  const sourceX = (cropImageElement.value.naturalWidth - sourceWidth) / 2 - cropOffset.x / cropScale.value
  const sourceY = (cropImageElement.value.naturalHeight - sourceHeight) / 2 - cropOffset.y / cropScale.value

  ctx.drawImage(
    cropImageElement.value,
    sourceX,
    sourceY,
    sourceWidth,
    sourceHeight,
    0,
    0,
    outputWidth,
    outputHeight
  )

  const quality = cropMode.value === 'avatar' ? 0.9 : 0.84
  const result = canvasToDataUrl(canvas, quality)
  const blob = await canvasToBlob(canvas, quality)
  if (cropMode.value === 'avatar') {
    form.avatar = result
    avatarFileName.value = cropFileName.value
    avatarFile.value = blob ? new File([blob], 'avatar.jpg', { type: 'image/jpeg' }) : null
  } else {
    form.background = result
    backgroundFileName.value = cropFileName.value
    backgroundFile.value = blob ? new File([blob], 'background.jpg', { type: 'image/jpeg' }) : null
  }
  cropDialogVisible.value = false
}

const loadForm = async () => {
  loading.value = true
  try {
    const user = await userStore.getUserInfo()
    const extra = getProfileExtra(user.id)

    form.id = user.id
    form.username = user.username || ''
    form.nickname = user.nickname || ''
    form.signature = user.signature || ''
    form.phone = user.phone || ''
    form.phoneVisible = user.phoneVisible ?? 0
    form.email = user.email || ''
    form.emailVisible = user.emailVisible ?? 0
    form.wechat = user.wechat || ''
    form.wechatVisible = user.wechatVisible ?? 0
    form.qq = user.qq || ''
    form.qqVisible = user.qqVisible ?? 0
    form.privateChatEnabled = user.privateChatEnabled ?? 1
    form.gender = user.gender ?? null
    form.avatar = extra.avatar || user.avatar || ''
    form.background = user.profileBackground || extra.background || ''
    form.themeKey = extra.themeKey || getSavedThemeKey()
    form.backgroundKey = getSavedBackgroundKey()
    form.globalBackgroundUrl = form.backgroundKey === 'custom' ? await getSavedBackgroundBlobUrl() : getSavedBackgroundUrl()
    if (form.backgroundKey === 'custom' && !form.globalBackgroundUrl) {
      form.backgroundKey = 'system'
    }
    originalThemeKey.value = form.themeKey
    originalBackgroundKey.value = form.backgroundKey
    originalGlobalBackgroundUrl.value = form.globalBackgroundUrl
    previewAppearance({
      themeKey: form.themeKey,
      backgroundKey: form.backgroundKey,
      customUrl: form.globalBackgroundUrl
    })
    initialSnapshot.value = buildSnapshot()
  } finally {
    loading.value = false
  }
}

const pickAvatar = () => avatarInput.value?.click()
const pickBackground = () => backgroundInput.value?.click()
const pickGlobalBackground = () => globalBackgroundInput.value?.click()

const resetProfileBackground = () => {
  form.background = ''
  backgroundFile.value = null
  backgroundFileName.value = '主题默认背景'
}

const handleAvatarChange = async event => {
  const file = event.target.files?.[0]
  if (!file) return
  await openCropper(file, 'avatar')
  event.target.value = ''
}

const handleBackgroundChange = async event => {
  const file = event.target.files?.[0]
  if (!file) return
  await openCropper(file, 'background')
  event.target.value = ''
}

const handleGlobalBackgroundChange = async event => {
  const file = event.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    event.target.value = ''
    return
  }

  const backgroundBlob = await resizeGlobalBackground(file)
  const backgroundUrl = URL.createObjectURL(backgroundBlob)
  form.backgroundKey = 'custom'
  form.globalBackgroundUrl = backgroundUrl
  try {
    await saveBackgroundBlob(backgroundBlob)
    previewAppearance({
      themeKey: form.themeKey,
      backgroundKey: form.backgroundKey,
      customUrl: form.globalBackgroundUrl
    })
    applyBackground(form.backgroundKey, form.globalBackgroundUrl)
  } catch {
    form.backgroundKey = originalBackgroundKey.value
    form.globalBackgroundUrl = originalGlobalBackgroundUrl.value
    previewAppearance({
      themeKey: form.themeKey,
      backgroundKey: form.backgroundKey,
      customUrl: form.globalBackgroundUrl
    })
    ElMessage.error('背景图片过大，本地保存失败，请换一张图片')
    event.target.value = ''
    return
  }
  originalBackgroundKey.value = form.backgroundKey
  originalGlobalBackgroundUrl.value = form.globalBackgroundUrl
  initialSnapshot.value = buildSnapshot()
  ElMessage.success('全局背景已应用')
  event.target.value = ''
}

const selectTheme = key => {
  if (form.themeKey === key) return
  form.themeKey = key
  previewAppearance({
    themeKey: form.themeKey,
    backgroundKey: form.backgroundKey,
    customUrl: form.globalBackgroundUrl
  })
}

const resetGlobalBackground = async () => {
  form.backgroundKey = 'system'
  form.globalBackgroundUrl = ''
  await removeBackgroundBlob()
  previewAppearance({
    themeKey: form.themeKey,
    backgroundKey: form.backgroundKey,
    customUrl: form.globalBackgroundUrl
  })
  applyBackground(form.backgroundKey, form.globalBackgroundUrl)
  originalBackgroundKey.value = form.backgroundKey
  originalGlobalBackgroundUrl.value = form.globalBackgroundUrl
  initialSnapshot.value = buildSnapshot()
  ElMessage.success('已恢复系统默认背景')
}

const startPasswordCountdown = () => {
  passwordCodeCountdown.value = 60
  clearInterval(passwordCountdownTimer)
  passwordCountdownTimer = setInterval(() => {
    passwordCodeCountdown.value -= 1
    if (passwordCodeCountdown.value <= 0) {
      clearInterval(passwordCountdownTimer)
      passwordCountdownTimer = null
    }
  }, 1000)
}

const sendPasswordCode = async () => {
  const target = passwordForm.channel === 'phone' ? form.phone : form.email
  if (!target) {
    ElMessage.warning(passwordForm.channel === 'phone' ? '请先绑定手机号并保存资料' : '请先绑定邮箱并保存资料')
    return
  }
  passwordCodeLoading.value = true
  try {
    const res = await sendCodeApi({
      scene: 'change_password',
      channel: passwordForm.channel,
      target
    })
    await ElMessageBox.alert(res.msg || `验证码：${res.data}`, '验证码', {
      confirmButtonText: '知道了'
    })
    startPasswordCountdown()
  } finally {
    passwordCodeLoading.value = false
  }
}

const handleUpdatePassword = async () => {
  await passwordFormRef.value.validate()
  passwordSaving.value = true
  try {
    await updatePasswordApi(passwordForm)
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    allowRouteLeave.value = true
    router.push('/login')
  } finally {
    passwordSaving.value = false
  }
}

const restoreLocalAppearance = () => {
  previewAppearance({
    themeKey: originalThemeKey.value,
    backgroundKey: originalBackgroundKey.value,
    customUrl: originalGlobalBackgroundUrl.value
  })
}

const handleSave = async (target = '/profile') => {
  form.username = form.username.trim()
  form.nickname = form.nickname.trim()
  await formRef.value.validate()

  saving.value = true
  try {
    let avatarUrl = form.avatar
    let backgroundUrl = form.background

    if (avatarFile.value) {
      const res = await uploadImageApi(avatarFile.value)
      avatarUrl = res.data
      form.avatar = avatarUrl
      avatarFile.value = null
    }

    if (backgroundFile.value) {
      const res = await uploadImageApi(backgroundFile.value)
      backgroundUrl = res.data
      form.background = backgroundUrl
      backgroundFile.value = null
    }

    await updateUserApi({
      id: form.id,
      username: form.username,
      nickname: form.nickname,
      signature: form.signature,
      phone: form.phone,
      phoneVisible: form.phoneVisible,
      email: form.email,
      emailVisible: form.emailVisible,
      wechat: form.wechat,
      wechatVisible: form.wechatVisible,
      qq: form.qq,
      qqVisible: form.qqVisible,
      privateChatEnabled: form.privateChatEnabled,
      gender: form.gender,
      avatar: avatarUrl,
      profileBackground: backgroundUrl
    })

    saveProfileExtra(form.id, {
      avatar: avatarUrl,
      background: '',
      themeKey: form.themeKey
    })
    applyAppearance({
      themeKey: form.themeKey,
      backgroundKey: form.backgroundKey,
      customUrl: form.globalBackgroundUrl
    })
    originalThemeKey.value = form.themeKey
    originalBackgroundKey.value = form.backgroundKey
    originalGlobalBackgroundUrl.value = form.globalBackgroundUrl
    avatarFileName.value = ''
    backgroundFileName.value = ''
    initialSnapshot.value = buildSnapshot()

    await userStore.getUserInfo()
    ElMessage.success('保存成功')
    if (target) {
      allowRouteLeave.value = true
      router.push(target)
    }
  } finally {
    saving.value = false
  }
}

const handleCancel = target => {
  if (target) {
    router.push(target)
    return
  }
  router.back()
}

onBeforeRouteLeave(async () => {
  if (allowRouteLeave.value || !hasUnsavedChanges()) return true

  try {
    await ElMessageBox.confirm('资料修改尚未保存，是否先保存再离开？', '未保存的修改', {
      confirmButtonText: '保存后离开',
      cancelButtonText: '不保存离开',
      distinguishCancelAndClose: true,
      type: 'warning'
    })
    await handleSave(null)
    return true
  } catch (action) {
    if (action === 'cancel') {
      restoreLocalAppearance()
      return true
    }
    return false
  }
})

onMounted(loadForm)

onBeforeUnmount(() => {
  clearInterval(passwordCountdownTimer)
})
</script>

<style scoped>
.edit-page {
  min-height: 100vh;
  background: transparent;
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
  background: var(--topbar-bg);
  border-bottom: 1px solid var(--border);
  backdrop-filter: blur(16px);
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
}

.brand-mark {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: var(--primary);
  color: #fff;
  font-weight: 800;
}

.brand h1 {
  margin: 0;
  font-size: 18px;
}

.brand p {
  margin: 4px 0 0;
  color: var(--muted);
}

.edit-main {
  max-width: 1280px;
  margin: 0 auto;
  padding: 26px 24px 40px;
}

.hero-card {
  width: 75%;
  margin: 0 auto;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.08);
}

.hero-banner {
  position: relative;
  aspect-ratio: 3.36 / 1;
  background:
    var(--hero-gradient),
    var(--primary);
  background-size: cover;
  background-position: center;
}

.banner-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 34px 34px 72px;
  color: #fff;
}

.avatar-editor {
  display: grid;
  gap: 8px;
  justify-items: center;
}

.avatar-editor span,
.banner-actions span {
  color: rgba(255, 255, 255, 0.78);
  font-size: 12px;
}

.hero-avatar {
  filter: drop-shadow(0 16px 28px rgba(0, 0, 0, 0.16));
}

.banner-actions {
  position: absolute;
  left: 34px;
  right: 34px;
  bottom: 18px;
  z-index: 2;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  opacity: 0.82;
}

.banner-content h2 {
  margin: 0;
  font-size: 30px;
}

.banner-content p {
  margin: 8px 0 0;
  max-width: 760px;
  line-height: 1.7;
}

.banner-content .preview-signature {
  max-width: 680px;
  color: rgba(255, 255, 255, 0.9);
  word-break: break-word;
}

.field-tip {
  margin-top: 6px;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.5;
}

.panel-grid {
  display: grid;
  grid-template-columns: 1.35fr 1fr;
  gap: 20px;
  width: 75%;
  margin-top: 22px;
  margin-left: auto;
  margin-right: auto;
}

.panel {
  padding: 22px;
  border-radius: 18px;
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  backdrop-filter: blur(18px);
}

.panel-head h3 {
  margin: 0;
  font-size: 18px;
}

.panel-head p {
  margin: 8px 0 0;
  color: var(--muted);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 18px;
  margin-top: 18px;
}

.span-2 {
  grid-column: 1 / -1;
}

.full-width {
  width: 100%;
}

.privacy-card {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
  margin-top: 18px;
  padding: 16px 18px;
  border-radius: 14px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
}

.theme-panel {
  margin-top: 18px;
  padding: 16px;
  border-radius: 14px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
}

.theme-head strong {
  display: block;
  font-size: 15px;
}

.theme-head span {
  display: block;
  margin-top: 6px;
  color: var(--muted);
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.theme-card {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr);
  gap: 5px 10px;
  align-items: center;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--surface);
  cursor: pointer;
  text-align: left;
}

.theme-card.active {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--glow-primary);
}

.theme-swatch {
  grid-row: 1 / 3;
  width: 18px;
  height: 18px;
  border-radius: 999px;
}

.global-bg-panel {
  display: grid;
  gap: 12px;
  margin-top: 14px;
}

.global-bg-preview {
  display: flex;
  align-items: flex-end;
  min-height: 132px;
  padding: 14px;
  border-radius: 14px;
  color: #fff;
  overflow: hidden;
  background:
    linear-gradient(115deg, rgba(255, 255, 255, 0.58), transparent 36%),
    radial-gradient(circle at 10% 12%, var(--glow-primary), transparent 30%),
    radial-gradient(circle at 84% 16%, var(--glow-accent), transparent 28%),
    radial-gradient(circle at 50% 90%, var(--glow-primary), transparent 34%),
    repeating-linear-gradient(0deg, rgba(15, 23, 42, 0.05) 0 1px, transparent 1px 18px),
    linear-gradient(135deg, var(--bg-start), var(--bg-end));
  background-size: cover;
  background-position: center;
  box-shadow: inset 0 0 0 1px rgba(15, 23, 42, 0.08);
}

.global-bg-preview span {
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.42);
  font-size: 13px;
}

.global-bg-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.theme-card strong {
  color: var(--text);
  font-size: 14px;
}

.theme-card small {
  color: var(--muted);
  line-height: 1.5;
}

.privacy-card strong {
  display: block;
  font-size: 15px;
}

.privacy-card span {
  display: block;
  margin-top: 6px;
  color: var(--muted);
}

.hidden-input {
  display: none;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.cropper-shell {
  display: grid;
  gap: 18px;
}

.crop-stage {
  position: relative;
  height: 420px;
  overflow: hidden;
  border-radius: 18px;
  background: var(--bg);
  cursor: grab;
  user-select: none;
  touch-action: none;
}

.crop-stage:active {
  cursor: grabbing;
}

.crop-image {
  position: absolute;
  left: 50%;
  top: 50%;
  max-width: none;
  transform-origin: center;
  user-select: none;
  pointer-events: none;
}

.crop-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.44);
}

.crop-frame {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  border: 2px solid #fff;
  box-shadow: 0 0 0 999px rgba(0, 0, 0, 0.42);
  pointer-events: none;
}

.crop-controls {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
}

.crop-controls span {
  color: var(--muted);
  font-size: 14px;
}

@media (max-width: 1024px) {
  .hero-card {
    width: 100%;
  }

  .panel-grid {
    width: 100%;
  }

  .panel-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .crop-stage {
    height: 360px;
  }

  .crop-frame {
    max-width: calc(100% - 36px);
  }
}
</style>

