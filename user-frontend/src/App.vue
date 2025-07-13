<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
console.log('App组件已挂载')
</script>

<style>
#app {
  min-height: 100vh;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  background: #ffffff;
}
</style>
