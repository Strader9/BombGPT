import request from '../utils/request'

// 用户提交反馈 / 想法
export function submitFeedback(data) {
  return request({
    url: '/feedback/submit',
    method: 'post',
    data
  })
}

// 用户查看自己的反馈记录
export function getMyFeedback(username) {
  return request({
    url: '/feedback/my',
    method: 'get',
    params: {
      username
    }
  })
}

// 管理员查看全部反馈
export function getAllFeedback() {
  return request({
    url: '/feedback/list',
    method: 'get'
  })
}

// 管理员回复反馈
export function replyFeedback(id, reply) {
  return request({
    url: '/feedback/reply',
    method: 'post',
    data: {
      id,
      reply
    }
  })
}
