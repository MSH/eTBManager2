/***************************************************************
* By Oleksii Kurasov - 2012-06-07
* 
* Fix error related to wrong definition of column ntmHealthUnit in table tbunit
* 
****************************************************************/
UPDATE `etbmanager`.`tbunit` SET `ntmHealthUnit`=0 where ntmHealthUnit is NULL;
ALTER TABLE `etbmanager`.`tbunit` CHANGE COLUMN `ntmHealthUnit` `ntmHealthUnit` BIT(1) NOT NULL DEFAULT 0  ;
