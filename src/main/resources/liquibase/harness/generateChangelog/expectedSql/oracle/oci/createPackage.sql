CREATE PUBLIC SYNONYM "AWS$ARN" FOR "AWS$ARN";

CREATE PUBLIC SYNONYM "AZURE$PA" FOR "AZURE$PA";

CREATE PUBLIC SYNONYM "GCP$PA" FOR "GCP$PA";

CREATE PUBLIC SYNONYM "OCI$RESOURCE_PRINCIPAL" FOR "OCI$RESOURCE_PRINCIPAL";

CREATE OR REPLACE PACKAGE test_package AS
            PROCEDURE test_procedure;
            END test_package;
/