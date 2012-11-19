/****************************************************************
 * Include two new fields in some tables of the data model to
 * indicate when a record was created and when it was updated
 * for the last time. 
 * 
 * The rationality is to use this new structure to syncronize
 * with the client version (desktop module)
 * 
 * By Ricardo Memoria
 * 18-nov-2012
 * 
 ****************************************************************/

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

ALTER TABLE `administrativeunit` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL , ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_administrativeunit_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_administrativeunit_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_administrativeunit_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_administrativeunit_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `agerange` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL  , ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_agerange_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_agerange_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_agerange_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_agerange_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `countrystructure` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL, ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_countrystructure_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_countrystructure_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_countrystructure_transactionlog1` (`lastTransaction_ID` ASC)
, ADD INDEX `fk_countrystructure_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `examculture` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL  , ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_examculture_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_examculture_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_examculture_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_examculture_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `examdst` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL, ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_examdst_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_examdst_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_examdst_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_examdst_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `examhiv` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL  , ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_examhiv_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_examhiv_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_examhiv_transactionlog1` (`lastTransaction_ID` ASC)
, ADD INDEX `fk_examhiv_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `exammicroscopy` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL  , ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_exammicroscopy_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_exammicroscopy_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_exammicroscopy_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_exammicroscopy_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `examxray` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `LOCALIZATION_ID` , ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` , 
  ADD CONSTRAINT `fk_examxray_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_examxray_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_examxray_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_examxray_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `fieldvalue` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `displayOrder` , ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` , 
  ADD CONSTRAINT `fk_fieldvalue_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_fieldvalue_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_fieldvalue_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_fieldvalue_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `forecasting` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL , ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_forecasting_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_forecasting_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_forecasting_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_forecasting_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `healthsystem` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL   , ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_healthsystem_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_healthsystem_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_healthsystem_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_healthsystem_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `laboratory` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL, ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_laboratory_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_laboratory_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_laboratory_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_laboratory_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `medicalexamination` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL , ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_medicalexamination_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_medicalexamination_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_medicalexamination_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_medicalexamination_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `medicine` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL, ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_medicine_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_medicine_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_medicine_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_medicine_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `patient` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL , ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_patient_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_patient_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_patient_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_patient_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `productgroup` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL, ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_productgroup_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_productgroup_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_productgroup_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_productgroup_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `regimen` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL, ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_regimen_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_regimen_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_regimen_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_regimen_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `source` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL, ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_source_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_source_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_source_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_source_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `substance` ADD COLUMN `lasttransaction_id` INT(11) NULL DEFAULT NULL, ADD COLUMN `createTransaction_id` INT(11) NULL DEFAULT NULL  AFTER `lasttransaction_id` ,
  ADD CONSTRAINT `fk_substance_transactionlog1`
  FOREIGN KEY (`lasttransaction_id` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_substance_transactionlog2`
  FOREIGN KEY (`createTransaction_id` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_substance_transactionlog1` (`lasttransaction_id` ASC) 
, ADD INDEX `fk_substance_transactionlog2` (`createTransaction_id` ASC) ;

ALTER TABLE `sys_user` ADD COLUMN `lasttransaction_id` INT(11) NULL DEFAULT NULL, ADD COLUMN `createtransaction_id` INT(11) NULL DEFAULT NULL  AFTER `lasttransaction_id` ,
  ADD CONSTRAINT `fk_sys_user_transactionlog1`
  FOREIGN KEY (`lasttransaction_id` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_sys_user_transactionlog2`
  FOREIGN KEY (`createtransaction_id` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_sys_user_transactionlog1` (`lasttransaction_id` ASC) 
, ADD INDEX `fk_sys_user_transactionlog2` (`createtransaction_id` ASC) ;

ALTER TABLE `tag` ADD COLUMN `lasttransaction_id` INT(11) NULL DEFAULT NULL, ADD COLUMN `createtransaction_id` INT(11) NULL DEFAULT NULL  AFTER `lasttransaction_id` ,
  ADD CONSTRAINT `fk_tag_transactionlog1`
  FOREIGN KEY (`lasttransaction_id` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_tag_transactionlog2`
  FOREIGN KEY (`createtransaction_id` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_tag_transactionlog1` (`lasttransaction_id` ASC) 
, ADD INDEX `fk_tag_transactionlog2` (`createtransaction_id` ASC) ;

ALTER TABLE `tbcase` ADD COLUMN `LASTTRANSACTION_ID` INT(11) NULL DEFAULT NULL, ADD COLUMN `CREATETRANSACTION_ID` INT(11) NULL DEFAULT NULL  AFTER `LASTTRANSACTION_ID` ,
  ADD CONSTRAINT `FKTBCASE_LASTTRANS`
  FOREIGN KEY (`LASTTRANSACTION_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_tbcase_transactionlog1`
  FOREIGN KEY (`CREATETRANSACTION_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `FKTBCASE_LASTTRANS` (`LASTTRANSACTION_ID` ASC) 
, ADD INDEX `fk_tbcase_transactionlog1` (`CREATETRANSACTION_ID` ASC) ;

ALTER TABLE `tbcontact` ADD COLUMN `lastTransaction_ID` INT(11) NULL DEFAULT NULL, ADD COLUMN `createTransaction_ID` INT(11) NULL DEFAULT NULL  AFTER `lastTransaction_ID` ,
  ADD CONSTRAINT `fk_tbcontact_transactionlog1`
  FOREIGN KEY (`lastTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_tbcontact_transactionlog2`
  FOREIGN KEY (`createTransaction_ID` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_tbcontact_transactionlog1` (`lastTransaction_ID` ASC) 
, ADD INDEX `fk_tbcontact_transactionlog2` (`createTransaction_ID` ASC) ;

ALTER TABLE `tbunit` ADD COLUMN `lasttransaction_id` INT(11) NULL DEFAULT NULL, ADD COLUMN `createtransaction_id` INT(11) NULL DEFAULT NULL  AFTER `lasttransaction_id` ,
  ADD CONSTRAINT `fk_tbunit_transactionlog1`
  FOREIGN KEY (`lasttransaction_id` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_tbunit_transactionlog2`
  FOREIGN KEY (`createtransaction_id` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_tbunit_transactionlog1` (`lasttransaction_id` ASC) 
, ADD INDEX `fk_tbunit_transactionlog2` (`createtransaction_id` ASC) ;

ALTER TABLE `userprofile` ADD COLUMN `lasttransaction_id` INT(11) NULL DEFAULT NULL, ADD COLUMN `createtransaction_id` INT(11) NULL DEFAULT NULL  AFTER `lasttransaction_id` ,
  ADD CONSTRAINT `fk_userprofile_transactionlog1`
  FOREIGN KEY (`lasttransaction_id` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE CASCADE
  ON UPDATE SET NULL,
  ADD CONSTRAINT `fk_userprofile_transactionlog2`
  FOREIGN KEY (`createtransaction_id` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE CASCADE
  ON UPDATE SET NULL
, ADD INDEX `fk_userprofile_transactionlog1` (`lasttransaction_id` ASC)
, ADD INDEX `fk_userprofile_transactionlog2` (`createtransaction_id` ASC) ;

ALTER TABLE `workspace` ADD COLUMN `lasttransaction_id` INT(11) NULL DEFAULT NULL  ,
  ADD COLUMN `createtransaction_id` INT(11) NULL DEFAULT NULL,
  ADD CONSTRAINT `fk_workspace_transactionlog1`
  FOREIGN KEY (`lasttransaction_id` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_workspace_transactionlog2`
  FOREIGN KEY (`createtransaction_id` )
  REFERENCES `transactionlog` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `fk_workspace_transactionlog1` (`lasttransaction_id` ASC) 
, ADD INDEX `fk_workspace_transactionlog2` (`createtransaction_id` ASC) ;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

/* update transaction log in cases */
create index idx_entityid on transactionlog (entityId) using btree;

/* update transaction log in users */
update sys_user set createtransaction_id=
(select min(id)
from transactionlog
where role_id=2 and action=1
and entityId = sys_user.id);

update sys_user set lasttransaction_id=
(select min(id) from transactionlog where role_id=2 and action=2 and entityid=sys_user.id);

update sys_user set lasttransaction_id=createtransaction_id;

/* update transaction log in cases */
update tbcase set createtransaction_id=
(select min(id)
from transactionlog
where role_id=171 and action=1
and entityId = tbcase.id);

update tbcase set createtransaction_id=
(select min(id)
from transactionlog
where role_id=170 and action=1
and entityId = tbcase.id)
where createtransaction_id is null;

update tbcase set lasttransaction_id=
(select min(id)
from transactionlog
where role_id=171 and action=2
and entityId = tbcase.id);

update tbcase set lasttransaction_id=
(select min(id)
from transactionlog
where role_id=170 and action=2
and entityId = tbcase.id);

update tbcase set lasttransaction_id=createtransaction_id where lasttransaction_id is null;

