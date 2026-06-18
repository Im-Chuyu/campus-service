import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import './assets/styles/global.css'
import { applyAppearance, getSavedBackgroundBlobUrl, getSavedBackgroundKey, getSavedThemeKey } from './utils/theme'

const app = createApp(App)

if (getSavedBackgroundKey() === 'custom') {
  getSavedBackgroundBlobUrl().then(url => {
    applyAppearance({
      themeKey: getSavedThemeKey(),
      backgroundKey: url ? 'custom' : 'system',
      customUrl: url
    })
  })
} else {
  applyAppearance({
    themeKey: getSavedThemeKey(),
    backgroundKey: getSavedBackgroundKey()
  })
}

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
