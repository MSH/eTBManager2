ALTER TABLE `etbmanager`.`workspace` ADD COLUMN `StartTBTreatBeforeValidation` BIT(1) NOT NULL AFTER `useCustomTheme`,
 ADD COLUMN `StartDRTBTreatBeforeValidation` BIT(1) NOT NULL AFTER `StartTBTreatBeforeValidation`;
 
update Workspace set StartDRTBTreatBeforeValidation=1, StartTBTreatBeforeValidation=1;