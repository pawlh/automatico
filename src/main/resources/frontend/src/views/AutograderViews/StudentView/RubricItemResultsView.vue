<script setup lang="ts">

import {generateResultsHtmlStringFromTestNode, sanitizeHtml} from "@/utils/utils";
import type {TestResult} from "@/types/types";
import PopUp from "@/components/PopUp.vue";
import {ref} from "vue";

defineProps<{
  testResults?: TestResult
  textResults?: string
}>();

const areErrorDetailsOpen = ref<boolean>(false)
</script>

<template>
  <span id="testResults" v-if="testResults?.root" v-html="generateResultsHtmlStringFromTestNode(testResults.root, '')" />
  <span id="testResults" v-if="testResults?.extraCredit" v-html="generateResultsHtmlStringFromTestNode(testResults.extraCredit, '')" />
  <span id="textResults" v-else-if="textResults" v-html="sanitizeHtml(textResults)"/>

  <div class="itemHeader" id="programErrorWarning" v-if="testResults?.error" >
    <h3 class="failure">Your program produced errors</h3>
    <button id="errorLogButton" @click="areErrorDetailsOpen = true">View error output</button>
  </div>

  <PopUp v-if="areErrorDetailsOpen"
         @closePopUp="areErrorDetailsOpen = false">
    <p class="category">Program Error Output</p>
    <hr>
    <span class="failure">{{testResults?.error}}</span>
  </PopUp>
</template>

<style scoped>

#programErrorWarning {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  text-align: center;
}
#errorLogButton {
  font-size: 15px;
  margin-top: 10px;
}

#textResults, .failure {
  white-space: pre-wrap;
}

</style>
