package liquibase.harness.diff

import com.datical.liquibase.ext.config.LiquibaseProConfiguration
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import org.apache.commons.io.FileUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.Paths

import static liquibase.harness.diff.DiffCommandTestHelper.*
import static liquibase.harness.util.TestUtils.*
import static liquibase.harness.util.FileUtils.*

class DiffCommandTest extends Specification {
    @Shared
    RollbackStrategy strategy;
    @Shared
    List<DatabaseUnderTest> databases;

    def setupSpec() {
        databases = TestConfig.instance.getFilteredDatabasesUnderTest()
        strategy = chooseRollbackStrategy()
        strategy.prepareForRollback(databases)
    }

    @Unroll
    def "apply diffChangelog test against #testInput.referenceDatabase.name #testInput.referenceDatabase.version to targetDatabase #testInput.targetDatabase.name #testInput.targetDatabase.version"() {
        given: "read input data for diff changelog test"
        Map<String, Object> argsMap = new HashMap()
        argsMap.put("url", testInput.targetDatabase.url)
        argsMap.put("username", testInput.targetDatabase.username)
        argsMap.put("password", testInput.targetDatabase.password)
        argsMap.put("referenceUrl", testInput.referenceDatabase.url)
        argsMap.put("referenceUsername", testInput.referenceDatabase.username)
        argsMap.put("referencePassword", testInput.referenceDatabase.password)
        argsMap.put("changelogFile", testInput.pathToChangelogFile)

        Map<String, Object> argsMapRef = new HashMap()
        argsMapRef.put("url", testInput.referenceDatabase.url)
        argsMapRef.put("username", testInput.referenceDatabase.username)
        argsMapRef.put("password", testInput.referenceDatabase.password)
        argsMapRef.put("changelogFile", testInput.pathToReferenceChangelogFile)

        and: "check databases are online"
        assert testInput.targetDatabase.database.getConnection() instanceof JdbcConnection: "Target database " +
                "${testInput.targetDatabase.name}${testInput.targetDatabase.version} is offline!"
        assert testInput.referenceDatabase.database.getConnection() instanceof JdbcConnection: "Reference database " +
                "${testInput.referenceDatabase.name}${testInput.referenceDatabase.version} is offline!"

        when: "update changelog against target database and generate diff changelog for different file formats"

        executeCommandScope("update", argsMap)
        executeCommandScope("update", argsMapRef)

        argsMap.put("excludeObjects", "(?i)posts, (?i)authors, (?i)databasechangelog, (?i)databasechangeloglock")//excluding static test-harness objects from generated changelog
        def map = new LinkedHashMap<String, String>()
        map.put("changelogFileXml", testInput.pathToGeneratedXmlDiffChangelogFile)
        map.put("changelogFileSql", testInput.pathToGeneratedSqlDiffChangelogFile.replace(".sql", ".$testInput.targetDatabase.name" + ".sql"))
        map.put("changelogFileYml", testInput.pathToGeneratedYmlDiffChangelogFile)
        map.put("changelogFileJson", testInput.pathToGeneratedJsonDiffChangelogFile)
        for (Map.Entry<String, String> entry : map.entrySet()) {
            argsMap.put("changelogFile", entry.value)
            executeCommandScope("diffChangelog", argsMap)
        }

        and: "verify that the 'stored objects' directories are created"
        argsMapRef.put("referenceUrl", testInput.targetDatabase.url)
        argsMapRef.put("referenceUsername", testInput.targetDatabase.username)
        argsMapRef.put("referencePassword", testInput.targetDatabase.password)
        argsMapRef.put("changelogFile", testInput.pathToGeneratedXmlDiffChangelogFile)
        Map<String, Object> scopeValues = new HashMap<>()
        scopeValues.put(LiquibaseProConfiguration.INLINE_SQL_KEY.getKey(), false)
        executeCommandScope("diffChangelog", argsMapRef, scopeValues)
        File expectedDiffFolder = new File(testInput.pathToExpectedDiffFolder);
        File[] subDirs = expectedDiffFolder.listFiles(new FileFilter() {
            @Override
            boolean accept(File file) {
                return file.isDirectory()
            }
        })
        Optional<File> objectsFolder = Arrays.stream(subDirs)
                .filter(f -> f.getName().startsWith("objects-"))
                .findFirst();

        if (objectsFolder.isEmpty()) {
            throw new RuntimeException("No 'objects' folder created inside " + testInput.pathToExpectedDiffFolder);
        }
        File actualObjectsFolder = objectsFolder.get()
        for (String expectedObjectType : Arrays.asList("function", "trigger", "storedprocedure")) {
            File objectDir = new File(actualObjectsFolder, expectedObjectType);
            assert objectDir.exists() && objectDir.isDirectory() :
                    "Directory for stored object '" + expectedObjectType + "' was not created at path: " + objectDir.getPath() + "!";
        }


        then: "validate generated diff changelog"
        assert validateGeneratedDiffChangelog(testInput)

        cleanup: "try to rollback changes out from target database, delete generated changelog file"
        argsMap.put("changelogFile", testInput.pathToChangelogFile)
        argsMapRef.put("changelogFile", testInput.pathToReferenceChangelogFile)
        strategy.performRollback(argsMap)
        strategy.performRollback(argsMapRef)
        clearFolder(actualObjectsFolder.getAbsolutePath())
        for (Map.Entry<String, String> entry : map.entrySet()) {
            deleteFile(entry.value)
        }

        where:
        testInput << buildTestInput()
    }

//    @Unroll
//    def "apply diff test against #testInput.referenceDatabase.name #testInput.referenceDatabase.version to targetDatabase #testInput.targetDatabase.name #testInput.targetDatabase.version"() {
//        given: "read input data for diff test"
//        Map<String, Object> argsMap = new HashMap()
//        argsMap.put("url", testInput.targetDatabase.url)
//        argsMap.put("username", testInput.targetDatabase.username)
//        argsMap.put("password", testInput.targetDatabase.password)
//        argsMap.put("referenceUrl", testInput.referenceDatabase.url)
//        argsMap.put("referenceUsername", testInput.referenceDatabase.username)
//        argsMap.put("referencePassword", testInput.referenceDatabase.password)
//        argsMap.put("changelogFile", testInput.pathToChangelogFile)
//
//        and: "check databases are online"
//        assert testInput.targetDatabase.database.getConnection() instanceof JdbcConnection: "Target database " +
//                "${testInput.targetDatabase.name}${testInput.targetDatabase.version} is offline!"
//        assert testInput.referenceDatabase.database.getConnection() instanceof JdbcConnection: "Reference database " +
//                "${testInput.referenceDatabase.name}${testInput.referenceDatabase.version} is offline!"
//
//        when: "execute update and diff commands"
//
//        executeCommandScope("update", argsMap)
//        argsMap.put("excludeObjects", "(?i)posts, (?i)authors, (?i)databasechangelog, (?i)databasechangeloglock")
//        String generatedDiffContent = removeDatabaseInfoFromDiff(executeCommandScope("diff", argsMap).toString())
//
//        then: "validate generated diff"
//        String expectedDiffContent = removeDatabaseInfoFromDiff(getResourceContent(testInput.pathToExpectedDiffFile))
//        assert expectedDiffContent == generatedDiffContent
//
//        cleanup: "rollback changes"
//        strategy.performRollback(argsMap)
//
//        where:
//        testInput << buildTestInput()
//    }
//    def cleanupSpec() {
//        strategy.cleanupDatabase(databases)
//    }
}
