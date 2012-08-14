/***************************************************************
* By MSANTOS - 2012-08-08
* SEVERE MODIFICATION
* 
* For all implementations of ETBMANAGER
* The field treatmentUnit of TbCase had the name changed to ownerUnit because of its meaning.
* This script has to be executed to update the ownerUnit field.
* 
****************************************************************/
ALTER TABLE `tbcase`
DROP FOREIGN KEY `FKCB85A29E45C57184`;

ALTER TABLE `tbcase` CHANGE COLUMN `TREATMENT_UNIT_ID` `OWNER_UNIT_ID` INT(11) DEFAULT NULL;

ALTER TABLE `tbcase` ADD CONSTRAINT `FKCB85A29E45C57184` FOREIGN KEY `FKCB85A29E45C57184` (`OWNER_UNIT_ID`)
    REFERENCES `tbunit` (`id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE;
