/***************************************************************
 * 21-feb-2013
 * By A.M.
 * 
 * AZ only!!! Replace some incorect field values. Add new fields in TbCaseAZ
 * 
****************************************************************/
UPDATE `etbmanager`.`fieldvalue` SET `name1`='Oçaqlı', `SHORT_NAME1`='Oçaqlı' WHERE `id`='939788';

ALTER TABLE `etbmanager`.`tbcaseaz` ADD COLUMN `editingDate` DATE NULL DEFAULT NULL  AFTER `colPrevTreatUnknown` , ADD COLUMN `createUser_id` INT(11) NULL DEFAULT NULL  AFTER `editingDate` , ADD COLUMN `editingUser_id` INT(11) NULL DEFAULT NULL  AFTER `createUser_id` , 
  ADD CONSTRAINT `FKA77F57DD0C41C1`
  FOREIGN KEY (`createUser_id` )
  REFERENCES `etbmanager`.`sys_user` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `FKA77F5754AAC5A5`
  FOREIGN KEY (`editingUser_id` )
  REFERENCES `etbmanager`.`sys_user` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `FKA77F57DD0C41C1` (`createUser_id` ASC) 
, ADD INDEX `FKA77F5754AAC5A5` (`editingUser_id` ASC) ;





