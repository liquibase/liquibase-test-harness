-- This test was created to reproduce a Liquibase bug. dropSequence doesn't work as it depends on creteSequence
CREATE SEQUENCE  IF NOT EXISTS public.test_sequence AS int START WITH 1 INCREMENT BY 1 MINVALUE 1
DROP SEQUENCE public.test_sequence CASCADE