package liquibase.harness.data

import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.harness.change.ChangeObjectTestHelper.buildTestInput

class ChangeDataTest extends Specification {

    @Unroll
    def "apply #testInput.changeData against #testInput.databaseName, #testInput.version; verify generated query, checking query and obtained result set"() {

        where: "test input in next data table"
        testInput << buildTestInput()
    }
}
