INVALID TEST

 unimplemented: ALTER COLUMN TYPE from int to varchar is prohibited until v21.1
  Hint: You have attempted to use a feature that is not yet implemented.

See: https://go.crdb.dev/issue-v/54844/v20.2
https://www.cockroachlabs.com/docs/v20.2/alter-column#parameters

In CockroachDB versions < v21.1, support for altering column types is limited to increasing the precision of the current column type