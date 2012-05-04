/***************************************************************
* By Ricardo Memória - 18-04-2012
* 
* Adjust foreign key between movement and fieldvalue tables 
* 
****************************************************************/



ALTER TABLE `movement`
 add ADJUSTMENT_ID int;

ALTER TABLE `movement` ADD CONSTRAINT `FKF9D200AFAB3243E7` FOREIGN KEY `FKF9D200AFAB3243E7` (`ADJUSTMENT_ID`)
    REFERENCES `fieldvalue` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;
