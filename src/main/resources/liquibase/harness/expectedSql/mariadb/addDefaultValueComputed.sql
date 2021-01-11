-- This test shows bug that NOW() function is not supported for MYSQL, at least for `date` type
-- https://datical.atlassian.net/browse/DAT-5605 should help adding more details
ALTER TABLE posts ALTER inserted_date SET DEFAULT NOW()
