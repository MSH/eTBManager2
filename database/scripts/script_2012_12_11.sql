/***************************************************************
 * 11-dec-2012
 * By Oleksii Kurasov
 * 
 * UA only!!! Fix very old records in examDST
 * 
****************************************************************/
UPDATE `etbmanager`.`examdst` SET `DISCRIMINATOR`='gen' WHERE `DISCRIMINATOR`is NULL;