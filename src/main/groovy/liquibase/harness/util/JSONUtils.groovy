package liquibase.harness.util

import groovy.json.JsonSlurper
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
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
     * Works only on the 1st level of nesting. Compare mode NON_EXTENSIBLE.
     */
    static boolean compareJSONArrays(JSONArray jsonArray, JSONArray jsonArrayToCompare) {
        if (jsonArray.length() != jsonArrayToCompare.length()) {
            return false
        }
        def compareMarker = true
        for (int i = 0; i < jsonArray.length(); i++) {
            if (!compareMarker) {
                return false
            }
            for (int j = 0; j < jsonArrayToCompare.length(); j++) {
                def jsonObjectRight = new JSONObject(jsonArray.get(i).toString())
                def jsonObjectLeft = new JSONObject(jsonArrayToCompare.get(j).toString())
                def result = JSONCompare.compareJSON(jsonObjectLeft, jsonObjectRight, JSONCompareMode.NON_EXTENSIBLE)
                compareMarker = result.passed()
                if (result.passed()) {
                    break
                }
            }
        }
        return compareMarker
    }

    /**
     * Compares values of elements in JSON arrays. Ignores order of elements.
     * Works only on the 1st level of nesting. Compare mode LENIENT.
     */
    static boolean compareJSONArraysExtensible(JSONArray jsonArray, JSONArray jsonArrayToCompare) {
        if (jsonArray.length() != jsonArrayToCompare.length()) {
            return false
        }
        def compareMarker = true
        for (int i = 0; i < jsonArray.length(); i++) {
            if (!compareMarker) {
                return false
            }
            for (int j = 0; j < jsonArrayToCompare.length(); j++) {
                def jsonObjectRight = new JSONObject(jsonArray.get(i).toString())
                def jsonObjectLeft = new JSONObject(jsonArrayToCompare.get(j).toString())
                def result = JSONCompare.compareJSON(jsonObjectLeft, jsonObjectRight, JSONCompareMode.LENIENT)
                compareMarker = result.passed()
                if (result.passed()) {
                    break
                }
            }
        }
        return compareMarker
    }

    static JSONObject getJsonFromResource(String resourceName) {
        return new JSONObject(FileUtils.getResourceContent(resourceName))
    }

    static void compareJSONObjects(JSONObject expected, JSONObject actual) {
        def mapExpected = new JsonSlurper().parseText(expected.toString())
        def mapActual = new JsonSlurper().parseText(actual.toString())
        assert mapExpected == mapActual
    }
}
