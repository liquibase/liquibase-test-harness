package liquibase.harness.snapshot

import liquibase.CatalogAndSchema
import liquibase.database.OfflineConnection
import liquibase.snapshot.SnapshotControl
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.statement.SqlStatement
import liquibase.statement.core.RawSqlStatement
import org.junit.Assume
import spock.lang.Specification
import spock.lang.Unroll

import static SnapshotObjectTestHelper.*

class SnapshotObjectTests extends Specification {

    @Unroll
    def "Snapshot #input.testName '#input.permutation.setup' on #input.database.name"() {
        when:
        Assume.assumeFalse("Cannot test against offline database", input.database.database.getConnection() instanceof OfflineConnection)

        input.database.database.execute([new RawSqlStatement(input.permutation.setup)] as SqlStatement[], null)
        input.database.database.commit()

        def snapshot = SnapshotGeneratorFactory.instance.createSnapshot(new CatalogAndSchema(null, null), input.database.database, new SnapshotControl(input.database.database))

        then:
        input.permutation.verify.apply(snapshot) == null

        cleanup:
        if (!(input.database.database.getConnection() instanceof OfflineConnection)) {
            def cleanupSql = input.permutation.cleanup
            def generatedCleanup = false
            if (cleanupSql == null) {
                if (input.permutation.setup.toLowerCase().startsWith("create ")) {
                    def splitSetup = input.permutation.setup.split("\\s+")
                    cleanupSql = "drop ${splitSetup[1]} ${splitSetup[2]}"
                    generatedCleanup = true
                }
            }
            assert cleanupSql != null: "No cleanup config specified and one cannot be auto-generated"
            try {
                input.database.database.execute([new RawSqlStatement(cleanupSql)] as SqlStatement[], null)
                input.database.database.commit()
            } catch (Throwable e) {
                if (generatedCleanup) {
                    throw new RuntimeException("Cannot execute generated cleanup statement $cleanupSql: $e.message. You may need to specify a 'cleanup' block", e)
                } else {
                    throw new RuntimeException("Cannot execute specified cleanup statement $cleanupSql: $e.message", e)
                }
            }
        }

        where:
        input << buildTestInput()
    }
}
