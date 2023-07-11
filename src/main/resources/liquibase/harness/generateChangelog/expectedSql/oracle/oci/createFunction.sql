CREATE OR REPLACE FUNCTION test_function(id IN NUMBER)
                                                RETURN VARCHAR2 AS
                                                BEGIN
                                                RETURN '2';
                                                END;
/

CREATE PUBLIC SYNONYM "AWS$ARN" FOR "AWS$ARN";

CREATE PUBLIC SYNONYM "AZURE$PA" FOR "AZURE$PA";

CREATE PUBLIC SYNONYM "GCP$PA" FOR "GCP$PA";

CREATE PUBLIC SYNONYM "OCI$RESOURCE_PRINCIPAL" FOR "OCI$RESOURCE_PRINCIPAL";