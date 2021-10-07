CREATE TRIGGER test_trigger ON posts AFTER UPDATE AS RAISERROR ('error', 1, 1)
sp_rename 'test_trigger', 'test_trigger_renamed'