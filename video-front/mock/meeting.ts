import type { MockMethod } from 'vite-plugin-mock'
export default [
  {
    url: '/api/meeting/list',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: [
          {
            id: 1001,
            roomName: '技术需求评审会议室',
            creator: 'admin',
            createTime: '2026-07-08 09:20:15',
            status: '使用中'
          },
          {
            id: 1002,
            roomName: '前端联调会议室',
            creator: 'test',
            createTime: '2026-07-08 10:15:30',
            status: '空闲'
          },
          {
            id: 1003,
            roomName: '产品方案讨论室',
            creator: 'admin',
            createTime: '2026-07-07 16:40:22',
            status: '使用中'
          }
        ],
        msg: '查询成功'
      }
    }
  }
] as MockMethod[]