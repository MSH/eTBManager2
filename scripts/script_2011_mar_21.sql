/***************************************************************
* Inclusion of 2 new permissions to control case comments
* Include tables for case comments and modifications in comorbidity
* 
* By Ricardo Memoria - 21-mar-2011
****************************************************************/

insert into UserRole (id, changeable, code, Role_Name, internalUse, byCaseClassification)
values (159, false, '010113', 'CASE_COMMENTS', false, true);

insert into UserRole (id, changeable, code, Role_Name, internalUse, byCaseClassification)
values (160, false, '010114', 'REM_COMMENTS', false, true);

insert into UserRole (id, changeable, code, Role_Name, internalUse, byCaseClassification)
values (161, false, '011012', 'REM_COM', false, true);

INSERT INTO UserPermission
(canchange, canexecute, grantPermission, profile_id, role_id)
select false, true, true, id, 159 from UserProfile
where name like "%adminis%"
and not exists (select * from UserPermission where role_id=159 and profile_id=UserProfile.id);

INSERT INTO UserPermission
(canchange, canexecute, grantPermission, profile_id, role_id)
select false, true,  true, id, 160 from UserProfile
where name like "%adminis%"
and not exists (select * from UserPermission where role_id=160 and profile_id=UserProfile.id);

CREATE TABLE `CaseComorbidity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `comment` varchar(200) DEFAULT NULL,
  `COMORBIDITY_ID` int(11) NOT NULL,
  `CASE_ID` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9298ED455ED13EEE` (`COMORBIDITY_ID`),
  KEY `FK9298ED45925E7A4E` (`id`),
  KEY `FK9298ED458B327BA` (`CASE_ID`),
  CONSTRAINT `FK9298ED458B327BA` FOREIGN KEY (`CASE_ID`) REFERENCES `TbCase` (`id`),
  CONSTRAINT `FK9298ED455ED13EEE` FOREIGN KEY (`COMORBIDITY_ID`) REFERENCES `FieldValue` (`id`)
);

CREATE TABLE CaseComment (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `comment` longtext NOT NULL,
  `comment_date` datetime NOT NULL,
  `section` int(11) NOT NULL,
  `CASE_ID` int(11) NOT NULL,
  `USER_ID` int(11) NOT NULL,
  `view` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKBA21BCEFD84E76CC` (`USER_ID`),
  KEY `FKBA21BCEF8B327BA` (`CASE_ID`),
  CONSTRAINT `FKBA21BCEF8B327BA` FOREIGN KEY (`CASE_ID`) REFERENCES `TbCase` (`id`),
  CONSTRAINT `FKBA21BCEFD84E76CC` FOREIGN KEY (`USER_ID`) REFERENCES `Sys_User` (`id`)
);


insert into CaseComorbidity (case_id, comorbidity_id)
select tbcase_id, comorbidities_id from CASE_COMORBIDITIES;

drop table CASE_COMORBIDITIES;

ALTER TABLE CaseComorbidity
 DROP FOREIGN KEY `FK9298ED455ED13EEE`;

ALTER TABLE CaseComorbidity`
 DROP FOREIGN KEY `FK9298ED458B327BA`;

ALTER TABLE CaseComorbidity ADD CONSTRAINT `FK9298ED455ED13EEE` FOREIGN KEY `FK9298ED455ED13EEE` (`COMORBIDITY_ID`)
    REFERENCES `FieldValue` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK9298ED458B327BA` FOREIGN KEY `FK9298ED458B327BA` (`CASE_ID`)
    REFERENCES `TbCcase` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

ALTER TABLE CaseComment
 DROP FOREIGN KEY `FKBA21BCEF8B327BA`;

ALTER TABLE CaseComment
 DROP FOREIGN KEY `FKBA21BCEFD84E76CC`;

ALTER TABLE CaseComment ADD CONSTRAINT `FKBA21BCEF8B327BA` FOREIGN KEY `FKBA21BCEF8B327BA` (`CASE_ID`)
    REFERENCES `TbCase` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FKBA21BCEFD84E76CC` FOREIGN KEY `FKBA21BCEFD84E76CC` (`USER_ID`)
    REFERENCES `Sys_User` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

ALTER TABLE ExamCulture MODIFY 
 	COLUMN `comments` LONGTEXT CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL;
 
ALTER TABLE ExamMicroscopy MODIFY 
 	COLUMN `comments` LONGTEXT CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL;

ALTER TABLE ExamHIV MODIFY 
 	COLUMN `comments` LONGTEXT CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL;

ALTER TABLE ExamXRay MODIFY 
 	COLUMN `comments` LONGTEXT CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL;

ALTER TABLE ExamDST MODIFY
 	COLUMN `comments` LONGTEXT CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL;
