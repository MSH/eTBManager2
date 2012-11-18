/****************************************************************
 * 06-11-2012
 * By A.M.
 * 
 * ONLY FOR AZERBAIJAN
 * Extends table examdst
 ****************************************************************/

ALTER TABLE `etbmanager`.`examdst` ADD COLUMN `datePlating` DATE NULL DEFAULT NULL  AFTER `mtbDetected` ;

