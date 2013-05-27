/***************************************************************
 * 25-may-2013
 * By A.M.
 * 
 * AZ only!!! Correct discriminator of old exams
 * 
****************************************************************/

UPDATE `etbmanager`.`examdst` SET `DISCRIMINATOR`='az' WHERE `case_id` in (select c.id from `etbmanager`.`tbcaseaz` c);
