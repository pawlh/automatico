<script setup lang="ts">
import { AgGridVue } from 'ag-grid-vue3';
import type { CellClickedEvent } from 'ag-grid-community'
import 'ag-grid-community/styles/ag-grid.css';
import "ag-grid-community/styles/ag-theme-quartz.css";
import {onMounted, reactive, ref} from "vue";
import {testStudentModeGet, usersGet} from "@/services/adminService";
import PopUp from "@/components/PopUp.vue";
import type {User} from "@/types/types";
import StudentInfo from "@/views/AutograderViews/AdminView/StudentInfo.vue";
import {renderRepoLinkCell, standardColSettings} from "@/utils/tableUtils";
import Panel from "@/components/Panel.vue";

const selectedStudent = ref<User | null>(null);
let studentData: User[] = [];

const cellClickHandler = (event: CellClickedEvent) => {
  let findResult = studentData.find(user => user.netId === event.data.netId)
  selectedStudent.value = findResult || null; // Setting selected student opens a popup
}

onMounted(async () => {
  const userData = await usersGet();
  studentData = userData.filter(user => user.role == "STUDENT") // get rid of users that aren't students
  var dataToShow: any = []
  studentData.forEach(student => {
    dataToShow.push( {
          name: student.firstName + " " + student.lastName,
          netId: student.netId,
          repoUrl: student.repoUrl
        }
    )
  })
  rowData.value = dataToShow
})

const columnDefs = reactive([
  { headerName: "Student Name", field: "name", flex: 2, minWidth: 150, onCellClicked: cellClickHandler },
  { headerName: "BYU netID", field: "netId", flex: 1, minWidth: 75, onCellClicked: cellClickHandler },
  { headerName: "Github Repo URL", field: "repoUrl", flex: 5, sortable: false, cellRenderer: renderRepoLinkCell, onCellClicked: cellClickHandler }
])
const rowData = reactive({
  value: []
})

const activateTestStudentMode = async () => {
  await testStudentModeGet()
  window.location.href = '/';
}
</script>

<template>
  <Panel class="test-student-mode-container">
    <div>
      <p>Use "Test Student Mode" to use the autograder as the course's test student.</p>
      <p>To return to admin mode, just log out and then log in again.</p>
    </div>
    <div>
      <button @click="activateTestStudentMode">Go to Test Student Mode</button>
    </div>
  </Panel>

  <ag-grid-vue
      class="ag-theme-quartz"
      style="height: 75vh"
      :columnDefs="columnDefs"
      :rowData="rowData.value"
      :defaultColDef="standardColSettings"
  ></ag-grid-vue>

  <PopUp
      v-if="selectedStudent"
      @closePopUp="selectedStudent = null">
    <StudentInfo :student="selectedStudent"></StudentInfo>
  </PopUp>

</template>

<style scoped>
.test-student-mode-container {
  display: grid;
  grid-template-columns: 3fr 1fr;
}
</style>
