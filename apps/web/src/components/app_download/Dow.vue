<template>
    <v-card
      class="download-card mx-auto"
      max-width="450"
      elevation="12"
      rounded="xl"
    >
      <div class="close-icon">
        <v-icon
          icon="mdi-close"
          color="grey-lighten-1"
          @click="emit('close')"
        />
      </div>
  
      <v-img
        class="app-logo"
        :src="infoLogo"
        height="120"
        cover
      />
  
      <v-card-item class="justify-center">
        <v-card-title class="text-h4 text-primary">
          {{ appName }}
        </v-card-title>
      </v-card-item>
  
      <v-card-text class="text-center">
        <div class="text-h6 mb-4">立即下载体验全新版本</div>
        <div class="text-body-1 text-grey-darken-1">
          发现更多精彩内容，享受流畅使用体验
        </div>
      </v-card-text>
  
      <v-card-actions class="flex-column px-8 pb-6">
        <v-btn
          block
          size="large"
          color="indigo-darken-3"
          class="mb-4 download-btn"
          @click="download('android')"
        >
          <template #prepend>
            <v-icon icon="mdi-android" />
          </template>
          Android 版下载
        </v-btn>
  
        <v-btn
          block
          size="large"
          color="black"
          class="download-btn"
          @click="download('ios')"
        >
          <template #prepend>
            <v-icon icon="mdi-apple" />
          </template>
          iOS 版下载
        </v-btn>
      </v-card-actions>
  
      <div class="text-caption text-center text-grey pb-2">
        当前版本 v{{ version }}
      </div>
    </v-card>
    
    <v-snackbar
      v-model="showMessage"
      :timeout="3000"
      color="info"
    >
      {{ message }}
    </v-snackbar>
  </template>
  
  <script setup>
  import { computed, ref } from 'vue'
  import infoLogo from '@/assets/logo/logo/信息.png'
  
  const props = defineProps({
    appName: {
      type: String,
      default: '白帽工坊 App'
    },
    version: {
      type: String,
      default: '1.0.0'
    },
    iosUrl: {
      type: String,
      default: "您的iOS下载链接"
    },
    androidUrl: {
      type: String,
      default: "http://wacyg.fun/apk/白帽工坊.apk"
    },
    logo: {
      type: String,
      default: infoLogo
    }
  })
  
  const emit = defineEmits(['close'])
  
  const platformUrls = computed(() => ({
    ios: props.iosUrl,
    android: props.androidUrl
  }))
  
  const showMessage = ref(false)
  const message = ref('')
  
  const download = (platform) => {
    if (platform === 'ios') {
      message.value = '未开放iOS端'
      showMessage.value = true
    } else {
      window.open(platformUrls.value[platform], '_blank')
    }
  }
  </script>
  
  <style scoped>
.download-card {
  position: relative;
  top: 100px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.close-icon {
  position: absolute;
  right: 12px;
  top: 12px;
  cursor: pointer;
  z-index: 1;
}

.app-logo {
  margin: 40px auto 20px;
  width: 120px;
  transition: transform 0.3s ease;
  
  &:hover {
    transform: scale(1.1);
  }
}

.download-btn {
  border-radius: 10px;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 8px rgba(0, 0, 0, 0.2);
  }
}
  </style>