/***************************************************************
 * 21-mar-2013
 * By A.M.
 * 
 * UA only!!! Add new fields
 * 
****************************************************************/

ALTER TABLE `etbmanager`.`molecularbiology` 
ADD COLUMN `pcr` BIT(1) NOT NULL DEFAULT b'0'  AFTER `METHOD_ID` , 
ADD COLUMN `h` BIT(1) NULL  AFTER `pcr` , 
ADD COLUMN `r` BIT(1) NULL  AFTER `h` , 
ADD COLUMN `km` BIT(1) NULL  AFTER `r` , 
ADD COLUMN `cm` BIT(1) NULL  AFTER `km` , 
ADD COLUMN `e` BIT(1) NULL  AFTER `cm` , 
ADD COLUMN `lfx` BIT(1) NULL  AFTER `e` , 
ADD COLUMN `mfx` BIT(1) NULL  AFTER `lfx` ;
