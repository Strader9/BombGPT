import request from '../utils/request'

// 管理端：查询全部知识库
export function getAdminKnowledgeList() {
  return request({
    url: '/admin/knowledge/list',
    method: 'get'
  })
}

// 管理端：新增知识库问答
export function addKnowledge(data) {
  return request({
    url: '/admin/knowledge/add',
    method: 'post',
    data
  })
}

// 管理端：修改知识库问答
export function updateKnowledge(data) {
  return request({
    url: '/admin/knowledge/update',
    method: 'post',
    data
  })
}

// 管理端：删除知识库问答
export function deleteKnowledge(id) {
  return request({
    url: '/admin/knowledge/delete',
    method: 'post',
    data: {
      id
    }
  })
}
