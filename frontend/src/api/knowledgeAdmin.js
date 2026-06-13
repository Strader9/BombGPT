import request from '../utils/request'

// 管理端：分页查询知识库
export function getAdminKnowledgeList(pageNum = 1, pageSize = 50) {
  return request({
    url: '/admin/knowledge/list',
    method: 'get',
    params: {
      pageNum,
      pageSize
    }
  })
}

// 管理端：检索知识库
// 支持搜索：ID、分类ID、问题、答案、关键词、状态
export function searchAdminKnowledge(keyword) {
  return request({
    url: '/admin/knowledge/search',
    method: 'get',
    params: {
      keyword
    }
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
