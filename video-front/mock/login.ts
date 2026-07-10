import type { MockMethod } from 'vite-plugin-mock'
export default [
  {
    url: '/api/login',
    method: 'post',
    response: ({ body }) => {
      const { username, password } = body
      if(username === 'admin' && password === '123456'){
        return {
          code: 200,
          data: { token: 'mock-token-admin-001' },
          msg: '登录成功'
        }
      }else{
        return { code: 400, msg: '账号密码错误' }
      }
    }
  }
] as MockMethod[]