<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import {Phase, type Submission, type User} from "@/types/types";
import {submissionsLatestGet} from "@/services/adminService";
import {useAdminStore} from "@/stores/admin";
import PopUp from "@/components/PopUp.vue";
import { AgGridVue } from 'ag-grid-vue3';
import type { CellClickedEvent } from 'ag-grid-community'
import 'ag-grid-community/styles/ag-grid.css';
import "ag-grid-community/styles/ag-theme-quartz.css";
import {
  standardColSettings,
  renderPhaseCell,
  renderScoreCell,
  renderTimestampCell
} from "@/utils/tableUtils";
import StudentInfo from "@/views/AutograderViews/AdminView/StudentInfo.vue";
import { generateClickableLink, isPlausibleRepoUrl, nameFromNetId } from '@/utils/utils'
import {adminSubmissionPost} from "@/services/submissionService";
import SubmissionInfo from '@/views/AutograderViews/StudentView/SubmissionInfo.vue'
import LiveStatus from '@/views/AutograderViews/StudentView/LiveStatus.vue'
import { useSubmissionStore } from '@/stores/submissions'
import InfoPanel from "@/components/InfoPanel.vue";

const selectedSubmission = ref<Submission | null>(null);
const selectedStudent = ref<User | null>(null);
const runningAdminRepo = ref<boolean>(false)
const DEFAULT_SUBMISSIONS_TO_LOAD = 25;
let allSubmissionsLoaded = false;
let adminRepo = reactive( {
  value: ""
})


onMounted(async () => { await resetPage() })

const resetPage = async () => {
  runningAdminRepo.value = false;
  selectedSubmission.value = null;
  allSubmissionsLoaded = false;
  const submissionsData = await submissionsLatestGet(DEFAULT_SUBMISSIONS_TO_LOAD);
  loadSubmissionsToTable(submissionsData);
}

const refreshSubmissions = async () => {
  if (allSubmissionsLoaded) {
    await loadAllSubmissions();
  } else {
    loadSubmissionsToTable(await submissionsLatestGet(DEFAULT_SUBMISSIONS_TO_LOAD))
  }
}

const loadAllSubmissions = async () => {
  loadSubmissionsToTable( await submissionsLatestGet() )
  allSubmissionsLoaded = true;
}

const loadSubmissionsToTable = (submissionsData : Submission[]) => {
  const dataToShow: any = [];
  submissionsData.forEach(submission => {
    dataToShow.push( {
        ...submission,
          name: nameFromNetId(submission.netId),
        }
    )
  })
  rowData.value = dataToShow
}

const openSubmission = (event: CellClickedEvent) => {
  selectedSubmission.value = event.data
}

const adminDoneGrading = async () => {
  let data = await submissionsLatestGet(1)
  selectedSubmission.value = data[0]
}

const nameCellClicked = (event: CellClickedEvent) => {
  selectedStudent.value = useAdminStore().usersByNetId[event.data.netId];
}

const columnDefs = reactive([
  { headerName: "Name", field: 'name', flex:2, onCellClicked: nameCellClicked },
  { headerName: "Phase", field: 'phase', flex:1, cellRenderer: renderPhaseCell },
  { headerName: "Timestamp", field: 'timestamp', sort: 'desc', sortedAt: 0, filter: 'agDateColumnFilter', flex:1.5, cellRenderer: renderTimestampCell, onCellClicked: openSubmission},
  { headerName: "Score", field: 'score', flex:1, minWidth: 85, cellRenderer: renderScoreCell, onCellClicked: openSubmission },
  { headerName: "Notes", field: 'notes', flex:4, onCellClicked: openSubmission },
])
const rowData = reactive({
  value: []
})

const selectedAdminPhase = ref<Phase | null>(null)

const adminSubmit = async () => {
  if (selectedAdminPhase.value == null) {
    console.error("Tried to run an admin submission with no phase")
    return;
  }
  try {
    await adminSubmissionPost(selectedAdminPhase.value!, adminRepo.value)
    runningAdminRepo.value = true;
  } catch (error) {
    if (error instanceof Error) { alert("Error running grader: " + error.message) }
    else { alert("Unknown error running grader") }
  }
}
</script>


<template>
  <div class="adminSubmission">
    <input v-model="adminRepo.value" type="text" id="repoUrlInput" placeholder="Github Repo URL"/>
    <div id="submitDialog">
      <select id="phaseSelect" v-model="selectedAdminPhase">
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
      <button
        :disabled="(selectedAdminPhase === null)
          || useSubmissionStore().currentlyGrading
          || !isPlausibleRepoUrl(adminRepo.value)"
        class="primary"
        @click="adminSubmit">Submit</button>
    </div>
  </div>

  <ag-grid-vue
    class="ag-theme-quartz"
    style="height: 65vh"
    :columnDefs="columnDefs"
    :rowData="rowData.value"
    :defaultColDef="standardColSettings"
  ></ag-grid-vue>

  <div class="container">
    <p v-if="allSubmissionsLoaded">All latest submissions are loaded</p>
    <p v-else>Currently only the {{DEFAULT_SUBMISSIONS_TO_LOAD}} most recent latest submssions are loaded</p>
    <button id="loadMore" @click="loadAllSubmissions">
      <span v-if="allSubmissionsLoaded">Reload submissions list</span>
      <span v-else>Load all latest submissions</span>
    </button>
  </div>

  <PopUp
      v-if="selectedSubmission"
      @closePopUp="selectedSubmission = null">
    <SubmissionInfo
      :submission="selectedSubmission"
      @approvedSubmission="refreshSubmissions"/>
  </PopUp>

  <PopUp
      v-if="selectedStudent"
      @closePopUp="selectedStudent = null">
    <StudentInfo :student="selectedStudent"></StudentInfo>
  </PopUp>

  <PopUp
    v-if="runningAdminRepo"
    @closePopUp="resetPage">
    <div v-if="!selectedSubmission">
      <h3 style="width: 70vw">Running Grader As Admin</h3>
      <p>Github Repo: <span v-html="generateClickableLink(adminRepo.value)"/></p>
      <InfoPanel>
        <LiveStatus @show-results="adminDoneGrading"/>
      </InfoPanel>
    </div>
    <SubmissionInfo
        v-if="selectedSubmission"
        :submission="selectedSubmission"/>
  </PopUp>

</template>

<style scoped>
#repoUrlInput {
  flex-grow: 1;
  padding: 10px;
  margin-right: 10px;
}
.adminSubmission {
  display: flex;
  padding: 10px;
}
.container {
  padding: 10px;
  display: grid;
  grid-template-columns: 2fr 1fr;
  margin: 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
  background-color: #f2f2f2;
  align-items: center;
}

#loadMore {
  font-size: medium;
}
</style>
