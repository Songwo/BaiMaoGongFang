<template>
    <el-card class="vulnerability-management">
      <template #header>
        <div class="card-header">
          <span>漏洞管理</span>
          <div class="operation-bar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索 CVE ID/标题"
              clearable
              style="width: 300px; margin-right: 10px"
              @input="handleSearch"
            />
            <el-select
              v-model="severityFilter"
              placeholder="严重程度"
              clearable
              style="width: 120px"
              @change="loadBug"
            >
              <el-option
                v-for="item in severityOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-button type="primary" @click="handleAdd">新增漏洞</el-button>
          </div>
        </div>
      </template>
  
      <el-table :data="filteredVulnerabilities" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="cve_Id" label="CVE ID" width="150" />
        <el-table-column prop="title" label="标题" width="250" />
        <el-table-column label="严重程度" width="120">
          <template #default="scope">
            <el-tag :type="severityTagMap[scope.row.severity]">
              {{ scope.row.severity }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cvss_score" label="CVSS" width="100" />
        <el-table-column prop="published_date" label="发布日期" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.published_date) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
  
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
  
      <!-- 编辑/新增对话框 -->
      <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
        <el-form :model="currentVuln" label-width="120px">
          <el-form-item label="CVE ID" prop="cve_Id" required>
            <el-input v-model="currentVuln.cve_Id" />
          </el-form-item>
          
          <el-form-item label="标题" prop="title" required>
            <el-input v-model="currentVuln.title" />
          </el-form-item>
  
          <el-form-item label="描述" prop="description">
            <el-input v-model="currentVuln.description" type="textarea" :rows="4" />
          </el-form-item>
  
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="严重程度" prop="severity">
                <el-select v-model="currentVuln.severity">
                  <el-option
                    v-for="item in severityOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="CVSS 评分" prop="cvss_score">
                <el-input-number
                  v-model="currentVuln.cvss_score"
                  :min="0"
                  :max="10"
                  :step="0.1"
                />
              </el-form-item>
            </el-col>
          </el-row>
  
          <el-form-item label="影响版本">
            <div v-for="(version, index) in currentVuln.affected_versions" :key="index">
              <el-input
                v-model="currentVuln.affected_versions[index]"
                style="width: 80%; margin-right: 10px"
              />
              <el-button
                type="danger"
                circle
                :icon="Minus"
                @click="removeVersion(index)"
              />
            </div>
            <el-button type="primary" @click="addVersion">添加影响版本</el-button>
          </el-form-item>
  
          <el-form-item label="解决方案">
            <el-input
              v-model="currentVuln.solutions"
              type="textarea"
              :rows="3"
              placeholder="每条解决方案用换行分隔"
            />
          </el-form-item>
        </el-form>
  
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认</el-button>
        </template>
      </el-dialog>
    </el-card>
  </template>
  
  <script setup>
  import { ref, computed, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { Minus } from '@element-plus/icons-vue'
  import axios from 'axios'
  import store from '@/store'
  import ToUrl from '@/api/api'
  import { debounce } from 'lodash-es'
  
  // 初始数据
  const vulnerabilities = ref([])
  const cachedData = ref(new Map())
  const loading = ref(false)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  
  // 对话框相关状态
  const dialogVisible = ref(false)
  const isEditMode = ref(false)
  const currentVuln = ref(initEmptyVuln())
  
  // 搜索相关
  const searchKeyword = ref('')
  const severityFilter = ref('')
  
  // 配置选项
  const severityOptions = [
    { value: 'critical', label: '严重' },
    { value: 'high', label: '高危' },
    { value: 'medium', label: '中危' },
    { value: 'low', label: '低危' }
  ]
  
  const severityTagMap = {
    critical: 'danger',
    high: 'warning',
    medium: '',
    low: 'info'
  }
  
  // 防抖搜索
  const debouncedSearch = debounce((value) => {
    searchKeyword.value = value
    currentPage.value = 1
    loadBug()
  }, 300)
  
  const loadBug = async () => {
    try {
      loading.value = true
      const cacheKey = `vulns_${currentPage.value}_${pageSize.value}_${searchKeyword.value}_${severityFilter.value}`
      
      // 检查缓存
      if (cachedData.value.has(cacheKey)) {
        const cached = cachedData.value.get(cacheKey)
        if (Date.now() - cached.timestamp < 5 * 60 * 1000) { // 5分钟缓存
          vulnerabilities.value = cached.data
          total.value = cached.total
          return
        }
      }

      const response = await axios.get(ToUrl.url + `/admin/findAllHole`, {
        headers: {
          'Authorization': `Bearer ${store.state.token}`
        },
        params: {
          page: currentPage.value,
          pageSize: pageSize.value,
          keyword: searchKeyword.value,
          severity: severityFilter.value
        }
      })
      
      vulnerabilities.value = response.data.data
      total.value = response.data.total

      // 更新缓存
      cachedData.value.set(cacheKey, {
        data: vulnerabilities.value,
        total: total.value,
        timestamp: Date.now()
      })
    } catch (error) {
      ElMessage.error('数据加载失败: ' + error.message)
    } finally {
      loading.value = false
    }
  }

  // 计算属性
  const filteredVulnerabilities = computed(() => {
    return vulnerabilities.value
  })
  
  const dialogTitle = computed(() => {
    return isEditMode.value ? '编辑漏洞' : '新增漏洞'
  })

  // 分页处理
  const handlePageChange = (page) => {
    currentPage.value = page
    loadBug()
  }

  const handleSizeChange = (size) => {
    pageSize.value = size
    currentPage.value = 1
    loadBug()
  }

  // 搜索处理
  const handleSearch = (value) => {
    debouncedSearch(value)
  }

  onMounted(() => {
    loadBug()
  })
  
  // 方法
  function initEmptyVuln() {
    return {
      cve_Id: '',
      title: '',
      description: '',
      severity: 'high',
      cvss_score: 7.0,
      published_date: new Date().toISOString().split('T')[0],
      affected_versions: [''],
      solutions: '',
      type: ''
    }
  }
  
  function handleAdd() {
    isEditMode.value = false
    currentVuln.value = initEmptyVuln()
    dialogVisible.value = true
  }
  
  function handleEdit(vuln) {
    isEditMode.value = true
    currentVuln.value = { ...vuln }
    dialogVisible.value = true
  }
  
  async function handleDelete(vuln) {
  ElMessageBox.confirm('确认删除该漏洞记录？', '警告', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await axios.put(ToUrl.url+`/admin/deleteHole`,{
        id:vuln.id
      } ,{
        headers: {
          'Authorization': `Bearer ${store.state.token}`
        }
      })
      vulnerabilities.value = vulnerabilities.value.filter(v => v.cve_Id !== vuln.cve_Id)
      ElMessage.success('删除成功')
    } catch (error) {
      ElMessage.error('删除失败: ' + error.message)
    }
  })
}

  
  function addVersion() {
    currentVuln.value.affected_versions.push('')
  }
  
  function removeVersion(index) {
    currentVuln.value.affected_versions.splice(index, 1)
  }
  
  async function submitForm() {
  try {
    if (isEditMode.value) {
      // 编辑漏洞
      await axios.put(ToUrl.url+`/admin/UpdateHole`, currentVuln.value, {
        headers: {
          'Authorization': `Bearer ${store.state.token}`
        }
      })
      const index = vulnerabilities.value.findIndex(v => v.cve_Id === currentVuln.value.cve_Id)
      if (index !== -1) {
        vulnerabilities.value[index] = { ...currentVuln.value }
      }
      ElMessage.success('更新成功')
    } else {
      // 新增漏洞
      const response = await axios.post(ToUrl.url+'/admin/insertHole', currentVuln.value, {
        headers: {
          'Authorization': `Bearer ${store.state.token}`
        }
      })
      loadBug();
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
  } catch (error) {
    ElMessage.error('操作失败: ' + error.message)
  }
}

  
  function formatDate(dateStr) {
    return new Date(dateStr).toLocaleDateString()
  }
  </script>
  
  <style scoped>
  .operation-bar {
    display: flex;
    align-items: center;
    margin-top: 10px;
    gap: 10px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  </style>