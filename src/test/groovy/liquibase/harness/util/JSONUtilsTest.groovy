package liquibase.harness.util

import spock.lang.Specification

class JSONUtilsTest extends Specification {

    def "isEmptyJsonObject correctly identifies empty JSON objects"() {
        expect:
        JSONUtils.isEmptyJsonObject(input) == expected

        where:
        input                      | expected | description
        // Cases that SHOULD skip verification (return true)
        null                       | true     | "null input (file not found)"
        ""                         | true     | "empty string (empty file)"
        "   "                      | true     | "whitespace only"
        "{}"                       | true     | "empty object - primary skip signal"
        "{ }"                      | true     | "empty object with internal whitespace"
        "  {}  "                   | true     | "empty object with surrounding whitespace"
        "  { }  "                  | true     | "empty object with whitespace everywhere"
        "\n{}\n"                   | true     | "empty object with newlines"
        "\t{}\t"                   | true     | "empty object with tabs"

        // Cases that SHOULD run verification (return false)
        '{"key": "value"}'         | false    | "object with content"
        '{"snapshot": {}}'         | false    | "object with nested empty object"
        '[]'                       | false    | "empty array (not an object)"
        '[{}]'                     | false    | "array containing empty object"
        '{"items": []}'            | false    | "object with empty array"
        '{ "a": 1 }'               | false    | "object with numeric value"
        '{"nested": {"deep": {}}}' | false    | "deeply nested structure"
    }

    def "isEmptyJsonObject handles edge cases from getJSONFileContent scenarios"() {
        expect: """
            These tests document expected behavior when getJSONFileContent returns different values.
            If getJSONFileContent logic changes, these tests will catch breaking changes.

            IMPORTANT: When file is not found (null), the test FAILS due to the assert BEFORE
            isEmptyJsonObject is ever called:
                assert shouldRunChangeSet: "No expectedSnapshot for ..."

            isEmptyJsonObject(null) returning true is a safety fallback, not the primary check.
        """
        JSONUtils.isEmptyJsonObject(input) == expected

        where:
        input                           | expected | scenario
        // getJSONFileContent returns null when file is not found
        // NOTE: In practice, the test fails at the assert before reaching isEmptyJsonObject
        null                            | true     | "Safety fallback - actual failure happens at assert before this"

        // getJSONFileContent returns file content as string when file exists
        "{}"                            | true     | "File exists with {} - skip verification"
        '{"createTable":[]}'            | false    | "File exists with actual snapshot data - run verification"
        '{"insertData":[{"ID":1}]}'     | false    | "File exists with actual resultset data - run verification"
    }
}
