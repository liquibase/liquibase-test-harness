SET ECHO ON;
WHENEVER SQLERROR EXIT SQL.SQLCODE;

CREATE USER "C##LIQUIBASE" IDENTIFIED BY "Secret321"  ;

GRANT "DBA" TO "C##LIQUIBASE" ;