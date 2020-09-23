package liquibase.harness.util

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.change.Change
import liquibase.changelog.ChangeSet
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.DatabaseVersion
import liquibase.harness.config.TestConfig
import liquibase.harness.config.TestInput
import liquibase.sql.Sql
import liquibase.sqlgenerator.SqlGeneratorFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.Function
import java.util.stream.Collectors

class TestUtils {
    final static List supportedChangeLogFormats = ['xml', 'sql', 'json', 'yml', 'yaml'].asImmutable()
    static Logger logger = LoggerFactory.getLogger(TestUtils.class)

    static Liquibase createLiquibase(String pathToFile, Database database) {
        database.resetInternalState()
        return new Liquibase(pathToFile, new HarnessResourceAccessor(), database)
    }

    static List<String> toSqlFromLiquibaseChangeSets(Liquibase liquibase) {
        Database db = liquibase.database
        List<ChangeSet> changeSets = liquibase.databaseChangeLog.changeSets
        List<String> stringList = new ArrayList<>()
        changeSets.each { stringList.addAll(toSql(it, db)) }
        return stringList
    }

    private static List<String> toSql(ChangeSet changeSet, Database db) {
        return toSql(changeSet.changes, db)
    }

    private static List<String> toSql(List<? extends Change> changes, Database db) {
        List<String> stringList = new ArrayList<>()
        changes.each { stringList.addAll(toSql(it, db)) }
        return stringList
    }

    private static List<String> toSql(Change change, Database db) {
        Sql[] sqls = SqlGeneratorFactory.newInstance().generateSql(change, db)
        return sqls*.toSql()
    }

    static ArrayList<CatalogAndSchema> getCatalogAndSchema(Database database, String dbSchema) {
        List<String> schemaList = parseValuesToList(dbSchema, ",")

        List<CatalogAndSchema> finalList = new ArrayList<>()
        schemaList?.each { sch ->
            String[] catSchema = sch.split("\\.")
            String catalog, schema
            if (catSchema.length == 2) {
                catalog = catSchema[0]?.trim()
                schema = catSchema[1]?.trim()
            } else if (catSchema.length == 1) {
                catalog = null
                schema = catSchema[0]?.trim()
            } else {
                return finalList
            }
            finalList.add(new CatalogAndSchema(catalog, schema).customize(database))
        }

        return finalList
    }

    static List<String> parseValuesToList(String str, String regex = null) {
        List<String> returnList = new ArrayList<>()
        if (str) {
            if (regex == null) {
                returnList.add(str)
                return returnList
            }
            return str?.split(regex)*.trim()
        }
        return returnList
    }

    static List<TestInput> buildTestInput(TestConfig config) {
        List<TestInput> inputList = new ArrayList<>()
        for (DatabaseUnderTest databaseUnderTest : config.databasesUnderTest) {
            for (DatabaseVersion databaseVersion : databaseUnderTest.versions) {
                Map<String, String> changeObjectsToChangeFileMap = FileUtils.collectChangeObjects(
                        config.defaultChangeObjects,
                        databaseUnderTest.databaseSpecificChangeObjects,
                        databaseUnderTest.name,
                        databaseVersion.version,
                        config.inputFormat)
                for (Map.Entry<String, String> entry : changeObjectsToChangeFileMap.entrySet()) {
                    inputList.add(TestInput.builder()
                            .databaseName(databaseUnderTest.name)
                            .url(databaseVersion.url)
                            .dbSchema(databaseUnderTest.dbSchema)
                            .username(databaseUnderTest.username)
                            .password(databaseUnderTest.password)
                            .version(databaseVersion.version)
                            .context(config.context)
                            .changeObject(entry.key)
                            .pathToChangeLogFile(entry.value)
                            .build()
                    )
                }
            }
        }
        return inputList
    }

    static void validateAndSetPropertiesFromCommandLine(TestConfig testConfig) {
        String inputFormat = System.getProperty("inputFormat")
        String changeObjects = System.getProperty("changeObjects")
        String dbName = System.getProperty("dbName")
        String dbVersion = System.getProperty("dbVersion")
        if (inputFormat && (!supportedChangeLogFormats.contains(inputFormat))) {
            throw new IllegalArgumentException(inputFormat + " inputFormat is not supported")
        }
        testConfig.inputFormat = inputFormat ?: testConfig.inputFormat
        logger.warn("Only {} input files are taken into account for this test run", testConfig.inputFormat)

        if (changeObjects) {
            testConfig.defaultChangeObjects = Arrays.asList(changeObjects.split(","))
            //in case user provided changeObjects in cmd run only them regardless of config file
            testConfig.databasesUnderTest.forEach(db -> db.databaseSpecificChangeObjects = null)
            logger.info("running for next changeObjects :{}", testConfig.defaultChangeObjects)
        }
        if (dbName) {
            //TODO try improve this, add logging
            testConfig.databasesUnderTest = testConfig.databasesUnderTest.stream()
                    .filter(database -> database.name.equalsIgnoreCase(dbName))
            .findAny()
            .map(database -> Collections.singletonList(database))
            .orElse(testConfig.databasesUnderTest)

            if (dbVersion)
                for (DatabaseUnderTest databaseUnderTest : testConfig.databasesUnderTest) {
                    databaseUnderTest.versions = databaseUnderTest.versions.stream()
                            .filter(version -> version.version.equalsIgnoreCase(dbVersion))
                    .findAny()
                    .map(version -> Collections.singletonList(version))
                            .orElse(databaseUnderTest.versions)
                }
        }
        logger.info(testConfig.toString())
    }
}
