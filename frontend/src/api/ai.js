import request from '../utils/request'

// AI 智能问答接口
export function chatWithAi(question) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data: {
      question
    }
  })
}
