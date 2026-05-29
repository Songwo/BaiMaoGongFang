<template>
  <div class="bug-container">
    <!-- 头部 -->
    <el-card class="header-card">
      <div class="bug-header">
        <div class="bug-title-area">
          <h1>漏洞库</h1>
          <p class="subtitle">安全漏洞信息中心</p>
        </div>
        <div class="stats-area">
          <div class="stat-item">
            <el-icon><Warning /></el-icon>
            <div class="stat-info">
              <span class="stat-count">{{ totalVulnerabilitiesFromSource }}</span>
              <span class="stat-label">漏洞总数</span>
            </div>
          </div>
          <div class="stat-item">
            <el-icon><DataAnalysis /></el-icon>
            <div class="stat-info">
              <span class="stat-count">{{ criticalCount }}</span>
              <span class="stat-label">严重漏洞</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 主体内容 -->
    <el-row :gutter="24" class="main-content">
      <!-- 漏洞列表 -->
      <el-col :xs="24" :sm="16" class="vuln-column">
        <el-card shadow="hover">
          <template #header>
            <div class="filter-bar">
              <div class="filter-controls">
                <el-select v-model="selectedSeverity" placeholder="危险等级" @change="applyFilters">
                  <el-option label="全部等级" value="" />
                  <el-option v-for="level in severityLevels" :key="level" :label="level.toUpperCase()" :value="level" />
                </el-select>
                <el-select v-model="selectedType" placeholder="漏洞类型" @change="applyFilters">
                  <el-option label="全部类型" value="" />
                  <el-option v-for="type in vulnerabilityTypes" :key="type" :label="type" :value="type" />
                </el-select>
                <el-input 
                  v-model="searchQuery" 
                  placeholder="搜索CVE编号或漏洞名称" 
                  prefix-icon="Search"
                  clearable
                  @input="handleSearchInput"
                  class="search-input"
                />
              </div>
            </div>
          </template>

          <div v-loading="loadingVulns" class="vulnerability-list">
            <transition-group name="vuln-list">
              <el-card 
                v-for="vuln in displayedVulnerabilities" 
                :key="vuln.id" 
                class="vulnerability-card" 
                shadow="hover" 
                @click="showVulnDetails(vuln)"
              >
                <div class="vuln-content">
                  <div class="vuln-header">
                    <div class="cve-info">
                      <span class="cve-id">{{ vuln.cve_Id }}</span>
                      <el-tag :type="severityColor(vuln.severity)" effect="dark" class="severity-tag">
                        {{ vuln.severity.toUpperCase() }}
                      </el-tag>
                    </div>
                    <el-button 
                      v-if="store.state.token"
                      @click.stop="toggleBookmark(vuln.cve_Id)" 
                      :type="isBookmarked(vuln.cve_Id) ? 'warning' : 'info'" 
                      circle 
                      size="small" 
                      plain
                    >
                      <el-icon><StarFilled /></el-icon>
                    </el-button>
                  </div>
                  <h3 class="vuln-title">{{ vuln.title }}</h3>
                  <div class="vuln-meta">
                    <div class="meta-item">
                      <el-icon><Timer /></el-icon>
                      <span>{{ formatDate(vuln.published_date) }}</span>
                    </div>
                    <div class="meta-item">
                      <el-icon><ScaleToOriginal /></el-icon>
                      <span>CVSS: {{ vuln.cvss_score }}</span>
                    </div>
                    <div class="meta-item">
                      <el-icon><Folder /></el-icon>
                      <span>{{ vuln.type }}</span>
                    </div>
                  </div>
                  <el-button type="primary" size="small" class="detail-btn" @click.stop="showVulnDetails(vuln)">
                    查看详情
                  </el-button>
                </div>
              </el-card>
            </transition-group>

            <!-- 未登录用户的提示卡片 -->
            <div v-if="!store.state.token" class="more-content-prompt">
              <el-divider>
                <el-icon><InfoFilled /></el-icon>
                更多漏洞信息
              </el-divider>
              <p class="prompt-text">登录后可以查看更多漏洞和完整详情</p>
              <el-button type="primary" @click="router.push('/bmgf/login')">立即登录</el-button>
            </div>

            <el-pagination
              v-if="filteredVulnerabilities.length > vulnsPerPage"
              :current-page="currentPage"
              :page-size="vulnsPerPage"
              :total="filteredVulnerabilities.length"
              layout="prev, pager, next"
              @current-change="handlePageChange"
              class="pagination"
            />
            <el-empty v-if="displayedVulnerabilities.length === 0" description="暂无相关漏洞" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧边栏 -->
      <el-col :xs="24" :sm="8" class="sidebar-column">
        <!-- 严重性分布图表 -->
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <h3><el-icon><PieChart /></el-icon> 严重性分布</h3>
            </div>
          </template>
          <div id="severityBarChart" style="height: 250px; width: 100%;"></div>
        </el-card>

        <!-- 统计信息 -->
        <el-card class="stats-card" shadow="hover">
          <template #header>
            <div class="stats-header">
              <h3><el-icon><DataAnalysis /></el-icon> 统计信息</h3>
            </div>
          </template>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ totalVulnerabilitiesFromSource }}</div>
              <div class="stat-label">总数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ criticalCount }}</div>
              <div class="stat-label">严重</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ highCount }}</div>
              <div class="stat-label">高危</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ mediumCount }}</div>
              <div class="stat-label">中危</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ lowCount }}</div>
              <div class="stat-label">低危</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ totalFilteredVulnerabilities }}</div>
              <div class="stat-label">当前显示</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 漏洞详情对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="selectedVuln?.cve_Id" 
      width="60%"
      class="vuln-detail-dialog"
    >
      <div v-if="selectedVuln" class="dialog-content">
        <div class="vuln-detail-header">
          <el-tag :type="severityColor(selectedVuln.severity)" effect="dark" size="large">
            {{ selectedVuln.severity.toUpperCase() }}
          </el-tag>
          <div class="header-meta">
            <span class="cvss">CVSS: {{ selectedVuln.cvss_score }}</span>
            <span class="date">{{ formatDate(selectedVuln.published_date) }}</span>
          </div>
        </div>

        <h3 class="detail-title">{{ selectedVuln.title }}</h3>

        <el-divider />
        <h4>漏洞描述</h4>
        <div class="description-text">{{ selectedVuln.description }}</div>

        <el-divider />
        <h4>影响版本</h4>
        <div class="affected-versions">
          <el-tag v-if="selectedVuln.affected_versions" type="info" size="small">
            {{ selectedVuln.affected_versions }}
          </el-tag>
          <span v-else>N/A</span>
        </div>

        <el-divider />
        <h4>解决方案</h4>
        <el-alert 
          :title="selectedVuln.solutions || '暂无官方解决方案'" 
          :type="selectedVuln.solutions ? 'success' : 'info'" 
          :closable="false" 
          show-icon 
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import axios from 'axios';
import { 
  Search, StarFilled, Warning, DataAnalysis, PieChart, 
  Timer, ScaleToOriginal, Folder, InfoFilled 
} from '@element-plus/icons-vue';
import store from '@/store';
import ToUrl from '@/api/api';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { debounce } from 'lodash-es';

const router = useRouter();

// 响应式状态
const searchQuery = ref('');
const selectedSeverity = ref('');
const selectedType = ref('');
const selectedVuln = ref(null);
const dialogVisible = ref(false);
const loadingVulns = ref(false);
const currentPage = ref(1);
const vulnsPerPage = ref(10);
const BugHole = ref([]);
const totalVulnerabilitiesFromSource = ref(0);
const bookmarkedCves = ref(new Set());
const cachedData = ref(new Map());

// 配置项
const severityLevels = ['critical', 'high', 'medium', 'low'];
const vulnerabilityTypes = ['RCE', 'Privilege Escalation', 'Directory Traversal', 'SQL Injection', 'Cross-Site Scripting (XSS)'];

let barChartInstance = null;

// 防抖搜索
const debouncedSearch = debounce((query) => {
  searchQuery.value = query;
  applyFilters();
}, 300);

// 计算属性
const criticalCount = computed(() => BugHole.value.filter(v => v.severity === 'critical').length);
const highCount = computed(() => BugHole.value.filter(v => v.severity === 'high').length);
const mediumCount = computed(() => BugHole.value.filter(v => v.severity === 'medium').length);
const lowCount = computed(() => BugHole.value.filter(v => v.severity === 'low').length);

const filteredVulnerabilities = computed(() => {
  let result = [...BugHole.value];
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(vuln => 
      vuln.cve_Id.toLowerCase().includes(query) || 
      vuln.title.toLowerCase().includes(query)
    );
  }
  
  if (selectedSeverity.value) {
    result = result.filter(vuln => vuln.severity === selectedSeverity.value);
  }
  
  if (selectedType.value) {
    result = result.filter(vuln => vuln.type === selectedType.value);
  }
  
  return result;
});

const displayedVulnerabilities = computed(() => {
  const start = (currentPage.value - 1) * vulnsPerPage.value;
  return filteredVulnerabilities.value.slice(start, start + vulnsPerPage.value);
});

// 方法
const fetchVulnerabilities = async () => {
  try {
    loadingVulns.value = true;
    const cacheKey = `vulns_${store.state.token ? 'auth' : 'guest'}`;
    
    // 检查缓存
    if (cachedData.value.has(cacheKey)) {
      const cached = cachedData.value.get(cacheKey);
      if (Date.now() - cached.timestamp < 5 * 60 * 1000) { // 5分钟缓存
        BugHole.value = cached.data;
        totalVulnerabilitiesFromSource.value = BugHole.value.length;
        initOrUpdateCharts();
        return;
      }
    }

    const headers = store.state.token ? { 'Authorization': `Bearer ${store.state.token}` } : {};
    const response = await axios.get(ToUrl.url + `/user/findAllHole`, { headers });
    BugHole.value = response.data.data || [];
    
    if (!store.state.token) {
      BugHole.value = BugHole.value.slice(0, 5);
    }
    
    // 更新缓存
    cachedData.value.set(cacheKey, {
      data: BugHole.value,
      timestamp: Date.now()
    });
    
    totalVulnerabilitiesFromSource.value = BugHole.value.length;
    initOrUpdateCharts();
  } catch (error) {
    console.error('数据获取失败:', error);
    BugHole.value = [];
    totalVulnerabilitiesFromSource.value = 0;
  } finally {
    loadingVulns.value = false;
  }
};

const initOrUpdateCharts = () => {
  if (!BugHole.value.length) return;
  
  const barChartDom = document.getElementById('severityBarChart');
  if (!barChartDom) return;
  
  if (!barChartInstance) {
    barChartInstance = echarts.init(barChartDom);
    window.addEventListener('resize', () => barChartInstance?.resize());
  }
  
  // 使用 requestAnimationFrame 优化渲染
  requestAnimationFrame(() => {
    const barOption = {
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: {
        type: 'category',
        data: ['严重', '高危', '中危', '低危'],
        axisLabel: { fontSize: 10 }
      },
      yAxis: { type: 'value' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      series: [{
        data: [
          criticalCount.value,
          highCount.value,
          mediumCount.value,
          lowCount.value
        ],
        type: 'bar',
        itemStyle: {
          color: function(params) {
            const colors = ['#e74c3c', '#f1a340', '#5dade2', '#58d68d'];
            return colors[params.dataIndex];
          }
        }
      }]
    };
    
    barChartInstance.setOption(barOption);
  });
};

const showVulnDetails = (vuln) => {
  if (!store.state.token) {
    ElMessage.warning('登录后可查看完整漏洞详情');
    router.push('/bmgf/login');
    return;
  }
  selectedVuln.value = vuln;
  dialogVisible.value = true;
};

const loadBookmarks = () => {
  const saved = localStorage.getItem('vulnBookmarks');
  if (saved) {
    try {
      bookmarkedCves.value = new Set(JSON.parse(saved));
    } catch (e) {
      console.error("从localStorage解析书签失败", e);
      bookmarkedCves.value = new Set();
      localStorage.removeItem('vulnBookmarks');
    }
  }
};

const saveBookmarks = () => {
  localStorage.setItem('vulnBookmarks', JSON.stringify([...bookmarkedCves.value]));
};

const toggleBookmark = (cveId) => {
  if (bookmarkedCves.value.has(cveId)) {
    bookmarkedCves.value.delete(cveId);
  } else {
    bookmarkedCves.value.add(cveId);
  }
  saveBookmarks();
};

const isBookmarked = (cveId) => bookmarkedCves.value.has(cveId);

const severityColor = (level) => ({
  critical: 'danger',
  high: 'warning',
  medium: 'primary',
  low: 'info'
}[level] || 'info');

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  } catch (e) {
    return '无效日期';
  }
};

const applyFilters = () => {
  currentPage.value = 1;
};

const handlePageChange = (page) => {
  currentPage.value = page;
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const handleSearchInput = (value) => {
  debouncedSearch(value);
};

onMounted(() => {
  loadBookmarks();
  fetchVulnerabilities();
});

watch(BugHole, () => {
  if (BugHole.value.length) {
    initOrUpdateCharts();
  }
}, { deep: true });
</script>

<style lang="scss" scoped>
.bug-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;

  .header-card {
    margin-bottom: 24px;
    background: linear-gradient(135deg, #7c3aed, #a78bfa);
    border: none;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

    :deep(.el-card__header) {
      padding: 30px 40px;
    }

    .bug-header {
      color: white;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .bug-title-area {
        h1 {
          margin: 0;
          font-size: 2.2rem;
          text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.2);
        }

        .subtitle {
          margin: 10px 0 0;
          opacity: 0.9;
          font-size: 1.1rem;
        }
      }

      .stats-area {
        display: flex;
        gap: 24px;
        
        .stat-item {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .el-icon {
            font-size: 2rem;
            opacity: 0.9;
          }
          
          .stat-info {
            display: flex;
            flex-direction: column;
            
            .stat-count {
              font-size: 1.5rem;
              font-weight: bold;
            }
            
            .stat-label {
              font-size: 0.9rem;
              opacity: 0.9;
            }
          }
        }
      }
    }
  }

  .main-content {
    margin-top: 20px;

    .vuln-column {
      margin-bottom: 24px;
    }

    .filter-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      flex-wrap: wrap;
      gap: 12px;
      
      .filter-controls {
        display: flex;
        gap: 12px;
        flex-wrap: wrap;
        
        .el-select {
          width: 140px;
        }
        
        .search-input {
          width: 200px;
        }
      }
    }
  }

  .vulnerability-list {
    .vulnerability-card {
      margin-bottom: 16px;
      transition: all 0.3s;
      cursor: pointer;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
      }
      
      .vuln-content {
        .vuln-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;
          
          .cve-info {
            display: flex;
            align-items: center;
            gap: 12px;
            
            .cve-id {
              font-weight: 600;
              color: #333;
              font-size: 1.1rem;
            }
          }
        }
        
        .vuln-title {
          margin: 0 0 12px;
          font-size: 1.1rem;
          color: #333;
          line-height: 1.4;
        }
        
        .vuln-meta {
          display: flex;
          gap: 20px;
          margin-bottom: 12px;
          
          .meta-item {
            display: flex;
            align-items: center;
            gap: 8px;
            color: #666;
            font-size: 14px;
            
            .el-icon {
              font-size: 16px;
            }
          }
        }
        
        .detail-btn {
          align-self: flex-end;
          color: #ffffff !important;
        }
      }
    }
    
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: center;
    }
  }

  .sidebar-column {
    .chart-card, .stats-card {
      margin-bottom: 20px;
      border-radius: 8px;
      overflow: hidden;
      
      :deep(.el-card__header) {
        padding: 15px 20px;
        border-bottom: 1px solid #eee;
      }
      
      .chart-header, .stats-header {
        h3 {
          margin: 0;
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 1.1rem;
        }
      }
    }
    
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
      padding: 16px;
      
      .stat-item {
        text-align: center;
        padding: 12px;
        background: #f8f9fa;
        border-radius: 6px;
        
        .stat-value {
          font-size: 1.5rem;
          font-weight: 600;
          color: #333;
          margin-bottom: 4px;
        }
        
        .stat-label {
          font-size: 0.9rem;
          color: #666;
        }
      }
    }
  }

  .vuln-detail-dialog {
    :deep(.el-dialog__header) {
      padding: 20px;
      border-bottom: 1px solid #eee;
    }

    :deep(.el-dialog__title) {
      font-size: 18px;
      font-weight: 600;
      color: #333;
    }

    .dialog-content {
      padding: 20px;
      
      .vuln-detail-header {
        display: flex;
        align-items: center;
        gap: 16px;
        margin-bottom: 16px;
        
        .header-meta {
          display: flex;
          gap: 16px;
          color: #666;
        }
      }
      
      .detail-title {
        margin: 0 0 16px;
        font-size: 1.2rem;
        color: #333;
      }
      
      h4 {
        margin: 0 0 12px;
        font-size: 1.1rem;
        color: #333;
      }
      
      .description-text {
        line-height: 1.6;
        color: #333;
        margin-bottom: 16px;
      }
      
      .affected-versions {
        margin-bottom: 16px;
      }
    }
  }
}

.more-content-prompt {
  grid-column: 1 / -1;
  text-align: center;
  padding: 2rem 1rem;
  margin-top: 1rem;
  background: linear-gradient(to bottom, #fefefe, #f8f9fa);
  border: 1px dashed #dfe6ee;
  border-radius: 8px;

  .el-divider {
    margin: 1rem 0;
    
    :deep(.el-divider__text) {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #666;
      font-size: 1.1rem;
      font-weight: 600;
      
      .el-icon {
        font-size: 1.3rem;
        color: #409EFF;
      }
    }
  }

  .prompt-text {
    color: #409EFF;
    margin: 1rem 0;
    font-size: 1.1rem;
    font-weight: 500;
    background: linear-gradient(to right, #409EFF, #67C23A);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    animation: shimmer 2s infinite;
    text-shadow: 0 0 1px rgba(0,0,0,0.1);
  }
}

@keyframes shimmer {
  0% { opacity: 1; }
  50% { opacity: 0.8; }
  100% { opacity: 1; }
}

// 过渡动画
.vuln-list-enter-active,
.vuln-list-leave-active {
  transition: all 0.3s ease;
}

.vuln-list-enter-from,
.vuln-list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

// 响应式调整
@media (max-width: 1200px) {
  .bug-container {
    padding: 16px;
  }
}

@media (max-width: 992px) {
  .main-content {
    flex-direction: column;
  }
  
  .vuln-column, .sidebar-column {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .bug-container {
    padding: 12px;
  }
  
  .filter-controls {
    flex-direction: column;
    
    .el-select, .search-input {
      width: 100% !important;
    }
  }
}
</style>