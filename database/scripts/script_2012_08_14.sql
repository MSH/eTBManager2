/***************************************************************
* By Alexandra M.
* 
* Add new fields in case data. Only for Azerbarbaijan
* 
****************************************************************/

ALTER TABLE `etbmanager`.`tbcaseaz` ADD COLUMN `toThirdCategory` BIT(1) NOT NULL DEFAULT b'0'  AFTER `referToOtherTBUnit` , ADD COLUMN `dateIniThirdCat` DATETIME NULL  AFTER `toThirdCategory` , ADD COLUMN `dateEndThirdCat` DATETIME NULL  AFTER `dateIniThirdCat` ;