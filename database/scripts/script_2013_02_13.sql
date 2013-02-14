/***************************************************************
 * 13-feb-2013
 * By A.M.
 * 
 * UA only!!! Add new field to CaseDataUA
 * 
****************************************************************/

ALTER TABLE `etbmanager`.`casedataua` ADD COLUMN `causeChangeTreat_id` INT(11) NULL DEFAULT NULL  AFTER `hospitalizationDate5` , 
  ADD CONSTRAINT `FKDF8312E6FFF48273`
  FOREIGN KEY (`causeChangeTreat_id` )
  REFERENCES `etbmanager`.`fieldvalue` (`id` )
  ON DELETE SET NULL
  ON UPDATE CASCADE
, ADD INDEX `FKDF8312E6FFF48273` (`causeChangeTreat_id` ASC) ;


