/***************************************************************
* By Oleksii Kurasov - 21-05-2012
* 
* For EIDSS integration in Azerbaijan
* 
****************************************************************/


ALTER TABLE `etbmanager`.`tbcaseaz` ADD COLUMN `EIDSSID` VARCHAR(45) NULL DEFAULT NULL  AFTER `inprisonEndDate` , ADD COLUMN `EIDSSComment` VARCHAR(255) NULL DEFAULT NULL  AFTER `EIDSSID` 

, ADD UNIQUE INDEX `EIDSSID_UNIQUE` (`EIDSSID` ASC) ;

