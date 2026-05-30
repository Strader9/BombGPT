import request from '../utils/request'

// 1. 查询全部知识库
export function getKnowledgeList() {
  return request({
    url: '/knowledge/list',
    method: 'get'
  })
}

// 2. 按分类查询知识库
export function getKnowledgeByCategory(categoryId) {
  return request({
    url: '/knowledge/category',
    method: 'get',
    params: {
      id: categoryId
    }
  })
}

// 3. 搜索知识库
export function searchKnowledge(keyword) {
  return request({
    url: '/knowledge/search',
    method: 'get',
    params: {
      keyword: keyword
    }
  })
}

// 4. 根据ID查询知识库详情
export function getKnowledgeDetail(id) {
  return request({
    url: '/knowledge/detail',
    method: 'get',
    params: {
      id: id
    }
  })
}

// 热门排行榜
export function getHotKnowledge() {
  return request({
    url: '/knowledge/hot',
    method: 'get'
  })
}
