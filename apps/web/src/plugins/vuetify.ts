// src/plugins/vuetify.ts
import { createVuetify, type VuetifyOptions } from 'vuetify'
import * as mdi from '@mdi/js'  // 导入所有图标

export default createVuetify({
  icons: {
    defaultSet: 'mdi',
    aliases: mdi,  // 使用整个 mdi 图标集
  },
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          primary: '#1976D2',
          indigo: '#303F9F'
        }
      }
    }
  }
} as VuetifyOptions)