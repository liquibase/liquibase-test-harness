package liquibase.sdk.test.util

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.change.Change
import liquibase.changelog.ChangeSet
import liquibase.database.Database
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.ResourceAccessor
import liquibase.sdk.test.config.DatabaseUnderTest
import liquibase.sdk.test.config.DatabaseVersion
import liquibase.sdk.test.config.TestConfig
import liquibase.sdk.test.config.TestInput
import liquibase.sql.Sql
import liquibase.sqlgenerator.SqlGeneratorFactory

import java.util.logging.Logger
import java.util.regex.Pattern

class TestUtils {
    final static List supportedChangeLogFormats = ['xml', 'sql', 'json', 'yml', 'yaml'].asImmutable()

    static ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor()

    static Liquibase createLiquibase(String pathToFile, Database database) {
        database.resetInternalState()
        return new Liquibase(pathToFile, resourceAccessor, database)
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
        def changelogPaths = resourceAccessor.list(null, "liquibase/sdk/test/changelogs", true, true, false)

        for (DatabaseUnderTest databaseUnderTest : config.databasesUnderTest) {
            for (DatabaseVersion databaseVersion : databaseUnderTest.versions) {
                for (def changeLogPath : changelogPaths) {
                    def validChangeLog = false

                    //is it a common changelog?
                    if (changeLogPath =~ /liquibase\/sdk\/test\/changelogs\/\w+\.\w+$/) {
                        validChangeLog = true
                    } else if (changeLogPath =~ Pattern.compile("liquibase/sdk/test/changelogs/${databaseUnderTest.name}/\\w+\\.\\w+\$")) {
                        //is it a database-specific changelog?
                        validChangeLog = true
                    } else if (changeLogPath =~ Pattern.compile("liquibase/sdk/test/changelogs/${databaseUnderTest.name}/${databaseVersion}/\\w+\\.\\w+\$")) {
                        //is it a database-version specific changelog?
                        validChangeLog = true
                    }

                    if (!validChangeLog) {
                        continue
                    }

                    def fileNameMatch = changeLogPath =~ /.*?(\w+)\.\w+$/
                    if (!fileNameMatch.matches()) {
                        throw new RuntimeException("Cannot find changeObject in " + changeLogPath)
                    }

                    def testInput = TestInput.builder()
                            .databaseName(databaseUnderTest.name)
                            .url(databaseVersion.url)
                            .dbSchema(databaseUnderTest.dbSchema)
                            .username(databaseUnderTest.username)
                            .password(databaseUnderTest.password)
                            .version(databaseVersion.version)
                            .context(config.context)
                            .changeObject(fileNameMatch.group(1))
                            .pathToChangeLogFile(changeLogPath)
                            .build()
                    testInput.database = DatabaseConnectionUtil.initializeDatabase(testInput)

                    inputList.add(testInput)
                }
            }
        }
        return inputList
    }

    static void validateAndSetPropertiesFromCommandLine(TestConfig testConfig) {
        def log = Logger.getLogger(this.class.name)

        String inputFormat = System.getProperty("inputFormat")
        String changeObjects = System.getProperty("changeObjects")
        String dbName = System.getProperty("dbName")
        String dbVersion = System.getProperty("dbVersion")
        if (inputFormat && (!supportedChangeLogFormats.contains(inputFormat))) {
            throw new IllegalArgumentException(inputFormat + " inputFormat is not supported")
        }
        testConfig.inputFormat = inputFormat ?: testConfig.inputFormat
        log.warning("Only " + testConfig.inputFormat + " input files are taken into account for this test run")

        if (changeObjects) {
            testConfig.defaultChangeObjects = Arrays.asList(changeObjects.split(","))
            //in case user provided changeObjects in cmd run only them regardless of config file
            for (def db : testConfig.databasesUnderTest) {
                db.databaseSpecificChangeObjects = null
            }
            log.info("running for next changeObjects : " + testConfig.defaultChangeObjects)
        }
        if (dbName) {
            //TODO try improve this, add logging
            testConfig.databasesUnderTest = testConfig.databasesUnderTest.stream()
                    .filter({ it.name.equalsIgnoreCase(dbName) })
                    .findAny()
                    .map({ Collections.singletonList(it) })
                    .orElse(testConfig.databasesUnderTest)

            if (dbVersion)
                for (DatabaseUnderTest databaseUnderTest : testConfig.databasesUnderTest) {
                    databaseUnderTest.versions = databaseUnderTest.versions.stream()
                            .filter({ it.version.equalsIgnoreCase(dbVersion) })
                            .findAny()
                            .map({ Collections.singletonList(it) })
                            .orElse(databaseUnderTest.versions)
                }
        }
        log.info(testConfig.toString())
    }
}
