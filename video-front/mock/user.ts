import type { MockMethod } from 'vite-plugin-mock'
export default [
  {
    url: '/api/user/list',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: [
          {
            id: 1,
            username: 'admin',
            nickname: '超级管理员',
            role: '管理员',
            createTime: '2026-07-01 08:30:00',
            deleted: 0
          },
          {
            id: 2,
            username: 'test',
            nickname: '测试普通用户',
            role: '普通用户',
            createTime: '2026-07-05 14:22:10',
            deleted: 0
          },
          {
            id: 3,
            username: 'user01',
            nickname: '视频创作者',
            role: '普通用户',
            createTime: '2026-07-07 09:10:44',
            deleted: 1
          }
        ],
        msg: '查询成功'
      }
    }
  }
] as MockMethod[]