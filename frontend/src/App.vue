<template>
  <div class="layout">
    <aside class="sidebar">
      <h2>Greenhouse</h2>
      <router-link to="/">实时看板</router-link>
      <router-link to="/control">设备控制</router-link>
      <router-link to="/strategy">策略管理</router-link>
      <router-link to="/alarm">告警中心</router-link>
    </aside>
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<style>
body { margin: 0; font-family: Arial, sans-serif; background: #f4f6fb; }
.layout { display: grid; grid-template-columns: 220px 1fr; min-height: 100vh; }
.sidebar { background: #1f2a44; color: #fff; padding: 20px; display: flex; flex-direction: column; gap: 12px; }
.sidebar a { color: #dce4ff; text-decoration: none; }
.sidebar a.router-link-active { color: #fff; font-weight: bold; }
.content { padding: 20px; }
.card { background: #fff; border-radius: 10px; padding: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
</style>
