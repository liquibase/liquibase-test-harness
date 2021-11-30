package liquibase.harness.util

import liquibase.util.StringUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.JSONCompareResult
import org.skyscreamer.jsonassert.comparator.DefaultComparator
import org.skyscreamer.jsonassert.comparator.JSONCompareUtil

import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.getKeys
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.qualify

class SnapshotHelpers {

    static void snapshotMatchesSpecifiedStructure(String expected, String actual) {
        JSONAssert.assertEquals(expected, actual, new GeneralSnapshotComparator())
    }

    static class GeneralSnapshotComparator extends DefaultComparator {
        GeneralSnapshotComparator() {
            super(JSONCompareMode.LENIENT)
        }

        @Override
        void compareJSONArray(String prefix, JSONArray exp, JSONArray act, JSONCompareResult result) throws JSONException {
            if (exp.length() != 0) {
                if (JSONCompareUtil.allSimpleValues(exp)) {
                    this.compareJSONArrayOfSimpleValues(prefix, exp, act, result)
                } else if (JSONCompareUtil.allJSONObjects(exp)) {
                    this.compareJSONArrayOfJsonObjects(prefix, exp, act, result)
                } else {
                    this.recursivelyCompareJSONArray(prefix, exp, act, result)
                }
            }
        }

        @Override
        void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result) throws JSONException {
            if (expectedValue instanceof String && actualValue instanceof String) {
                if (actualValue.matches(expectedValue)) {
                    result.passed()
                } else if (!StringUtil.equalsIgnoreCaseAndEmpty(expectedValue, actualValue)) {
                    result.fail(prefix, expectedValue, actualValue)
                }
            } else {
                super.compareValues(prefix, expectedValue, actualValue, result)
            }
        }

        @Override
        protected void checkJsonObjectKeysExpectedInActual(String prefix, JSONObject expected, JSONObject actual,
                                                           JSONCompareResult result) throws JSONException {
            Set<String> expectedKeys = getKeys(expected)
            if (expected.has("_noMatch")) {
                expectedKeys.remove("_noMatch")
                expected.remove("_noMatch")
                for (String key : expectedKeys) {
                    if (actual.has(key)) {
                        if (actual instanceof JSONObject) {
                            checkArrayContainsObject(expected.getJSONArray(key), actual.getJSONArray(key))
                                    ? result.fail(prefix, expected, actual)
                                    : result.passed()
                        } else {
                            result.fail(prefix, expected, actual)
                        }
                    } else {
                        result.passed()
                        return
                    }
                }
            } else {
                for (String key : expectedKeys) {
                    Object expectedValue = expected.get(key)
                    if (actual.has(key)) {
                        Object actualValue = actual.get(key)
                        compareValues(qualify(prefix, key), expectedValue, actualValue, result)

                    } else {
                        result.missing(prefix, key)
                    }
                }
            }
        }

        private static boolean checkArrayContainsObject(JSONArray expected, JSONArray actual) {
            JSONObject expectedOuter = expected.get(0) as JSONObject

            Iterator iterator = expectedOuter.keys()
            while (iterator.hasNext()) {
                String expectedArrayName = iterator.next()
                JSONObject innerOne = expectedOuter.get(expectedArrayName) as JSONObject
                int objectsMatched = 0
                for (int i = 0; i < innerOne.names().length(); i++) {
                    String expectedPropertyName = innerOne.names().get(i)
                    String expectedPropertyValue = innerOne.get(innerOne.names().get(i) as String)
                    for (int j = 0; j < actual.length(); j++) {
                        JSONObject actualObjectOuter = actual.get(j) as JSONObject
                        JSONObject actualArray = actualObjectOuter.get(expectedArrayName) as JSONObject
                        String actualPropertyValue = actualArray.get(expectedPropertyName)
                        if (actualPropertyValue.equalsIgnoreCase(expectedPropertyValue)) {
                            objectsMatched++
//                            break
                        }
                    }
                }
                return objectsMatched == innerOne.names().length()
            }
        }
    }
}
