<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { Submission } from '@/types/types'
import { renderPhaseCell, renderScoreCell, renderTimestampCell, standardColSettings } from '@/utils/tableUtils'
import { submissionsGet } from '@/services/submissionService'
import { AgGridVue } from 'ag-grid-vue3'
import PopUp from '@/components/PopUp.vue'
import type { CellClickedEvent } from 'ag-grid-community'
import SubmissionInfo from '@/views/AutograderViews/StudentView/SubmissionInfo.vue'


onMounted(async () => { await loadSubmissions() })
const selectedSubmission = ref<Submission | null>(null);

const loadSubmissions = async () => {
  try {
    loadSubmissionsToTable( await submissionsGet(null) )
  } catch (e) {
    console.error(e)
  }
}

const loadSubmissionsToTable = (submissionsData : Submission[]) => {
  rowData.value = submissionsData
}

const handleSubmissionOpen = (event: CellClickedEvent) => {
  selectedSubmission.value = event.data
}

const columnDefs = reactive([
  { headerName: "Phase", field: 'phase', flex:1, onCellClicked: handleSubmissionOpen, cellRenderer: renderPhaseCell },
  { headerName: "Timestamp", field: 'timestamp', onCellClicked: handleSubmissionOpen, sort: 'desc', sortingOrder: 0, filter: 'agDateColumnFilter', flex:1.5, cellRenderer: renderTimestampCell},
  { headerName: "Score", field: 'score', flex:1, onCellClicked: handleSubmissionOpen, minWidth: 85, filter: false, sortable: false, cellRenderer: renderScoreCell },
  { headerName: "Notes", field: 'notes', flex:3, onCellClicked: handleSubmissionOpen, sortable: false, hide: (window.innerWidth < 700) },
])
const rowData = reactive({
  value: [] as Submission[]
})
</script>

<template>
  <ag-grid-vue
    class="ag-theme-quartz"
    style="height: 40vh; width: 100%"
    :columnDefs="columnDefs"
    :rowData="rowData.value"
    :defaultColDef="standardColSettings"
  ></ag-grid-vue>

  <PopUp
    v-if="selectedSubmission"
    @closePopUp="selectedSubmission = null">
    <SubmissionInfo :submission="selectedSubmission"/>
  </PopUp>
</template>

<style scoped>

</style>
