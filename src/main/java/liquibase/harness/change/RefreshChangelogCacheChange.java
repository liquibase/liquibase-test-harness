package liquibase.harness.change;

import liquibase.change.custom.CustomTaskChange;
import liquibase.change.custom.CustomTaskRollback;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.changelog.StandardChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.executor.ExecutorService;
import liquibase.structure.core.DatabaseObjectFactory;
import liquibase.Scope;

/**
 * Forces Liquibase to refresh all internal caches by clearing them completely.
 * This includes changelog history, database structure snapshots, executor cache, and database factory cache.
 * This is useful in scenarios where the database has been modified outside of Liquibase's normal operation,
 * such as during testing or maintenance, to ensure a completely fresh state.
 */
public class RefreshChangelogCacheChange implements CustomTaskChange, CustomTaskRollback {

    @Override
    public void execute(Database database) throws CustomChangeException {
        try {
            // Clear changelog history cache
            ChangeLogHistoryService historyService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
            if (historyService instanceof StandardChangeLogHistoryService) {
                ((StandardChangeLogHistoryService) historyService).reset();
            }
            
            // Clear database object factory cache (database structure snapshots)
            DatabaseObjectFactory.getInstance().reset();
            
            // Clear executor service cache
            Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
            
            // Clear database factory cache
            DatabaseFactory.reset();
            
        } catch (Exception e) {
            // Log but don't fail - this is a best effort operation
            System.err.println("Warning: Could not clear all caches: " + e.getMessage());
        }
    }

    @Override
    public void rollback(Database database) throws CustomChangeException, RollbackImpossibleException {
        // No rollback needed - this is a cache operation
    }

    @Override
    public String getConfirmationMessage() {
        return "Changelog cache refreshed";
    }

    @Override
    public void setUp() {
        // No setup required
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        // Not needed for this change
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
}