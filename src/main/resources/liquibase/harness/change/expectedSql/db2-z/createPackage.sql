SKIP TEST: INT-2469 - db2-z has no packages; the createPackage changelog only deploys plain
function/table SQL, which the sql and createFunction tests already cover. Package coverage lives in
liquibase-pro-tests UpdateTests / PostgresEnd2EndTests on oracle and edb-edb.
