import request from '@/axios/index'
import store from '@/store'

// 获取更新日志列表
export function getChangelogs() {
  return request({
    url: '/api/changelog/list',
    method: 'get'
  })
}

// 获取更新日志详情
export function getChangelogDetail(id: string) {
  return request({
    url: `/api/changelog/detail/${id}`,
    method: 'get'
  })
}

// 创建更新日志（管理员）
export function createChangelog(data: any) {
  return request({
    url: '/api/changelog/create',
    method: 'post',
    data,
    headers: {
      'X-User-ID': store.state.id,
      'Content-Type': 'application/json'
    }
  })
}

// 更新更新日志（管理员）
export function updateChangelog(data: any) {
  return request({
    url: `/api/changelog/update/${data.id}`,
    method: 'put',
    data,
    headers: {
      'X-User-ID': store.state.id,
      'Content-Type': 'application/json'
    }
  })
}

// 删除更新日志（管理员）
export function deleteChangelog(id: string) {
  return request({
    url: `/api/changelog/delete/${id}`,
    method: 'delete',
    headers: {
      'X-User-ID': store.state.id,
      'Content-Type': 'application/json'
    }
  })
} 