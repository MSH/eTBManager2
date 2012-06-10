/***************************************************************
* By Oleksii Kurasov - 05-06-2012
* 
* Add doctor by case
* Add TBUnit field, which patient refer to 
* 
****************************************************************/



ALTER TABLE `etbmanager`.`tbcaseaz` ADD COLUMN `doctor` VARCHAR(45) NULL DEFAULT NULL  AFTER `EIDSSComment` ;

ALTER TABLE `etbmanager`.`tbcaseaz` ADD COLUMN `referToTBUnit_id` INT(11) NULL DEFAULT NULL  AFTER `doctor` ;

ALTER TABLE `etbmanager`.`tbcaseaz` 

  ADD CONSTRAINT `FKA77F5734401382`

  FOREIGN KEY (`referToTBUnit_id` )

  REFERENCES `etbmanager`.`tbunit` (`id` )

  ON DELETE SET NULL

  ON UPDATE NO ACTION

, ADD INDEX `FKA77F5734401382` (`referToTBUnit_id` ASC) ;

ALTER TABLE `etbmanager`.`tbcaseaz` ADD COLUMN `referToOtherTBUnit` BIT(1) NOT NULL DEFAULT 0  AFTER `referToTBUnit_id` ;

