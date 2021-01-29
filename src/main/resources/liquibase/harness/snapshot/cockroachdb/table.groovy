import liquibase.harness.snapshot.SnapshotTest
import liquibase.snapshot.DatabaseSnapshot
import liquibase.structure.core.Table

[
        [
                setup : "create table test_table (test_col int, col2 varchar(10))",
                verify: {
                    DatabaseSnapshot snapshot ->
                        snapshot.get(new Table(name: "test_table")).with {
                            assert name == "test_table"
                            assert columns*.name.toString().equalsIgnoreCase("[test_col, col2, rowid]")
                            assert primaryKey != null
                            //for cockroach rowid is auto created for tables without primary keys
                        }

                }
        ],
] as SnapshotTest.TestConfig[]

