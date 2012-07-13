/***************************************************************
* By Alexey Kurasov
* 
* FIX wery old records in medicalexamination
* 
****************************************************************/
UPDATE `etbmanager`.`medicalexamination` SET `DISCRIMINATOR`='gen' WHERE DISCRIMINATOR is NULL;