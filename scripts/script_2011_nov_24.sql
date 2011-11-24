/***************************************************************************************
* Adjust medicine order foreign key
* by Ricardo Memoria
* 24/11/2011
****************************************************************************************/

ALTER TABLE `etbmanager`.`medicineorder`
 DROP FOREIGN KEY `FK51C00554DD15FB9F`;

ALTER TABLE `etbmanager`.`medicineorder` ADD CONSTRAINT `FK51C00554DD15FB9F` FOREIGN KEY `FK51C00554DD15FB9F` (`SHIP_ADMINUNIT_ID`)
    REFERENCES `administrativeunit` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;