<script setup lang="ts">
import router from '@/router'
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const currentPath = computed(() => route.path)
</script>

<template>
  <div id="navBar">
    <p
      class="navItem"
      :class="{ active: currentPath === '/' }"
      @click="router.push('/')"
    >
      Autograder
    </p>
    <p
      class="navItem"
      :class="{ active: currentPath === '/help' }"
      @click="router.push('/help')"
    >
      Help Queue
    </p>
  </div>
</template>

<style scoped>
#navBar {
  display: flex;
  flex-direction: row;
  width: 100%;
  justify-content: space-between;
  margin-bottom: 15px;
}

.navItem {
  padding: 10px;
  background-color: #adadad;
  width: 100%;
  text-align: center;
  font-weight: bold;
  color: white;
  cursor: pointer;
}

.active {
  background-color: #787878;
  cursor: auto;
}
</style>
