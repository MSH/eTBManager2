/***************************************************************
* By Ricardo Memoria
* 
* Changing column type of field age in tbcontact from int to string
* Reason: It's not possible to include information about babies with less than 1 year old
* 
****************************************************************/


ALTER TABLE `tbcontact` MODIFY COLUMN `age` VARCHAR(255) NULL;
