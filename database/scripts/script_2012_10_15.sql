/****************************************************************
 * 15-10-2012
 * By A.M.
 * 
 * ONLY FOR AZERBAIJAN
 * Extends tables examculture and exammicroscopy
 * Add new field to TbCaseAZ
 ****************************************************************/

CREATE  TABLE `etbmanager`.`examculture_az` (
  `id` INT(11) NOT NULL ,
  `datePlating` DATE NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `index2` (`id` ASC) ,
  CONSTRAINT `index2`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanager`.`examculture` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

ALTER TABLE `etbmanager`.`exammicroscopy` ADD COLUMN `datePlating` DATE NULL DEFAULT NULL  AFTER `LAB_ADMINUNIT_ID` ;

ALTER TABLE `etbmanager`.`tbcaseaz` ADD COLUMN `unicalID` VARCHAR(15) NULL DEFAULT NULL  AFTER `dateIniThirdCat` ;