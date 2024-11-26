<script setup lang="ts">

import {onMounted, ref} from "vue";
import {commitAnalyticsGet} from "@/services/adminService";
import sound from "@/assets/wet-hands.mp3"

export type Option = 'update' | 'cached' | 'when'

const lastCache = ref<string>('')
const infoText = ref<string>('')
const cachedButtonDisabled = ref<boolean>(false)
const updateButtonDisabled = ref<boolean>(false)

const audio = new Audio(sound)

onMounted(async () => {
  await getMostRecent()
})

const getMostRecent = async () => {
  const data: string = await commitAnalyticsGet('when')
  if (data.length == 0) {
    lastCache.value = 'never'
    cachedButtonDisabled.value = true
  } else {
    lastCache.value = data
    cachedButtonDisabled.value = false
  }
}

const getCachedData = async () => {
  await getData('Downloading cached data...', 'cached', false)
}

const getNewData = async () => {
  await getData('Downloading data... (should take around 90 seconds) Enjoy this song in the meantime.',
      'update', true)
  await getMostRecent()
}

const getData = async (info: string, option: Option, music: boolean) => {
  updateButtonDisabled.value = true
  cachedButtonDisabled.value = true
  infoText.value = info
  if (music) playAudio()
  const data: string = await commitAnalyticsGet(option)
  triggerDownload(data, 'commit-data-' + Math.floor(Date.now() / 1000) + '.csv')
  if (data.length == 0) {
    infoText.value = 'Error occurred server side. Check logs or browser console.'
  } else {
    infoText.value = 'Complete! Check your downloads folder for the .csv file.'
  }
  if (music) stopAudio()
  updateButtonDisabled.value = false
  cachedButtonDisabled.value = false
}

const triggerDownload = (csvData: string, filename: string) => {
  const blob = new Blob([csvData], { type: 'text/csv' })
  const link = document.createElement('a')

  link.href = window.URL.createObjectURL(blob)
  link.download = filename

  document.body.appendChild(link)

  link.click()

  document.body.removeChild(link)
}

const playAudio = () => {
  audio.volume = .5
  audio.play()
}

const stopAudio = () => {
  audio.pause()
  audio.currentTime = 0
}

</script>

<template>
  <div class="container">
    <h3>Analytics Download</h3>
    <p class="desc">Downloading commit analytics CSV data can be done in one of two ways: downloading a cached version,
        which was compiled at the timestamp below, or updating the data. Updating the data takes around 90 seconds.
        By default, the data is sorted by section and then by netID.</p>
    <p>Last update: {{ lastCache }}</p>
    <button :disabled="cachedButtonDisabled" @click="getCachedData">Download Cached Data</button>
    <button :disabled="updateButtonDisabled" @click="getNewData">Download New Data</button>
    <p>CSV commit data contains these columns:</p>
    <table>
      <thead>
      <tr>
        <th>netID</th>
        <th>phase</th>
        <th>numCommits</th>
        <th>numDays</th>
        <th>section</th>
        <th>timestamp</th>
      </tr>
      </thead>
      <tbody>
      <tr>
        <td>
          The netID of the student
        </td>
        <td>
          The phase that was passed off
        </td>
        <td>
          The total number of commits for this phase
        </td>
        <td>
          How many distinct days are represented amongst the commits
        </td>
        <td>
          The section this student is in
        </td>
        <td>
          When this passoff occurred (mountain time)
        </td>
      </tr>
      </tbody>
    </table>
    <p>{{ infoText }}</p>
  </div>
</template>

<style scoped>
.container {
  padding: 10px;
  text-align: center;
}

.desc {
  text-align: center;
  width: 70%;
  margin: 0 auto;
}

button {
  margin: 0 .5rem;
}

table {
  width: 100%;
}

th {
  background-color: #333;
  color: #fff;
}

th, td {
  padding: 0.25rem;
  border: 1px solid #ccc;
}
</style>
