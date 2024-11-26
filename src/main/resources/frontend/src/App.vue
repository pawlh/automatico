<script setup lang="ts">

import { computed, onMounted, reactive } from 'vue'
import {useAuthStore} from "@/stores/auth";
import { loadUser, logoutPost } from '@/services/authService'
import router from '@/router'
import '@/assets/fontawesome/css/fontawesome.css'
import '@/assets/fontawesome/css/solid.css'
import { useAppConfigStore } from '@/stores/appConfig'
import BannerMessage from '@/components/BannerMessage.vue'
import PopUp from '@/components/PopUp.vue'
import RepoEditor from '@/components/RepoEditor.vue'
import AboutPage from '@/components/AboutPage.vue'
import { ServerError } from '@/network/ServerError'
import NavBar from '@/components/NavBar.vue'

const greeting = computed(() => {
  if (useAuthStore().isLoggedIn) {
    return `${useAuthStore().user?.firstName} ${useAuthStore().user?.lastName} - ${useAuthStore().user?.netId} (${useAuthStore().user?.role.toLowerCase()}) - `
  }
});

const logOut = async () => {
  try {
    await logoutPost()
    useAuthStore().user = null
  } catch (e) {
    if (e instanceof ServerError){
      alert(e.message)
    } else {
      alert(e)
    }
  }
  await router.push({ name: "login" })
}

onMounted( async () => {
  await useAppConfigStore().updateConfig();
})

const openRepoEditor = reactive({value: false})

const repoEditDone = () => {
  openRepoEditor.value = false
  loadUser()
}
</script>

<template>
  <header>
    <h1>CS 240</h1>
    <h4>Get TA help or run your code through the Autograder</h4>
    <p>{{ greeting }} <a v-if="useAuthStore().isLoggedIn" @click="logOut">Logout</a></p>
    <p>{{ useAuthStore().user?.repoUrl }}</p>
    <BannerMessage/>
  </header>
  <main>
    <NavBar/>

    <PopUp
      id="repoEditorPopUp"
      v-if="openRepoEditor.value"
      @closePopUp="openRepoEditor.value = false">
      <RepoEditor
      @repoEditSuccess="repoEditDone" :user="useAuthStore().user"/>
    </PopUp>

    <router-view/>
    <AboutPage/>
  </main>
</template>

<style scoped>
header {
  text-align: center;

  margin-bottom: 20px;

  padding: 20px;

  width: 100%;

  background-color: var(--color--secondary--background);
  color: var(--color--secondary--text);
}

h1 {
  font-weight: bold;
}

main {
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: var(--color--surface--background);
  color: var(--color--surface--text);
  padding: 20px;
  border-radius: 3px;

  width: 60vw;

  box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.2);
}

a {
  color: white;
  text-decoration: underline;
  cursor: pointer;
}

#repoEditorPopUp {
  text-align: center;
}

@media only screen and (max-width: 600px) {
  main {
    width: 95%;
    max-width: none;
    margin: 0 0 20px;
  }
}

@media only screen and (min-width: 601px) and (max-width: 900px){
  main {
    width: 75%;
    max-width: none;
    margin: 0 0 20px;
  }
}
</style>
