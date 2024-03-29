CREATE OR REPLACE FORCE VIEW "TEST_VIEW" ("ID", "FIRST_NAME", "LAST_NAME", "EMAIL") AS select id, first_name, last_name, email from authors;

CREATE PUBLIC SYNONYM "AWS$ARN" FOR "AWS$ARN";

CREATE PUBLIC SYNONYM "AZURE$PA" FOR "AZURE$PA";

CREATE PUBLIC SYNONYM "GCP$PA" FOR "GCP$PA";

CREATE PUBLIC SYNONYM "OCI$RESOURCE_PRINCIPAL" FOR "OCI$RESOURCE_PRINCIPAL";