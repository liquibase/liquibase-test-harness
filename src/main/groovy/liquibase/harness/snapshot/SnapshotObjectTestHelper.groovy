package liquibase.harness.snapshot

import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.TestUtils

class SnapshotObjectTestHelper {
    final static String baseSnapshotPath = "liquibase/harness/snapshot"

    static List<TestInput> buildTestInput() {
        def loader = new GroovyClassLoader()
        def returnList = new ArrayList<TestInput>()
        DatabaseConnectionUtil databaseConnectionUtil = new DatabaseConnectionUtil()

        for (def databaseUnderTest : databaseConnectionUtil.initializeDatabasesConnection(TestConfig.instance.databasesUnderTest)) {
            Map nameToPathMap = TestUtils.resolveInputFilePaths(databaseUnderTest, baseSnapshotPath, "groovy")
            for (def file : nameToPathMap.values()) {

                def testClass = loader.parseClass(new InputStreamReader(TestConfig.getInstance().resourceAccessor.openStream(null, file)), file)
                for (def testConfig : (Collection<SnapshotTest.TestConfig>) ((Script) testClass.newInstance()).run()) {
                    returnList.add(new TestInput(
                            database: databaseUnderTest,
                            permutation: testConfig,
                            testName: testClass.getName()
                    ))
                }

            }
        }
        return returnList
    }

    static class TestInput {
        DatabaseUnderTest database
        SnapshotTest.TestConfig permutation
        String testName
    }
}
