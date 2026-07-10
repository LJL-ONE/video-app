import type { MockMethod } from 'vite-plugin-mock'

// 视频详情数据（含 fileUrl、coverUrl 等完整字段）
const videoDetailMap: Record<number, any> = {
  2001: {
    id: 2001,
    title: 'Vue3+Vite前端搭建教程',
    category: '技术分享',
    fileUrl: 'https://www.w3schools.com/html/mov_bbb.mp4',
    coverUrl: 'https://picsum.photos/seed/2001/640/360',
    status: '已发布',
    uploader: '技术小栈',
    createTime: '2026-07-06 16:30:22',
    size: 52428800,
    duration: 315,
    description:
      '本视频将从零开始，带你使用 Vue3 + Vite + TypeScript 搭建一个完整的现代前端工程。\n内容涵盖：项目初始化、路由配置、状态管理、Element Plus 组件库集成、Mock 数据联调等实战内容，适合前端初学者与进阶开发者学习参考。'
  },
  2002: {
    id: 2002,
    title: '日常vlog记录',
    category: '生活日常',
    fileUrl: 'https://www.w3schools.com/html/movie.mp4',
    coverUrl: 'https://picsum.photos/seed/2002/640/360',
    status: '已发布',
    uploader: '生活记录者',
    createTime: '2026-07-07 11:12:05',
    size: 36700160,
    duration: 182,
    description: '记录一天的生活点滴，城市漫步、咖啡馆时光、夕阳下的街道，分享平凡日子里的小确幸。'
  },
  2003: {
    id: 2003,
    title: '需求评审会议录屏',
    category: '会议录屏',
    // fileUrl 为空，用于演示「视频准备中」占位
    fileUrl: '',
    coverUrl: 'https://picsum.photos/seed/2003/640/360',
    status: '待审核',
    uploader: '项目经理',
    createTime: '2026-07-08 10:05:11',
    size: 104857600,
    duration: 540,
    description: '本周需求评审会议录屏，包含新功能讨论、技术方案评估与排期确认等内容。视频资源转码中，请稍后观看。'
  }
}

// 推荐列表数据
const videoList = [
  {
    id: 2001,
    title: 'Vue3+Vite前端搭建教程',
    category: '技术分享',
    coverUrl: 'https://picsum.photos/seed/2001/640/360',
    uploader: '技术小栈',
    uploadTime: '2026-07-06 16:30:22',
    createTime: '2026-07-06 16:30:22',
    duration: 315,
    status: '已发布'
  },
  {
    id: 2002,
    title: '日常vlog记录',
    category: '生活日常',
    coverUrl: 'https://picsum.photos/seed/2002/640/360',
    uploader: '生活记录者',
    uploadTime: '2026-07-07 11:12:05',
    createTime: '2026-07-07 11:12:05',
    duration: 182,
    status: '已发布'
  },
  {
    id: 2003,
    title: '需求评审会议录屏',
    category: '会议录屏',
    coverUrl: 'https://picsum.photos/seed/2003/640/360',
    uploader: '项目经理',
    uploadTime: '2026-07-08 10:05:11',
    createTime: '2026-07-08 10:05:11',
    duration: 540,
    status: '待审核'
  },
  {
    id: 2004,
    title: 'TypeScript类型体操进阶',
    category: '技术分享',
    coverUrl: 'https://picsum.photos/seed/2004/640/360',
    uploader: '技术小栈',
    uploadTime: '2026-07-09 09:20:00',
    createTime: '2026-07-09 09:20:00',
    duration: 420,
    status: '已发布'
  },
  {
    id: 2005,
    title: '周末爬山记',
    category: '生活日常',
    coverUrl: 'https://picsum.photos/seed/2005/640/360',
    uploader: '生活记录者',
    uploadTime: '2026-07-09 14:00:00',
    createTime: '2026-07-09 14:00:00',
    duration: 260,
    status: '已发布'
  }
]

export default [
  {
    url: '/api/video/list',
    method: 'get',
    response: () => {
      return {
        code: 200,
        data: videoList,
        msg: '查询成功'
      }
    }
  },
  {
    // 匹配 /api/video/{数字id}
    url: /\/api\/video\/(\d+)/,
    method: 'get',
    response: ({ url }: { url: string }) => {
      const id = Number(url.split('/').pop())
      const detail = videoDetailMap[id]
      if (!detail) {
        return { code: 404, msg: '视频不存在' }
      }
      return { code: 200, data: detail, msg: '查询成功' }
    }
  },
  {
    url: '/api/upload',
    method: 'post',
    response: () => {
      return { code: 200, msg: '文件上传临时完成' }
    }
  },
  {
    url: '/api/video/upload',
    method: 'post',
    response: ({ body }) => {
      return {
        code: 200,
        msg: '视频投稿成功',
        data: {
          id: Date.now(),
          title: body?.title || '',
          category: body?.category || '',
          uploader: body?.uploader || ''
        }
      }
    }
  }
] as MockMethod[]