<script setup lang="ts">

import { onMounted, ref } from 'vue'
import {useSubmissionStore} from "@/stores/submissions";
import { Phase, type Submission } from '@/types/types'
import { uiConfig } from '@/stores/uiConfig'
import { submissionPost } from '@/services/submissionService'
import LiveStatus from '@/views/AutograderViews/StudentView/LiveStatus.vue'
import SubmissionHistory from '@/views/AutograderViews/StudentView/SubmissionHistory.vue'
import InfoPanel from '@/components/InfoPanel.vue'
import ResultsPreview from '@/views/AutograderViews/StudentView/ResultsPreview.vue'
import {useAppConfigStore} from "@/stores/appConfig";
import ShutdownWarning from '@/components/ShutdownWarning.vue'

// periodically check if grading is happening
onMounted(async () => {
  setInterval(async () => {
    if (!useSubmissionStore().currentlyGrading)
      await useSubmissionStore().checkGrading();
  }, 5000);
})

const selectedPhase = ref<Phase | null>(null);
const openGrader = ref<boolean>(false);
const showResults = ref<boolean>(false);
const lastSubmission = ref<Submission | null>(null);

const submitSelectedPhase = async () => {
  if (selectedPhase.value === null) { // make typescript happy
    console.error("submitPhase() was called without a phase selected")
    return
  }
  await submitPhase(selectedPhase.value)
}

const submitPhase = async (phase: Phase) => {
  try {
    showResults.value = false;
    await submissionPost(phase);
    openGrader.value = true;
    useSubmissionStore().currentlyGrading = true;
  } catch (e) {
    alert(e)
    useSubmissionStore().currentlyGrading = false;
  }
}

const handleGradingDone = async () => {
  useSubmissionStore().currentlyGrading = false;
  lastSubmission.value = await useSubmissionStore().getLastSubmission();
  showResults.value = true;
}

const isPhaseDisabled = () => {
  return selectedPhase.value != null && !useAppConfigStore().phaseActivationList[Phase[selectedPhase.value] as unknown as Phase]
}

</script>

<template>
  <div id="studentContainer">
    <ShutdownWarning/>
    <div id="submittingZone">
      <div id="phaseDetails">
        <h3 v-html="uiConfig.getPhaseName(selectedPhase)"/>
        <a
          target="_blank"
          :href="uiConfig.getSpecLink(selectedPhase)">
          <span v-if="selectedPhase">Review phase specs on Github</span>
          <span v-else>Review project specs on Github</span>
        </a>
      </div>

      <div v-if="isPhaseDisabled()">
        <br>
        <span id="submissionClosedWarning">Submissions to this phase are currently disabled</span>
      </div>

      <div id="submitDialog">
        <select v-model="selectedPhase" @change="useSubmissionStore().checkGrading()">
          <option :value=null selected disabled>Select a phase</option>
          <option :value=Phase.GitHub>GitHub Repository</option>
          <option :value=Phase.Phase0>Phase 0</option>
          <option :value=Phase.Phase1>Phase 1</option>
          <option :value=Phase.Phase3>Phase 3</option>
          <option :value=Phase.Phase4>Phase 4</option>
          <option :value=Phase.Phase5>Phase 5</option>
          <option :value=Phase.Phase6>Phase 6</option>
          <option :value=Phase.Quality>Code Quality Check</option>
        </select>
        <button :disabled="(selectedPhase === null) || isPhaseDisabled() || useSubmissionStore().currentlyGrading" class="primary" @click="submitSelectedPhase">Submit</button>
      </div>
    </div>

    <InfoPanel
      style="max-width: 100%; min-height: 300px; margin: 0; justify-content: center"
      v-if="openGrader">
      <LiveStatus v-if="useSubmissionStore().currentlyGrading" @show-results="handleGradingDone"/>
      <ResultsPreview v-if="showResults && lastSubmission" :submission="lastSubmission"/>
    </InfoPanel>

    <div id="submission-history" style="width: 100%">
      <div id="submission-history-header">
        <h3>Submission History</h3>
        <p>Click on a submission to see details</p>
      </div>
      <SubmissionHistory :key="lastSubmission?.timestamp"/>
    </div>
  </div>
</template>

<style scoped>
#submissionClosedWarning {
  background-color: red;
  padding: 10px;
  border-radius: 10px;
  color: white;
  font-weight: bold;
}

#studentContainer {
  width: 90%;
  display: flex;
  flex-direction: column;
  align-items: center;
}
#submitDialog {
  display: flex;
  align-items: center;
}

#submitDialog * {
  margin: 20px 5px;
}

h3 {
  font-size: xx-large;
}

#phaseDetails {
  display: flex;
  flex-direction: column;
  align-items: center;
}

#submittingZone {
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 500px;
}

#submission-history-header {
  text-align: center;
  margin: 15px;
}
</style>
