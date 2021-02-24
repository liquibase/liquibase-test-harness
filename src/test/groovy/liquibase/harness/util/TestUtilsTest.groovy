package liquibase.harness.util

import liquibase.database.OfflineConnection
import liquibase.database.core.MySQLDatabase
import liquibase.database.core.OracleDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.harness.config.DatabaseUnderTest
import spock.lang.Specification

class TestUtilsTest extends Specification {

    def "getInputFilesPaths"() {
        final String baseChangelogPath = "liquibase/harness/change/changelogs"
        final String baseSnapshotPath = "liquibase/harness/snapshot"

        when:
        def database = new MySQLDatabase()
        database.setConnection(new OfflineConnection("offline:mysql?version=8.1", new ClassLoaderResourceAccessor()))

        DatabaseUnderTest databaseUnderTest = new DatabaseUnderTest()
        databaseUnderTest.database = database
        databaseUnderTest.name = "mysql"
        databaseUnderTest.version = "8"

        def changeLogPaths = TestUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath, "xml")

        then:
        changeLogPaths["addColumn"] == "liquibase/harness/change/changelogs/addColumn.xml"
        changeLogPaths["addPrimaryKey"] == "liquibase/harness/change/changelogs/addPrimaryKey.xml"
        changeLogPaths["renameColumn"] == "liquibase/harness/change/changelogs/renameColumn.xml"


        when:
        changeLogPaths = TestUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath, "sql")

        then:
        changeLogPaths["renameColumn"] == "liquibase/harness/change/changelogs/mysql/8/renameColumn.sql"
        changeLogPaths["renameTable"] == "liquibase/harness/change/changelogs/mysql/renameTable.sql"


        when:
        database = new PostgresDatabase()
        database.setConnection(new OfflineConnection("offline:postgresql?version=12.1", new ClassLoaderResourceAccessor()))

        DatabaseUnderTest databaseUnderTestPostgre = new DatabaseUnderTest()
        databaseUnderTestPostgre.database = database
        databaseUnderTestPostgre.name = "postgresql"
        databaseUnderTestPostgre.version = "12"
        changeLogPaths = TestUtils.resolveInputFilePaths(databaseUnderTestPostgre, baseChangelogPath, "xml")

        then:
        changeLogPaths["addColumn"] == "liquibase/harness/change/changelogs/addColumn.xml"
        changeLogPaths["addPrimaryKey"] == "liquibase/harness/change/changelogs/addPrimaryKey.xml"
        changeLogPaths["datatypes.binary"] == "liquibase/harness/change/changelogs/postgresql/datatypes.binary.xml"

        when:
        database = new OracleDatabase()
        database.setConnection(new OfflineConnection("offline:oracle?version=18.4.0", new ClassLoaderResourceAccessor()))
	
        DatabaseUnderTest databaseUnderTestOracle = new DatabaseUnderTest()
        databaseUnderTestOracle.database = database
        databaseUnderTestOracle.name = "oracle"
        databaseUnderTestOracle.version = "18"
        changeLogPaths = TestUtils.resolveInputFilePaths(databaseUnderTestOracle, baseChangelogPath, "xml")
        def snapshotPaths = TestUtils.resolveInputFilePaths(databaseUnderTestOracle, baseSnapshotPath, "groovy")

        then:
        changeLogPaths["addColumn"] == "liquibase/harness/change/changelogs/addColumn.xml"
        changeLogPaths["addPrimaryKey"] == "liquibase/harness/change/changelogs/oracle/addPrimaryKey.xml"
        changeLogPaths["datatypes.character"] == "liquibase/harness/change/changelogs/oracle/18.4.0/datatypes.character.xml"

        snapshotPaths["column"] == "liquibase/harness/snapshot/column.groovy"
        snapshotPaths["table"] == "liquibase/harness/snapshot/table.groovy"
    }
}
