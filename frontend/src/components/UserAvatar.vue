<template>
  <span class="user-avatar" :class="{ admin: isAdmin, clickable }" :style="avatarBoxStyle">
    <el-avatar :size="size" :src="src" @click="$emit('click', $event)">
      <slot>{{ fallbackText }}</slot>
    </el-avatar>
    <span v-if="isAdmin" class="admin-mark">{{ compact ? 'A' : 'ADMIN' }}</span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  src: { type: String, default: '' },
  size: { type: Number, default: 42 },
  text: { type: String, default: 'U' },
  roleId: { type: [Number, String], default: null },
  compact: { type: Boolean, default: true },
  clickable: { type: Boolean, default: false }
})

defineEmits(['click'])

const isAdmin = computed(() => Number(props.roleId) === 1)
const fallbackText = computed(() => (props.text || 'U').slice(0, 1))
const avatarBoxStyle = computed(() => ({
  width: `${props.size}px`,
  height: `${props.size}px`
}))
</script>

<style scoped>
.user-avatar {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  overflow: visible;
  vertical-align: middle;
  line-height: 0;
}

.user-avatar.clickable {
  cursor: pointer;
}

.user-avatar.admin :deep(.el-avatar) {
  border: 2px solid rgba(251, 191, 36, 0.9);
  box-shadow: 0 10px 24px rgba(146, 64, 14, 0.18);
}

.user-avatar :deep(.el-avatar) {
  display: inline-flex;
  overflow: hidden;
  border-radius: 50%;
  background: var(--theme-tint);
  color: var(--primary-deep);
}

.user-avatar :deep(.el-avatar > img) {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.admin-mark {
  position: absolute;
  right: -5px;
  bottom: -4px;
  min-width: 18px;
  height: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
  border-radius: 999px;
  border: 2px solid var(--surface, #fff);
  background: linear-gradient(135deg, #fde68a, #f59e0b 55%, #ea580c);
  color: #7c2d12;
  font-size: 10px;
  font-weight: 900;
  letter-spacing: 0;
  line-height: 1;
  box-shadow: 0 8px 18px rgba(120, 53, 15, 0.28);
}
</style>
