import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import ControlView from '../views/ControlView.vue'
import HistoryTrendView from '../views/HistoryTrendView.vue'
import StrategyView from '../views/StrategyView.vue'
import AlarmView from '../views/AlarmView.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: DashboardView },
    { path: '/control', component: ControlView },
    { path: '/history', component: HistoryTrendView },
    { path: '/strategy', component: StrategyView },
    { path: '/alarm', component: AlarmView }
  ]
})
