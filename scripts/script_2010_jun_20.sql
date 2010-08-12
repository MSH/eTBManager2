ALTER TABLE PrescribedMedicine 
 ADD COLUMN `IniDate` DATETIME AFTER `SOURCE_ID`,
 ADD COLUMN `EndDate` DATETIME AFTER `IniDate`;

ALTER TABLE PrescribedMedicine 
 ADD COLUMN `CASE_ID` INT(11) NOT NULL AFTER `SOURCE_ID`;

update PrescribedMedicine
set CASE_ID = (select a.case_id from CaseRegimen a where a.id = PrescribedMedicine.regimen_id),
inidate = (select a.inidate from CaseRegimen a where a.id = PrescribedMedicine.regimen_id),
enddate = (select a.enddate from CaseRegimen a where a.id = PrescribedMedicine.regimen_id);


ALTER TABLE PrescribedMedicine
 ADD CONSTRAINT `FK85BFA2118B327BA` FOREIGN KEY `FK85BFA2118B327BA` (`CASE_ID`)
    REFERENCES `tbcase` (`id`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT;
    
ALTER TABLE CaseRegimen DROP FOREIGN KEY `FKC2387DF118FE5940` ;

ALTER TABLE CaseRegimen 
	ADD COLUMN `endIntensivePhase` DATE NULL,
	DROP COLUMN `HEALTHUNIT_ID`, 
	DROP COLUMN `phase`,
	DROP INDEX `FKC2387DF118FE5940`;


ALTER TABLE PrescribedMedicine DROP COLUMN `REGIMEN_ID`,
 DROP FOREIGN KEY `FK85BFA211FE259F98`;
 
