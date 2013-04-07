/***************************************************************
 * 28-mar-2013
 * By A.M.
 * 
 * UA only!!! Add new table 
 * 
****************************************************************/

CREATE  TABLE `etbmanager`.`medicinereceiving_ua` (
  `id` INT(11) NOT NULL ,
  `consignmentNumber` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_id_medreceveing` (`id` ASC) ,
  CONSTRAINT `fk_id_medreceveing`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanager`.`medicinereceiving` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

INSERT INTO `etbmanager`.`medicinereceiving_ua` SELECT id,null FROM `etbmanager`.`medicinereceiving`;