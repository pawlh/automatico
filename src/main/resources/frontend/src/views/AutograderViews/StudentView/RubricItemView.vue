<script setup lang="ts">
import type { RubricItem } from '@/types/types'
import { resultsScoreDisplayText, sanitizeHtml } from '@/utils/utils'
import MoreInfo from '@/components/MoreInfo.vue'
import RubricItemResultsView from '@/views/AutograderViews/StudentView/RubricItemResultsView.vue'

defineProps<{
  rubricItem: RubricItem;
}>();

</script>

<template>
  <div id="rubric-item-container">

    <p class="itemHeader">
      <span class="category" v-html="rubricItem.category + ' '"/>
      <span class="score" v-html="resultsScoreDisplayText(rubricItem.results)"/>
    </p>
    <hr/>
    <div id="details">
      <div class="rubricDetails">
        <h4>Requirements</h4>
        <p v-html="rubricItem.criteria"/>
      </div>
      <div class="rubricDetails">
        <h4>Result Notes</h4>
        <p v-html="sanitizeHtml(rubricItem.results.notes)"/>
      </div>

      <MoreInfo v-if="rubricItem.results.testResults || rubricItem.results.textResults" text="details">
        <div>
          <span class="category" v-html="rubricItem.category + ' '"/>
          <span class="score" v-html="resultsScoreDisplayText(rubricItem.results) + '<br/>'"/>
        </div>
        <hr style="min-width: 250px; width: 100%"> <!-- the min-width is a round about way to make the window wide enough the button -->
        <RubricItemResultsView :test-results="rubricItem.results.testResults" :text-results="rubricItem.results.textResults"/>
      </MoreInfo>

    </div>
  </div>
</template>

<style scoped>
.category {
  font-weight: 700;
  font-size: 20px;
}

.score {
  font-weight: normal;
}

#details {
  display: flex;
}

#details > .rubricDetails {
  width: 45%;
  margin: 0 5px;
}

#moreDetails i {
  font-size: 30px;
}

.itemHeader {
  margin-top: 10px;
}
</style>
