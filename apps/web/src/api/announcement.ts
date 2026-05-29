import request from '@/axios/index'
import store from '@/store'

// 获取公告列表
export function getAnnouncements() {
  return request({
    url: '/api/announcement/list',
    method: 'get'
  })
}

// 获取公告详情
export function getAnnouncementDetail(id: string) {
  return request({
    url: `/api/announcement/detail/${id}`,
    method: 'get'
  })
}

// 创建公告（管理员）
export function createAnnouncement(data: any) {
  return request({
    url: '/api/announcement/create',
    method: 'post',
    data,
    headers: {
      'X-User-ID': store.state.id,
      'Content-Type': 'application/json'
    }
  })
}

// 更新公告（管理员）
export function updateAnnouncement(data: any) {
  return request({
    url: `/api/announcement/update/${data.id}`,
    method: 'put',
    data,
    headers: {
      'X-User-ID': store.state.id,
      'Content-Type': 'application/json'
    }
  })
}

// 删除公告（管理员）
export function deleteAnnouncement(id: string) {
  return request({
    url: `/api/announcement/delete/${id}`,
    method: 'delete',
    headers: {
      'X-User-ID': store.state.id,
      'Content-Type': 'application/json'
    }
  })
} 