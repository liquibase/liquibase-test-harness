package liquibase.sdk.test.util

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.change.Change
import liquibase.changelog.ChangeSet
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.ResourceAccessor
import liquibase.sdk.test.config.DatabaseUnderTest
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
        for (DatabaseUnderTest databaseUnderTest : config.databasesUnderTest) {
            def database = DatabaseConnectionUtil.initializeDatabase(databaseUnderTest.url, databaseUnderTest.username, databaseUnderTest.password)
            if (database == null) {
                Logger.getLogger(TestUtils.name).info("Cannot connect to $databaseUnderTest.url. Using offline connection")

                for (def possibleDatabase : DatabaseFactory.getInstance().getImplementedDatabases()) {
                    if (possibleDatabase.getDefaultDriver(databaseUnderTest.url) != null) {
                        println "Database ${possibleDatabase.shortName} accepts $databaseUnderTest.url"

                        database = DatabaseConnectionUtil.initializeDatabase("offline:${possibleDatabase.shortName}", databaseUnderTest.username, null)
                        break
                    }
                }
            }

            for (def changeLogEntry : getChangeLogPaths(database).entrySet()) {
                def databaseName = databaseUnderTest.name
                if (databaseName == null) {
                    databaseName = database.getShortName()
                }

                def testInput = TestInput.builder()
                        .databaseName(databaseName)
                        .url(databaseUnderTest.url)
                        .dbSchema(databaseUnderTest.dbSchema)
                        .username(databaseUnderTest.username)
                        .password(databaseUnderTest.password)
                        .version(database.getDatabaseProductVersion())
                        .context(config.context)
                        .changeObject(changeLogEntry.key)
                        .pathToChangeLogFile(changeLogEntry.value)
                        .database(database)
                        .build()

                inputList.add(testInput)
            }
        }
        return inputList
    }

    protected static SortedMap<String, String> getChangeLogPaths(Database database) {
        def databaseShortName = database.getShortName()
        def majorVersion = database.getConnection().getDatabaseMajorVersion()
        def minorVersion = database.getConnection().getDatabaseMinorVersion()

        def returnPaths = new TreeMap<String, String>()
        for (String changeLogPath : resourceAccessor.list(null, "liquibase/sdk/test/changelogs", true, true, false)) {
            def validChangeLog = false

            //is it a common changelog?
            if (changeLogPath =~ Pattern.compile("liquibase/sdk/test/changelogs/[\\w.]+\$")) {
                validChangeLog = true
            } else if (changeLogPath =~ Pattern.compile("liquibase/sdk/test/changelogs/${databaseShortName}/[\\w.]+\$")) {
                //is it a database-specific changelog?
                validChangeLog = true
            } else if (changeLogPath =~ Pattern.compile("liquibase/sdk/test/changelogs/${databaseShortName}/${majorVersion}/[\\w.]+\$")) {
                //is it a database-major-version specific changelog?
                validChangeLog = true
            } else if (changeLogPath =~ Pattern.compile("liquibase/sdk/test/changelogs/${databaseShortName}/${majorVersion}/${minorVersion}/[\\w.]+\$")) {
                //is it a database-minor-version specific changelog?
                validChangeLog = true
            }

            if (validChangeLog) {
                def fileName = changeLogPath.replaceFirst(".*/", "").replaceFirst("\\..*?\$", "")
                if (!returnPaths.containsKey(fileName) || returnPaths.get(fileName).length() < changeLogPath.length()) {
                    returnPaths.put(fileName, changeLogPath)
                }
            }
        }

        Logger.getLogger(this.class.name).info("Found " + returnPaths.size() + " changeLogs for " + database.getShortName() + "/" + database.getDatabaseProductVersion() + " in liquibase/sdk/test/changelogs")


        return returnPaths
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

//        if (changeObjects) {
//            testConfig.defaultChangeObjects = Arrays.asList(changeObjects.split(","))
//            //in case user provided changeObjects in cmd run only them regardless of config file
//            for (def db : testConfig.databasesUnderTest) {
//                db.databaseSpecificChangeObjects = null
//            }
//            log.info("running for next changeObjects : " + testConfig.defaultChangeObjects)
//        }
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
