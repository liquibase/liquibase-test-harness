import liquibase.sdk.test.snapshot.SnapshotTest
import liquibase.snapshot.DatabaseSnapshot
import liquibase.structure.core.Column
import liquibase.structure.core.Table

[
        [
                setup : "create table test_table (test_col int)",
                verify: {
                    DatabaseSnapshot snapshot ->
                        snapshot.get(new Column(Table.class, null, null, "test_table", "test_col")).with {
                            assert type.typeName.toLowerCase().startsWith("int")
                        }

                }
        ],
        [
                setup : "create table test_table (test_col varchar(255))",
                verify: {
                    DatabaseSnapshot snapshot ->
                        snapshot.get(new Column(Table.class, null, null, "test_table", "test_col")).with {
                            assert type.typeName.toLowerCase() == "varchar"
                            assert type.columnSize == 255
                        }
                }
        ],
        [
                setup : "create table test_table (test_col int not null)",
                verify: { DatabaseSnapshot snapshot ->
                    snapshot.get(new Column(Table.class, null, null, "test_table", "test_col")).with {
                        assert !nullable
                    }
                }
        ],
//        new SnapshotTest.TestConfig(
//                setup: "create table UPPER_TABLE (UPPER_COL int)",
//                verify: { DatabaseSnapshot snapshot ->
//                    snapshot.get(new Column(Table.class, null, null, "UPPER_TABLE", "UPPER_COL")).with{
//                        assert name == "UPPER_COL"
//                    }
//                },
//        ),
] as SnapshotTest.TestConfig[]

