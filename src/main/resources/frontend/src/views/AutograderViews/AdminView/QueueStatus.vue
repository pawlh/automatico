<script lang="ts" setup>
import {onMounted, onUnmounted, reactive, ref} from "vue";
import {getQueueStatus} from "@/services/adminService";
import { reRunSubmissionsPost } from '@/services/submissionService'
import Panel from "@/components/Panel.vue";

const currentlyGrading = ref<string[]>([]);
const inQueue = ref<string[]>([]);

const getQueueStatusPoll = async () => {
  const queueStatus = await getQueueStatus();
  currentlyGrading.value = queueStatus.currentlyGrading;
  inQueue.value = queueStatus.inQueue;
};

let intervalId: number;
onMounted(async () => {
  await getQueueStatusPoll();
  intervalId = setInterval(getQueueStatusPoll, 5000);
});

onUnmounted(() => {
  clearInterval(intervalId);
});

let reRunStatusMessage = reactive({value: "This is run automatically everytime the autograder server starts up."})

const reRunQueue = async () => {
  reRunStatusMessage.value = "Refreshing grading queue...";
  try {
    await reRunSubmissionsPost();
    reRunStatusMessage.value = "The queue has been refreshed and all submissions previously stuck in the queue are running through the grader again";
  } catch (e) {
    reRunStatusMessage.value = "Something went wrong while re-running queue."
  }
}
</script>

<template>
  <div class="container">
    <Panel class="grading-queue">
      <div>
        <h3>Currently Grading</h3>
        <ul v-if="currentlyGrading.length > 0">
          <li v-for="submission in currentlyGrading" :key="submission">{{ submission }}</li>
        </ul>
        <p v-else>No submissions are being graded</p>
      </div>
      <div>
        <h3>In Queue</h3>
        <ol v-if="inQueue.length > 0">
          <li v-for="submission in inQueue" :key="submission">{{ submission }}</li>
        </ol>
        <p v-else>No submissions in queue</p>
      </div>
    </Panel>
    <div>
      <div id="queue-refresh">
        <p>This re-runs every submission in the queue. Used if something has gone wrong.</p>
        <button @click="reRunQueue">Rerun Submissions In Queue</button>
        <p id="queue-refresh-message">{{reRunStatusMessage.value}}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  align-items: first;
}

.grading-queue {
margin: 10px;
padding: 10px;
border: 1px solid #ccc;
border-radius: 5px;
background-color: #f2f2f2;
}

#queue-refresh-message {
  font-weight: bold;
}

#queue-refresh {
  min-height: 30vh;
  padding: 10px;
}
</style>
