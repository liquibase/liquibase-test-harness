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
        final String baseSnapshotPath = "liquibase/harness/snapshot"

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
        def snapshotPathsPostgreSql = FileUtils.resolveInputFilePaths(databaseUnderTestPostgre, baseSnapshotPath, "groovy")

        then:
        changeLogPathsPostgreSql["addColumn"] == "liquibase/harness/change/changelogs/addColumn.xml"
        changeLogPathsPostgreSql["addPrimaryKey"] == "liquibase/harness/change/changelogs/addPrimaryKey.xml"
        changeLogPathsPostgreSql["datatypes.binary"] == "liquibase/harness/change/changelogs/postgresql/datatypes.binary.xml"

        snapshotPathsPostgreSql["column"] == "liquibase/harness/snapshot/column.groovy"
        snapshotPathsPostgreSql["table"] == "liquibase/harness/snapshot/table.groovy"


        when:
        def changeLogPathsOracle = FileUtils.resolveInputFilePaths(databaseUnderTestOracle, baseChangelogPath, "xml")
        def snapshotPathsOracle = FileUtils.resolveInputFilePaths(databaseUnderTestOracle, baseSnapshotPath, "groovy")

        then:
        changeLogPathsOracle["addColumn"] == "liquibase/harness/change/changelogs/addColumn.xml"
        changeLogPathsOracle["addPrimaryKey"] == "liquibase/harness/change/changelogs/oracle/addPrimaryKey.xml"
        changeLogPathsOracle["datatypes.character"] == "liquibase/harness/change/changelogs/oracle/18.4.0/datatypes.character.xml"

        snapshotPathsOracle["column"] == "liquibase/harness/snapshot/oracle/column.groovy"
        snapshotPathsOracle["table"] == "liquibase/harness/snapshot/oracle/table.groovy"
    }
}
