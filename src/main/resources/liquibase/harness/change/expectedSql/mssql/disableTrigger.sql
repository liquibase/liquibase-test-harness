CREATE TRIGGER test_trigger ON posts AFTER UPDATE AS RAISERROR ('error', 1, 1)
DISABLE TRIGGER test_trigger  ON posts