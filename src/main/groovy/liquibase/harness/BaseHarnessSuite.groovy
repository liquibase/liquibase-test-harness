package liquibase.harness;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses([ChangeObjectTests, SnapshotObjectTests])
public abstract class BaseHarnessSuite {
}
