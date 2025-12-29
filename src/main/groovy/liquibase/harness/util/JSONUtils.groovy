package liquibase.harness.util

import groovy.json.JsonSlurper
import liquibase.util.StringUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.RegularExpressionValueMatcher
import org.skyscreamer.jsonassert.comparator.CustomComparator

import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException

class JSONUtils {

    static JSONArray mapResultSetToJSONArray(ResultSet resultSet) throws SQLException, JSONException {
        JSONArray jArray = new JSONArray()
        ResultSetMetaData rsmd = resultSet.getMetaData()
        int columnCount = rsmd.getColumnCount()
        while (resultSet.next()) {
            JSONObject jsonObject = new JSONObject()
            for (int index = 1; index <= columnCount; index++) {
                String column = rsmd.getColumnName(index)
                Object value = resultSet.getObject(column)
                if (value == null) {
                    jsonObject.put(column, "")
                } else if (value instanceof Integer) {
                    jsonObject.put(column, (Integer) value)
                } else if (value instanceof String) {
                    jsonObject.put(column, (String) value)
                } else if (value instanceof Boolean) {
                    jsonObject.put(column, (Boolean) value)
                } else if (value instanceof Date) {
                    jsonObject.put(column, ((Date) value))
                } else if (value instanceof Long) {
                    jsonObject.put(column, (Long) value)
                } else if (value instanceof Double) {
                    jsonObject.put(column, (Double) value)
                } else if (value instanceof Float) {
                    jsonObject.put(column, (Float) value)
                } else if (value instanceof BigDecimal) {
                    jsonObject.put(column, (BigDecimal) value)
                } else if (value instanceof Byte) {
                    jsonObject.put(column, (Byte) value)
                } else if (value instanceof byte[]) {
                    jsonObject.put(column, (byte[]) value)
                } else if (value instanceof Object) {
                    jsonObject.put(column, value)
                } else {
                    throw new IllegalArgumentException("Unmappable object type: " + value.getClass())
                }
            }
            jArray.put(jsonObject)
        }
        return jArray
    }

    /**
     * Compares exactly number and values of elements in JSON arrays. Ignores order of elements.
     */
    static boolean compareJSONArrays(JSONArray jsonArray, JSONArray jsonArrayToCompare, JSONCompareMode jsonCompareMode) {
        assert jsonArray.length() == jsonArrayToCompare.length(): "Expected ${jsonArray.length()} entries but got ${jsonArrayToCompare.length()}"

        for (int i = 0; i < jsonArray.length(); i++) {
            def foundMatch = false
            def unmatchedEntries = new LinkedHashMap()
            for (int j = 0; j < jsonArrayToCompare.length(); j++) {
                def jsonObjectRight = new JSONObject(jsonArray.get(i).toString())
                def jsonObjectLeft = new JSONObject(jsonArrayToCompare.get(j).toString())
                def result = JSONCompare.compareJSON(jsonObjectLeft, jsonObjectRight, new CustomComparator(
                        jsonCompareMode, new Customization("***", new RegularExpressionValueMatcher<>())
                ))

                if (result.passed()) {
                    foundMatch = true
                    break
                }

                unmatchedEntries.put(jsonArrayToCompare.get(j), StringUtil.limitSize(result.getMessage().replaceAll("\n ; \n", "\n"), 500))
            }

            if (!foundMatch) {
                String finalMessage = "Unexpected JSON entry: " + jsonArray.get(i).toString() + "\n"
                for (def unmatchedEntry : unmatchedEntries.entrySet()) {
                    finalMessage = finalMessage + "  DID NOT MATCH: " + unmatchedEntry.key + "\n  BECAUSE:\n" + StringUtil.indent((String) unmatchedEntry.value, 4) + "\n\n"
                }
                throw new AssertionError((Object) finalMessage)
            }
        }

        return true
    }

    static void compareJSONObjects(JSONObject expected, JSONObject actual) {
        def mapExpected = new JsonSlurper().parseText(expected.toString())
        def mapActual = new JsonSlurper().parseText(actual.toString())
        assert mapExpected == mapActual
    }

    /**
     * Checks if a JSON string represents an empty object {}.
     * Used to signal that snapshot/resultset verification should be skipped
     * for changetypes that don't produce verifiable state changes.
     *
     * @param jsonContent The JSON string to check
     * @return true if the content is null, empty, or an empty JSON object {}
     */
    static boolean isEmptyJsonObject(String jsonContent) {
        if (jsonContent == null) {
            return true
        }
        // Remove all whitespace to handle variations like "{ }" or "  {}  "
        String noWhitespace = jsonContent.replaceAll("\\s", "")
        return noWhitespace.isEmpty() || noWhitespace == "{}"
    }
}
