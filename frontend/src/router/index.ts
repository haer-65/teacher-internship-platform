import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/store/modules/auth';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: {
      requiresAuth: false
    }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '工作台' }
      },
      {
        path: '/plan',
        name: 'Plan',
        component: () => import('@/views/plan/PlanEntryView.vue'),
        meta: { title: '实习计划' }
      },
      {
        path: '/plan/manage',
        name: 'PlanManage',
        component: () => import('@/views/plan/PlanListView.vue'),
        meta: { title: '实习计划管理' }
      },
      {
        path: '/plan/edit',
        name: 'PlanCreate',
        component: () => import('@/views/plan/PlanEditView.vue'),
        meta: { title: '新建计划草稿' }
      },
      {
        path: '/plan/edit/:id',
        name: 'PlanEdit',
        component: () => import('@/views/plan/PlanEditView.vue'),
        meta: { title: '编辑计划草稿' }
      },
      {
        path: '/plan/detail/:id',
        name: 'PlanDetail',
        component: () => import('@/views/plan/PlanDetailView.vue'),
        meta: { title: '计划详情' }
      },
      {
        path: '/plan/student',
        name: 'StudentPlanList',
        component: () => import('@/views/plan/StudentPlanListView.vue'),
        meta: { title: '已发布计划' }
      },
      {
        path: '/plan/student/detail/:id',
        name: 'StudentPlanDetail',
        component: () => import('@/views/plan/StudentPlanDetailView.vue'),
        meta: { title: '计划详情' }
      },
      {
        path: '/application',
        name: 'Application',
        component: () => import('@/views/application/ApplicationEntryView.vue'),
        meta: { title: '申请分配' }
      },
      {
        path: '/application/student',
        name: 'StudentApplicationList',
        component: () => import('@/views/application/StudentApplicationListView.vue'),
        meta: { title: '我的申请' }
      },
      {
        path: '/application/student/form',
        name: 'StudentApplicationCreate',
        component: () => import('@/views/application/ApplicationFormView.vue'),
        meta: { title: '填写申请' }
      },
      {
        path: '/application/student/form/:id',
        name: 'StudentApplicationEdit',
        component: () => import('@/views/application/ApplicationFormView.vue'),
        meta: { title: '编辑申请' }
      },
      {
        path: '/application/admin',
        name: 'AdminApplicationList',
        component: () => import('@/views/application/AdminApplicationListView.vue'),
        meta: { title: '申请审核' }
      },
      {
        path: '/application/assignment',
        name: 'AssignmentManagement',
        component: () => import('@/views/application/AssignmentManagementView.vue'),
        meta: { title: '分配管理' }
      },
      {
        path: '/material',
        name: 'Material',
        component: () => import('@/views/material/MaterialEntryView.vue'),
        meta: { title: '过程材料' }
      },
      {
        path: '/material/student',
        name: 'StudentMaterial',
        component: () => import('@/views/material/StudentMaterialView.vue'),
        meta: { title: '我的材料' }
      },
      {
        path: '/material/teacher',
        name: 'TeacherMaterial',
        component: () => import('@/views/material/TeacherMaterialView.vue'),
        meta: { title: '指导材料' }
      },
      {
        path: '/material/admin',
        name: 'AdminMaterial',
        component: () => import('@/views/material/AdminMaterialView.vue'),
        meta: { title: '材料管理' }
      },
      {
        path: '/evaluation',
        name: 'Evaluation',
        component: () => import('@/views/evaluation/EvaluationEntryView.vue'),
        meta: { title: '指导评价' }
      },
      {
        path: '/evaluation/teacher/pending',
        name: 'TeacherEvaluationPending',
        component: () => import('@/views/evaluation/TeacherEvaluationPendingView.vue'),
        meta: { title: '教师待评价' }
      },
      {
        path: '/evaluation/teacher/process/detail/:materialVersionId',
        name: 'TeacherMaterialEvaluation',
        component: () => import('@/views/evaluation/TeacherMaterialEvaluationView.vue'),
        meta: { title: '材料评价详情' }
      },
      {
        path: '/evaluation/teacher/final',
        name: 'TeacherFinalEvaluation',
        component: () => import('@/views/evaluation/TeacherFinalEvaluationView.vue'),
        meta: { title: '综合评价' }
      },
      {
        path: '/evaluation/student',
        name: 'StudentEvaluationView',
        component: () => import('@/views/evaluation/StudentEvaluationView.vue'),
        meta: { title: '我的评价结果' }
      },
      {
        path: '/score',
        name: 'Score',
        component: () => import('@/views/score/ScoreEntryView.vue'),
        meta: { title: '成绩管理' }
      },
      {
        path: '/score/list',
        name: 'ScoreList',
        component: () => import('@/views/score/ScoreListView.vue'),
        meta: { title: '成绩管理' }
      },
      {
        path: '/score/detail/:id',
        name: 'ScoreDetail',
        component: () => import('@/views/score/ScoreDetailView.vue'),
        meta: { title: '成绩详情' }
      },
      {
        path: '/score/publish',
        name: 'ScorePublish',
        component: () => import('@/views/score/ScorePublishView.vue'),
        meta: { title: '成绩发布' }
      },
      {
        path: '/score/student',
        name: 'StudentScoreSheet',
        component: () => import('@/views/score/StudentScoreSheetView.vue'),
        meta: { title: '我的成绩单' }
      },
      {
        path: '/notice',
        name: 'Notice',
        component: () => import('@/views/notice/NoticeListView.vue'),
        meta: { title: '消息中心' }
      },
      {
        path: '/notice/detail/:id',
        name: 'NoticeDetail',
        component: () => import('@/views/notice/NoticeDetailView.vue'),
        meta: { title: '消息详情' }
      },
      {
        path: '/stats',
        name: 'Stats',
        component: () => import('@/views/stats/StatsAnalysisView.vue'),
        meta: { title: '统计分析' }
      },
      {
        path: '/system',
        name: 'System',
        component: () => import('@/views/system/SystemManagementView.vue'),
        meta: { title: '系统管理' }
      },
      {
        path: '/system/users',
        name: 'SystemUsers',
        component: () => import('@/views/system/UserManagementView.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: '/system/base',
        name: 'SystemBase',
        component: () => import('@/views/system/SystemBaseDataView.vue'),
        meta: { title: '基础数据维护' }
      },
      {
        path: '/system/params',
        name: 'SystemParams',
        component: () => import('@/views/system/SystemParamView.vue'),
        meta: { title: '系统参数' }
      },
      {
        path: '/system/logs',
        name: 'SystemLogs',
        component: () => import('@/views/system/SystemLogView.vue'),
        meta: { title: '日志审计' }
      },
      {
        path: '/system/rbac',
        name: 'SystemRbac',
        component: () => import('@/views/system/SystemRbacView.vue'),
        meta: { title: '角色权限配置' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore();
  const requiresAuth = to.meta.requiresAuth !== false;

  if (!requiresAuth) {
    next();
    return;
  }

  if (!authStore.token) {
    next('/login');
    return;
  }

  if (!authStore.userInfo?.accountNo) {
    try {
      await authStore.refreshMe();
    } catch (_error) {
      next('/login');
      return;
    }
  }

  next();
});

router.afterEach((to) => {
  document.title = to.meta.title ? `${String(to.meta.title)} - 师范生教育实习全过程管理平台` : '师范生教育实习全过程管理平台';
});

export default router;

