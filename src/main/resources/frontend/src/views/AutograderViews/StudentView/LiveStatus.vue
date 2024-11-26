<script setup lang="ts">

import {onMounted, ref} from "vue";
import type {Submission} from "@/types/types";
import {subscribeToGradingUpdates} from "@/stores/submissions";

const emit = defineEmits<{
  "show-results": [submission: Submission];
}>();

type GradingStatus = {
  status: string;
  type: "update" | "warning" | "error";
}

const statuses = ref<GradingStatus[]>([]);
const warnings = ref<boolean>(false);
const submission = ref<Submission | undefined>(undefined);

onMounted(() => {
  subscribeToGradingUpdates((event: MessageEvent) => {
    const messageData = JSON.parse(event.data);

    switch (messageData.type) {
      case 'queueStatus':
        statuses.value.push({type: 'update', status: `You are currently #${messageData.position} in line`}) ;
        return;
      case 'started':
        statuses.value.push({type: 'update', status: `Autograding has started`});
        return;
      case 'warning':
        warnings.value = true;
        statuses.value.push({type: messageData.type, status: messageData.message});
        return;
      case 'update':
        statuses.value.push({type: messageData.type, status: messageData.message});
        return;
      case 'results':
        statuses.value.push({type: 'update', status: `Finished!`});
        const results = JSON.parse(messageData.results);
        if(!warnings.value) showResults(results);
        else submission.value = results;
        return;
      case 'error':
        statuses.value.push({type: 'error', status: `Error: ${messageData.message}`});
        warnings.value = true;
        const errorResults = JSON.parse(messageData.results);
        submission.value = errorResults;
        return;
    }
  });
});

const showResults = (results: Submission) => {
  emit("show-results", results);
}

const getStatusClass = (status: GradingStatus) => {
  switch (status.type) {
    case "warning":
      return "status warning"
    case "error":
      return "status error"
    default:
      return "status";
  }
}

</script>

<template>
<div class="status-container">
  <span v-for="status of statuses" :class=getStatusClass(status)>{{ status.status }}</span>
  <button v-if="warnings && submission" @click="() => {showResults(submission!)}">See Results</button>
</div>
</template>

<style scoped>
.status-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.status {
  font-size: 1.5rem;
  font-weight: bold;
  text-align: center;
}

.warning {
  background-color: #ff7;
}

.error {
  background-color: #f66;
}
</style>
