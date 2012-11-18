/*
 * Database Server: MySQL 5.1
 * Database: e-TB Manager
 * Author: Ricardo Memoria
 * 
 * 1. Update the patient name composition option selected in the workspaces
 * 2. Include new feature in role
 * 3. Include support for laboratory functionalities creating laboratory sample by patient
 */

update MedicineRegimen, Regimen
set MedicineRegimen.monthstreatment = Regimen.monthsintensivephase
where MedicineRegimen.regimen_id = Regimen.id
and MedicineRegimen.phase=0;

update MedicineRegimen, Regimen
set MedicineRegimen.monthstreatment = Regimen.monthscontinuousphase
where MedicineRegimen.regimen_id = Regimen.id
and MedicineRegimen.phase=1;

update Workspace set patientNameComposition=3 where patientNameComposition=2;

ALTER TABLE UserRole ADD COLUMN `internalUse` BIT(1) NOT NULL DEFAULT False AFTER `Role_Name`;

update UserRole set internalUse = false;

insert into UserRole (changeable, code, executable, role_name, internalUse) 
  values (false, '040901', true, 'WSADDREMUSER', true);


update TransactionLog
set entityDescription= (select concat(p.patient_name, if(p.middleName is null, '', concat(' ', p.middleName)),
   if(p.lastName is null, '', concat(' ', p.lastName)))
  from Patient p inner join TbCase c on c.patient_id = p.id where c.id = TransactionLog.entityId)
where entityClass='TbCase';

update TransactionLog
set entityDescription = entityId
where entityDescription is null;




/* SUPPORT FOR LABORATORY FUNCTIONALITIES */
CREATE TABLE `etbmanager`.`PatientSample` (
  `ID` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `SampleNumber` VARCHAR(50) NULL,
  `dateCollected` DATE NOT NULL,
  `CASE_ID` INTEGER NOT NULL,
  `ExamCulture_ID` INTEGER NULL,
  `ExamSputumSmear_ID` INTEGER NULL,
  `ExamSusceptibilityTest_ID` INTEGER NULL,
  PRIMARY KEY (`ID`)
);

ALTER TABLE `etbmanager`.`PatientSample`
 ADD CONSTRAINT `CTRT_samplecase` FOREIGN KEY `FK_samplecase` (`CASE_ID`)
    REFERENCES `TbCase` (`ID`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,
 ADD CONSTRAINT `CTRT_smpCulture` FOREIGN KEY `FK_smpCulture` (`ExamCulture_ID`)
    REFERENCES `ExamCulture` (`ID`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,
 ADD CONSTRAINT `CTRT_smpSputum` FOREIGN KEY `FK_smpSputum` (`ExamSputumSmear_ID`)
    REFERENCES `ExamSputumSmear` (`ID`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,
 ADD CONSTRAINT `CTRT_smpDST` FOREIGN KEY `FK_smpDST` (`ExamSusceptibilityTest_ID`)
    REFERENCES `ExamSusceptibilityTest` (`ID`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT
    ;

/* UPDATE EXAMS ALREADY EXISTING */
/* EXAM SPUTUM SMEAR */

create temporary table tmp ( id integer);

insert into tmp select max(e.id)
from ExamSputumSmear e
group by e.case_id, e.event_date
having count(*) > 1;

delete from ExamSputumSmear
 where ExamSputumSmear.id in (select id from tmp);

delete from tmp;

insert into PatientSample (datecollected, samplenumber, case_id, ExamSputumSmear_ID)
  select event_date, labidnumber, case_id, id from ExamSputumSmear;

ALTER TABLE `etbmanager`.`ExamSputumSmear` DROP COLUMN `EVENT_DATE`,
 DROP COLUMN `labIdNumber`,
 DROP COLUMN `CASE_ID`,
 DROP FOREIGN KEY `FKA5F78C098B327BA`;


/* EXAM CULTURE */
insert into tmp select max(e.id)
from ExamCulture e
group by e.case_id, e.event_date
having count(*) > 1;

delete from ExamCulture
 where ExamCulture.id in (select id from tmp);


/* get ids of exam culture that already exists in patientSample*/
delete from tmp;
insert into tmp select e.id from PatientSample s
  inner join ExamCulture e on e.event_date = s.dateCollected
  and e.case_id = s.case_id;

/* include new samples */
insert into PatientSample (datecollected, samplenumber, case_id, examCulture_id)
  select e.event_date, e.labidnumber, e.case_id, e.id from ExamCulture e
  where e.id not in (select id from tmp);
/* update samples */
update PatientSample, ExamCulture
set PatientSample.examCulture_ID = ExamCulture.id
where ExamCulture.event_date = PatientSample.dateCollected
and ExamCulture.case_id = PatientSample.case_id;


ALTER TABLE `etbmanager`.`ExamCulture` DROP COLUMN `EVENT_DATE`,
 DROP COLUMN `labIdNumber`,
 DROP COLUMN `CASE_ID`,
  DROP FOREIGN KEY `FKB2F04C6F8B327BA`;

/* EXAM DST */
delete from tmp;

insert into tmp select max(e.id)
from ExamSusceptibilityTest e
group by e.case_id, e.event_date
having count(*) > 1;

delete from ExamSusceptibilityTest
 where ExamSusceptibilityTest.id in (select id from tmp);


/* get ids of examsmearsputum that already exists in patientSample*/
delete from tmp;
insert into tmp select e.id from PatientSample s
  inner join ExamSusceptibilityTest e on e.event_date = s.dateCollected
  and e.case_id = s.case_id;

/* include new samples */
insert into PatientSample (datecollected, samplenumber, case_id, ExamSusceptibilityTest_id)
  select e.event_date, e.labidnumber, e.case_id, id from ExamSusceptibilityTest e
  where e.id not in (select id from tmp);
/* update samples */
update PatientSample, ExamSusceptibilityTest
set PatientSample.examSusceptibilityTest_ID = ExamSusceptibilityTest.id
where ExamSusceptibilityTest.event_date = PatientSample.dateCollected
and ExamSusceptibilityTest.case_id = PatientSample.case_id;

ALTER TABLE `etbmanager`.`ExamSusceptibilityTest` DROP COLUMN `EVENT_DATE`,
 DROP COLUMN `labIdNumber`,
 DROP COLUMN `CASE_ID`,
 DROP FOREIGN KEY `FKD76A756C8B327BA`;

drop table tmp;

ALTER TABLE FieldValue ADD COLUMN other BIT(1);

update FieldValue set other = false;
