--liquibase formatted sql
--changeset oleh:1
-- Database: postgresql
-- Change Parameter: sequenceName=test_sequence
CREATE SEQUENCE  IF NOT EXISTS public.test_sequence AS int START WITH 1 INCREMENT BY 1 MINVALUE 1
--rollback DROP SEQUENCE public.test_sequence