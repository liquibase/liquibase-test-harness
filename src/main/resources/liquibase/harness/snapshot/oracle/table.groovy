import liquibase.harness.snapshot.SnapshotTest
import liquibase.snapshot.DatabaseSnapshot
import liquibase.structure.core.Table

[
        [
                setup : "create table test_table (test_col int, col2 varchar(10))",
                verify: {
                    DatabaseSnapshot snapshot ->
                        snapshot.get(new Table(name: "test_table")).with {
                            assert name == "TEST_TABLE"
                            assert columns*.name.toString().equalsIgnoreCase("[TEST_COL, COL2]")
                            assert primaryKey == null
                        }

                }
        ],
] as SnapshotTest.TestConfig[]

