/***************************************************************
 * 20-feb-2013
 * By A.M.
 * 
 * UA only!!! Once again correct mistake with Discriminator-field, which must be not-null
 * 
****************************************************************/

UPDATE `etbmanager`.`medicalexamination` SET `DISCRIMINATOR`='gen' WHERE `DISCRIMINATOR` is null;



