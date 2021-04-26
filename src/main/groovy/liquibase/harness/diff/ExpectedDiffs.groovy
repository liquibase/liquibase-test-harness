package liquibase.harness.diff

class ExpectedDiffs {
    List<String> missingObjects
    List<String> unexpectedObjects
    List<HarnessObjectDifference> changedObjects
}
