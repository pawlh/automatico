<script setup lang="ts">
import type { Submission } from '@/types/types'
import 'ag-grid-community/styles/ag-grid.css'
import 'ag-grid-community/styles/ag-theme-quartz.css'
import {
  commitVerificationFailed,
  generateClickableCommitLink,
  generateClickableLink,
  nameOnSubmission,
  phaseString,
  readableTimestamp,
  submissionScoreDisplayText,
  sortedItems
} from '@/utils/utils'
import RubricItemView from '@/views/AutograderViews/StudentView/RubricItemView.vue'
import InfoPanel from '@/components/InfoPanel.vue'
import { useAuthStore } from '@/stores/auth'
import { approveSubmissionPost } from '@/services/adminService'
import { ref } from 'vue'

const { submission } = defineProps<{
  submission: Submission;
}>();

const unapproved = ref<boolean>(true);

const approve = async (penalize: boolean, emit: (event: string, ...args: any[]) => void) => {
  try {
    await approveSubmissionPost(submission.netId, submission.phase, penalize);
  } catch (e) {
    console.log("Error while approving submission for " + submission.netId + " on phase " + submission.phase)
    alert("Something went wrong while sending the approval. Try refreshing the page before trying again")
    return
  }
  unapproved.value = false;
  emit("approvedSubmission")
}

</script>

<template>
  <div class="container">
    <h2>{{phaseString(submission.phase)}}</h2>
    <h3>{{nameOnSubmission(submission)}} ({{submission.netId}})</h3>
    <p>{{readableTimestamp(submission.timestamp)}}</p>
    <p v-html="generateClickableLink(submission.repoUrl)"/>
    <p>Commit: <span v-html="generateClickableCommitLink(submission.repoUrl, submission.headHash)"/></p>
    <p>Status:
      <span v-if="!submission.passed">failed <i class="fa-solid fa-circle-xmark" style="color: red"/></span>
      <span v-else-if="commitVerificationFailed(submission)">
        <i class="fa-solid fa-triangle-exclamation" style="color: red"/>
        <b> commit verification failed! Needs TA approval. Go see a TA </b>
        <i class="fa-solid fa-triangle-exclamation" style="color: red"/>
      </span>
      <span v-else>Passed <i class="fa-solid fa-circle-check" style="color: green"/></span>
    </p>

    <div v-if="useAuthStore().user?.role == 'ADMIN' && submission.passed && commitVerificationFailed(submission)">
      <InfoPanel id="approveSubmission">
        <h4>Approve Blocked Submission</h4>
        <p>This submission was blocked because it did not meet the git commit requirements.</p>
        <p>Meet with the student and explain the importance of frequent and consistent commits.</p>
        <p>You may, at your discretion, deduct 10% if it looks like the student is not learning the value/habit of repeated commits</p>
        <div id="approvalButtons" v-if="unapproved">
          <button @click="approve(true, $emit)">Approve with penalty</button>
          <button @click="approve(false, $emit)" class="small" style="font-weight: normal; font-size: 0.9rem">Approve without penalty</button>
        </div>
        <div v-else>
          <h4>Approval was successful! Grade sent to canvas</h4>
        </div>
      </InfoPanel>
    </div>

    <div id="important">
      <InfoPanel class="info-box">
        <p>Score:</p>
        <h1 v-if="!submission.passed">Failed</h1>
        <div v-else>
<!--          <h3 v-if="commitVerificationFailed(submission)">Score withheld for commits<br>Score Before Penalty:</h3>-->
          <p class="score" v-html="submissionScoreDisplayText(submission, false)"/>
        </div>
      </InfoPanel>
      <InfoPanel id="notesBox" class="info-box">
        <p>Notes:</p>
        <p v-html="submission.notes.replace('\n', '<br />')"/>
      </InfoPanel>
    </div>
  </div>
  <div class="container">
    <RubricItemView v-if="submission.rubric.items" v-for="item in sortedItems(submission.rubric.items)" :rubric-item="item"/>
  </div>

</template>

<style scoped>
#approveSubmission {
  text-align: left;
  align-items: start;
}
#approvalButtons {
  text-align: center;
  width: 100%;
}
.container {
  flex-direction: column;
  text-align: left;
  max-width: 600px;
}

.container p {
  padding: 1px 0;
}

#important {
  display: flex;
  flex-direction: row;
}

#notesBox {
  width: 100%;
}

.score {
  display: inline;
  font-size: 1.3em;
}
</style>
