package liquibase.sdk.test.util

import liquibase.sdk.test.config.DatabaseUnderTest
import liquibase.sdk.test.config.DatabaseVersion
import spock.lang.Specification

class TestUtilsTest extends Specification {

    def "getChangeLogPaths"() {
        when:
        def paths = TestUtils.getChangeLogPaths(new DatabaseUnderTest(name: "mysql"), new DatabaseVersion(version: 8))

        then:
        paths["addColumn"] == "liquibase/sdk/test/changelogs/addColumn.xml"
        paths["addPrimaryKey"] == "liquibase/sdk/test/changelogs/addPrimaryKey.xml"
        paths["renameColumn"] == "liquibase/sdk/test/changelogs/mysql/8/renameColumn.sql"
    }
}
