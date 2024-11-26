<script setup lang="ts">
import { defineAsyncComponent, onMounted } from 'vue'
import { listOfPhases } from '@/types/types'
import { useAppConfigStore } from '@/stores/appConfig'
import { generateClickableLink, readableTimestamp } from '@/utils/utils'
import ConfigSection from '@/components/config/ConfigSection.vue'
import ScheduleShutdownEditor from '@/components/config/ScheduleShutdownEditor.vue'
import PenaltyConfigEditor from '@/components/config/PenaltyConfigEditor.vue'

// Lazy Load Editor Components
const BannerConfigEditor = defineAsyncComponent(() => import('@/components/config/BannerConfigEditor.vue'))
const LivePhaseConfigEditor = defineAsyncComponent(() => import('@/components/config/LivePhaseConfigEditor.vue'))
const CourseIdConfigEditor = defineAsyncComponent(() => import('@/components/config/CourseIdConfigEditor.vue'))

const appConfigStore = useAppConfigStore();

onMounted( async () => {
  await useAppConfigStore().updateConfig();
})

</script>

<template>
  <div id="configContainer">
    <ConfigSection title="Banner Message" description="A dynamic message displayed across the top of the Autograder">
      <template #editor="{ closeEditor }">
        <BannerConfigEditor :closeEditor="closeEditor"/>
      </template>
      <template #current>
      <div v-if="appConfigStore.bannerMessage">
        <p><span class="infoLabel">Message: </span><span v-text="appConfigStore.bannerMessage"/></p>
        <p><span class="infoLabel">Link: </span>
          <span v-if="appConfigStore.bannerLink" v-html="generateClickableLink(appConfigStore.bannerLink)"/>
          <span v-else>none</span>
        </p>
        <p><span class="infoLabel">Expires: </span><span v-text="readableTimestamp(appConfigStore.bannerExpiration)"/></p>
      </div>
        <p v-else>There is currently no banner message</p>
      </template>
    </ConfigSection>

    <ConfigSection title="Live Phases" description="These phases are live and open for students to submit to">
      <template #editor="{ closeEditor }">
        <LivePhaseConfigEditor :closeEditor="closeEditor"/>
      </template>
      <template #current>
        <div v-for="phase in listOfPhases()">
          <p>
            <i v-if="appConfigStore.phaseActivationList[phase]" class="fa-solid fa-circle-check" style="color: green"/>
            <i v-else class="fa-solid fa-x" style="color: red"/>
            {{phase}}</p>
        </div>
      </template>
    </ConfigSection>

    <ConfigSection title="Schedule Shutdown" description="Schedule a time for all graded phases to be deactivated for the end of the semester, per University Policy">
      <template #editor="{ closeEditor }">
        <ScheduleShutdownEditor :close-editor="closeEditor"/>
      </template>
      <template #current>
        <p><span class="infoLabel">Scheduled to shutdown: </span> {{readableTimestamp(appConfigStore.shutdownSchedule)}}</p>
        <p v-if="appConfigStore.shutdownSchedule != 'never'"><span class="infoLabel">Warning duration: </span> {{appConfigStore.shutdownWarningMilliseconds / (60 * 60 * 1000)}} hours</p>
      </template>
    </ConfigSection>

    <ConfigSection title="Penalties" description="Values used for calculating penalties">
      <template #editor="{ closeEditor }">
        <PenaltyConfigEditor :closeEditor="closeEditor"/>
      </template>
      <template #current>
        <p><span class="infoLabel">Late Penalty: </span>{{Math.round(appConfigStore.perDayLatePenalty * 100)}}%</p>
        <p><span class="infoLabel">Max Days Penalized: </span>{{appConfigStore.maxLateDaysPenalized}} days</p>

        <p><span class="infoLabel">Git Commit Penalty: </span>{{Math.round(appConfigStore.gitCommitPenalty * 100)}}%</p>
        <p><span class="infoLabel">Lines Per Commit: </span>{{appConfigStore.linesChangedPerCommit }} lines</p>
        <p><span class="infoLabel">Clock Forgiveness: </span>{{appConfigStore.clockForgivenessMinutes}} minutes</p>
      </template>
    </ConfigSection>

    <ConfigSection title="Course IDs" description="Phase assignment ID numbers, rubric IDs, and rubric points">
      <template #editor="{ closeEditor }">
        <CourseIdConfigEditor :closeEditor="closeEditor"/>
      </template>
      <template #current>
        <p><span class="infoLabel">Course ID:</span> {{appConfigStore.courseNumber}}</p>
        <p><em>Open editor to see the rest of the values</em></p>
      </template>
    </ConfigSection>
  </div>
</template>

<style scoped>
:deep(input) {
  border: 1px solid #ccc;
  padding: 8px;
  border-radius: 4px;
}
:deep(input[type="text"]) {
  width: 100%;
}

.infoLabel {
  font-weight: bold;
}

#configContainer {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr; /* Three columns of 100px each */
  grid-gap: 10px; /* Gap between grid items */
  margin: 10px;
}

input[type="text"]{
  padding: 5px;
  width: 100%;
}
</style>
