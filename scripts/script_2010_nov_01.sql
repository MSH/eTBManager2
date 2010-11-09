ALTER TABLE TbCase ADD COLUMN `REGIMEN_ID` INT(11) AFTER `version`,
 ADD COLUMN `TREATMENT_UNIT_ID` INT(11) AFTER `REGIMEN_ID`,
 ADD CONSTRAINT `FK94DC02DE2F370FE8` FOREIGN KEY `FK94DC02DE2F370FE8` (`REGIMEN_ID`)
    REFERENCES `Regimen` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK94DC02DEAA884A53` FOREIGN KEY `FK94DC02DEAA884A53` (`TREATMENT_UNIT_ID`)
    REFERENCES `Tbunit` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

ALTER TABLE TbCase ADD COLUMN `iniContinuousPhase` DATE;

update TbCase
set regimen_id = (select cr.regimen_id
from CaseRegimen cr where cr.case_id = TbCase.id
and cr.inidate = tbcase.initreatmentdate limit 1),
inicontinuousphase = (select cr.inicontphase
from CaseRegimen cr where cr.case_id = TbCase.id
and cr.inidate = tbcase.initreatmentdate limit 1);

update PrescribedMedicine
set inidate = cast(inidate as date),
enddate = cast(enddate as date);

ALTER TABLE PrescribedMedicine MODIFY COLUMN `IniDate` DATE NOT NULL,
 MODIFY COLUMN `EndDate` DATE NOT NULL;


update TbCase
set inicontinuousphase = (select min(pm.inidate)
from PrescribedMedicine pm
where pm.case_id = TbCase.id
and pm.inidate > TbCase.initreatmentdate)
where inicontinuousphase is null
and initreatmentdate is not null;


update TbCase
set inicontinuousphase = date_add(initreatmentdate, interval (select max(monthstreatment)
from MedicineRegimen m
where m.regimen_id = TbCase.regimen_id
and m.phase=0) month)
where regimen_id is not null
and inicontinuousphase is null
and initreatmentdate is not null;

drop table CaseRegimen;

alter table Regimen
  add caseClassification INT(11) NULL;

/* Ukraine */
update Regimen
set caseClassification=0
where workspace_id in (29107, 940358)
and mdrtreatment=tbtreatment;

/* Philippines */
update Regimen
set caseClassification=1
where workspace_id in (20271)
and mdrtreatment=tbtreatment;

/* MDR-TB regimens */
update Regimen
set caseClassification=1
where mdrtreatment=true and tbtreatment=false;

/* TB regimens */
update Regimen
set caseClassification=0
where tbtreatment=true and mdrtreatment=false;

update Regimen
set caseClassification=1
where caseClassification is null;

ALTER TABLE Regimen MODIFY COLUMN `caseClassification` INT(11) NOT NULL;

ALTER TABLE Regimen DROP COLUMN `mdrTreatment`,
 DROP COLUMN `tbTreatment`;

