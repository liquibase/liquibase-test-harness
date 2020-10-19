import liquibase.sdk.test.snapshot.SnapshotTest
import liquibase.snapshot.DatabaseSnapshot
import liquibase.structure.core.Column
import liquibase.structure.core.Table

[
        [
                setup : "create table test_table (test_col int, col2 varchar(10))",
                verify: {
                    DatabaseSnapshot snapshot ->
                        snapshot.get(new Table(name: "test_table")).with {
                            assert name == "test_table"
                            assert columns*.name.toString() == "[test_col, col2]"
                            assert primaryKey == null
                        }

                }
        ],
] as SnapshotTest.TestConfig[]

