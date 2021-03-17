-- This test was created to reproduce a Liquibase bug. The sqlGenerator is producing "serial INTEGER" instead of "serial SERIAL")
-- https://github.com/liquibase/liquibase/issues/1393
CREATE TABLE datatypes10_test_table (serial INTEGER GENERATED BY DEFAULT AS IDENTITY, bigserial BIGINT GENERATED BY DEFAULT AS IDENTITY, macaddr8 MACADDR8)
