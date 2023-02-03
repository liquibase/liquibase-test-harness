CREATE SEQUENCE  IF NOT EXISTS "test_sequence" AS bigint START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;

CREATE VIEW "pg_buffercache" AS SELECT p.bufferid,
    p.relfilenode,
    p.reltablespace,
    p.reldatabase,
    p.relforknumber,
    p.relblocknumber,
    p.isdirty,
    p.usagecount,
    p.pinning_backends
   FROM pg_buffercache_pages() p(bufferid integer, relfilenode oid, reltablespace oid, reldatabase oid, relforknumber smallint, relblocknumber bigint, isdirty boolean, usagecount smallint, pinning_backends integer);;

CREATE VIEW "pg_stat_statements" AS SELECT pg_stat_statements.userid,
    pg_stat_statements.dbid,
    pg_stat_statements.queryid,
    pg_stat_statements.query,
    pg_stat_statements.calls,
    pg_stat_statements.total_time,
    pg_stat_statements.min_time,
    pg_stat_statements.max_time,
    pg_stat_statements.mean_time,
    pg_stat_statements.stddev_time,
    pg_stat_statements.rows,
    pg_stat_statements.shared_blks_hit,
    pg_stat_statements.shared_blks_read,
    pg_stat_statements.shared_blks_dirtied,
    pg_stat_statements.shared_blks_written,
    pg_stat_statements.local_blks_hit,
    pg_stat_statements.local_blks_read,
    pg_stat_statements.local_blks_dirtied,
    pg_stat_statements.local_blks_written,
    pg_stat_statements.temp_blks_read,
    pg_stat_statements.temp_blks_written,
    pg_stat_statements.blk_read_time,
    pg_stat_statements.blk_write_time
   FROM pg_stat_statements(true) pg_stat_statements(userid, dbid, queryid, query, calls, total_time, min_time, max_time, mean_time, stddev_time, rows, shared_blks_hit, shared_blks_read, shared_blks_dirtied, shared_blks_written, local_blks_hit, local_blks_read, local_blks_dirtied, local_blks_written, temp_blks_read, temp_blks_written, blk_read_time, blk_write_time);;

CREATE OR REPLACE FUNCTION "public".pg_buffercache_pages()
 RETURNS SETOF record
 LANGUAGE c
 PARALLEL SAFE
AS '$libdir/pg_buffercache', $function$pg_buffercache_pages$function$;

CREATE OR REPLACE FUNCTION "public".pg_stat_statements(showtext boolean, OUT userid oid, OUT dbid oid, OUT queryid bigint, OUT query text, OUT calls bigint, OUT total_time double precision, OUT min_time double precision, OUT max_time double precision, OUT mean_time double precision, OUT stddev_time double precision, OUT rows bigint, OUT shared_blks_hit bigint, OUT shared_blks_read bigint, OUT shared_blks_dirtied bigint, OUT shared_blks_written bigint, OUT local_blks_hit bigint, OUT local_blks_read bigint, OUT local_blks_dirtied bigint, OUT local_blks_written bigint, OUT temp_blks_read bigint, OUT temp_blks_written bigint, OUT blk_read_time double precision, OUT blk_write_time double precision)
 RETURNS SETOF record
 LANGUAGE c
 PARALLEL SAFE STRICT
AS '$libdir/pg_stat_statements', $function$pg_stat_statements_1_3$function$;

CREATE OR REPLACE FUNCTION "public".pg_stat_statements_reset()
 RETURNS void
 LANGUAGE c
 PARALLEL SAFE
AS '$libdir/pg_stat_statements', $function$pg_stat_statements_reset$function$;