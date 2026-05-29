// src/plugins/vuetify.d.ts
import { VuetifyOptions } from 'vuetify'

declare module 'vuetify' {
  export interface VuetifyOptions {
    // 添加自定义配置类型（如果有）
  }
}

declare module '@vuetify/nightly' { // 如果使用nightly版本
  export function createVuetify(options?: VuetifyOptions): any
}

declare module 'vuetify/lib/framework' { // 标准版本类型声明
  import { VuetifyOptions } from 'vuetify'
  export function createVuetify(options?: VuetifyOptions): any
}