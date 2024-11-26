<script setup lang="ts">
import type { RepoUpdate, User } from '@/types/types'
import { generateClickableLink, simpleTimestamp } from '@/utils/utils'
import RepoEditor from '@/components/RepoEditor.vue'
import { onMounted, ref } from 'vue'
import { repoHistoryGet } from '@/services/userService'

const { student } = defineProps<{
  student: User;
}>();

onMounted(async () => {
  repoUpdateHistory.value = (await repoHistoryGet(student.netId)).reverse();
});
const reloadPage = () => {
  window.location.reload()
}

const repoUpdateHistory = ref<RepoUpdate[]>([])

</script>

<template>
<div>
  <h3>GitHub Repo URL for {{student.firstName}} {{student.lastName}}</h3>
  <p>Current Repo:
    <span v-if="student.repoUrl" v-html="generateClickableLink(student.repoUrl)"/>
    <span v-else>No repo</span>
  </p>

  <br/>

  <div id="changeAndHistory">
    <div>
      <h4>Change Their Repo</h4>
      <RepoEditor
        :user="student"
         @repoEditSuccess="reloadPage"/>
      <p><em>If the repo saves successfully, the page will reload.</em></p>
    </div>

    <div>
      <h4>Repo Change History</h4>
      <p class="repoUpdateLine" v-for="update in repoUpdateHistory">
        {{simpleTimestamp(update.timestamp)}} -
        <span v-html="generateClickableLink(update.repoUrl)"/>
        <span v-if="update.adminUpdate"> by admin {{update.adminNetId}}</span>
      </p>
    </div>
  </div>
</div>
</template>

<style scoped>
#changeAndHistory {
  display: grid;
  grid-template-columns: 2fr 3fr;
  column-gap: 20px;
  text-align: center;
}
.repoUpdateLine {
  text-align: left;
}
</style>
