package liquibase.sdk.test.util

import liquibase.database.OfflineConnection
import liquibase.database.core.MySQLDatabase
import liquibase.resource.ClassLoaderResourceAccessor
import spock.lang.Specification

class TestUtilsTest extends Specification {

    def "getChangeLogPaths"() {
        when:
        def database = new MySQLDatabase()
        database.setConnection(new OfflineConnection("offline:mysql?version=8.1", new ClassLoaderResourceAccessor()))
        def paths = TestUtils.getChangeLogPaths(database)

        then:
        paths["addColumn"] == "liquibase/sdk/test/changelogs/addColumn.xml"
        paths["addPrimaryKey"] == "liquibase/sdk/test/changelogs/addPrimaryKey.xml"
        paths["renameColumn"] == "liquibase/sdk/test/changelogs/mysql/8/renameColumn.sql"
    }
}
