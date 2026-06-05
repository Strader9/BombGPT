import request from '../utils/request'

// 创建新对话
export function createConversation(username, title = '新对话') {
  return request({
    url: '/memory/conversation/create',
    method: 'post',
    data: {
      username,
      title
    }
  })
}

// 查询当前用户的历史对话
export function getConversationList(username) {
  return request({
    url: '/memory/conversation/list',
    method: 'get',
    params: {
      username
    }
  })
}

// 查询某个对话里的所有消息
export function getMessageList(conversationId, username) {
  return request({
    url: '/memory/message/list',
    method: 'get',
    params: {
      conversationId,
      username
    }
  })
}

// 保存一条聊天消息
export function saveMessage(data) {
  return request({
    url: '/memory/message/save',
    method: 'post',
    data
  })
}

// 修改对话标题
export function updateConversationTitle(id, username, title) {
  return request({
    url: '/memory/conversation/update-title',
    method: 'post',
    data: {
      id,
      username,
      title
    }
  })
}

// 删除对话
export function deleteConversation(id, username) {
  return request({
    url: '/memory/conversation/delete',
    method: 'post',
    data: {
      id,
      username
    }
  })
}
