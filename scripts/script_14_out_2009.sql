ALTER TABLE CaseDataUA DROP COLUMN `aids`,
 DROP COLUMN `aidsART`,
 DROP COLUMN `aidsCotrimoxasol`,
 DROP COLUMN `aidsVCT`;

ALTER TABLE Workspace
  ADD COLUMN StartTreatBeforeValidation BIT(1) default null,
  ADD COLUMN patientAddrRequiredLevels int(11) DEFAULT NULL,
  ADD COLUMN displayCaseNumber int(11) DEFAULT NULL;
  
update Workspace 
set StartTreatBeforeValidation=false,
	patientAddrRequiredLevels=1,
	displayCaseNumber=0;

alter table TbCase
  ADD COLUMN daysTreatPlanned INT(11) DEFAULT NULL;
 
CREATE TABLE  CaseDispensingDays (
  `id` int(11) NOT NULL,
  `day1` bit(1) NOT NULL,
  `day10` bit(1) NOT NULL,
  `day11` bit(1) NOT NULL,
  `day12` bit(1) NOT NULL,
  `day13` bit(1) NOT NULL,
  `day14` bit(1) NOT NULL,
  `day15` bit(1) NOT NULL,
  `day16` bit(1) NOT NULL,
  `day17` bit(1) NOT NULL,
  `day18` bit(1) NOT NULL,
  `day19` bit(1) NOT NULL,
  `day2` bit(1) NOT NULL,
  `day20` bit(1) NOT NULL,
  `day21` bit(1) NOT NULL,
  `day22` bit(1) NOT NULL,
  `day23` bit(1) NOT NULL,
  `day24` bit(1) NOT NULL,
  `day25` bit(1) NOT NULL,
  `day26` bit(1) NOT NULL,
  `day27` bit(1) NOT NULL,
  `day28` bit(1) NOT NULL,
  `day29` bit(1) NOT NULL,
  `day3` bit(1) NOT NULL,
  `day30` bit(1) NOT NULL,
  `day31` bit(1) NOT NULL,
  `day4` bit(1) NOT NULL,
  `day5` bit(1) NOT NULL,
  `day6` bit(1) NOT NULL,
  `day7` bit(1) NOT NULL,
  `day8` bit(1) NOT NULL,
  `day9` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
);

ALTER TABLE CaseDispensingDays ADD CONSTRAINT `FK_casedispensingdays1` FOREIGN KEY `FK_casedispensingdays1` (`id`)
    REFERENCES `casedispensing` (`id`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT;

insert into CaseDispensingDays (id, day1, day2, day3, day4, day5, day6, day7, day8, day9, day10, day11, day12,
 day13, day14, day15, day16, day17, day18, day19, day20, day21, day22, day23, day24, day25, day26, day27, day28,
 day29, day30, day31)
select id, day1, day2, day3, day4, day5, day6, day7, day8, day9, day10, day11, day12,
 day13, day14, day15, day16, day17, day18, day19, day20, day21, day22, day23, day24, day25, day26, day27, day28,
 day29, day30, day31 from CaseDispensing;


ALTER TABLE CaseDispensing
  ADD COLUMN `TotalDays` INTEGER UNSIGNED ZEROFILL NOT NULL DEFAULT 0;


update CaseDispensing
set totaldays= day1+day2+day3+day4+day5+day6+day7+day8+day9+day10+day11+day12+day13+day14+day15+day16+day17+day18+day19
  +day20+day21+day22+day23+day24+day25+day26+day27+day28+day29+day30+day31;


ALTER TABLE CaseDispensing
  DROP COLUMN day1,
  DROP COLUMN day2,
  DROP COLUMN day3,
  DROP COLUMN day4,
  DROP COLUMN day5,
  DROP COLUMN day6,
  DROP COLUMN day7,
  DROP COLUMN day8,
  DROP COLUMN day9,
  DROP COLUMN day10,
  DROP COLUMN day11,
  DROP COLUMN day12,
  DROP COLUMN day13,
  DROP COLUMN day14,
  DROP COLUMN day15,
  DROP COLUMN day16,
  DROP COLUMN day17,
  DROP COLUMN day18,
  DROP COLUMN day19,
  DROP COLUMN day20,
  DROP COLUMN day21,
  DROP COLUMN day22,
  DROP COLUMN day23,
  DROP COLUMN day24,
  DROP COLUMN day25,
  DROP COLUMN day26,
  DROP COLUMN day27,
  DROP COLUMN day28,
  DROP COLUMN day29,
  DROP COLUMN day30,
  DROP COLUMN day31;

insert into UserRole (id, changeable, code, executable, Role_name, internalUse)
value (120, true, '040201', false, 'ADMSTR', false);

insert into UserPermission (canChange, canExecute, canOpen, grantPermission, PROFILE_ID, ROLE_ID)
select true, false, false, true, id, 120  from UserProfile where name = 'Administrator';

update UserRole set changeable=false, executable=true where id in (61, 64);
update UserPermission set canExecute=true, canOpen=false, canChange=false where role_id in (61, 64) and canChange=true;
delete from UserPermission where role_id in (61,64) and canChange=false;

ALTER TABLE `etbmanager`.`TbContact`
 DROP FOREIGN KEY `FK69755B21789BDFF`;

ALTER TABLE `etbmanager`.`TbContact`
 DROP FOREIGN KEY `FK69755B28A7B0429`;

ALTER TABLE `etbmanager`.`TbContact`
 DROP FOREIGN KEY `FK69755B28B327BA`;

ALTER TABLE `etbmanager`.`TbContact` ADD CONSTRAINT `FK69755B21789BDFF` FOREIGN KEY `FK69755B21789BDFF` (`CONDUCT_ID`)
    REFERENCES `fieldvalue` (`id`)
    ON DELETE CASCADE;

drop table MedicineDispensing;
drop table UnitDispensing;

delete from UserPermission where role_id in (38,44);
delete from UserRole where id in (38,44);

update UserRole set executable=false, changeable=true
where id = 16;

update UserPermission set canopen=true, canchange=true, canexecute=false
where role_id = 16 and canexecute=true;

update UserPermission set canopen=true, canchange=false where role_id=16 and canexecute=false;

