/*******************************************************************************
 * e-TB Manager script for version higher than 1.5-b191
 * by Ricardo Memoria
 *
 * This script will reformulate the permission list for user profiles,
 * incoroprating new permissions to the case and drug management and
 * sturcturing the permissions by case classification (TB, DR-TB or MNT)
 */

alter table UserRole add column byCaseClassification bit(1);

alter table UserPermission add column caseClassification int(11);

alter table UserRole drop column executable;

alter table TransactionLog
add column caseClassification int(11);

/* Include new permissions */

insert into UserRole (id, changeable, code, role_name, internalUse, byCaseClassification) values
  (147, false, '010112', 'CASE_PERSONALVIEW', false, false);

insert into UserRole (id, changeable, code, role_name, internalUse, byCaseClassification) values
  (140, true, '010101', 'CASE_DATA', false, false);

insert into UserRole (id, changeable, code, role_name, internalUse, byCaseClassification) values
  (141, true, '010103', 'CASE_INTAKEMED', false, false);

insert into UserRole (id, changeable, code, role_name, internalUse, byCaseClassification) values
  (146, true, '010105', 'CASE_ADDINFO', false, false);

insert into UserRole (id, changeable, code, role_name, internalUse, byCaseClassification) values
  (142, false, '010106', 'CASE_DRUGOGRAM', false, false);

insert into UserRole (id, changeable, code, role_name, internalUse, byCaseClassification) values
  (143, false, '010108', 'CASE_DEL_VAL', false, false);

insert into UserRole (id, changeable, code, role_name, internalUse, byCaseClassification) values
  (144, false, '020401', 'NEW_ORDER', false, false);

insert into UserRole (id, changeable, code, role_name, internalUse, byCaseClassification) values
  (145, false, '020701', 'NEW_TRANSFER', false, false);

/* set initially all case permissions to TB */
update UserPermission set caseclassification=0
where role_id in (100,140,106,141,105,142,101,102,103,104);

update TransactionLog set caseclassification=0
where role_id in (100,140,106,141,105,142,101,102,103,104);

update TransactionLog set role_id=140 where role_id=100;

/* MDRCASES */
update UserPermission
set role_id=140, caseclassification=1
where role_id=110;

update TransactionLog set caseClassification=1, role_id=140
where role_id=110;

/* NMT */
update UserPermission
set role_id=140, caseClassification=2
where role_id=137;

update TransactionLog set caseClassification=2, role_id=140
where role_id=137;


/* MDRVALIDATE */
update UserPermission
set role_id=101, caseClassification=1
where role_id=111;


/* MDRTRANSFER */
update UserPermission
set role_id=102, caseClassification=1
where role_id=112;


/* MDRCLOSE */
update UserPermission
set role_id=103, caseClassification=1
where role_id=113;


/* MDRREOPEN */
update UserPermission
set role_id=104, caseClassification=1
where role_id=114;

/* MDREXAMS */
update UserPermission
set role_id=105, caseClassification=1
where role_id=115;

/* MDRTREAT */
update UserPermission
set role_id=106, caseClassification=1
where role_id=116;


/* UPDATE PERMISSIONS OF USER PROFILES */

alter table UserPermission drop column canOpen;


/* DRTB CASE_VIEW */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select false, grantpermission, profile_id, 100, caseClassification
  from UserPermission where role_id=140 and caseClassification in (1,2);

  
/* CASE_DATA */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select false, grantpermission, profile_id, 140, caseClassification
  from UserPermission where role_id=100 and canchange=true;


/* CASE_PERSONALVIEW */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select false, grantpermission, profile_id, 147, caseClassification
  from UserPermission where role_id=100;

/* CASE_INTAKEMED */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 141, caseClassification
  from UserPermission where role_id=106;

/* CASE_DRUGOGRAM */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 142, caseClassification
  from UserPermission where role_id=100;

/* CASE_ADDINFO */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 146, caseClassification
  from UserPermission where role_id=100;

/* CASE_DELVAL */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 143, caseClassification
  from UserPermission where role_id=101;

/* MNT */
/* CASE_TREAT */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 106, caseClassification
  from UserPermission where role_id=100 and caseClassification=2;

/* CASE_INTAKEMED */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 141, caseClassification
  from UserPermission where role_id=100 and caseClassification=2;

/* CASE_EXAMS */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 105, caseClassification
  from UserPermission where role_id=100 and caseClassification=2;

/* CASE_VALIDATE */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 101, caseClassification
  from UserPermission where role_id=100 and caseClassification=2;

/* CASE_DELVAL */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 143, caseClassification
  from UserPermission where role_id=100 and caseClassification=2;

/* CASE_TRANSFER */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 102, caseClassification
  from UserPermission where role_id=100 and caseClassification=2;

/* CASE_CLOSE */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 103, caseClassification
  from UserPermission where role_id=100 and caseClassification=2;

/* CASE_REOPEN */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 104, caseClassification
  from UserPermission where role_id=100 and caseClassification=2;


update UserRole set bycaseclassification=0;

update UserRole set changeable=false, role_name='CASE_VIEW',bycaseClassification=1 where id=100;

update UserRole set code='010102', role_name='CASE_TREAT' where id=106;

update UserRole set code='010103', role_name='CASE_EXAMS' where id=105;

update UserRole set code='010107', role_name='CASE_VALIDATE' where id=101;

update UserRole set code='010109', role_name='CASE_TRANSFER' where id=102;

update UserRole set code='010110', role_name='CASE_CLOSE' where id=103;

update UserRole set code='010111', role_name='CASE_REOPEN' where id=104;

update TransactionLog
set role_id=140, caseClassification=1 where role_id=110;

update TransactionLog
set role_id=101, caseClassification=1 where role_id=111;

update TransactionLog
set role_id=102, caseClassification=1 where role_id=112;

update TransactionLog
set role_id=103, caseClassification=1 where role_id=113;

update TransactionLog
set role_id=104, caseClassification=1 where role_id=114;

update TransactionLog
set role_id=105, caseClassification=1 where role_id=115;

update TransactionLog
set role_id=106, caseClassification=1 where role_id=116;

update TransactionLog
set role_id=140, caseClassification=2 where role_id=137;


delete from UserRole where id in (110, 111, 112, 113, 114, 115, 116, 137);

update UserRole set role_name='MEDMAN' where id=9;

/* NEW_ORDER */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 144, caseClassification
  from UserPermission p
  inner join UserRole r on r.id = p.role_id
  where role_id=18 and changeable=true;

/* NEW_TRANSFER */
insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 145, caseClassification
  from UserPermission p
  inner join UserRole r on r.id = p.role_id
  where role_id=46 and changeable=true;


/* ORDERS */
update UserRole set changeable=false where id=18;

/* TRANSFER */
update UserRole set changeable=false where id=46;

/* MED_REPORTS */
insert into UserRole (id, changeable, code, role_name, internalUse, byCaseClassification) values
  (150, false, '021000', 'MED_REPORTS', false, false);

/* MED_REPORTS */
insert into UserRole (id, changeable, code, role_name, internalUse, byCaseClassification) values
  (151, false, '030500', 'INDICATORS', false, false);

insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 150, caseClassification
  from UserPermission p
  inner join UserRole r on r.id = p.role_id
  where role_id=37;

insert into UserPermission (canchange, grantpermission,
  profile_id, role_id, caseClassification)
  select canchange, grantpermission, profile_id, 151, caseClassification
  from UserPermission p
  inner join UserRole r on r.id = p.role_id
  where role_id=10;

/* STOCKPOS */
update UserRole set code='021001' where id=37;

/* MOVS */
update UserRole set code='021002' where id=27;

/* REL_ESTPOS */
update UserRole set code='021003', role_name='REP_ESTPOS' where id=31;

/* REL_EVOLPOS */
update UserRole set code='021004', role_name='REP_STOCKEVOL' where id=32;

/* REL_COSTPAT */
update UserRole set code='021005', role_name='REP_COSTPAT' where id=35;

/* TRANSF_PRT */
delete from UserRole where id=48;

/* ORDER_PRINT */
delete from UserRole where id=26;

update UserPermission set canExecute = true;

