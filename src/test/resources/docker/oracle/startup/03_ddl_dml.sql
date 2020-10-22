--------------------------------------------------------
--  DDL for Table AUTHORS
--------------------------------------------------------

  CREATE TABLE "C##LIQUIBASE"."AUTHORS" 
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

  CREATE UNIQUE INDEX "C##LIQUIBASE"."AUTHORS_PK" ON "C##LIQUIBASE"."AUTHORS" ("ID") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  Constraints for Table AUTHORS
--------------------------------------------------------

  ALTER TABLE "C##LIQUIBASE"."AUTHORS" MODIFY ("ID" NOT NULL ENABLE);
  ALTER TABLE "C##LIQUIBASE"."AUTHORS" MODIFY ("FIRST_NAME" NOT NULL ENABLE);
  ALTER TABLE "C##LIQUIBASE"."AUTHORS" MODIFY ("LAST_NAME" NOT NULL ENABLE);
  ALTER TABLE "C##LIQUIBASE"."AUTHORS" MODIFY ("EMAIL" NOT NULL ENABLE);
  ALTER TABLE "C##LIQUIBASE"."AUTHORS" MODIFY ("BIRTHDATE" NOT NULL ENABLE);
  ALTER TABLE "C##LIQUIBASE"."AUTHORS" MODIFY ("ADDED" NOT NULL ENABLE);
  ALTER TABLE "C##LIQUIBASE"."AUTHORS" ADD CONSTRAINT "AUTHORS_PK" PRIMARY KEY ("ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 
  TABLESPACE "USERS"  ENABLE;

--------------------------------------------------------
--  insert data into authors
--------------------------------------------------------

INSERT INTO "C##LIQUIBASE"."AUTHORS" VALUES('1','Eileen','Lubowitz','ppaucek@example.org','04-MAR-91', CURRENT_TIMESTAMP);
INSERT INTO "C##LIQUIBASE"."AUTHORS" VALUES('2','Tamia','Mayert','shansen@example.org','27-MAR-16', CURRENT_TIMESTAMP);
INSERT INTO "C##LIQUIBASE"."AUTHORS" VALUES('3','Cyril','Funk','reynolds.godfrey@example.com','21-APR-88', CURRENT_TIMESTAMP);
INSERT INTO "C##LIQUIBASE"."AUTHORS" VALUES('4','Nicolas','Buckridge','xhoeger@example.net','03-MAR-17', CURRENT_TIMESTAMP);
INSERT INTO "C##LIQUIBASE"."AUTHORS" VALUES('5','Jayden','Walter','lillian66@example.com','27-FEB-10', CURRENT_TIMESTAMP);

--------------------------------------------------------
--  DDL for Table POSTS
--------------------------------------------------------

  CREATE TABLE "C##LIQUIBASE"."POSTS" 
   (	"ID" NUMBER(*,0), 
	"AUTHOR_ID" NUMBER(*,0), 
	"TITLE" VARCHAR2(255 BYTE), 
	"DESCRIPTION" VARCHAR2(500 BYTE), 
	"CONTENT" VARCHAR2(4000 BYTE), 
	"INSERTED_DATE" TIMESTAMP (6) -- DEFAULT CURRENT_TIMESTAMP
   ) SEGMENT CREATION DEFERRED 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 
 NOCOMPRESS LOGGING
  TABLESPACE "USERS" ;

--------------------------------------------------------
--  DDL for Index POSTS_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "C##LIQUIBASE"."POSTS_PK" ON "C##LIQUIBASE"."POSTS" ("ID") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  Constraints for Table POSTS
--------------------------------------------------------

  ALTER TABLE "C##LIQUIBASE"."POSTS" MODIFY ("ID" NOT NULL ENABLE);
  ALTER TABLE "C##LIQUIBASE"."POSTS" MODIFY ("AUTHOR_ID" NOT NULL ENABLE);
  ALTER TABLE "C##LIQUIBASE"."POSTS" MODIFY ("TITLE" NOT NULL ENABLE);
  ALTER TABLE "C##LIQUIBASE"."POSTS" MODIFY ("DESCRIPTION" NOT NULL ENABLE);
  ALTER TABLE "C##LIQUIBASE"."POSTS" MODIFY ("CONTENT" NOT NULL ENABLE);
  --ALTER TABLE "C##LIQUIBASE"."POSTS" MODIFY ("INSERTED_DATE" NOT NULL ENABLE);
  ALTER TABLE "C##LIQUIBASE"."POSTS" ADD CONSTRAINT "POSTS_PK" PRIMARY KEY ("ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 
  TABLESPACE "USERS"  ENABLE;

--------------------------------------------------------
--  insert data into posts
--------------------------------------------------------

INSERT INTO "C##LIQUIBASE"."POSTS" VALUES('1','1','temporibus','voluptatum','Fugit non et doloribus repudiandae.', CURRENT_TIMESTAMP);
INSERT INTO "C##LIQUIBASE"."POSTS" VALUES('2','2','ea','aut','Tempora molestias maiores provident molestiae sint possimus quasi.',CURRENT_TIMESTAMP);
INSERT INTO "C##LIQUIBASE"."POSTS" VALUES('3','3','illum','rerum','Delectus recusandae sit officiis dolor.', CURRENT_TIMESTAMP);
INSERT INTO "C##LIQUIBASE"."POSTS" VALUES('4','4','itaque','deleniti','Magni nam optio id recusandae.', CURRENT_TIMESTAMP);
INSERT INTO "C##LIQUIBASE"."POSTS" VALUES('5','5','ad','similique','Rerum tempore quis ut nesciunt qui excepturi est.', CURRENT_TIMESTAMP);
   