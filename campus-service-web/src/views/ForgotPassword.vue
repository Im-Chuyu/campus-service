<template>
  <div class="forgot-page">
    <section class="forgot-shell">
      <div class="brand-panel">
        <p class="eyebrow">Account Recovery</p>
        <h1>找回账号密码</h1>
        <p class="desc">
          通过已绑定的手机号或邮箱接收验证码，验证通过后即可重新设置登录密码。
        </p>
      </div>

      <div class="forgot-card">
        <div class="card-head">
          <h2>重置密码</h2>
          <p>验证码有效期为 5 分钟，重置成功后请使用新密码登录。</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleReset">
          <el-form-item label="接收方式" prop="channel">
            <el-segmented v-model="form.channel" :options="channelOptions" />
          </el-form-item>

          <el-form-item :label="form.channel === 'phone' ? '手机号' : '邮箱'" prop="target">
            <el-input
              v-model="form.target"
              size="large"
              :placeholder="form.channel === 'phone' ? '请输入绑定手机号' : '请输入绑定邮箱'"
              clearable
            >
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
              <template #append>
                <el-button :loading="codeLoading" :disabled="codeCountdown > 0" @click="sendResetCode">
                  {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="验证码" prop="code">
            <el-input v-model="form.code" size="large" maxlength="6" placeholder="请输入6位验证码" clearable />
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="form.newPassword"
              size="large"
              type="password"
              show-password
              placeholder="8-20位，包含字母和数字"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              size="large"
              type="password"
              show-password
              placeholder="请再次输入新密码"
            >
              <template #prefix>
                <el-icon><Key /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleReset">
            重置密码
          </el-button>
        </el-form>

        <div class="extra">
          <el-button link type="primary" @click="$router.push('/login')">返回登录</el-button>
          <el-divider direction="vertical" />
          <el-button link type="primary" @click="$router.push('/register')">注册账号</el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onBeforeUnmount, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Key, Lock, Message } from '@element-plus/icons-vue'
import { resetPasswordApi, sendCodeApi } from '../api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const codeLoading = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null

const channelOptions = [
  { label: '手机号', value: 'phone' },
  { label: '邮箱', value: 'email' }
]

const form = reactive({
  channel: 'phone',
  target: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const validateTarget = (rule, value, callback) => {
  if (!value) {
    callback(new Error(form.channel === 'phone' ? '请输入手机号' : '请输入邮箱'))
    return
  }
  if (form.channel === 'phone' && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
    return
  }
  if (form.channel === 'email' && !/^[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}$/.test(value)) {
    callback(new Error('邮箱格式不正确'))
    return
  }
  callback()
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
    return
  }
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const rules = {
  channel: [{ required: true, message: '请选择接收方式', trigger: 'change' }],
  target: [{ validator: validateTarget, trigger: 'blur' }],
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
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

const startCountdown = () => {
  codeCountdown.value = 60
  clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    codeCountdown.value -= 1
    if (codeCountdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

const sendResetCode = async () => {
  await formRef.value.validateField('target')
  codeLoading.value = true
  try {
    const res = await sendCodeApi({
      scene: 'reset_password',
      channel: form.channel,
      target: form.target
    })
    await ElMessageBox.alert(res.msg || `验证码：${res.data}`, '验证码', {
      confirmButtonText: '知道了'
    })
    startCountdown()
  } finally {
    codeLoading.value = false
  }
}

const handleReset = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await resetPasswordApi(form)
    ElMessage.success('密码重置成功，请重新登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}

watch(
  () => form.channel,
  () => {
    form.target = ''
    form.code = ''
    formRef.value?.clearValidate(['target', 'code'])
  }
)

onBeforeUnmount(() => {
  clearInterval(countdownTimer)
})
</script>

<style scoped>
.forgot-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
}

.forgot-shell {
  width: min(1080px, 100%);
  display: grid;
  grid-template-columns: 0.9fr minmax(360px, 0.8fr);
  gap: 34px;
  align-items: stretch;
}

.brand-panel,
.forgot-card {
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  backdrop-filter: blur(16px);
}

.brand-panel {
  padding: 42px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.eyebrow {
  margin: 0 0 14px;
  color: var(--primary);
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
}

.brand-panel h1 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 48px;
}

.desc {
  margin: 18px 0 0;
  color: var(--muted);
  line-height: 1.8;
}

.forgot-card {
  padding: 34px;
}

.card-head {
  margin-bottom: 24px;
}

.card-head h2 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 30px;
}

.card-head p {
  margin: 10px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.submit-btn {
  width: 100%;
  height: 46px;
  margin-top: 4px;
}

.extra {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  margin-top: 18px;
}

@media (max-width: 900px) {
  .forgot-page {
    padding: 22px;
  }

  .forgot-shell {
    grid-template-columns: 1fr;
  }

  .brand-panel,
  .forgot-card {
    padding: 24px;
  }

  .brand-panel h1 {
    font-size: 36px;
  }
}
</style>
