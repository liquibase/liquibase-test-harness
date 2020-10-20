package liquibase.sdk.test.util

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.change.Change
import liquibase.changelog.ChangeSet
import liquibase.database.Database
import liquibase.sdk.test.config.TestConfig
import liquibase.sql.Sql
import liquibase.sqlgenerator.SqlGeneratorFactory

import java.util.logging.Logger
import java.util.regex.Pattern

class TestUtils {

    static Liquibase createLiquibase(String pathToFile, Database database) {
        database.resetInternalState()
        return new Liquibase(pathToFile, TestConfig.instance.resourceAccessor, database)
    }

    static String toSqlFromLiquibaseChangeSets(Liquibase liquibase) {
        Database db = liquibase.database
        List<ChangeSet> changeSets = liquibase.databaseChangeLog.changeSets
        List<String> stringList = new ArrayList<>()
        changeSets.each { stringList.addAll(toSql(it, db)) }

        return stringList.join(System.lineSeparator())
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


    static SortedMap<String, String> getChangeLogPaths(Database database) {
        def databaseShortName = database.getShortName()
        def majorVersion = database.getConnection().getDatabaseMajorVersion()
        def minorVersion = database.getConnection().getDatabaseMinorVersion()

        def returnPaths = new TreeMap<String, String>()
        for (String changeLogPath : TestConfig.instance.resourceAccessor.list(null, "liquibase/sdk/test/changelogs", true, true, false)) {
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
                def fileName = changeLogPath.replaceFirst(".*/", "").replaceFirst("\\.[^.]+\$", "")
                if (!returnPaths.containsKey(fileName) || returnPaths.get(fileName).length() < changeLogPath.length()) {
                    returnPaths.put(fileName, changeLogPath)
                }
            }
        }

        Logger.getLogger(this.class.name).info("Found " + returnPaths.size() + " changeLogs for " + database.getShortName() + "/" + database.getDatabaseProductVersion() + " in liquibase/sdk/test/changelogs")


        return returnPaths
    }
}
