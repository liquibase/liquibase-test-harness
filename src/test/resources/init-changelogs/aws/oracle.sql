--liquibase formatted sql
--changeset liquibase:1 splitStatements:false runAlways:true

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE AUTHORS';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
         RAISE;
      END IF;
END;

--changeset liquibase:2 runAlways:true

--------------------------------------------------------
--  DDL for Table AUTHORS
--------------------------------------------------------

CREATE TABLE "AUTHORS"
   (	"ID" NUMBER(*,0),
	"FIRST_NAME" VARCHAR2(50 BYTE),
	"LAST_NAME" VARCHAR2(50 BYTE),
	"EMAIL" VARCHAR2(100 BYTE),
	"BIRTHDATE" DATE,
	"ADDED" TIMESTAMP (6) DEFAULT CURRENT_TIMESTAMP
   ) SEGMENT CREATION DEFERRED
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
 NOCOMPRESS LOGGING
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Index AUTHORS_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "AUTHORS_PK" ON "AUTHORS" ("ID")
  PCTFREE 10 INITRANS 2 MAXTRANS 255
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  Constraints for Table AUTHORS
--------------------------------------------------------

  ALTER TABLE "AUTHORS" MODIFY ("ID" NOT NULL ENABLE);
  ALTER TABLE "AUTHORS" MODIFY ("FIRST_NAME" NOT NULL ENABLE);
  ALTER TABLE "AUTHORS" MODIFY ("LAST_NAME" NOT NULL ENABLE);
  ALTER TABLE "AUTHORS" MODIFY ("EMAIL" NOT NULL ENABLE);
  ALTER TABLE "AUTHORS" MODIFY ("BIRTHDATE" NOT NULL ENABLE);
  ALTER TABLE "AUTHORS" MODIFY ("ADDED" NOT NULL ENABLE);
  ALTER TABLE "AUTHORS" ADD CONSTRAINT "AUTHORS_PK" PRIMARY KEY ("ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255
  TABLESPACE "USERS"  ENABLE;

--------------------------------------------------------
--  insert data into authors
--------------------------------------------------------

INSERT INTO "AUTHORS" VALUES('1','Eileen','Lubowitz','ppaucek@example.org','04-MAR-91', TO_DATE('1996-05-04', 'yyyy-MM-dd'));
INSERT INTO "AUTHORS" VALUES('2','Tamia','Mayert','shansen@example.org','27-MAR-16', TO_DATE('2000-05-25', 'yyyy-MM-dd'));
INSERT INTO "AUTHORS" VALUES('3','Cyril','Funk','reynolds.godfrey@example.com','21-APR-88', TO_DATE('1997-09-22', 'yyyy-MM-dd'));
INSERT INTO "AUTHORS" VALUES('4','Nicolas','Buckridge','xhoeger@example.net','03-MAR-17', TO_DATE('1978-12-13', 'yyyy-MM-dd'));
INSERT INTO "AUTHORS" VALUES('5','Jayden','Walter','lillian66@example.com','27-FEB-10', TO_DATE('1979-12-06', 'yyyy-MM-dd'));

--changeset liquibase:3 splitStatements:false runAlways:true

  BEGIN
     EXECUTE IMMEDIATE 'DROP TABLE POSTS';
  EXCEPTION
     WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
           RAISE;
        END IF;
  END;

--changeset liquibase:4 runAlways:true

--------------------------------------------------------
--  DDL for Table POSTS
--------------------------------------------------------

  CREATE TABLE "POSTS"
   (	"ID" NUMBER(*,0),
	"AUTHOR_ID" NUMBER(*,0),
	"TITLE" VARCHAR2(255 BYTE),
	"DESCRIPTION" VARCHAR2(500 BYTE),
	"CONTENT" VARCHAR2(4000 BYTE),
	"INSERTED_DATE" DATE
   ) SEGMENT CREATION DEFERRED
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
 NOCOMPRESS LOGGING
  TABLESPACE "USERS" ;

--------------------------------------------------------
--  DDL for Index POSTS_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "POSTS_PK" ON "POSTS" ("ID")
  PCTFREE 10 INITRANS 2 MAXTRANS 255
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  Constraints for Table POSTS
--------------------------------------------------------

  ALTER TABLE "POSTS" MODIFY ("ID" NOT NULL ENABLE);
  ALTER TABLE "POSTS" MODIFY ("AUTHOR_ID" NOT NULL ENABLE);
  ALTER TABLE "POSTS" MODIFY ("TITLE" NOT NULL ENABLE);
  ALTER TABLE "POSTS" MODIFY ("DESCRIPTION" NOT NULL ENABLE);
  ALTER TABLE "POSTS" MODIFY ("CONTENT" NOT NULL ENABLE);
  ALTER TABLE "POSTS" ADD CONSTRAINT "POSTS_PK" PRIMARY KEY ("ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255
  TABLESPACE "USERS"  ENABLE;

--------------------------------------------------------
--  insert data into posts
--------------------------------------------------------

INSERT INTO "POSTS" VALUES('1','1','temporibus','voluptatum','Fugit non et doloribus repudiandae.', TO_DATE('1996-05-04', 'yyyy-MM-dd'));
INSERT INTO "POSTS" VALUES('2','2','ea','aut','Tempora molestias maiores provident molestiae sint possimus quasi.', TO_DATE('2000-05-25', 'yyyy-MM-dd'));
INSERT INTO "POSTS" VALUES('3','3','illum','rerum','Delectus recusandae sit officiis dolor.', TO_DATE('1997-09-22', 'yyyy-MM-dd'));
INSERT INTO "POSTS" VALUES('4','4','itaque','deleniti','Magni nam optio id recusandae.', TO_DATE('1978-12-13', 'yyyy-MM-dd'));
INSERT INTO "POSTS" VALUES('5','5','ad','similique','Rerum tempore quis ut nesciunt qui excepturi est.', TO_DATE('1979-12-06', 'yyyy-MM-dd'));