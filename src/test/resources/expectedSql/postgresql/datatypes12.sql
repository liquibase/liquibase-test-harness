-- This test was created to reproduce a Liquibase bug. The sqlGenerator is producing "serial INTEGER" instead of "serial SERIAL")
CREATE TABLE public.datatypes12_test_table (serial SERIAL, bigserial BIGSERIAL)
