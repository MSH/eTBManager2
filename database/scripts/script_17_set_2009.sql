delete from ForecastingRegimen;
delete from ForecastingMedicine;
delete from Forecasting;

ALTER TABLE MedicineRegimen ADD COLUMN `MonthsTreatment` INT(11) AFTER `REGIMEN_ID`;

update MedicineRegimen, Regimen
set MedicineRegimen.monthsTreatment = Regimen.monthsContinuousPhase
where MedicineRegimen.phase=1;

update MedicineRegimen, Regimen
set MedicineRegimen.monthsTreatment = Regimen.monthsIntensivePhase
where MedicineRegimen.phase=0;

ALTER TABLE Regimen DROP COLUMN `monthsContinuousPhase`,
 DROP COLUMN `monthsIntensivePhase`;

/* Include pulmonary types in field values */
insert into FieldValue
(customid, field, name1, name2, short_name1, short_name2, workspace_id)
select icd10, 13, form_name, null, icd10, null, workspace_id from clinicalform
where infectionsite=0;

/* Include extrapulmonary types in field values */
insert into FieldValue
(customid, field, name1, name2, short_name1, short_name2, workspace_id)
select icd10, 14, form_name, null, icd10, null, workspace_id from clinicalform
where infectionsite=1;


/* Adjust ClinicalForm */
ALTER TABLE TbCase
  ADD PULMONARY_ID INT(11),
  ADD EXTRAPULMONARY_ID INT(11),
  ADD EXTRAPULMONARY2_ID INT(11);

update TbCase, FieldValue
 set TbCase.extrapulmonary_id = FieldValue.id
 where TbCase.clinicalform_id = FieldValue.customId
 and TbCase.infectionSite=1;

update TbCase, FieldValue
 set TbCase.extrapulmonary2_id = FieldValue.id
 where TbCase.extrapulmonary2_id = FieldValue.customId
 and TbCase.infectionSite=1;

update TbCase, FieldValue
 set TbCase.pulmonary_id = FieldValue.id
 where TbCase.clinicalform_id = FieldValue.customId
 and TbCase.infectionSite=0;

ALTER TABLE `TbCase` ADD CONSTRAINT `CONSTR_PULMONARY` FOREIGN KEY `FK_PULMONARY` (`PULMONARY_ID`)
    REFERENCES `FieldValue` (`id`)
    ON DELETE SET NULL
    ON UPDATE SET NULL;

ALTER TABLE `TbCase` ADD CONSTRAINT `CONSTR_EXTRAPULMONARY` FOREIGN KEY `FK_EXTRAPUL1` (`EXTRAPULMONARY_ID`)
    REFERENCES `FieldValue` (`id`)
    ON DELETE SET NULL
    ON UPDATE SET NULL;


ALTER TABLE `TbCase` ADD CONSTRAINT `CONSTR_EXTRAPULMONARY2` FOREIGN KEY `FK_EXTRAPUL2` (`EXTRAPULMONARY2_ID`)
    REFERENCES `FieldValue` (`id`)
    ON DELETE SET NULL
    ON UPDATE SET NULL;

delete from UserPermission where role_id=57;
update TransactionLog set role_id=58 where role_id=57;
delete from UserRole where id=57;
