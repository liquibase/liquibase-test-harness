CREATE OR REPLACE TRIGGER test_trigger BEFORE UPDATE ON posts
                                                            FOR EACH ROW BEGIN END;
/

CREATE PUBLIC SYNONYM "AWS$ARN" FOR "AWS$ARN";

CREATE PUBLIC SYNONYM "AZURE$PA" FOR "AZURE$PA";

CREATE PUBLIC SYNONYM "GCP$PA" FOR "GCP$PA";

CREATE PUBLIC SYNONYM "OCI$RESOURCE_PRINCIPAL" FOR "OCI$RESOURCE_PRINCIPAL";