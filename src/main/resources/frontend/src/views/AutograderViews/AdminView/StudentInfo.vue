<script setup lang="ts">
import type {Submission, User} from "@/types/types";
import {onMounted, reactive, ref} from "vue";
import {submissionsForUserGet} from "@/services/adminService";
import { AgGridVue } from 'ag-grid-vue3';
import type { CellClickedEvent } from 'ag-grid-community'
import 'ag-grid-community/styles/ag-grid.css';
import "ag-grid-community/styles/ag-theme-quartz.css";
import PopUp from "@/components/PopUp.vue";
import {renderPhaseCell, renderScoreCell, renderTimestampCell, standardColSettings} from "@/utils/tableUtils";
import SubmissionInfo from "@/views/AutograderViews/StudentView/SubmissionInfo.vue";
import {generateClickableLink} from "@/utils/utils";
import RepoView from '@/views/AutograderViews/AdminView/RepoView.vue'

const { student } = defineProps<{
  student: User;
}>();

const studentSubmissions = ref<Submission[]>([])
const selectedSubmission = ref<Submission | null>(null);
const openRepoView = reactive({value: false})

onMounted(async () => {
  await loadStudentSubmissions()
});

const loadStudentSubmissions = async() => {
  studentSubmissions.value = await submissionsForUserGet(student.netId);
  var dataToShow: any = []
  studentSubmissions.value.forEach(submission => {
    dataToShow.push( submission )
  })
  rowData.value = dataToShow
}

const cellClickHandler = (event: CellClickedEvent) => {
  selectedSubmission.value = event.data;
}

const columnDefs = reactive([
  { headerName: "Phase", field: 'phase', flex:1, cellRenderer: renderPhaseCell },
  { headerName: "Timestamp", field: "timestamp", sort: 'desc', sortedAt: 0, flex:1, cellRenderer: renderTimestampCell},
  { headerName: "Score", field: "score", flex:1, cellRenderer: renderScoreCell, onCellClicked: cellClickHandler },
  { headerName: "Notes", field: "notes", flex:5, onCellClicked: cellClickHandler }
])
const rowData = reactive({
  value: []
})
</script>

<template>
  <h3>{{student.firstName}} {{student.lastName}}</h3>
  <p>netID: {{student.netId}}</p>
  <p v-if="student.role == 'STUDENT'">
    Github Repo:
    <span v-if="student.repoUrl" v-html="generateClickableLink(student.repoUrl)"/>
    <span v-else>No repo</span>
    <button
      v-if="student.role == 'STUDENT'"
      class="small"
      id="openRepoView"
      @click="openRepoView.value = true">
      History/Change
    </button>
  </p>

  <ag-grid-vue
      class="ag-theme-quartz"
      style="height: 35vh; width: 75vw"
      :columnDefs="columnDefs"
      :rowData="rowData.value"
      :defaultColDef="standardColSettings"
  ></ag-grid-vue>

  <PopUp
      v-if="selectedSubmission"
      @closePopUp="selectedSubmission = null">
    <SubmissionInfo :submission="selectedSubmission"
                    @approvedSubmission="loadStudentSubmissions"/>
  </PopUp>

  <PopUp
      v-if="openRepoView.value"
      @closePopUp="openRepoView.value = false">
    <RepoView :student="student"/>
  </PopUp>
</template>

<style scoped>
a:visited, a {
  color: darkblue;
  font-style: italic;
}

#openRepoView  {
  margin: 5px;
  font-size: smaller;
  padding: 2px;
}
</style>
