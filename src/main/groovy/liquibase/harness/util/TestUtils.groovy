package liquibase.harness.util

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.change.Change
import liquibase.changelog.ChangeSet
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.sql.Sql
import liquibase.sqlgenerator.SqlGeneratorFactory
import liquibase.util.StringUtil

import java.util.logging.Logger

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

    static SortedMap<String, String> resolveInputFilePaths(DatabaseUnderTest database, String basePath, String inputFormat) {
        inputFormat = inputFormat ?: ""
        def returnPaths = new TreeMap<String, String>()
        for (String filePath : TestConfig.instance.resourceAccessor.list(null, basePath, true, true, false)) {
            def validFile = false
            //is it a common changelog?
            if (filePath =~ basePath+"/[\\w.]*\\."+inputFormat+"\$") {
                validFile = true
            } else if (filePath =~ basePath+"/${database.name}/[\\w.]*\\.${inputFormat}\$") {
                //is it a database-specific changelog?
                validFile = true
            } else if (filePath =~ basePath+"/${database.name}/${database.version}/[\\w.]*\\.${inputFormat}\$") {
                //is it a database-major-version specific changelog?
                validFile = true
            }
            if (validFile) {
                def fileName = filePath.replaceFirst(".*/", "").replaceFirst("\\.[^.]+\$", "")
                if (!returnPaths.containsKey(fileName) || returnPaths.get(fileName).length() < filePath.length()) {
                    returnPaths.put(fileName, filePath)
                }
            }
        }

        Logger.getLogger(this.class.name).info("Found " + returnPaths.size() + " changeLogs for " + database.name +
                "/" + database.version + " in "+basePath)
        return returnPaths
    }

    /**
     * Standardizes sql content. Removes line ending differences, and unnecessary leading/trailing whitespace
     * @param sql
     * @return
     */
    static String cleanSql(String sql) {
        if (sql == null) {
            return null
        }
        return StringUtil.trimToNull(sql.replace("\r", "")
                .replaceAll(/(?m)^--.*/, "") //remove comments
                .replaceAll(/(?m)^\s+/, "") //remove beginning whitepace per line
                .replaceAll(/(?m)\s+$/, "") //remove trailing whitespace per line
        ) //remove trailing whitespace per line
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
}
