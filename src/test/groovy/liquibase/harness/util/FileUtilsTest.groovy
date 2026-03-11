package liquibase.harness.util

import liquibase.database.OfflineConnection
import liquibase.database.core.MySQLDatabase
import liquibase.database.core.OracleDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.harness.config.DatabaseUnderTest
import liquibase.resource.ClassLoaderResourceAccessor
import spock.lang.Specification

class FileUtilsTest extends Specification {

    def "getInputFilesPaths"() {
        final String baseChangelogPath = "liquibase/harness/change/changelogs"

        given:

        def mySqlDatabase = new MySQLDatabase()
        mySqlDatabase.setConnection(new OfflineConnection("offline:mysql?version=8.1", new ClassLoaderResourceAccessor()))
        DatabaseUnderTest databaseUnderTestMySQL = new DatabaseUnderTest()
        databaseUnderTestMySQL.database = mySqlDatabase
        databaseUnderTestMySQL.name = "mysql"
        databaseUnderTestMySQL.version = "8"

        def postgreSqlDatabase = new PostgresDatabase()
        postgreSqlDatabase.setConnection(new OfflineConnection("offline:postgresql?version=12.1", new ClassLoaderResourceAccessor()))
        DatabaseUnderTest databaseUnderTestPostgre = new DatabaseUnderTest()
        databaseUnderTestPostgre.database = postgreSqlDatabase
        databaseUnderTestPostgre.name = "postgresql"
        databaseUnderTestPostgre.version = "12"

        def oracleDatabase = new OracleDatabase()
        oracleDatabase.setConnection(new OfflineConnection("offline:oracle?version=18.4.0", new ClassLoaderResourceAccessor()))
        DatabaseUnderTest databaseUnderTestOracle = new DatabaseUnderTest()
        databaseUnderTestOracle.database = oracleDatabase
        databaseUnderTestOracle.name = "oracle"
        databaseUnderTestOracle.version = "18.4.0"

        when:
        def changeLogPathsMySqlXmlFormat = FileUtils.resolveInputFilePaths(databaseUnderTestMySQL, baseChangelogPath, "xml")
        def changeLogPathsMySql = FileUtils.resolveInputFilePaths(databaseUnderTestMySQL, baseChangelogPath, "sql")

        then:
        changeLogPathsMySqlXmlFormat["addColumn"] == "liquibase/harness/change/changelogs/addColumn.xml"
        changeLogPathsMySqlXmlFormat["addPrimaryKey"] == "liquibase/harness/change/changelogs/addPrimaryKey.xml"
        changeLogPathsMySqlXmlFormat["renameColumn"] == "liquibase/harness/change/changelogs/renameColumn.xml"

        changeLogPathsMySql["renameColumn"] == "liquibase/harness/change/changelogs/mysql/8/renameColumn.sql"
        changeLogPathsMySql["renameTable"] == "liquibase/harness/change/changelogs/mysql/renameTable.sql"


        when:
        def changeLogPathsPostgreSql = FileUtils.resolveInputFilePaths(databaseUnderTestPostgre, baseChangelogPath, "xml")

        then:
        changeLogPathsPostgreSql["addColumn"] == "liquibase/harness/change/changelogs/addColumn.xml"
        changeLogPathsPostgreSql["addPrimaryKey"] == "liquibase/harness/change/changelogs/addPrimaryKey.xml"
        changeLogPathsPostgreSql["datatypes.binary"] == "liquibase/harness/change/changelogs/postgresql/datatypes.binary.xml"


        when:
        def changeLogPathsOracle = FileUtils.resolveInputFilePaths(databaseUnderTestOracle, baseChangelogPath, "xml")

        then:
        changeLogPathsOracle["addColumn"] == "liquibase/harness/change/changelogs/addColumn.xml"
        changeLogPathsOracle["addPrimaryKey"] == "liquibase/harness/change/changelogs/oracle/addPrimaryKey.xml"
        changeLogPathsOracle["datatypes.character"] == "liquibase/harness/change/changelogs/oracle/datatypes.character.xml"

    }

    def "substitutePlaceholders replaces catalog and schema placeholders"() {
        expect:
        FileUtils.substitutePlaceholders(input, catalogName, schemaName) == expected

        where:
        input                                                    | catalogName | schemaName | expected
        'ALTER TABLE ${CATALOG_NAME}.${SCHEMA_NAME}.authors'     | 'LTHDB'     | 'PUBLIC'   | 'ALTER TABLE LTHDB.PUBLIC.authors'
        'ALTER TABLE ${SCHEMA_NAME}.authors'                     | null        | 'public'   | 'ALTER TABLE public.authors'
        'ALTER TABLE "${SCHEMA_NAME}".authors'                   | null        | 'C##LIQ'   | 'ALTER TABLE "C##LIQ".authors'
        'SELECT * FROM authors'                                  | 'LTHDB'     | 'PUBLIC'   | 'SELECT * FROM authors'
        null                                                     | 'LTHDB'     | 'PUBLIC'   | null
        ''                                                       | 'LTHDB'     | 'PUBLIC'   | ''
        '${CATALOG_NAME}.${SCHEMA_NAME}'                         | null        | null       | '${CATALOG_NAME}.${SCHEMA_NAME}'
        '${CATALOG_NAME}.${SCHEMA_NAME}'                         | 'CAT'       | null       | 'CAT.${SCHEMA_NAME}'
    }

    def "shouldExcludeDefaultChangelogs returns true when marker file exists"() {
        given:
        final String baseChangelogPath = "liquibase/harness/change/changelogs"

        expect:
        FileUtils.shouldExcludeDefaultChangelogs("testdb-exclude", baseChangelogPath) == true
        FileUtils.shouldExcludeDefaultChangelogs("mysql", baseChangelogPath) == false
        FileUtils.shouldExcludeDefaultChangelogs("nonexistent-db", baseChangelogPath) == false
    }

    def "resolveInputFilePaths excludes defaults when marker exists"() {
        given:
        final String baseChangelogPath = "liquibase/harness/change/changelogs"

        def mySqlDatabase = new MySQLDatabase()
        mySqlDatabase.setConnection(new OfflineConnection("offline:mysql?version=8.1", new ClassLoaderResourceAccessor()))
        DatabaseUnderTest databaseWithDefaults = new DatabaseUnderTest()
        databaseWithDefaults.database = mySqlDatabase
        databaseWithDefaults.name = "mysql"
        databaseWithDefaults.version = "8"

        DatabaseUnderTest databaseExcludingDefaults = new DatabaseUnderTest()
        databaseExcludingDefaults.database = mySqlDatabase
        databaseExcludingDefaults.name = "testdb-exclude"
        databaseExcludingDefaults.version = "1.0"

        when:
        def pathsWithDefaults = FileUtils.resolveInputFilePaths(databaseWithDefaults, baseChangelogPath, "xml")
        def pathsWithoutDefaults = FileUtils.resolveInputFilePaths(databaseExcludingDefaults, baseChangelogPath, "xml")

        then:
        // MySQL should include default changelogs like addColumn
        pathsWithDefaults.containsKey("addColumn")
        pathsWithDefaults.containsKey("createTable")

        // testdb-exclude should NOT include default changelogs, only its own
        !pathsWithoutDefaults.containsKey("addColumn")
        !pathsWithoutDefaults.containsKey("createTable")
        pathsWithoutDefaults.containsKey("customTestChange")
        pathsWithoutDefaults["customTestChange"] == "liquibase/harness/change/changelogs/testdb-exclude/customTestChange.xml"
    }

}
