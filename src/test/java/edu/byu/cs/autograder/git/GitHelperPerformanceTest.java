package edu.byu.cs.autograder.git;

import edu.byu.cs.util.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class GitHelperPerformanceTest {
    private GitHelperUtils utils;

    @BeforeEach
    void preparation() {
        utils = new GitHelperUtils();
    }

    @Test
    void performanceTest() throws Exception {
        System.out.println("Executing performance tests\n");

        // Execute lots of tests
        var results = new LinkedList<PerformanceResults>();
        var performanceStart = Instant.now();
        for (int commits = 100; commits <= 1000; commits += 100) {
            for (int patches = 0; patches <= 1000; patches += 200) {
                results.add(executePerformanceTest(commits, patches, 6000, false));
            }
        }
        var performanceEnd = Instant.now();
        printTimeElapsed("performance testing", performanceStart, performanceEnd);

        // Print out the results
        var outputStart = Instant.now();
        File outputFile = File.createTempFile("git-performance-test", ".csv");
        String csv = toCsvString(results);
        FileUtils.writeStringToFile(csv, outputFile);
        var outputEnd = Instant.now();
        printTimeElapsed("output generation", outputStart, outputEnd);
        System.out.printf("Saved results to file: %s", outputFile.getAbsolutePath());
    }

    PerformanceResults executePerformanceTest(int totalCommits, int commitLines, int maxSeconds, boolean assertResults) throws GitAPIException {
        if (totalCommits > 24*60) {
            throw new IllegalArgumentException("totalCommits must be less than 24*60, the number of minutes in one day");
        }

        System.out.printf("\nExecuting performance test for %s commits with %s changed patches within %s seconds...\n", totalCommits, commitLines, maxSeconds);
        var testStart = Instant.now();
        long testDuration;
        long generationDuration;
        AtomicLong evaluationDuration = new AtomicLong();

        try (var repoContext = utils.initializeTest("performance-test", "performance.txt")){
            var generationStart = Instant.now();
            testDuration = printTimeElapsed("test initialization", testStart, generationStart);

            for (int i = 1; i <= totalCommits; ++i) {
                utils.makeCommit(
                        repoContext,
                        "Change " + i + "\nEmpty line",
                        0,
                        totalCommits - i,
                        commitLines
                );
            }
            var generationEnd = Instant.now();
            generationDuration = printTimeElapsed("generating commits", generationStart, generationEnd);

            Assertions.assertTimeout(Duration.ofSeconds(maxSeconds), () -> {
                var evaluationStart = Instant.now();
                CommitVerificationResult result = utils.evaluateRepo().eval(repoContext.git());
                var evaluationEnd = Instant.now();
                evaluationDuration.set(printTimeElapsed("evaluating history", evaluationStart, evaluationEnd));

                CommitVerificationResult expected = utils.generalCommitVerificationResult(false, totalCommits, 1);
                if (assertResults) utils.assertCommitVerification(expected, result);
            });
        }

        return new PerformanceResults(totalCommits, commitLines, testDuration, generationDuration, evaluationDuration.longValue());
    }


    private String toCsvString(List<PerformanceResults> results) {
        StringBuilder builder = new StringBuilder();
        builder.append(PerformanceResults.getCsvHeader());
        builder.append('\n');
        results.forEach(result -> {
            builder.append(result.toCsvEntry());
            builder.append('\n');
        });
        return builder.toString();
    }
    private record PerformanceResults(
            int numCommits,
            int numPatches,

            long initMillis,
            long generationMillis,
            long evaluationMillis
    ) {
        public static String getCsvHeader() {
            return "Num Commits, Num Patches, Initialization Millis, Generation Millis, Evaluation Millis";
        }
        public String toCsvEntry() {
            return numCommits + "," + numPatches + "," + initMillis + "," + generationMillis + "," + evaluationMillis;
        }
    }


    /**
     * Prints out the result, and also returns the duration in milliseconds.
     *
     * @param name
     * @param start
     * @param end
     * @return A long representing the duration of the operation in millis.
     */
    private long printTimeElapsed(String name, Instant start, Instant end) {
        long millis = end.minusMillis(start.toEpochMilli()).toEpochMilli();
        long seconds = end.minusSeconds(start.getEpochSecond()).getEpochSecond();
        System.out.printf("Finished %s in %d millis (%d) seconds\n", name, millis, seconds);
        return millis;
    }
}