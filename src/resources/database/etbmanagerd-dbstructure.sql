SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `etbmanagerd` ;
CREATE SCHEMA IF NOT EXISTS `etbmanagerd` DEFAULT CHARACTER SET utf8 ;
USE `etbmanagerd` ;

-- -----------------------------------------------------
-- Table `etbmanagerd`.`workspace`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`workspace` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`workspace` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `alternateLocale` VARCHAR(10) NULL DEFAULT NULL ,
  `customPath` VARCHAR(100) NULL DEFAULT NULL ,
  `defaultLocale` VARCHAR(10) NULL DEFAULT NULL ,
  `defaultTimeZone` VARCHAR(200) NULL DEFAULT NULL ,
  `description` VARCHAR(150) NULL DEFAULT NULL ,
  `extension` VARCHAR(10) NULL DEFAULT NULL ,
  `name1` VARCHAR(100) NULL DEFAULT NULL ,
  `name2` VARCHAR(100) NULL DEFAULT NULL ,
  `patientNameComposition` INT(11) NULL DEFAULT NULL ,
  `weekFreq1` INT(11) NULL DEFAULT NULL ,
  `weekFreq2` INT(11) NULL DEFAULT NULL ,
  `weekFreq3` INT(11) NULL DEFAULT NULL ,
  `weekFreq4` INT(11) NULL DEFAULT NULL ,
  `weekFreq5` INT(11) NULL DEFAULT NULL ,
  `weekFreq6` INT(11) NULL DEFAULT NULL ,
  `weekFreq7` INT(11) NULL DEFAULT NULL ,
  `patientAddrRequiredLevels` INT(11) NULL DEFAULT NULL ,
  `confirmedCaseNumber` INT(11) NULL DEFAULT NULL ,
  `useCustomTheme` BIT(1) NULL DEFAULT NULL ,
  `url` VARCHAR(200) NULL DEFAULT NULL ,
  `sendSystemMessages` TINYINT(1) NULL DEFAULT NULL ,
  `monthsToAlertExpiredMedicines` INT(11) NULL DEFAULT NULL ,
  `lasttransaction_id` INT(11) NULL DEFAULT NULL ,
  `createtransaction_id` INT(11) NULL DEFAULT NULL ,
  `ulaActive` TINYINT(1) NULL DEFAULT NULL ,
  `expiredMedicineAdjustmentType_ID` INT(11) NULL DEFAULT NULL ,
  `caseValidationTB` INT(11) NULL DEFAULT NULL ,
  `caseValidationDRTB` INT(11) NULL DEFAULT NULL ,
  `caseValidationNTM` INT(11) NULL DEFAULT NULL ,
  `minStockOnHand` INT(11) NULL DEFAULT NULL ,
  `maxStockOnHand` INT(11) NULL DEFAULT NULL ,
  `suspectCaseNumber` INT(11) NULL DEFAULT NULL ,
  `treatMonitoringInput` INT(11) NOT NULL ,
  `allowRegAfterDiagnosis` TINYINT(1) NOT NULL ,
  `allowDiagAfterTreatment` TINYINT(1) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_workspace_transactionlog1` (`lasttransaction_id` ASC) ,
  INDEX `fk_workspace_transactionlog2` (`createtransaction_id` ASC) ,
  INDEX `FK4217EC95E0BC8241` (`createtransaction_id` ASC) ,
  INDEX `FK4217EC95798D9E5B` (`lasttransaction_id` ASC) ,
  CONSTRAINT `FK4217EC95798D9E5B`
    FOREIGN KEY (`lasttransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK4217EC95E0BC8241`
    FOREIGN KEY (`createtransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_workspace_transactionlog1`
    FOREIGN KEY (`lasttransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_workspace_transactionlog2`
    FOREIGN KEY (`createtransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`healthsystem`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`healthsystem` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`healthsystem` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `name1` VARCHAR(100) NOT NULL ,
  `name2` VARCHAR(100) NULL DEFAULT NULL ,
  `WORKSPACE_ID` INT(11) NOT NULL ,
  `legacyId` VARCHAR(50) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK28F57FCBB9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `fk_healthsystem_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_healthsystem_transactionlog2` (`createTransaction_ID` ASC) ,
  INDEX `FK55C53F8BE0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FK55C53F8B798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK55C53F8B798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK55C53F8BB3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK55C53F8BE0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_healthsystem_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_healthsystem_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tbunit`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tbunit` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tbunit` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `address` VARCHAR(80) NULL DEFAULT NULL ,
  `batchControl` BIT(1) NOT NULL ,
  `changeEstimatedQuantity` BIT(1) NOT NULL ,
  `dispensingFrequency` INT(11) NULL DEFAULT NULL ,
  `district` VARCHAR(50) NULL DEFAULT NULL ,
  `legacyId` VARCHAR(50) NULL DEFAULT NULL ,
  `mdrHealthUnit` BIT(1) NOT NULL ,
  `medicineStorage` BIT(1) NOT NULL ,
  `medicineSupplier` BIT(1) NOT NULL ,
  `name1` VARCHAR(100) NULL DEFAULT NULL ,
  `name2` VARCHAR(100) NULL DEFAULT NULL ,
  `numDaysOrder` INT(11) NULL DEFAULT NULL ,
  `orderOverMinimum` BIT(1) NOT NULL ,
  `receivingFromSource` BIT(1) NOT NULL ,
  `tbHealthUnit` BIT(1) NOT NULL ,
  `treatmentHealthUnit` BIT(1) NOT NULL ,
  `WORKSPACE_ID` INT(11) NOT NULL ,
  `ADMINUNIT_ID` INT(11) NOT NULL ,
  `AUTHORIZERUNIT_ID` INT(11) NULL DEFAULT NULL ,
  `FIRSTLINE_SUPPLIER_ID` INT(11) NULL DEFAULT NULL ,
  `HEALTHSYSTEM_ID` INT(11) NOT NULL ,
  `SECONDLINE_SUPPLIER_ID` INT(11) NULL DEFAULT NULL ,
  `notifHealthUnit` BIT(1) NULL DEFAULT NULL ,
  `active` BIT(1) NULL DEFAULT NULL ,
  `medManStartDate` DATE NULL DEFAULT NULL ,
  `patientDispensing` BIT(1) NULL DEFAULT NULL ,
  `addressCont` VARCHAR(200) NULL DEFAULT NULL ,
  `shipAddress` VARCHAR(200) NULL DEFAULT NULL ,
  `shipAddressCont` VARCHAR(200) NULL DEFAULT NULL ,
  `shipContactName` VARCHAR(200) NULL DEFAULT NULL ,
  `shipContactPhone` VARCHAR(200) NULL DEFAULT NULL ,
  `zipCode` VARCHAR(50) NULL DEFAULT NULL ,
  `phoneNumber` VARCHAR(100) NULL DEFAULT NULL ,
  `ntmHealthUnit` BIT(1) NOT NULL DEFAULT b'0' ,
  `lasttransaction_id` INT(11) NULL DEFAULT NULL ,
  `createtransaction_id` INT(11) NULL DEFAULT NULL ,
  `limitDateMedicineMovement` DATE NULL DEFAULT NULL ,
  `shipZipCode` VARCHAR(100) NULL DEFAULT NULL ,
  `ship_adminunit_id` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK94F2ED12755AB8EC` (`HEALTHSYSTEM_ID` ASC) ,
  INDEX `FK94F2ED128A1004CB` (`ADMINUNIT_ID` ASC) ,
  INDEX `FK94F2ED12E2F2D51` (`AUTHORIZERUNIT_ID` ASC) ,
  INDEX `FK94F2ED12F0E95A7B` (`SECONDLINE_SUPPLIER_ID` ASC) ,
  INDEX `FK94F2ED12B9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `FK94F2ED1247B0F0B7` (`FIRSTLINE_SUPPLIER_ID` ASC) ,
  INDEX `fk_tbunit_transactionlog1` (`lasttransaction_id` ASC) ,
  INDEX `fk_tbunit_transactionlog2` (`createtransaction_id` ASC) ,
  INDEX `FKCB8E00F2E0BC8241` (`createtransaction_id` ASC) ,
  INDEX `FKCB8E00F2798D9E5B` (`lasttransaction_id` ASC) ,
  INDEX `FK_unit_shipau` (`ship_adminunit_id` ASC) ,
  CONSTRAINT `FKCB8E00F221EFF75D`
    FOREIGN KEY (`HEALTHSYSTEM_ID` )
    REFERENCES `etbmanagerd`.`healthsystem` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKCB8E00F2798D9E5B`
    FOREIGN KEY (`lasttransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FKCB8E00F28C2681AC`
    FOREIGN KEY (`SECONDLINE_SUPPLIER_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKCB8E00F2A96C5482`
    FOREIGN KEY (`AUTHORIZERUNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKCB8E00F2B3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKCB8E00F2CEDEEA7C`
    FOREIGN KEY (`ADMINUNIT_ID` )
    REFERENCES `etbmanagerd`.`administrativeunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKCB8E00F2E0BC8241`
    FOREIGN KEY (`createtransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FKCB8E00F2E2EE17E8`
    FOREIGN KEY (`FIRSTLINE_SUPPLIER_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_tbunit_transactionlog1`
    FOREIGN KEY (`lasttransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_tbunit_transactionlog2`
    FOREIGN KEY (`createtransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FK_unit_shipau`
    FOREIGN KEY (`ship_adminunit_id` )
    REFERENCES `etbmanagerd`.`administrativeunit` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`workspacelog`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`workspacelog` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`workspacelog` (
  `id` INT(11) NOT NULL ,
  `name1` VARCHAR(100) NULL DEFAULT NULL ,
  `name2` VARCHAR(100) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`userlog`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`userlog` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`userlog` (
  `id` INT(11) NOT NULL ,
  `name` VARCHAR(100) NOT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`userrole`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`userrole` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`userrole` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `changeable` BIT(1) NOT NULL ,
  `code` VARCHAR(255) NULL DEFAULT NULL ,
  `Role_Name` VARCHAR(80) NOT NULL ,
  `internalUse` BIT(1) NOT NULL DEFAULT b'0' ,
  `byCaseClassification` BIT(1) NULL DEFAULT NULL ,
  `messageKey` VARCHAR(100) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
AUTO_INCREMENT = 225
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`transactionlog`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`transactionlog` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`transactionlog` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `action` INT(11) NULL DEFAULT NULL ,
  `entityDescription` VARCHAR(100) NULL DEFAULT NULL ,
  `entityId` INT(11) NULL DEFAULT NULL ,
  `transactionDate` DATETIME NOT NULL ,
  `ROLE_ID` INT(11) NOT NULL ,
  `USERLOG_ID` INT(11) NULL DEFAULT NULL ,
  `WORKSPACELOG_ID` INT(11) NULL DEFAULT NULL ,
  `comments` LONGTEXT NULL DEFAULT NULL ,
  `titleSuffix` VARCHAR(200) NULL DEFAULT NULL ,
  `ADMINUNIT_ID` INT(11) NULL DEFAULT NULL ,
  `UNIT_ID` INT(11) NULL DEFAULT NULL ,
  `entityClass` VARCHAR(100) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK82CFA6F8AF6C57` (`ROLE_ID` ASC) ,
  INDEX `FK82CFA6E97A0CC8` (`USERLOG_ID` ASC) ,
  INDEX `FK82CFA65B56462C` (`WORKSPACELOG_ID` ASC) ,
  INDEX `FK82CFA6B5E1CBB7` (`USERLOG_ID` ASC) ,
  INDEX `FK82CFA67EB849D` (`WORKSPACELOG_ID` ASC) ,
  INDEX `FK3C3C7BA6CEDEEA7C` (`ADMINUNIT_ID` ASC) ,
  INDEX `FK3C3C7BA671E04A4B` (`UNIT_ID` ASC) ,
  INDEX `idx_entityid` USING BTREE (`entityId` ASC) ,
  INDEX `IDX_WS_EntityClass` (`WORKSPACELOG_ID` ASC, `entityClass` ASC) ,
  CONSTRAINT `FK3C3C7BA671E04A4B`
    FOREIGN KEY (`UNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FK3C3C7BA67EB849D`
    FOREIGN KEY (`WORKSPACELOG_ID` )
    REFERENCES `etbmanagerd`.`workspacelog` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK3C3C7BA6B5E1CBB7`
    FOREIGN KEY (`USERLOG_ID` )
    REFERENCES `etbmanagerd`.`userlog` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK3C3C7BA6B93F8B48`
    FOREIGN KEY (`ROLE_ID` )
    REFERENCES `etbmanagerd`.`userrole` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK3C3C7BA6CEDEEA7C`
    FOREIGN KEY (`ADMINUNIT_ID` )
    REFERENCES `etbmanagerd`.`administrativeunit` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`countrystructure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`countrystructure` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`countrystructure` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `STRUCTURE_LEVEL` INT(11) NULL DEFAULT NULL ,
  `name1` VARCHAR(100) NULL DEFAULT NULL ,
  `name2` VARCHAR(100) NULL DEFAULT NULL ,
  `WORKSPACE_ID` INT(11) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK7716629DB9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `fk_countrystructure_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_countrystructure_transactionlog2` (`createTransaction_ID` ASC) ,
  INDEX `FK31DFFE9DE0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FK31DFFE9D798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK31DFFE9D798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK31DFFE9DB3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK31DFFE9DE0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_countrystructure_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_countrystructure_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`administrativeunit`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`administrativeunit` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`administrativeunit` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'Unique identifier of the record' ,
  `code` VARCHAR(15) NOT NULL COMMENT 'Hierarchical code of the administrative unit in relation to the others' ,
  `legacyId` VARCHAR(50) NULL DEFAULT NULL COMMENT 'Identifies an ID of a legacy system. It helps on integration with other systems.' ,
  `name1` VARCHAR(100) NOT NULL COMMENT 'Name of the administrative unit in the country\'s main language' ,
  `name2` VARCHAR(100) NULL DEFAULT NULL COMMENT 'Name of the administrative unit in an alternate language, if available' ,
  `unitsCount` INT(11) NOT NULL COMMENT 'Number of sub units under this unit' ,
  `WORKSPACE_ID` INT(11) NOT NULL COMMENT 'Workspace that this record belongs to' ,
  `COUNTRYSTRUCTURE_ID` INT(11) NULL DEFAULT NULL COMMENT 'The geographic unit of this admin unit (region, city, municipality, village, etc).' ,
  `PARENT_ID` INT(11) NULL DEFAULT NULL COMMENT 'The parent administrative unit of this record (Null, if it\'s in the top level)' ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL COMMENT 'ID of the last transaction log executed in this record that resulted in the creation or update of this record' ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL COMMENT 'ID of the transaction log that created this record' ,
  PRIMARY KEY (`id`) ,
  INDEX `FKA92443F27180F26C` (`COUNTRYSTRUCTURE_ID` ASC) ,
  INDEX `FKA92443F2B9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `FKA92443F28CCD4614` (`PARENT_ID` ASC) ,
  INDEX `fk_administrativeunit_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_administrativeunit_transactionlog2` (`createTransaction_ID` ASC) ,
  INDEX `FK3B6F13B2E0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FK3B6F13B2798D9E5B` (`lastTransaction_ID` ASC) ,
  INDEX `fk_adminunit_code` (`code` ASC) ,
  CONSTRAINT `FK3B6F13B23B7F905D`
    FOREIGN KEY (`COUNTRYSTRUCTURE_ID` )
    REFERENCES `etbmanagerd`.`countrystructure` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK3B6F13B2798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK3B6F13B2B3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK3B6F13B2D19C2BC5`
    FOREIGN KEY (`PARENT_ID` )
    REFERENCES `etbmanagerd`.`administrativeunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK3B6F13B2E0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_administrativeunit_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_administrativeunit_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8
COMMENT = 'Administrative unit of the country, i.e, each register represents a geographical unit'
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`agerange`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`agerange` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`agerange` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `iniAge` INT(11) NOT NULL ,
  `name1` VARCHAR(100) NULL DEFAULT NULL ,
  `name2` VARCHAR(100) NULL DEFAULT NULL ,
  `WORKSPACE_ID` INT(11) NULL DEFAULT NULL ,
  `endAge` INT(11) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK59D8FB9EB9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `fk_agerange_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_agerange_transactionlog2` (`createTransaction_ID` ASC) ,
  INDEX `FK57C1879EE0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FK57C1879E798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK57C1879E798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK57C1879EB3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK57C1879EE0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_agerange_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_agerange_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 9
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`source`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`source` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`source` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `ABBREV_NAME1` VARCHAR(255) NOT NULL ,
  `ABBREV_NAME2` VARCHAR(255) NULL DEFAULT NULL ,
  `name1` VARCHAR(100) NOT NULL ,
  `name2` VARCHAR(100) NULL DEFAULT NULL ,
  `WORKSPACE_ID` INT(11) NOT NULL ,
  `legacyId` VARCHAR(50) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK93F5543BB9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `fk_source_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_source_transactionlog2` (`createTransaction_ID` ASC) ,
  INDEX `FKCA90681BE0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FKCA90681B798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FKCA90681B798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FKCA90681BB3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKCA90681BE0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_source_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_source_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`fieldvalue`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`fieldvalue` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`fieldvalue` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `customId` VARCHAR(20) NULL DEFAULT NULL ,
  `field` INT(11) NULL DEFAULT NULL ,
  `name1` VARCHAR(100) NULL DEFAULT NULL ,
  `name2` VARCHAR(100) NULL DEFAULT NULL ,
  `SHORT_NAME1` VARCHAR(255) NULL DEFAULT NULL ,
  `SHORT_NAME2` VARCHAR(255) NULL DEFAULT NULL ,
  `WORKSPACE_ID` INT(11) NULL DEFAULT NULL ,
  `other` BIT(1) NULL DEFAULT NULL ,
  `otherDescription` VARCHAR(100) NULL DEFAULT NULL ,
  `displayOrder` INT(11) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK98AC68B7B9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `fk_fieldvalue_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_fieldvalue_transactionlog2` (`createTransaction_ID` ASC) ,
  INDEX `FK239D7CB7E0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FK239D7CB7798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK239D7CB7798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK239D7CB7B3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK239D7CB7E0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_fieldvalue_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_fieldvalue_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`productgroup`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`productgroup` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`productgroup` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `GROUP_CODE` VARCHAR(30) NULL DEFAULT NULL ,
  `GROUP_LEVEL` INT(11) NULL DEFAULT NULL ,
  `GROUP_NAME` VARCHAR(100) NULL DEFAULT NULL ,
  `WORKSPACE_ID` INT(11) NOT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK44F14AF0B9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `fk_productgroup_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_productgroup_transactionlog2` (`createTransaction_ID` ASC) ,
  INDEX `FK3CE8E6F0E0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FK3CE8E6F0798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK3CE8E6F0798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK3CE8E6F0B3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE,
  CONSTRAINT `FK3CE8E6F0E0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_productgroup_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_productgroup_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`medicine`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`medicine` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`medicine` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `abbrevName` VARCHAR(30) NULL DEFAULT NULL ,
  `category` INT(11) NULL DEFAULT NULL ,
  `dosageForm` VARCHAR(50) NULL DEFAULT NULL ,
  `name1` VARCHAR(100) NOT NULL ,
  `name2` VARCHAR(100) NULL DEFAULT NULL ,
  `line` INT(11) NULL DEFAULT NULL ,
  `strength` VARCHAR(30) NULL DEFAULT NULL ,
  `strengthUnit` VARCHAR(50) NULL DEFAULT NULL ,
  `WORKSPACE_ID` INT(11) NOT NULL ,
  `GROUP_ID` INT(11) NULL DEFAULT NULL ,
  `legacyId` VARCHAR(50) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKCE2ABA5A7A85F89D` (`GROUP_ID` ASC) ,
  INDEX `FKCE2ABA5AB9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `fk_medicine_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_medicine_transactionlog2` (`createTransaction_ID` ASC) ,
  INDEX `FKCA50563AE0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FKCA50563A798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FKCA50563A271B370E`
    FOREIGN KEY (`GROUP_ID` )
    REFERENCES `etbmanagerd`.`productgroup` (`id` )
    ON DELETE CASCADE,
  CONSTRAINT `FKCA50563A798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FKCA50563AB3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKCA50563AE0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_medicine_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_medicine_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 941302
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`movement`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`movement` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`movement` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comment` VARCHAR(250) NULL DEFAULT NULL ,
  `mov_date` DATE NOT NULL ,
  `oper` INT(11) NOT NULL ,
  `quantity` INT(11) NOT NULL ,
  `recordDate` DATETIME NOT NULL ,
  `type` INT(11) NULL DEFAULT NULL ,
  `totalPrice` FLOAT NOT NULL ,
  `MEDICINE_ID` INT(11) NOT NULL ,
  `SOURCE_ID` INT(11) NOT NULL ,
  `UNIT_ID` INT(11) NOT NULL ,
  `stockQuantity` INT(11) NULL DEFAULT NULL ,
  `unitPrice` FLOAT NULL DEFAULT NULL ,
  `ADJUSTMENT_ID` INT(11) NULL DEFAULT NULL ,
  `availableQuantity` INT(11) NULL DEFAULT NULL ,
  `totalPriceInventory` FLOAT NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKFDAC64CF817DE04C` (`SOURCE_ID` ASC) ,
  INDEX `FKFDAC64CFFBB4E36C` (`MEDICINE_ID` ASC) ,
  INDEX `FKFDAC64CFD6A3231A` (`UNIT_ID` ASC) ,
  INDEX `FKF9D200AFAB3243E7` (`ADJUSTMENT_ID` ASC) ,
  CONSTRAINT `FKF9D200AF1CBB077D`
    FOREIGN KEY (`SOURCE_ID` )
    REFERENCES `etbmanagerd`.`source` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKF9D200AF71E04A4B`
    FOREIGN KEY (`UNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKF9D200AFAB3243E7`
    FOREIGN KEY (`ADJUSTMENT_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKF9D200AFBC45025D`
    FOREIGN KEY (`MEDICINE_ID` )
    REFERENCES `etbmanagerd`.`medicine` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`medicinereceiving`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`medicinereceiving` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`medicinereceiving` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `receivingDate` DATE NOT NULL ,
  `SOURCE_ID` INT(11) NOT NULL ,
  `UNIT_ID` INT(11) NOT NULL ,
  `comments` LONGTEXT NULL DEFAULT NULL ,
  `totalPrice` FLOAT NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK603BCDA6817DE04C` (`SOURCE_ID` ASC) ,
  INDEX `FK603BCDA6D6A3231A` (`UNIT_ID` ASC) ,
  CONSTRAINT `FKFE056DE61CBB077D`
    FOREIGN KEY (`SOURCE_ID` )
    REFERENCES `etbmanagerd`.`source` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKFE056DE671E04A4B`
    FOREIGN KEY (`UNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`medicinereceivingitem`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`medicinereceivingitem` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`medicinereceivingitem` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `RECEIVING_ID` INT(11) NOT NULL ,
  `MOVEMENT_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK1CD379D95E43ACEE` (`RECEIVING_ID` ASC) ,
  INDEX `FK1CD379D98D91D34C` (`MOVEMENT_ID` ASC) ,
  CONSTRAINT `FK806185F94E21F23D`
    FOREIGN KEY (`MOVEMENT_ID` )
    REFERENCES `etbmanagerd`.`movement` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK806185F9D418CD1D`
    FOREIGN KEY (`RECEIVING_ID` )
    REFERENCES `etbmanagerd`.`medicinereceiving` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`batch`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`batch` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`batch` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `batchNumber` VARCHAR(30) NOT NULL ,
  `brandName` VARCHAR(80) NULL DEFAULT NULL ,
  `container` INT(11) NULL DEFAULT NULL ,
  `expiryDate` DATE NOT NULL ,
  `manufacturer` VARCHAR(80) NULL DEFAULT NULL ,
  `quantityContainer` INT(11) NOT NULL ,
  `quantityReceived` INT(11) NOT NULL ,
  `unitPrice` FLOAT NOT NULL ,
  `MEDICINE_ID` INT(11) NOT NULL ,
  `RECEIVINGITEM_ID` INT(11) NULL DEFAULT NULL ,
  `registCardNumber` VARCHAR(30) NULL DEFAULT NULL ,
  `registCardBeginDate` DATE NULL DEFAULT NULL ,
  `registCardEndDate` DATE NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK3CFE71A2DCF2A2E` (`RECEIVINGITEM_ID` ASC) ,
  INDEX `FK3CFE71AFBB4E36C` (`MEDICINE_ID` ASC) ,
  CONSTRAINT `FK592D73A76AA9ADD`
    FOREIGN KEY (`RECEIVINGITEM_ID` )
    REFERENCES `etbmanagerd`.`medicinereceivingitem` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK592D73ABC45025D`
    FOREIGN KEY (`MEDICINE_ID` )
    REFERENCES `etbmanagerd`.`medicine` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`medicinedispensing`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`medicinedispensing` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`medicinedispensing` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `endDate` DATE NULL DEFAULT NULL ,
  `iniDate` DATE NULL DEFAULT NULL ,
  `UNIT_ID` INT(11) NULL DEFAULT NULL ,
  `dispensingDate` DATE NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK590328D4D6A3231A` (`UNIT_ID` ASC) ,
  CONSTRAINT `FK746D909471E04A4B`
    FOREIGN KEY (`UNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`batchdispensing`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`batchdispensing` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`batchdispensing` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `quantity` INT(11) NOT NULL ,
  `BATCH_ID` INT(11) NOT NULL ,
  `DISPENSING_ID` INT(11) NULL DEFAULT NULL ,
  `SOURCE_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK7EA765941CBB077D` (`SOURCE_ID` ASC) ,
  INDEX `FK7EA765943B281F77` (`BATCH_ID` ASC) ,
  INDEX `FK7EA76594BA967077` (`DISPENSING_ID` ASC) ,
  CONSTRAINT `FK4342D1941CBB077D`
    FOREIGN KEY (`SOURCE_ID` )
    REFERENCES `etbmanagerd`.`source` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK4342D1943B281F77`
    FOREIGN KEY (`BATCH_ID` )
    REFERENCES `etbmanagerd`.`batch` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK4342D194BA967077`
    FOREIGN KEY (`DISPENSING_ID` )
    REFERENCES `etbmanagerd`.`medicinedispensing` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`batchmovement`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`batchmovement` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`batchmovement` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `quantity` INT(11) NOT NULL ,
  `BATCH_ID` INT(11) NOT NULL ,
  `MOVEMENT_ID` INT(11) NOT NULL ,
  `availableQuantity` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK919891E9DB4F70C8` (`BATCH_ID` ASC) ,
  INDEX `FK919891E98D91D34C` (`MOVEMENT_ID` ASC) ,
  CONSTRAINT `FK5E1EFDE93B281F77`
    FOREIGN KEY (`BATCH_ID` )
    REFERENCES `etbmanagerd`.`batch` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK5E1EFDE94E21F23D`
    FOREIGN KEY (`MOVEMENT_ID` )
    REFERENCES `etbmanagerd`.`movement` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`batchquantity`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`batchquantity` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`batchquantity` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `quantity` INT(11) NOT NULL ,
  `BATCH_ID` INT(11) NOT NULL ,
  `SOURCE_ID` INT(11) NOT NULL ,
  `UNIT_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK4B2EF5C5817DE04C` (`SOURCE_ID` ASC) ,
  INDEX `FK4B2EF5C5DB4F70C8` (`BATCH_ID` ASC) ,
  INDEX `FK4B2EF5C5D6A3231A` (`UNIT_ID` ASC) ,
  CONSTRAINT `FK17B561C51CBB077D`
    FOREIGN KEY (`SOURCE_ID` )
    REFERENCES `etbmanagerd`.`source` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK17B561C53B281F77`
    FOREIGN KEY (`BATCH_ID` )
    REFERENCES `etbmanagerd`.`batch` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK17B561C571E04A4B`
    FOREIGN KEY (`UNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`patient`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`patient` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`patient` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `birthDate` DATETIME NULL DEFAULT NULL ,
  `gender` INT(11) NOT NULL ,
  `lastName` VARCHAR(100) NULL DEFAULT NULL ,
  `legacyId` VARCHAR(50) NULL DEFAULT NULL ,
  `middleName` VARCHAR(100) NULL DEFAULT NULL ,
  `motherName` VARCHAR(100) NULL DEFAULT NULL ,
  `PATIENT_NAME` VARCHAR(100) NOT NULL ,
  `recordNumber` INT(11) NULL DEFAULT NULL ,
  `securityNumber` VARCHAR(50) NULL DEFAULT NULL ,
  `WORKSPACE_ID` INT(11) NOT NULL ,
  `fatherName` VARCHAR(100) NULL DEFAULT NULL ,
  `salary` INT(11) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK340C82E5B9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `fk_patient_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_patient_transactionlog2` (`createTransaction_ID` ASC) ,
  INDEX `FKD0D3EB05E0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FKD0D3EB05798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FKD0D3EB05798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FKD0D3EB05B3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKD0D3EB05E0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_patient_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_patient_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`regimen`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`regimen` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`regimen` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `monthsContinuousPhase` INT(11) NULL DEFAULT NULL ,
  `monthsIntensivePhase` INT(11) NULL DEFAULT NULL ,
  `regimen_name` VARCHAR(100) NULL DEFAULT NULL ,
  `WORKSPACE_ID` INT(11) NOT NULL ,
  `legacyId` VARCHAR(50) NULL DEFAULT NULL ,
  `caseClassification` INT(11) NOT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKA3F54741B9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `fk_regimen_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_regimen_transactionlog2` (`createTransaction_ID` ASC) ,
  INDEX `FK40BCAF61E0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FK40BCAF61798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK40BCAF61798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK40BCAF61B3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK40BCAF61E0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_regimen_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_regimen_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 940907
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tbcase`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tbcase` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tbcase` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `age` INT(11) NOT NULL ,
  `caseNumber` INT(11) NULL DEFAULT NULL ,
  `category` INT(11) NULL DEFAULT NULL ,
  `classification` INT(11) NOT NULL ,
  `CURR_ADDRESS` VARCHAR(255) NULL DEFAULT NULL ,
  `CURR_COMPLEMENT` VARCHAR(255) NULL DEFAULT NULL ,
  `CURR_LOCALITYTYPE` INT(11) NULL DEFAULT NULL ,
  `CURR_ZIPCODE` VARCHAR(255) NULL DEFAULT NULL ,
  `diagnosisDate` DATE NULL DEFAULT NULL ,
  `endTreatmentDate` DATE NULL DEFAULT NULL ,
  `infectionSite` INT(11) NULL DEFAULT NULL ,
  `iniTreatmentDate` DATE NULL DEFAULT NULL ,
  `legacyId` VARCHAR(50) NULL DEFAULT NULL ,
  `nationality` INT(11) NULL DEFAULT NULL ,
  `NOTIF_ADDRESS` VARCHAR(255) NULL DEFAULT NULL ,
  `NOTIF_COMPLEMENT` VARCHAR(255) NULL DEFAULT NULL ,
  `NOTIF_LOCALITYTYPE` INT(11) NULL DEFAULT NULL ,
  `MOBILENUMBER` VARCHAR(100) NULL DEFAULT NULL ,
  `PHONENUMBER` VARCHAR(100) NULL DEFAULT NULL ,
  `NOTIF_ZIPCODE` VARCHAR(255) NULL DEFAULT NULL ,
  `notifAddressChanged` BIT(1) NOT NULL ,
  `otherOutcome` VARCHAR(100) NULL DEFAULT NULL ,
  `outcomeDate` DATE NULL DEFAULT NULL ,
  `patientType` INT(11) NULL DEFAULT NULL ,
  `patientTypeOther` VARCHAR(100) NULL DEFAULT NULL ,
  `registrationDate` DATE NOT NULL ,
  `state` INT(11) NOT NULL ,
  `CURR_ADMINUNIT_ID` INT(11) NULL DEFAULT NULL ,
  `NOTIF_ADMINUNIT_ID` INT(11) NULL DEFAULT NULL ,
  `NOTIFICATION_UNIT_ID` INT(11) NULL DEFAULT NULL ,
  `PATIENT_ID` INT(11) NOT NULL ,
  `diagnosisType` INT(11) NULL DEFAULT NULL ,
  `registrationCode` VARCHAR(50) NULL DEFAULT NULL ,
  `drugResistanceType` INT(11) NULL DEFAULT NULL ,
  `comments` LONGTEXT NULL DEFAULT NULL ,
  `patientContactName` VARCHAR(100) NULL DEFAULT NULL ,
  `tbContact` BIT(1) NULL DEFAULT NULL ,
  `EXTRAPULMONARY_ID` INT(11) NULL DEFAULT NULL ,
  `EXTRAPULMONARY2_ID` INT(11) NULL DEFAULT NULL ,
  `PULMONARY_ID` INT(11) NULL DEFAULT NULL ,
  `daysTreatPlanned` INT(11) NULL DEFAULT NULL ,
  `validationState` INT(11) UNSIGNED NULL DEFAULT NULL ,
  `issueCounter` INT(11) UNSIGNED NULL DEFAULT NULL ,
  `version` INT(11) NULL DEFAULT NULL ,
  `REGIMEN_ID` INT(11) NULL DEFAULT NULL ,
  `iniContinuousPhase` DATE NULL DEFAULT NULL ,
  `REGIMEN_INI_ID` INT(11) NULL DEFAULT NULL ,
  `OWNER_UNIT_ID` INT(11) NULL DEFAULT NULL ,
  `LASTTRANSACTION_ID` INT(11) NULL DEFAULT NULL ,
  `CREATETRANSACTION_ID` INT(11) NULL DEFAULT NULL ,
  `suspectRegistrationCode` VARCHAR(50) NULL DEFAULT NULL ,
  `suspectClassification` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK94DC02DEE27737C6` (`NOTIFICATION_UNIT_ID` ASC) ,
  INDEX `FK94DC02DEBEF3BD68` (`PATIENT_ID` ASC) ,
  INDEX `FK94DC02DE8C7B4D7A` (`NOTIF_ADMINUNIT_ID` ASC) ,
  INDEX `FK94DC02DEEE83C9B8` (`CURR_ADMINUNIT_ID` ASC) ,
  INDEX `FK94DC02DEA02EA184` (`PULMONARY_ID` ASC) ,
  INDEX `FK94DC02DE82A21950` (`EXTRAPULMONARY_ID` ASC) ,
  INDEX `FK94DC02DED9B34E64` (`EXTRAPULMONARY2_ID` ASC) ,
  INDEX `FK94DC02DE47681D40` (`PULMONARY_ID` ASC) ,
  INDEX `FK94DC02DE2F370FE8` (`REGIMEN_ID` ASC) ,
  INDEX `FK94DC02DECEBEE212` (`REGIMEN_INI_ID` ASC) ,
  INDEX `FKCB85A29E5E8B80BF` (`OWNER_UNIT_ID` ASC) ,
  INDEX `FKTBCASE_LASTTRANS` (`LASTTRANSACTION_ID` ASC) ,
  INDEX `fk_tbcase_transactionlog1` (`CREATETRANSACTION_ID` ASC) ,
  INDEX `FKCB85A29EE0BC8241` (`CREATETRANSACTION_ID` ASC) ,
  INDEX `FKCB85A29E798D9E5B` (`LASTTRANSACTION_ID` ASC) ,
  CONSTRAINT `FKCB85A29E246C43F1`
    FOREIGN KEY (`PULMONARY_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKCB85A29E3352AF69`
    FOREIGN KEY (`CURR_ADMINUNIT_ID` )
    REFERENCES `etbmanagerd`.`administrativeunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKCB85A29E45C57184`
    FOREIGN KEY (`OWNER_UNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKCB85A29E5E8B80BF`
    FOREIGN KEY (`OWNER_UNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` ),
  CONSTRAINT `FKCB85A29E5FA64001`
    FOREIGN KEY (`EXTRAPULMONARY_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKCB85A29E798D9E5B`
    FOREIGN KEY (`LASTTRANSACTION_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FKCB85A29E7DB45EF7`
    FOREIGN KEY (`NOTIFICATION_UNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKCB85A29E8B5B7C57`
    FOREIGN KEY (`PATIENT_ID` )
    REFERENCES `etbmanagerd`.`patient` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKCB85A29EB6B77515`
    FOREIGN KEY (`EXTRAPULMONARY2_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKCB85A29ECEBEE212`
    FOREIGN KEY (`REGIMEN_INI_ID` )
    REFERENCES `etbmanagerd`.`regimen` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKCB85A29ED14A332B`
    FOREIGN KEY (`NOTIF_ADMINUNIT_ID` )
    REFERENCES `etbmanagerd`.`administrativeunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKCB85A29EE0BC8241`
    FOREIGN KEY (`CREATETRANSACTION_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FKCB85A29EFB9ECED7`
    FOREIGN KEY (`REGIMEN_ID` )
    REFERENCES `etbmanagerd`.`regimen` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKTBCASE_LASTTRANS`
    FOREIGN KEY (`LASTTRANSACTION_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_tbcase_transactionlog1`
    FOREIGN KEY (`CREATETRANSACTION_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`uitheme`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`uitheme` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`uitheme` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(255) NULL DEFAULT NULL ,
  `systemTheme` BIT(1) NOT NULL ,
  `defaultTheme` BIT(1) NOT NULL ,
  `path` VARCHAR(250) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
AUTO_INCREMENT = 4
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`userprofile`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`userprofile` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`userprofile` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(100) NOT NULL ,
  `WORKSPACE_ID` INT(11) NOT NULL ,
  `legacyId` VARCHAR(50) NULL DEFAULT NULL ,
  `lasttransaction_id` INT(11) NULL DEFAULT NULL ,
  `createtransaction_id` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK3EFA133EB9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `fk_userprofile_transactionlog1` (`lasttransaction_id` ASC) ,
  INDEX `fk_userprofile_transactionlog2` (`createtransaction_id` ASC) ,
  INDEX `FK7857D37EE0BC8241` (`createtransaction_id` ASC) ,
  INDEX `FK7857D37E798D9E5B` (`lasttransaction_id` ASC) ,
  CONSTRAINT `FK7857D37E798D9E5B`
    FOREIGN KEY (`lasttransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK7857D37EB3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK7857D37EE0BC8241`
    FOREIGN KEY (`createtransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_userprofile_transactionlog1`
    FOREIGN KEY (`lasttransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE CASCADE
    ON UPDATE SET NULL,
  CONSTRAINT `fk_userprofile_transactionlog2`
    FOREIGN KEY (`createtransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE CASCADE
    ON UPDATE SET NULL)
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`userworkspace`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`userworkspace` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`userworkspace` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `playOtherUnits` BIT(1) NOT NULL ,
  `USER_VIEW` INT(11) NOT NULL ,
  `ADMINUNIT_ID` INT(11) NULL DEFAULT NULL ,
  `PROFILE_ID` INT(11) NOT NULL ,
  `TBUNIT_ID` INT(11) NOT NULL ,
  `USER_ID` INT(11) NOT NULL ,
  `WORKSPACE_ID` INT(11) NOT NULL ,
  `HEALTHSYSTEM_ID` INT(11) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `laboratory_id` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKE6C764EA8A1004CB` (`ADMINUNIT_ID` ASC) ,
  INDEX `FKE6C764EAD84E76CC` (`USER_ID` ASC) ,
  INDEX `FKE6C764EAB9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `FKE6C764EAC9D89CEC` (`TBUNIT_ID` ASC) ,
  INDEX `FKE6C764EAB3AFD59D` (`PROFILE_ID` ASC) ,
  INDEX `FKE6C764EA755AB8EC` (`HEALTHSYSTEM_ID` ASC) ,
  INDEX `fkuserworkspace_createtx` (`createTransaction_ID` ASC) ,
  INDEX `fkuserworkspace_lasttx` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK3FB6152A21EFF75D`
    FOREIGN KEY (`HEALTHSYSTEM_ID` )
    REFERENCES `etbmanagerd`.`healthsystem` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK3FB6152A6515C41D`
    FOREIGN KEY (`TBUNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK3FB6152A7730850C`
    FOREIGN KEY (`PROFILE_ID` )
    REFERENCES `etbmanagerd`.`userprofile` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK3FB6152AB3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK3FB6152ABA5DB63D`
    FOREIGN KEY (`USER_ID` )
    REFERENCES `etbmanagerd`.`sys_user` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK3FB6152ACEDEEA7C`
    FOREIGN KEY (`ADMINUNIT_ID` )
    REFERENCES `etbmanagerd`.`administrativeunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fkuserworkspace_createtx`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fkuserworkspace_lasttx`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`sys_user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`sys_user` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`sys_user` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comments` VARCHAR(200) NULL DEFAULT NULL ,
  `email` VARCHAR(80) NOT NULL ,
  `language` VARCHAR(6) NULL DEFAULT NULL ,
  `login` VARCHAR(30) NOT NULL ,
  `user_name` VARCHAR(80) NOT NULL ,
  `user_password` VARCHAR(32) NOT NULL ,
  `registrationDate` DATETIME NULL DEFAULT NULL ,
  `state` INT(11) NULL DEFAULT NULL ,
  `timeZone` VARCHAR(50) NULL DEFAULT NULL ,
  `ADMINUNIT_ID` INT(11) NULL DEFAULT NULL ,
  `DEFAULTWORKSPACE_ID` INT(11) NULL DEFAULT NULL ,
  `PARENTUSER_ID` INT(11) NULL DEFAULT NULL ,
  `legacyId` VARCHAR(50) NULL DEFAULT NULL ,
  `THEME_ID` INT(11) NULL DEFAULT NULL ,
  `sendSystemMessages` TINYINT(1) NULL DEFAULT NULL ,
  `lasttransaction_id` INT(11) NULL DEFAULT NULL ,
  `createtransaction_id` INT(11) NULL DEFAULT NULL ,
  `ulaAccepted` TINYINT(1) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK7873F63D92323AE2` (`PARENTUSER_ID` ASC) ,
  INDEX `FK7873F63D8A1004CB` (`ADMINUNIT_ID` ASC) ,
  INDEX `FK7873F63D885461E` (`DEFAULTWORKSPACE_ID` ASC) ,
  INDEX `FK74A81DFD26183FC3` (`THEME_ID` ASC) ,
  INDEX `fk_sys_user_transactionlog1` (`lasttransaction_id` ASC) ,
  INDEX `fk_sys_user_transactionlog2` (`createtransaction_id` ASC) ,
  CONSTRAINT `FK74A81DFD26183FC3`
    FOREIGN KEY (`THEME_ID` )
    REFERENCES `etbmanagerd`.`uitheme` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FK74A81DFD74417A53`
    FOREIGN KEY (`PARENTUSER_ID` )
    REFERENCES `etbmanagerd`.`sys_user` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FK74A81DFDEE97D5CD`
    FOREIGN KEY (`DEFAULTWORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`userworkspace` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_sys_user_transactionlog1`
    FOREIGN KEY (`lasttransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_sys_user_transactionlog2`
    FOREIGN KEY (`createtransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`casecomment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`casecomment` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`casecomment` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comment` LONGTEXT NOT NULL ,
  `comment_date` DATETIME NOT NULL ,
  `view` INT(11) NOT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  `USER_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKBA21BCEFD84E76CC` (`USER_ID` ASC) ,
  INDEX `FKBA21BCEF8B327BA` (`CASE_ID` ASC) ,
  CONSTRAINT `FKF37F7D2FA3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKF37F7D2FBA5DB63D`
    FOREIGN KEY (`USER_ID` )
    REFERENCES `etbmanagerd`.`sys_user` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`casecomorbidity`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`casecomorbidity` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`casecomorbidity` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comment` VARCHAR(200) NULL DEFAULT NULL ,
  `COMORBIDITY_ID` INT(11) NOT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  `duration` VARCHAR(100) NULL DEFAULT NULL ,
  `otherCaseComorbidity` VARCHAR(255) NULL DEFAULT NULL ,
  `COMORB_ID` INT(11) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK9298ED455ED13EEE` (`COMORBIDITY_ID` ASC) ,
  INDEX `FK9298ED45925E7A4E` (`id` ASC) ,
  INDEX `FK9298ED458B327BA` (`CASE_ID` ASC) ,
  INDEX `FK6A9C8D85C37951F6` (`COMORB_ID` ASC) ,
  INDEX `fkcasecom_lasttx` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK6A9C8D853BD5659F`
    FOREIGN KEY (`COMORBIDITY_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK6A9C8D85A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK6A9C8D85C37951F6`
    FOREIGN KEY (`COMORB_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` ),
  CONSTRAINT `fkcasecom_lasttx`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`casedatabr`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`casedatabr` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`casedatabr` (
  `id` INT(11) NOT NULL ,
  `country` VARCHAR(100) NULL DEFAULT NULL ,
  `failureType` INT(11) NULL DEFAULT NULL ,
  `numSinan` VARCHAR(100) NULL DEFAULT NULL ,
  `prefixMobile` VARCHAR(10) NULL DEFAULT NULL ,
  `prefixPhone` VARCHAR(10) NULL DEFAULT NULL ,
  `usOrigem` VARCHAR(100) NULL DEFAULT NULL ,
  `ADMINUNIT_USORIGEM_ID` INT(11) NULL DEFAULT NULL ,
  `CONTAGPLACE` INT(11) NULL DEFAULT NULL ,
  `EDUCATIONALDEGREE` INT(11) NULL DEFAULT NULL ,
  `POSITION` INT(11) NULL DEFAULT NULL ,
  `PREGNANCEPERIOD` INT(11) NULL DEFAULT NULL ,
  `SCHEMACHANGETYPE` INT(11) NULL DEFAULT NULL ,
  `SKINCOLOR` INT(11) NULL DEFAULT NULL ,
  `currAddressNumber` VARCHAR(50) NULL DEFAULT NULL ,
  `notifAddressNumber` VARCHAR(50) NULL DEFAULT NULL ,
  `tipoResistencia` INT(11) NULL DEFAULT NULL ,
  `currDistrict` VARCHAR(100) NULL DEFAULT NULL ,
  `notifDistrict` VARCHAR(100) NULL DEFAULT NULL ,
  `MICROBACTERIOSE` INT(11) NULL DEFAULT NULL ,
  `contagPlace_Complement` VARCHAR(255) NULL DEFAULT NULL ,
  `educationalDegree_Complement` VARCHAR(255) NULL DEFAULT NULL ,
  `positionOther` VARCHAR(255) NULL DEFAULT NULL ,
  `skinColor_Complement` VARCHAR(255) NULL DEFAULT NULL ,
  `clinicalEvolution` INT(11) NULL DEFAULT NULL ,
  `supervisedTreatment` INT(11) NULL DEFAULT NULL ,
  `supervisionUnitName` VARCHAR(100) NULL DEFAULT NULL ,
  `OUTCOME_REGIMENCHANGED` INT(11) NULL DEFAULT NULL ,
  `MICROBACTERIOSE_Complement` VARCHAR(255) NULL DEFAULT NULL ,
  `RESISTANCETYPE_Complement` VARCHAR(255) NULL DEFAULT NULL ,
  `SCHEMACHANGETYPE_Complement` VARCHAR(255) NULL DEFAULT NULL ,
  `OUTCOME_RESISTANCETYPE` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK1FB9D4EA6808C532` (`POSITION` ASC) ,
  INDEX `FK1FB9D4EA828ADA68` (`EDUCATIONALDEGREE` ASC) ,
  INDEX `FK1FB9D4EA61B11A23` (`PREGNANCEPERIOD` ASC) ,
  INDEX `FK1FB9D4EA309085EB` (`ADMINUNIT_USORIGEM_ID` ASC) ,
  INDEX `FK1FB9D4EAB51C5C0F` (`SKINCOLOR` ASC) ,
  INDEX `FK1FB9D4EA5DE9D754` (`SCHEMACHANGETYPE` ASC) ,
  INDEX `FK1FB9D4EA448BD538` (`CONTAGPLACE` ASC) ,
  INDEX `FK1FB9D4EAE40EAEE2` (`MICROBACTERIOSE` ASC) ,
  INDEX `FK1FB9D4EAE3EE98C9` (`OUTCOME_REGIMENCHANGED` ASC) ,
  CONSTRAINT `FKDF8310AA218FFBE9`
    FOREIGN KEY (`CONTAGPLACE` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKDF8310AA3AEDFE05`
    FOREIGN KEY (`SCHEMACHANGETYPE` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKDF8310AA3EB540D4`
    FOREIGN KEY (`PREGNANCEPERIOD` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKDF8310AA450CEBE3`
    FOREIGN KEY (`POSITION` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKDF8310AA5F8F0119`
    FOREIGN KEY (`EDUCATIONALDEGREE` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKDF8310AA755F6B9C`
    FOREIGN KEY (`ADMINUNIT_USORIGEM_ID` )
    REFERENCES `etbmanagerd`.`administrativeunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKDF8310AA82DD737C`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKDF8310AA922082C0`
    FOREIGN KEY (`SKINCOLOR` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKDF8310AAC0F2BF7A`
    FOREIGN KEY (`OUTCOME_REGIMENCHANGED` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKDF8310AAC112D593`
    FOREIGN KEY (`MICROBACTERIOSE` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`casedatana`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`casedatana` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`casedatana` (
  `id` INT(11) NOT NULL ,
  `comments` LONGTEXT NULL DEFAULT NULL ,
  `EVENT_DATE` DATE NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`casedataph`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`casedataph` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`casedataph` (
  `id` INT(11) NOT NULL ,
  `personToNotify` VARCHAR(100) NULL DEFAULT NULL ,
  `registrationCode` VARCHAR(100) NULL DEFAULT NULL ,
  `screeningCode` VARCHAR(100) NULL DEFAULT NULL ,
  `SOURCEOFREFERRAL_ID` INT(11) NULL DEFAULT NULL ,
  `catIVRegNumber` VARCHAR(50) NULL DEFAULT NULL ,
  `personAddress` VARCHAR(100) NULL DEFAULT NULL ,
  `personPhone` VARCHAR(100) NULL DEFAULT NULL ,
  `preEnrollmentNumber` VARCHAR(50) NULL DEFAULT NULL ,
  `preEnrollmentOrigin` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK1FB9D692974F3294` (`SOURCEOFREFERRAL_ID` ASC) ,
  CONSTRAINT `FKDF83125274535945`
    FOREIGN KEY (`SOURCEOFREFERRAL_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`casedataua`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`casedataua` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`casedataua` (
  `id` INT(11) NOT NULL ,
  `alcoholAbuse` BIT(1) NOT NULL ,
  `closestContact` VARCHAR(100) NULL DEFAULT NULL ,
  `dateCMCCRegistration` DATE NULL DEFAULT NULL ,
  `dateEndHospitalization` DATE NULL DEFAULT NULL ,
  `dateFirstVisitGMC` DATE NULL DEFAULT NULL ,
  `dateFirstVisitTB` DATE NULL DEFAULT NULL ,
  `dateHospitalization` DATE NULL DEFAULT NULL ,
  `diagnosisSource` INT(11) NULL DEFAULT NULL ,
  `employerName` VARCHAR(100) NULL DEFAULT NULL ,
  `extraOutcomeInfo` INT(11) NULL DEFAULT NULL ,
  `healthWorker` BIT(1) NOT NULL ,
  `healthWorkerGMC` BIT(1) NOT NULL ,
  `healthWorkerTB` BIT(1) NOT NULL ,
  `homeless` BIT(1) NOT NULL ,
  `hospitalizationDate` DATE NULL DEFAULT NULL ,
  `injectableDrugUse` BIT(1) NOT NULL ,
  `migrant` BIT(1) NOT NULL ,
  `otherFeature` VARCHAR(255) NULL DEFAULT NULL ,
  `prisioner` BIT(1) NOT NULL ,
  `pulmonaryDestruction` INT(11) NULL DEFAULT NULL ,
  `pulmonaryMBT` INT(11) NULL DEFAULT NULL ,
  `refugee` BIT(1) NOT NULL ,
  `tbContact` BIT(1) NOT NULL ,
  `unemployed` BIT(1) NOT NULL ,
  `DETECTION_ID` INT(11) NULL DEFAULT NULL ,
  `DIAGNOSIS_ID` INT(11) NULL DEFAULT NULL ,
  `POSITION_ID` INT(11) NULL DEFAULT NULL ,
  `detectionOther` VARCHAR(100) NULL DEFAULT NULL ,
  `diagnosisOther` VARCHAR(100) NULL DEFAULT NULL ,
  `positionOther` VARCHAR(100) NULL DEFAULT NULL ,
  `dateFirstSymptoms` DATE NULL DEFAULT NULL ,
  `transferOutDescription` VARCHAR(100) NULL DEFAULT NULL ,
  `hiv` BIT(1) NULL DEFAULT NULL ,
  `mbtResult` INT(11) NULL DEFAULT NULL ,
  `startedVCTdate` DATE NULL DEFAULT NULL ,
  `regcategory_other` VARCHAR(255) NULL DEFAULT NULL ,
  `REGISTRATION_CATEGORY` INT(11) NULL DEFAULT NULL ,
  `dischargeDate` DATE NULL DEFAULT NULL ,
  `kotrymoksTreatDate` DATE NULL DEFAULT NULL ,
  `testHIVdate` DATE NULL DEFAULT NULL ,
  `treatARTdate` DATE NULL DEFAULT NULL ,
  `hospitalizationDate2` DATE NULL DEFAULT NULL ,
  `hospitalizationDate3` DATE NULL DEFAULT NULL ,
  `hospitalizationDate4` DATE NULL DEFAULT NULL ,
  `hospitalizationDate5` DATE NULL DEFAULT NULL ,
  `causeChangeTreat_id` INT(11) NULL DEFAULT NULL ,
  `refuse2line` BIT(1) NOT NULL ,
  `dischargeDate2` DATE NULL DEFAULT NULL ,
  `dischargeDate3` DATE NULL DEFAULT NULL ,
  `dischargeDate4` DATE NULL DEFAULT NULL ,
  `dischargeDate5` DATE NULL DEFAULT NULL ,
  `riskClass` INT(11) NULL DEFAULT NULL ,
  `dateRegTo4Cat` DATE NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK1FB9D726E2D44132` (`DIAGNOSIS_ID` ASC) ,
  INDEX `FK1FB9D726334507FA` (`POSITION_ID` ASC) ,
  INDEX `FK1FB9D726940091E` (`DETECTION_ID` ASC) ,
  INDEX `FK1FB9D7266F51F1CD` (`REGISTRATION_CATEGORY` ASC) ,
  INDEX `FKDF8312E6FFF48273` (`causeChangeTreat_id` ASC) ,
  CONSTRAINT `FKDF8312E610492EAB`
    FOREIGN KEY (`POSITION_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL,
  CONSTRAINT `FKDF8312E64C56187E`
    FOREIGN KEY (`REGISTRATION_CATEGORY` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL,
  CONSTRAINT `FKDF8312E6BFD867E3`
    FOREIGN KEY (`DIAGNOSIS_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL,
  CONSTRAINT `FKDF8312E6E6442FCF`
    FOREIGN KEY (`DETECTION_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL,
  CONSTRAINT `FKDF8312E6FFF48273`
    FOREIGN KEY (`causeChangeTreat_id` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FK_CASEDATAUA_TBCASE`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tbcaseke`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tbcaseke` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tbcaseke` (
  `id` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKA7807882DD737C` (`id` ASC) ,
  CONSTRAINT `FKA7807882DD737C`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`casedispensing_ke`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`casedispensing_ke` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`casedispensing_ke` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `DISP_MONTH` INT(11) NULL DEFAULT NULL ,
  `totalDays` INT(11) NOT NULL ,
  `DISP_YEAR` INT(11) NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKF2ECAF8F8B327BA` (`CASE_ID` ASC) ,
  INDEX `FKD88943AF879D4645` (`CASE_ID` ASC) ,
  CONSTRAINT `FKD88943AF879D4645`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcaseke` (`id` )
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tbcaseng`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tbcaseng` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tbcaseng` (
  `TB_DIAGNOSIS_DATE` DATE NULL DEFAULT NULL ,
  `tbRegistrationNumber` VARCHAR(11) NULL DEFAULT NULL ,
  `id` INT(11) NOT NULL ,
  `emailAddress` VARCHAR(100) NULL DEFAULT NULL ,
  `suspectType` INT(11) NULL DEFAULT NULL ,
  `SUSPECT_TYPE` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKA780D782DD737C` (`id` ASC) ,
  INDEX `FKA780D770FEBE3C` (`SUSPECT_TYPE` ASC) ,
  CONSTRAINT `FKA780D770FEBE3C`
    FOREIGN KEY (`SUSPECT_TYPE` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKA780D782DD737C`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`casedispensing_ng`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`casedispensing_ng` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`casedispensing_ng` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `DISP_MONTH` INT(11) NULL DEFAULT NULL ,
  `totalDays` INT(11) NOT NULL ,
  `DISP_YEAR` INT(11) NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKD889440ED9D78D43` (`CASE_ID` ASC) ,
  CONSTRAINT `FKD889440ED9D78D43`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcaseng` (`id` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`casedispensingdays_ke`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`casedispensingdays_ke` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`casedispensingdays_ke` (
  `id` INT(11) NOT NULL ,
  `day1` INT(11) NULL DEFAULT NULL ,
  `day10` INT(11) NULL DEFAULT NULL ,
  `day11` INT(11) NULL DEFAULT NULL ,
  `day12` INT(11) NULL DEFAULT NULL ,
  `day13` INT(11) NULL DEFAULT NULL ,
  `day14` INT(11) NULL DEFAULT NULL ,
  `day15` INT(11) NULL DEFAULT NULL ,
  `day16` INT(11) NULL DEFAULT NULL ,
  `day17` INT(11) NULL DEFAULT NULL ,
  `day18` INT(11) NULL DEFAULT NULL ,
  `day19` INT(11) NULL DEFAULT NULL ,
  `day2` INT(11) NULL DEFAULT NULL ,
  `day20` INT(11) NULL DEFAULT NULL ,
  `day21` INT(11) NULL DEFAULT NULL ,
  `day22` INT(11) NULL DEFAULT NULL ,
  `day23` INT(11) NULL DEFAULT NULL ,
  `day24` INT(11) NULL DEFAULT NULL ,
  `day25` INT(11) NULL DEFAULT NULL ,
  `day26` INT(11) NULL DEFAULT NULL ,
  `day27` INT(11) NULL DEFAULT NULL ,
  `day28` INT(11) NULL DEFAULT NULL ,
  `day29` INT(11) NULL DEFAULT NULL ,
  `day3` INT(11) NULL DEFAULT NULL ,
  `day30` INT(11) NULL DEFAULT NULL ,
  `day31` INT(11) NULL DEFAULT NULL ,
  `day4` INT(11) NULL DEFAULT NULL ,
  `day5` INT(11) NULL DEFAULT NULL ,
  `day6` INT(11) NULL DEFAULT NULL ,
  `day7` INT(11) NULL DEFAULT NULL ,
  `day8` INT(11) NULL DEFAULT NULL ,
  `day9` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`casedispensingdays_ng`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`casedispensingdays_ng` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`casedispensingdays_ng` (
  `id` INT(11) NOT NULL ,
  `day1` INT(11) NULL DEFAULT NULL ,
  `day10` INT(11) NULL DEFAULT NULL ,
  `day11` INT(11) NULL DEFAULT NULL ,
  `day12` INT(11) NULL DEFAULT NULL ,
  `day13` INT(11) NULL DEFAULT NULL ,
  `day14` INT(11) NULL DEFAULT NULL ,
  `day15` INT(11) NULL DEFAULT NULL ,
  `day16` INT(11) NULL DEFAULT NULL ,
  `day17` INT(11) NULL DEFAULT NULL ,
  `day18` INT(11) NULL DEFAULT NULL ,
  `day19` INT(11) NULL DEFAULT NULL ,
  `day2` INT(11) NULL DEFAULT NULL ,
  `day20` INT(11) NULL DEFAULT NULL ,
  `day21` INT(11) NULL DEFAULT NULL ,
  `day22` INT(11) NULL DEFAULT NULL ,
  `day23` INT(11) NULL DEFAULT NULL ,
  `day24` INT(11) NULL DEFAULT NULL ,
  `day25` INT(11) NULL DEFAULT NULL ,
  `day26` INT(11) NULL DEFAULT NULL ,
  `day27` INT(11) NULL DEFAULT NULL ,
  `day28` INT(11) NULL DEFAULT NULL ,
  `day29` INT(11) NULL DEFAULT NULL ,
  `day3` INT(11) NULL DEFAULT NULL ,
  `day30` INT(11) NULL DEFAULT NULL ,
  `day31` INT(11) NULL DEFAULT NULL ,
  `day4` INT(11) NULL DEFAULT NULL ,
  `day5` INT(11) NULL DEFAULT NULL ,
  `day6` INT(11) NULL DEFAULT NULL ,
  `day7` INT(11) NULL DEFAULT NULL ,
  `day8` INT(11) NULL DEFAULT NULL ,
  `day9` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`casedispensingdaysna`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`casedispensingdaysna` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`casedispensingdaysna` (
  `id` INT(11) NOT NULL ,
  `day1` INT(11) NULL DEFAULT NULL ,
  `day10` INT(11) NULL DEFAULT NULL ,
  `day11` INT(11) NULL DEFAULT NULL ,
  `day12` INT(11) NULL DEFAULT NULL ,
  `day13` INT(11) NULL DEFAULT NULL ,
  `day14` INT(11) NULL DEFAULT NULL ,
  `day15` INT(11) NULL DEFAULT NULL ,
  `day16` INT(11) NULL DEFAULT NULL ,
  `day17` INT(11) NULL DEFAULT NULL ,
  `day18` INT(11) NULL DEFAULT NULL ,
  `day19` INT(11) NULL DEFAULT NULL ,
  `day2` INT(11) NULL DEFAULT NULL ,
  `day20` INT(11) NULL DEFAULT NULL ,
  `day21` INT(11) NULL DEFAULT NULL ,
  `day22` INT(11) NULL DEFAULT NULL ,
  `day23` INT(11) NULL DEFAULT NULL ,
  `day24` INT(11) NULL DEFAULT NULL ,
  `day25` INT(11) NULL DEFAULT NULL ,
  `day26` INT(11) NULL DEFAULT NULL ,
  `day27` INT(11) NULL DEFAULT NULL ,
  `day28` INT(11) NULL DEFAULT NULL ,
  `day29` INT(11) NULL DEFAULT NULL ,
  `day3` INT(11) NULL DEFAULT NULL ,
  `day30` INT(11) NULL DEFAULT NULL ,
  `day31` INT(11) NULL DEFAULT NULL ,
  `day4` INT(11) NULL DEFAULT NULL ,
  `day5` INT(11) NULL DEFAULT NULL ,
  `day6` INT(11) NULL DEFAULT NULL ,
  `day7` INT(11) NULL DEFAULT NULL ,
  `day8` INT(11) NULL DEFAULT NULL ,
  `day9` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tbcasena`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tbcasena` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tbcasena` (
  `comments` LONGTEXT NULL DEFAULT NULL ,
  `EVENT_DATE` DATE NULL DEFAULT NULL ,
  `id` INT(11) NOT NULL ,
  `unitRegCode` VARCHAR(50) NULL DEFAULT NULL ,
  `dischargeDt` DATE NULL DEFAULT NULL ,
  `hospitalized` BIT(1) NULL DEFAULT NULL ,
  `hospitalizedDt` DATE NULL DEFAULT NULL ,
  `socialDisabilityAwarded` TINYINT(1) NULL DEFAULT NULL ,
  `startDateSocialAward` DATE NULL DEFAULT NULL ,
  `commentSocialAward` LONGTEXT NULL DEFAULT NULL ,
  `foodPackageAwarded` TINYINT(1) NULL DEFAULT NULL ,
  `startDateFoodPackageAward` DATE NULL DEFAULT NULL ,
  `commentFoodPackageAward` LONGTEXT NULL DEFAULT NULL ,
  `transportAssistProvided` TINYINT(1) NULL DEFAULT NULL ,
  `startDateTransportAssist` DATE NULL DEFAULT NULL ,
  `commentTransportAssist` LONGTEXT NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKA780D182DD737C` (`id` ASC) ,
  CONSTRAINT `FKA780D182DD737C`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`casedispensingna`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`casedispensingna` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`casedispensingna` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `DISP_MONTH` INT(11) NULL DEFAULT NULL ,
  `totalDaysDot` INT(11) NULL DEFAULT NULL ,
  `DISP_YEAR` INT(11) NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  `totalDaysSelfAdmin` INT(11) NULL DEFAULT NULL ,
  `totalDaysNotTaken` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKDE8BC17D8B327BA` (`CASE_ID` ASC) ,
  INDEX `FKDE8BC17DA3F04EEB` (`CASE_ID` ASC) ,
  INDEX `FK6FC2D3DA3F04EEB` (`CASE_ID` ASC) ,
  INDEX `FK6FC2D3D7D942B7` (`CASE_ID` ASC) ,
  CONSTRAINT `FK6FC2D3D7D942B7`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcasena` (`id` )
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`resistancepattern`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`resistancepattern` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`resistancepattern` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `PATTERN_NAME` VARCHAR(100) NULL DEFAULT NULL ,
  `WORKSPACE_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `criteria` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK606E68F7B9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `FK12719137E0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FK12719137798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK12719137798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK12719137B3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK12719137E0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`caseresistancepattern`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`caseresistancepattern` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`caseresistancepattern` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `case_id` INT(11) NOT NULL ,
  `resistpattern_id` INT(11) NOT NULL ,
  `diagnosis` TINYINT(1) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `case_caserespat_fk` (`case_id` ASC) ,
  INDEX `respatt_caserespat_fk` (`resistpattern_id` ASC) ,
  CONSTRAINT `case_caserespat_fk`
    FOREIGN KEY (`case_id` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `respatt_caserespat_fk`
    FOREIGN KEY (`resistpattern_id` )
    REFERENCES `etbmanagerd`.`resistancepattern` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'Store resistance patterns by case';


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tbcaseaz`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tbcaseaz` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tbcaseaz` (
  `caseFindingStrategy` INT(11) NULL DEFAULT NULL ,
  `EDUCATIONALDEGREE_Complement` VARCHAR(255) NULL DEFAULT NULL ,
  `inprisionEndDate` DATETIME NULL DEFAULT NULL ,
  `inprisonIniDate` DATETIME NULL DEFAULT NULL ,
  `MARITALSTATUS_Complement` VARCHAR(255) NULL DEFAULT NULL ,
  `numberOfImprisonments` INT(11) NULL DEFAULT NULL ,
  `id` INT(11) NOT NULL ,
  `EDUCATIONALDEGREE_ID` INT(11) NULL DEFAULT NULL ,
  `MARITALSTATUS_ID` INT(11) NULL DEFAULT NULL ,
  `inprisonEndDate` DATETIME NULL DEFAULT NULL ,
  `EIDSSID` VARCHAR(45) NULL DEFAULT NULL ,
  `EIDSSComment` VARCHAR(255) NULL DEFAULT NULL ,
  `doctor` VARCHAR(45) NULL DEFAULT NULL ,
  `referToTBUnit_id` INT(11) NULL DEFAULT NULL ,
  `referToOtherTBUnit` BIT(1) NOT NULL DEFAULT b'0' ,
  `toThirdCategory` BIT(1) NOT NULL DEFAULT b'0' ,
  `dateEndThirdCat` DATE NULL DEFAULT NULL ,
  `dateIniThirdCat` DATE NULL DEFAULT NULL ,
  `unicalID` VARCHAR(15) NULL DEFAULT NULL ,
  `systemDate` DATE NULL DEFAULT NULL ,
  `inEIDSSDate` DATE NULL DEFAULT NULL ,
  `colPrevTreatUnknown` BIT(1) NULL DEFAULT b'0' ,
  `editingDate` DATE NULL DEFAULT NULL ,
  `createUser_id` INT(11) NULL DEFAULT NULL ,
  `editingUser_id` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `EIDSSID_UNIQUE` (`EIDSSID` ASC) ,
  INDEX `FKCDE6CB9782DD737C` (`id` ASC) ,
  INDEX `FKCDE6CB9712F828CE` (`MARITALSTATUS_ID` ASC) ,
  INDEX `FKCDE6CB97D82B85B5` (`EDUCATIONALDEGREE_ID` ASC) ,
  INDEX `FKA77F5734401382` (`referToTBUnit_id` ASC) ,
  INDEX `FKA77F57DD0C41C1` (`createUser_id` ASC) ,
  INDEX `FKA77F5754AAC5A5` (`editingUser_id` ASC) ,
  CONSTRAINT `FKA77F5712F828CE`
    FOREIGN KEY (`MARITALSTATUS_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKA77F5734401382`
    FOREIGN KEY (`referToTBUnit_id` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
  CONSTRAINT `FKA77F5754AAC5A5`
    FOREIGN KEY (`editingUser_id` )
    REFERENCES `etbmanagerd`.`sys_user` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKA77F5782DD737C`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKA77F57D82B85B5`
    FOREIGN KEY (`EDUCATIONALDEGREE_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKA77F57DD0C41C1`
    FOREIGN KEY (`createUser_id` )
    REFERENCES `etbmanagerd`.`sys_user` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`caseseveritymark`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`caseseveritymark` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`caseseveritymark` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comments` VARCHAR(100) NULL DEFAULT NULL ,
  `SYMPTOM_ID` INT(11) NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKBCC492DA15ABF9AB` (`SYMPTOM_ID` ASC) ,
  INDEX `FKBCC492DA5A44EA43` (`CASE_ID` ASC) ,
  CONSTRAINT `FKE543867A15ABF9AB`
    FOREIGN KEY (`SYMPTOM_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKE543867A5A44EA43`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcaseaz` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`substance`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`substance` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`substance` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `ABBREV_NAME1` VARCHAR(10) NULL DEFAULT NULL ,
  `ABBREV_NAME2` VARCHAR(10) NULL DEFAULT NULL ,
  `dstResultForm` BIT(1) NOT NULL ,
  `line` INT(11) NULL DEFAULT NULL ,
  `name1` VARCHAR(100) NULL DEFAULT NULL ,
  `name2` VARCHAR(100) NULL DEFAULT NULL ,
  `prevTreatmentForm` BIT(1) NOT NULL ,
  `prevTreatmentOrder` INT(11) NULL DEFAULT NULL ,
  `WORKSPACE_ID` INT(11) NOT NULL ,
  `legacyId` VARCHAR(50) NULL DEFAULT NULL ,
  `lasttransaction_id` INT(11) NULL DEFAULT NULL ,
  `createTransaction_id` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK9709E550B9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `fk_substance_transactionlog1` (`lasttransaction_id` ASC) ,
  INDEX `fk_substance_transactionlog2` (`createTransaction_id` ASC) ,
  INDEX `FK1F97C570E0BC8241` (`createTransaction_id` ASC) ,
  INDEX `FK1F97C570798D9E5B` (`lasttransaction_id` ASC) ,
  CONSTRAINT `FK1F97C570798D9E5B`
    FOREIGN KEY (`lasttransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK1F97C570B3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK1F97C570E0BC8241`
    FOREIGN KEY (`createTransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_substance_transactionlog1`
    FOREIGN KEY (`lasttransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_substance_transactionlog2`
    FOREIGN KEY (`createTransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 941006
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`casesideeffect`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`casesideeffect` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`casesideeffect` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `medicines` VARCHAR(255) NULL DEFAULT NULL ,
  `SE_MONTH` INT(11) NULL DEFAULT NULL ,
  `resolved` INT(11) NULL DEFAULT NULL ,
  `SIDEEFFECT_ID` INT(11) NOT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  `SUBSTANCE_ID` INT(11) NULL DEFAULT NULL ,
  `SUBSTANCE2_ID` INT(11) NULL DEFAULT NULL ,
  `DTYPE` VARCHAR(31) NULL DEFAULT NULL ,
  `actionTaken` INT(11) NULL DEFAULT NULL ,
  `grade` INT(11) NULL DEFAULT NULL ,
  `outcome` INT(11) NULL DEFAULT NULL ,
  `seriousness` INT(11) NULL DEFAULT NULL ,
  `DISCRIMINATOR` VARCHAR(31) NULL DEFAULT NULL ,
  `effectEnd` DATE NULL DEFAULT NULL ,
  `effectSt` DATE NULL DEFAULT NULL ,
  `otherAdverseEffect` VARCHAR(100) NULL DEFAULT NULL ,
  `comment` LONGTEXT NULL DEFAULT NULL ,
  `dateChangeReg` DATE NULL DEFAULT NULL ,
  `symptomTherapy` VARCHAR(255) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKA1F0CD38BAF4631B` (`SIDEEFFECT_ID` ASC) ,
  INDEX `FKA1F0CD388B327BA` (`CASE_ID` ASC) ,
  INDEX `FKA1F0CD3893A12976` (`SUBSTANCE2_ID` ASC) ,
  INDEX `FKA1F0CD38B7FBE608` (`SUBSTANCE_ID` ASC) ,
  INDEX `fkcasese_lasttx` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK9D7338D896FA537`
    FOREIGN KEY (`SUBSTANCE_ID` )
    REFERENCES `etbmanagerd`.`substance` (`id` )
    ON DELETE SET NULL,
  CONSTRAINT `FK9D7338D897F889CC`
    FOREIGN KEY (`SIDEEFFECT_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE CASCADE,
  CONSTRAINT `FK9D7338D8A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE,
  CONSTRAINT `FK9D7338D8E514E8A5`
    FOREIGN KEY (`SUBSTANCE2_ID` )
    REFERENCES `etbmanagerd`.`substance` (`id` )
    ON DELETE SET NULL,
  CONSTRAINT `fkcasese_lasttx`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`casesymptom`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`casesymptom` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`casesymptom` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comments` VARCHAR(100) NULL DEFAULT NULL ,
  `SYMPTOM_ID` INT(11) NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK19977CB938A7D2FA` (`SYMPTOM_ID` ASC) ,
  INDEX `FK19977CB98B327BA` (`CASE_ID` ASC) ,
  CONSTRAINT `FK52F53CF915ABF9AB`
    FOREIGN KEY (`SYMPTOM_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE CASCADE,
  CONSTRAINT `FK52F53CF9A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`clientsyncresult`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`clientsyncresult` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`clientsyncresult` (
  `id` VARCHAR(32) NOT NULL ,
  `syncStart` DATETIME NOT NULL ,
  `syncEnd` DATETIME NOT NULL ,
  `errorMessage` VARCHAR(250) NULL DEFAULT NULL ,
  `answerFileName` VARCHAR(100) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`databasechangelog`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`databasechangelog` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`databasechangelog` (
  `ID` VARCHAR(63) NOT NULL ,
  `AUTHOR` VARCHAR(63) NOT NULL ,
  `FILENAME` VARCHAR(200) NOT NULL ,
  `DATEEXECUTED` DATETIME NOT NULL ,
  `ORDEREXECUTED` INT(11) NOT NULL ,
  `EXECTYPE` VARCHAR(10) NOT NULL ,
  `MD5SUM` VARCHAR(35) NULL DEFAULT NULL ,
  `DESCRIPTION` VARCHAR(255) NULL DEFAULT NULL ,
  `COMMENTS` VARCHAR(255) NULL DEFAULT NULL ,
  `TAG` VARCHAR(255) NULL DEFAULT NULL ,
  `LIQUIBASE` VARCHAR(20) NULL DEFAULT NULL ,
  PRIMARY KEY (`ID`, `AUTHOR`, `FILENAME`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`databasechangeloglock`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`databasechangeloglock` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`databasechangeloglock` (
  `ID` INT(11) NOT NULL ,
  `LOCKED` TINYINT(1) NOT NULL ,
  `LOCKGRANTED` DATETIME NULL DEFAULT NULL ,
  `LOCKEDBY` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`ID`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`databasecolumn`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`databasecolumn` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`databasecolumn` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `table_name` VARCHAR(100) NOT NULL ,
  `column_name` VARCHAR(100) NOT NULL ,
  `description` LONGTEXT NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`databasetable`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`databasetable` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`databasetable` (
  `table_name` VARCHAR(100) NOT NULL ,
  `description` LONGTEXT NULL DEFAULT NULL ,
  PRIMARY KEY (`table_name`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`errorlog`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`errorlog` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`errorlog` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `exceptionClass` VARCHAR(100) NULL DEFAULT NULL ,
  `exceptionMessage` VARCHAR(200) NULL DEFAULT NULL ,
  `stackTrace` LONGTEXT NULL DEFAULT NULL ,
  `url` VARCHAR(150) NULL DEFAULT NULL ,
  `user` VARCHAR(100) NULL DEFAULT NULL ,
  `userId` INT(11) NULL DEFAULT NULL ,
  `errorDate` DATETIME NULL DEFAULT NULL ,
  `workspace` VARCHAR(100) NULL DEFAULT NULL ,
  `request` LONGTEXT NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`laboratory`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`laboratory` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`laboratory` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `abbrevName` VARCHAR(20) NULL DEFAULT NULL ,
  `name` VARCHAR(100) NOT NULL ,
  `WORKSPACE_ID` INT(11) NOT NULL ,
  `ADMINUNIT_ID` INT(11) NOT NULL ,
  `legacyId` VARCHAR(50) NULL DEFAULT NULL ,
  `HEALTHSYSTEM_ID` INT(11) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK2FD84BD38A1004CB` (`ADMINUNIT_ID` ASC) ,
  INDEX `FK2FD84BD3B9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `FK2FD84BD3755AB8EC` (`HEALTHSYSTEM_ID` ASC) ,
  INDEX `fk_laboratory_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_laboratory_transactionlog2` (`createTransaction_ID` ASC) ,
  INDEX `FKB9066FB3E0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FKB9066FB3798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FKB9066FB321EFF75D`
    FOREIGN KEY (`HEALTHSYSTEM_ID` )
    REFERENCES `etbmanagerd`.`healthsystem` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKB9066FB3798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FKB9066FB3B3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKB9066FB3CEDEEA7C`
    FOREIGN KEY (`ADMINUNIT_ID` )
    REFERENCES `etbmanagerd`.`administrativeunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKB9066FB3E0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_laboratory_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_laboratory_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`exambiopsy`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`exambiopsy` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`exambiopsy` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comments` VARCHAR(250) NULL DEFAULT NULL ,
  `dateCollected` DATE NOT NULL ,
  `dateRelease` DATE NULL DEFAULT NULL ,
  `sampleNumber` VARCHAR(50) NULL DEFAULT NULL ,
  `result` INT(11) NULL DEFAULT NULL ,
  `LABORATORY_ID` INT(11) NULL DEFAULT NULL ,
  `METHOD_ID` INT(11) NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `sampleType` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKB0D465ED66A4104C` (`LABORATORY_ID` ASC) ,
  INDEX `FKB0D465ED8861542` (`METHOD_ID` ASC) ,
  INDEX `FKB0D465ED8B327BA` (`CASE_ID` ASC) ,
  INDEX `FKB0D465ED43A836FD` (`LABORATORY_ID` ASC) ,
  INDEX `FKB0D465EDE58A3BF3` (`METHOD_ID` ASC) ,
  INDEX `FKB0D465EDA3F04EEB` (`CASE_ID` ASC) ,
  INDEX `FK709D9DADE0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FK709D9DAD798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK709D9DAD43A836FD`
    FOREIGN KEY (`LABORATORY_ID` )
    REFERENCES `etbmanagerd`.`laboratory` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FK709D9DAD798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK709D9DADA3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK709D9DADE0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK709D9DADE58A3BF3`
    FOREIGN KEY (`METHOD_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`examculture`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`examculture` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`examculture` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comments` TEXT NULL DEFAULT NULL ,
  `dateRelease` DATE NULL DEFAULT NULL ,
  `result` INT(11) NULL DEFAULT NULL ,
  `LABORATORY_ID` INT(11) NULL DEFAULT NULL ,
  `METHOD_ID` INT(11) NULL DEFAULT NULL ,
  `numberOfColonies` INT(11) NULL DEFAULT NULL ,
  `sampleType` INT(11) NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  `sampleNumber` VARCHAR(50) NULL DEFAULT NULL ,
  `dateCollected` DATE NULL DEFAULT NULL ,
  `DTYPE` VARCHAR(31) NULL DEFAULT NULL ,
  `IDENTIFICATION_ID` INT(11) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKB2F04C6F66A4104C` (`LABORATORY_ID` ASC) ,
  INDEX `FKB2F04C6F8861542` (`METHOD_ID` ASC) ,
  INDEX `FKB2F04C6F8B327BA` (`CASE_ID` ASC) ,
  INDEX `FKEC4E0CAFF1114346` (`IDENTIFICATION_ID` ASC) ,
  INDEX `fk_examculture_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_examculture_transactionlog2` (`createTransaction_ID` ASC) ,
  INDEX `FKEC4E0CAFE0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FKEC4E0CAF798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FKEC4E0CAF43A836FD`
    FOREIGN KEY (`LABORATORY_ID` )
    REFERENCES `etbmanagerd`.`laboratory` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKEC4E0CAF798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FKEC4E0CAFA3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKEC4E0CAFE0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FKEC4E0CAFE58A3BF3`
    FOREIGN KEY (`METHOD_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKEC4E0CAFF1114346`
    FOREIGN KEY (`IDENTIFICATION_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` ),
  CONSTRAINT `fk_examculture_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_examculture_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`examculture_az`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`examculture_az` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`examculture_az` (
  `id` INT(11) NOT NULL ,
  `datePlating` DATE NULL DEFAULT NULL ,
  `dateTestBegin` DATE NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `index2` (`id` ASC) ,
  INDEX `FKEB771C92CDFAFE7` (`id` ASC) ,
  CONSTRAINT `FKEB771C92CDFAFE7`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`examculture` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `index2`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`examculture` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`examculture_kh`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`examculture_kh` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`examculture_kh` (
  `id` INT(11) NOT NULL ,
  `IDENTIFICATION_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKEB772ED2CDFAFE7` (`id` ASC) ,
  INDEX `FKEB772EDF1114346` (`IDENTIFICATION_ID` ASC) ,
  CONSTRAINT `FKEB772ED2CDFAFE7`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`examculture` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKEB772EDF1114346`
    FOREIGN KEY (`IDENTIFICATION_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`examdst`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`examdst` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`examdst` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comments` TEXT NULL DEFAULT NULL ,
  `dateRelease` DATE NULL DEFAULT NULL ,
  `numContaminated` INT(11) NOT NULL ,
  `numResistant` INT(11) NOT NULL ,
  `numSusceptible` INT(11) NOT NULL ,
  `LABORATORY_ID` INT(11) NULL DEFAULT NULL ,
  `METHOD_ID` INT(11) NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  `sampleNumber` VARCHAR(50) NULL DEFAULT NULL ,
  `dateCollected` DATE NULL DEFAULT NULL ,
  `ongoing` TINYINT(1) NULL DEFAULT NULL ,
  `DTYPE` VARCHAR(31) NULL DEFAULT NULL ,
  `sampleType` INT(11) NULL DEFAULT NULL ,
  `DISCRIMINATOR` VARCHAR(31) NULL DEFAULT NULL ,
  `mtbDetected` INT(11) NULL DEFAULT NULL ,
  `datePlating` DATE NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `dateTestBegin` DATE NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKD76A756C66A4104C` (`LABORATORY_ID` ASC) ,
  INDEX `FKD76A756CA8FEE28C` (`METHOD_ID` ASC) ,
  INDEX `FKD76A756C8861542` (`METHOD_ID` ASC) ,
  INDEX `FK145D01068B327BA` (`CASE_ID` ASC) ,
  INDEX `FK145D010666A4104C` (`LABORATORY_ID` ASC) ,
  INDEX `FK145D01068861542` (`METHOD_ID` ASC) ,
  INDEX `fk_examdst_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_examdst_transactionlog2` (`createTransaction_ID` ASC) ,
  INDEX `FKB124E546E0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FKB124E546798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK145D010666A4104C`
    FOREIGN KEY (`LABORATORY_ID` )
    REFERENCES `etbmanagerd`.`laboratory` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK145D01068861542`
    FOREIGN KEY (`METHOD_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKB124E54643A836FD`
    FOREIGN KEY (`LABORATORY_ID` )
    REFERENCES `etbmanagerd`.`laboratory` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKB124E546798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FKB124E546A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKB124E546E0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FKB124E546E58A3BF3`
    FOREIGN KEY (`METHOD_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_examdst_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_examdst_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`examdstresult`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`examdstresult` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`examdstresult` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `result` INT(11) NOT NULL ,
  `EXAM_ID` INT(11) NOT NULL ,
  `SUBSTANCE_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKA3392A7779C49EF9` (`EXAM_ID` ASC) ,
  INDEX `FKA3392A77B7FBE608` (`SUBSTANCE_ID` ASC) ,
  INDEX `FK4FF058C3CB45202F` (`EXAM_ID` ASC) ,
  INDEX `FK4FF058C3B7FBE608` (`SUBSTANCE_ID` ASC) ,
  CONSTRAINT `FK786720E396FA537`
    FOREIGN KEY (`SUBSTANCE_ID` )
    REFERENCES `etbmanagerd`.`substance` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK786720E397ACDF1E`
    FOREIGN KEY (`EXAM_ID` )
    REFERENCES `etbmanagerd`.`examdst` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`examhiv`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`examhiv` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`examhiv` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comments` VARCHAR(250) NULL DEFAULT NULL ,
  `EVENT_DATE` DATE NULL DEFAULT NULL ,
  `laboratory` VARCHAR(100) NULL DEFAULT NULL ,
  `result` INT(11) NULL DEFAULT NULL ,
  `startedARVdate` DATE NULL DEFAULT NULL ,
  `startedCPTdate` DATE NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  `startedARTdate` DATE NULL DEFAULT NULL ,
  `startedVCTdate` DATE NULL DEFAULT NULL ,
  `cd4Count` INT(11) NULL DEFAULT NULL ,
  `cd4StDate` DATE NULL DEFAULT NULL ,
  `partnerResult` INT(11) NULL DEFAULT NULL ,
  `partnerResultDate` DATE NULL DEFAULT NULL ,
  `DISCRIMINATOR` VARCHAR(31) NULL DEFAULT NULL ,
  `artNumber` INT(11) NULL DEFAULT NULL ,
  `resultDate` DATE NULL DEFAULT NULL ,
  `ART_REGIMEN_ID` INT(11) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `viralLoad` INT(11) NULL DEFAULT NULL ,
  `viralLoadDateRelease` DATETIME NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK145D0ED68B327BA` (`CASE_ID` ASC) ,
  INDEX `FKB124F31626B85CCF` (`ART_REGIMEN_ID` ASC) ,
  INDEX `fk_examhiv_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_examhiv_transactionlog2` (`createTransaction_ID` ASC) ,
  CONSTRAINT `FKB124F31626B85CCF`
    FOREIGN KEY (`ART_REGIMEN_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKB124F316A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_examhiv_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_examhiv_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`examhiv_ke`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`examhiv_ke` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`examhiv_ke` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comments` LONGTEXT NULL DEFAULT NULL ,
  `EVENT_DATE` DATE NOT NULL ,
  `cd4Count` INT(11) NULL DEFAULT NULL ,
  `cd4StDate` DATE NULL DEFAULT NULL ,
  `laboratory` VARCHAR(100) NULL DEFAULT NULL ,
  `partnerResult` INT(11) NULL DEFAULT NULL ,
  `partnerResultDate` DATE NULL DEFAULT NULL ,
  `result` INT(11) NULL DEFAULT NULL ,
  `resultDate` DATE NULL DEFAULT NULL ,
  `startedARTdate` DATE NULL DEFAULT NULL ,
  `startedCPTdate` DATE NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKB942E7838B327BA` (`CASE_ID` ASC) ,
  INDEX `FKB942E783A3F04EEB` (`CASE_ID` ASC) ,
  CONSTRAINT `FKB942E783A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`examhiv_na`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`examhiv_na` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`examhiv_na` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comments` LONGTEXT NULL DEFAULT NULL ,
  `EVENT_DATE` DATE NULL DEFAULT NULL ,
  `artNumber` INT(11) NULL DEFAULT NULL ,
  `cd4Count` INT(11) NULL DEFAULT NULL ,
  `cd4StDate` DATE NULL DEFAULT NULL ,
  `laboratory` VARCHAR(100) NULL DEFAULT NULL ,
  `result` INT(11) NULL DEFAULT NULL ,
  `resultDate` DATE NULL DEFAULT NULL ,
  `startedARTdate` DATE NULL DEFAULT NULL ,
  `startedCPTdate` DATE NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  `ART_REGIMEN_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKB942E7BC49B4361E` (`ART_REGIMEN_ID` ASC) ,
  INDEX `FKB942E7BC8B327BA` (`CASE_ID` ASC) ,
  INDEX `FKB942E7BC26B85CCF` (`ART_REGIMEN_ID` ASC) ,
  INDEX `FKB942E7BCA3F04EEB` (`CASE_ID` ASC) ,
  INDEX `FK7ADD9F7C26B85CCF` (`ART_REGIMEN_ID` ASC) ,
  INDEX `FK7ADD9F7CA3F04EEB` (`CASE_ID` ASC) ,
  CONSTRAINT `FK7ADD9F7C26B85CCF`
    FOREIGN KEY (`ART_REGIMEN_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE CASCADE,
  CONSTRAINT `FK7ADD9F7CA3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`exammicroscopy`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`exammicroscopy` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`exammicroscopy` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comments` TEXT NULL DEFAULT NULL ,
  `dateRelease` DATE NULL DEFAULT NULL ,
  `result` INT(11) NULL DEFAULT NULL ,
  `LABORATORY_ID` INT(11) NULL DEFAULT NULL ,
  `numberOfAFB` INT(11) NULL DEFAULT NULL ,
  `METHOD_ID` INT(11) NULL DEFAULT NULL ,
  `sampleType` INT(11) NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NULL DEFAULT NULL ,
  `sampleNumber` VARCHAR(50) NULL DEFAULT NULL ,
  `dateCollected` DATE NULL DEFAULT NULL ,
  `DISCRIMINATOR` VARCHAR(31) NULL DEFAULT NULL ,
  `laboratoryName` VARCHAR(200) NULL DEFAULT NULL ,
  `LAB_ADMINUNIT_ID` INT(11) NULL DEFAULT NULL ,
  `datePlating` DATE NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKA5F78C0966A4104C` (`LABORATORY_ID` ASC) ,
  INDEX `FKA5F78C098861542` (`METHOD_ID` ASC) ,
  INDEX `FKC6929FC38B327BA` (`CASE_ID` ASC) ,
  INDEX `FK8B79F783FE125FEE` (`LAB_ADMINUNIT_ID` ASC) ,
  INDEX `FKC6929FC366A4104C` (`LABORATORY_ID` ASC) ,
  INDEX `FKC6929FC38861542` (`METHOD_ID` ASC) ,
  INDEX `fk_exammicroscopy_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_exammicroscopy_transactionlog2` (`createTransaction_ID` ASC) ,
  INDEX `FK8B79F783E0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FK8B79F783798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK8B79F78343A836FD`
    FOREIGN KEY (`LABORATORY_ID` )
    REFERENCES `etbmanagerd`.`laboratory` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FK8B79F783798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK8B79F783A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK8B79F783E0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK8B79F783E58A3BF3`
    FOREIGN KEY (`METHOD_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FK8B79F783FE125FEE`
    FOREIGN KEY (`LAB_ADMINUNIT_ID` )
    REFERENCES `etbmanagerd`.`administrativeunit` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKC6929FC366A4104C`
    FOREIGN KEY (`LABORATORY_ID` )
    REFERENCES `etbmanagerd`.`laboratory` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKC6929FC38861542`
    FOREIGN KEY (`METHOD_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_exammicroscopy_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_exammicroscopy_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`examskin`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`examskin` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`examskin` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comments` VARCHAR(250) NULL DEFAULT NULL ,
  `dateCollected` DATE NOT NULL ,
  `dateRelease` DATE NULL DEFAULT NULL ,
  `sampleNumber` VARCHAR(50) NULL DEFAULT NULL ,
  `result` INT(11) NULL DEFAULT NULL ,
  `LABORATORY_ID` INT(11) NULL DEFAULT NULL ,
  `METHOD_ID` INT(11) NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK774A4E5C66A4104C` (`LABORATORY_ID` ASC) ,
  INDEX `FK774A4E5C8861542` (`METHOD_ID` ASC) ,
  INDEX `FK774A4E5C8B327BA` (`CASE_ID` ASC) ,
  INDEX `FK774A4E5C43A836FD` (`LABORATORY_ID` ASC) ,
  INDEX `FK774A4E5CE58A3BF3` (`METHOD_ID` ASC) ,
  INDEX `FK774A4E5CA3F04EEB` (`CASE_ID` ASC) ,
  INDEX `FK737E761CE0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FK737E761C798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK737E761C43A836FD`
    FOREIGN KEY (`LABORATORY_ID` )
    REFERENCES `etbmanagerd`.`laboratory` (`id` )
    ON DELETE SET NULL,
  CONSTRAINT `FK737E761C798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK737E761CA3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK737E761CE0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK737E761CE58A3BF3`
    FOREIGN KEY (`METHOD_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`examxpert`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`examxpert` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`examxpert` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `case_ID` INT(11) NOT NULL ,
  `dateCollected` DATE NOT NULL ,
  `comments` VARCHAR(250) NULL DEFAULT NULL ,
  `dateRelease` DATE NULL DEFAULT NULL ,
  `sampleNumber` VARCHAR(50) NULL DEFAULT NULL ,
  `result` INT(11) NOT NULL ,
  `rifResult` INT(11) NULL DEFAULT NULL ,
  `method_ID` INT(11) NULL DEFAULT NULL ,
  `laboratory_ID` INT(11) NOT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `genex_case_fk` (`case_ID` ASC) ,
  INDEX `genex_lab_fk` (`laboratory_ID` ASC) ,
  INDEX `genex_transnew_fk` (`createTransaction_ID` ASC) ,
  INDEX `genex_transupdt_fk` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `genex_case_fk`
    FOREIGN KEY (`case_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `genex_lab_fk`
    FOREIGN KEY (`laboratory_ID` )
    REFERENCES `etbmanagerd`.`laboratory` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `genex_transnew_fk`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `genex_transupdt_fk`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`examxray`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`examxray` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`examxray` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comments` VARCHAR(250) NULL DEFAULT NULL ,
  `EVENT_DATE` DATE NOT NULL ,
  `baseline` INT(11) NULL DEFAULT NULL ,
  `evolution` INT(11) NULL DEFAULT NULL ,
  `result` INT(11) NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  `PRESENTATION_ID` INT(11) NULL DEFAULT NULL ,
  `destruction` BIT(1) NULL DEFAULT NULL ,
  `DISCRIMINATOR` VARCHAR(100) NOT NULL ,
  `LOCALIZATION_ID` INT(11) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK774C35718B327BA` (`CASE_ID` ASC) ,
  INDEX `FK774C3571A1A41069` (`PRESENTATION_ID` ASC) ,
  INDEX `FK774C3571D089CB3B` (`LOCALIZATION_ID` ASC) ,
  INDEX `fk_examxray_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_examxray_transactionlog2` (`createTransaction_ID` ASC) ,
  CONSTRAINT `FK7380D5517EA8371A`
    FOREIGN KEY (`PRESENTATION_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FK7380D551A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK7380D551D089CB3B`
    FOREIGN KEY (`LOCALIZATION_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_examxray_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_examxray_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`forecasting`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`forecasting` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`forecasting` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `bufferStock` INT(11) NOT NULL ,
  `casesFromDatabase` BIT(1) NOT NULL ,
  `comment` LONGTEXT NULL DEFAULT NULL ,
  `endDate` DATETIME NULL DEFAULT NULL ,
  `iniDate` DATETIME NULL DEFAULT NULL ,
  `medicineLine` INT(11) NULL DEFAULT NULL ,
  `name` VARCHAR(100) NOT NULL ,
  `numCasesOnTreatment` INT(11) NOT NULL ,
  `recordingDate` DATETIME NULL DEFAULT NULL ,
  `referenceDate` DATETIME NULL DEFAULT NULL ,
  `FORECASTING_VIEW` INT(11) NULL DEFAULT NULL ,
  `WORKSPACE_ID` INT(11) NOT NULL ,
  `ADMINUNIT_ID` INT(11) NULL DEFAULT NULL ,
  `TBUNIT_ID` INT(11) NULL DEFAULT NULL ,
  `USER_ID` INT(11) NULL DEFAULT NULL ,
  `leadTime` INT(11) NULL DEFAULT NULL ,
  `leadTimeMeasuring` INT(11) NULL DEFAULT NULL ,
  `publicView` BIT(1) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKC5CAEFE78A1004CB` (`ADMINUNIT_ID` ASC) ,
  INDEX `FKC5CAEFE7D84E76CC` (`USER_ID` ASC) ,
  INDEX `FKC5CAEFE7B9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `FKC5CAEFE7C9D89CEC` (`TBUNIT_ID` ASC) ,
  INDEX `fk_forecasting_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_forecasting_transactionlog2` (`createTransaction_ID` ASC) ,
  INDEX `FK62614807E0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FK62614807798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK626148076515C41D`
    FOREIGN KEY (`TBUNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FK62614807798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK62614807B3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK62614807BA5DB63D`
    FOREIGN KEY (`USER_ID` )
    REFERENCES `etbmanagerd`.`sys_user` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK62614807CEDEEA7C`
    FOREIGN KEY (`ADMINUNIT_ID` )
    REFERENCES `etbmanagerd`.`administrativeunit` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FK62614807E0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_forecasting_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_forecasting_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`forecastingmedicine`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`forecastingmedicine` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`forecastingmedicine` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `consumptionLT` INT(11) NOT NULL ,
  `consumptionCases` INT(11) NOT NULL ,
  `consumptionNewCases` INT(11) NOT NULL ,
  `stockOnHand` INT(11) NOT NULL ,
  `unitPrice` FLOAT NOT NULL ,
  `FORECASTING_ID` INT(11) NULL DEFAULT NULL ,
  `MEDICINE_ID` INT(11) NOT NULL ,
  `quantityExpired` INT(11) NULL DEFAULT NULL ,
  `stockOnOrder` INT(11) NULL DEFAULT NULL ,
  `stockOnOrderLT` INT(11) NULL DEFAULT NULL ,
  `quantityExpiredLT` INT(11) UNSIGNED NULL DEFAULT NULL ,
  `estimatedQtyCohort` INT(11) NULL DEFAULT NULL ,
  `quantityMissingLT` INT(11) NULL DEFAULT NULL ,
  `quantityToProcure` INT(11) NULL DEFAULT NULL ,
  `stockOutDate` DATE NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKD756D34112F7EF28` (`FORECASTING_ID` ASC) ,
  INDEX `FKD756D341FBB4E36C` (`MEDICINE_ID` ASC) ,
  CONSTRAINT `FK88C8A741BC45025D`
    FOREIGN KEY (`MEDICINE_ID` )
    REFERENCES `etbmanagerd`.`medicine` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK88C8A741D6789E97`
    FOREIGN KEY (`FORECASTING_ID` )
    REFERENCES `etbmanagerd`.`forecasting` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`forecastingbatch`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`forecastingbatch` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`forecastingbatch` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `consumptionInMonth` INT(11) NOT NULL ,
  `expiryDate` DATETIME NULL DEFAULT NULL ,
  `quantity` INT(11) NOT NULL ,
  `FORECASTINGMEDICINE_ID` INT(11) NULL DEFAULT NULL ,
  `quantityAvailable` INT(11) NULL DEFAULT NULL ,
  `quantityExpired` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKACB1CF93259FA308` (`FORECASTINGMEDICINE_ID` ASC) ,
  CONSTRAINT `FKE0B07B937AAD7377`
    FOREIGN KEY (`FORECASTINGMEDICINE_ID` )
    REFERENCES `etbmanagerd`.`forecastingmedicine` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`forecastingcasesontreat`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`forecastingcasesontreat` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`forecastingcasesontreat` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `monthIndex` INT(11) NOT NULL ,
  `numCases` INT(11) NULL DEFAULT NULL ,
  `FORECASTING_ID` INT(11) NULL DEFAULT NULL ,
  `REGIMEN_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK59F8823F2F370FE8` (`REGIMEN_ID` ASC) ,
  INDEX `FK59F8823F12F7EF28` (`FORECASTING_ID` ASC) ,
  CONSTRAINT `FK34EAAE7FD6789E97`
    FOREIGN KEY (`FORECASTING_ID` )
    REFERENCES `etbmanagerd`.`forecasting` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK34EAAE7FFB9ECED7`
    FOREIGN KEY (`REGIMEN_ID` )
    REFERENCES `etbmanagerd`.`regimen` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`forecastingnewcases`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`forecastingnewcases` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`forecastingnewcases` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `monthIndex` INT(11) NOT NULL ,
  `numNewCases` INT(11) NOT NULL ,
  `FORECASTING_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK5D8BD28A12F7EF28` (`FORECASTING_ID` ASC) ,
  CONSTRAINT `FK10C096AAD6789E97`
    FOREIGN KEY (`FORECASTING_ID` )
    REFERENCES `etbmanagerd`.`forecasting` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`forecastingorder`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`forecastingorder` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`forecastingorder` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `arrivalDate` DATETIME NULL DEFAULT NULL ,
  `quantity` INT(11) NOT NULL ,
  `FORECASTINGMEDICINE_ID` INT(11) NULL DEFAULT NULL ,
  `BATCH_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKAD707FA7259FA308` (`FORECASTINGMEDICINE_ID` ASC) ,
  INDEX `FKE16F2BA7AA08396` (`BATCH_ID` ASC) ,
  CONSTRAINT `FKE16F2BA77AAD7377`
    FOREIGN KEY (`FORECASTINGMEDICINE_ID` )
    REFERENCES `etbmanagerd`.`forecastingmedicine` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKE16F2BA7AA08396`
    FOREIGN KEY (`BATCH_ID` )
    REFERENCES `etbmanagerd`.`forecastingbatch` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`forecastingregimen`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`forecastingregimen` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`forecastingregimen` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `percNewCases` FLOAT NOT NULL ,
  `FORECASTING_ID` INT(11) NULL DEFAULT NULL ,
  `REGIMEN_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK9BFEF57A2F370FE8` (`REGIMEN_ID` ASC) ,
  INDEX `FK9BFEF57A12F7EF28` (`FORECASTING_ID` ASC) ,
  CONSTRAINT `FKCB02A17AD6789E97`
    FOREIGN KEY (`FORECASTING_ID` )
    REFERENCES `etbmanagerd`.`forecasting` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKCB02A17AFB9ECED7`
    FOREIGN KEY (`REGIMEN_ID` )
    REFERENCES `etbmanagerd`.`regimen` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`forecastingresult`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`forecastingresult` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`forecastingresult` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `monthIndex` INT(11) NOT NULL ,
  `numCasesOnTreatment` INT(11) NOT NULL ,
  `numNewCases` FLOAT NOT NULL ,
  `quantityExpired` INT(11) NOT NULL ,
  `quantityToExpire` INT(11) NOT NULL ,
  `stockOnHand` INT(11) NOT NULL ,
  `FORECASTING_ID` INT(11) NULL DEFAULT NULL ,
  `MEDICINE_ID` INT(11) NULL DEFAULT NULL ,
  `consumptionCases` INT(11) NULL DEFAULT NULL ,
  `consumptionNewCases` INT(11) NULL DEFAULT NULL ,
  `stockOnOrder` INT(11) NULL DEFAULT NULL ,
  `quantityMissing` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK50DDAE412F7EF28` (`FORECASTING_ID` ASC) ,
  INDEX `FK50DDAE4FBB4E36C` (`MEDICINE_ID` ASC) ,
  CONSTRAINT `FK50E4AEE4BC45025D`
    FOREIGN KEY (`MEDICINE_ID` )
    REFERENCES `etbmanagerd`.`medicine` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK50E4AEE4D6789E97`
    FOREIGN KEY (`FORECASTING_ID` )
    REFERENCES `etbmanagerd`.`forecasting` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`histology`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`histology` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`histology` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comments` VARCHAR(250) NULL DEFAULT NULL ,
  `EVENT_DATE` DATE NOT NULL ,
  `result` INT(11) NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKDE485CA28B327BA` (`CASE_ID` ASC) ,
  CONSTRAINT `FK66D63CC2A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`integration_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`integration_history` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`integration_history` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `lastIntegrationDate` DATE NULL DEFAULT NULL ,
  `noOfRecords` INT(11) NOT NULL ,
  `WORKSPACE_ID` INT(11) NOT NULL ,
  `user_id` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK6C22ED29BA5DB63D` (`user_id` ASC) ,
  INDEX `FK6C22ED29B3B1717` (`WORKSPACE_ID` ASC) ,
  CONSTRAINT `FK6C22ED29B3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` ),
  CONSTRAINT `FK6C22ED29BA5DB63D`
    FOREIGN KEY (`user_id` )
    REFERENCES `etbmanagerd`.`sys_user` (`id` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`issue`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`issue` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`issue` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `case_id` INT(11) NULL DEFAULT NULL ,
  `closed` TINYINT(1) NULL DEFAULT NULL ,
  `user_id` INT(11) NOT NULL ,
  `creationDate` DATETIME NOT NULL ,
  `answerDate` DATETIME NULL DEFAULT NULL ,
  `title` VARCHAR(200) NULL DEFAULT NULL ,
  `description` LONGTEXT NULL DEFAULT NULL ,
  `unit_id` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fkissue_unit` (`unit_id` ASC) ,
  INDEX `fkissue_case` (`case_id` ASC) ,
  INDEX `fkissue_user` (`user_id` ASC) ,
  CONSTRAINT `fkissue_case`
    FOREIGN KEY (`case_id` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fkissue_unit`
    FOREIGN KEY (`unit_id` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fkissue_user`
    FOREIGN KEY (`user_id` )
    REFERENCES `etbmanagerd`.`sys_user` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`issuefollowup`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`issuefollowup` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`issuefollowup` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `issue_id` INT(11) NOT NULL ,
  `text` LONGTEXT NULL DEFAULT NULL ,
  `user_id` INT(11) NOT NULL ,
  `followupDate` DATETIME NOT NULL ,
  `unit_id` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fkissuefollowup_unit` (`unit_id` ASC) ,
  INDEX `fkissuefollowup_issue` (`issue_id` ASC) ,
  INDEX `fkissuefollowup_user` (`user_id` ASC) ,
  CONSTRAINT `fkissuefollowup_issue`
    FOREIGN KEY (`issue_id` )
    REFERENCES `etbmanagerd`.`issue` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fkissuefollowup_unit`
    FOREIGN KEY (`unit_id` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fkissuefollowup_user`
    FOREIGN KEY (`user_id` )
    REFERENCES `etbmanagerd`.`sys_user` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`medicalexamination`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`medicalexamination` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`medicalexamination` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comments` LONGTEXT NULL DEFAULT NULL ,
  `EVENT_DATE` DATE NOT NULL ,
  `appointmentType` INT(11) NULL DEFAULT NULL ,
  `bloodPressureMax` FLOAT NULL DEFAULT NULL ,
  `bloodPressureMin` FLOAT NULL DEFAULT NULL ,
  `heartRate` FLOAT NULL DEFAULT NULL ,
  `heartRateRest` FLOAT NULL DEFAULT NULL ,
  `height` FLOAT NULL DEFAULT NULL ,
  `reasonNotUsingPrescMedicines` VARCHAR(100) NULL DEFAULT NULL ,
  `responsible` VARCHAR(100) NULL DEFAULT NULL ,
  `temperature` FLOAT NULL DEFAULT NULL ,
  `usingPrescMedicines` INT(11) NULL DEFAULT NULL ,
  `weight` FLOAT NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  `respRateRest` FLOAT NULL DEFAULT NULL ,
  `positionResponsible` VARCHAR(100) NULL DEFAULT NULL ,
  `clinicalEvolution` INT(11) NULL DEFAULT NULL ,
  `supervisedTreatment` INT(11) NULL DEFAULT NULL ,
  `supervisionUnitName` VARCHAR(100) NULL DEFAULT NULL ,
  `surgicalProcedure` INT(11) NULL DEFAULT NULL ,
  `surgicalProcedureDesc` VARCHAR(100) NULL DEFAULT NULL ,
  `dotDurinIntPhase` INT(11) NULL DEFAULT NULL ,
  `nutrtnSupport` INT(11) NULL DEFAULT NULL ,
  `patientRefBy` INT(11) NULL DEFAULT NULL ,
  `patientRefTo` INT(11) NULL DEFAULT NULL ,
  `REF_BY_DATE` DATE NULL DEFAULT NULL ,
  `REF_TO_DATE` DATE NULL DEFAULT NULL ,
  `referredByUnitName` VARCHAR(100) NULL DEFAULT NULL ,
  `referredToUnitName` VARCHAR(100) NULL DEFAULT NULL ,
  `dotProvName` VARCHAR(100) NULL DEFAULT NULL ,
  `DISCRIMINATOR` VARCHAR(31) NULL DEFAULT NULL ,
  `nextAppointment` DATE NULL DEFAULT NULL ,
  `dotContOptions` INT(11) NULL DEFAULT NULL ,
  `dotIntOptions` INT(11) NULL DEFAULT NULL ,
  `dotContProvName` VARCHAR(100) NULL DEFAULT NULL ,
  `Qualification` INT(11) NULL DEFAULT NULL ,
  `otherQualifiedProfessional` VARCHAR(100) NULL DEFAULT NULL ,
  `patientReferred` INT(11) NULL DEFAULT NULL ,
  `dotType` INT(11) NULL DEFAULT NULL ,
  `SIDE_EFFECT` INT(11) NULL DEFAULT NULL ,
  `dotDurinContPhase` INT(11) NULL DEFAULT NULL ,
  `otherPatRefBySrc` VARCHAR(255) NULL DEFAULT NULL ,
  `dotPhoneNumber` VARCHAR(50) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `DOTTYPE_ID` INT(11) NULL DEFAULT NULL ,
  `PATIENTREFTO_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK62CB08C08B327BA` (`CASE_ID` ASC) ,
  INDEX `FK919DA4C0677ECC33` (`SIDE_EFFECT` ASC) ,
  INDEX `fk_medicalexamination_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_medicalexamination_transactionlog2` (`createTransaction_ID` ASC) ,
  CONSTRAINT `FK919DA4C0677ECC33`
    FOREIGN KEY (`SIDE_EFFECT` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FK919DA4C0A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_medicalexamination_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_medicalexamination_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`medicinecomponent`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`medicinecomponent` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`medicinecomponent` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `strength` INT(11) NULL DEFAULT NULL ,
  `MEDICINE_ID` INT(11) NULL DEFAULT NULL ,
  `SUBSTANCE_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKE31987C3B7FBE608` (`SUBSTANCE_ID` ASC) ,
  INDEX `FKE31987C3FBB4E36C` (`MEDICINE_ID` ASC) ,
  CONSTRAINT `FK80E3280396FA537`
    FOREIGN KEY (`SUBSTANCE_ID` )
    REFERENCES `etbmanagerd`.`substance` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK80E32803BC45025D`
    FOREIGN KEY (`MEDICINE_ID` )
    REFERENCES `etbmanagerd`.`medicine` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 1692
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`medicinedispensingitem`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`medicinedispensingitem` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`medicinedispensingitem` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `DISPENSING_ID` INT(11) NULL DEFAULT NULL ,
  `MEDICINE_ID` INT(11) NOT NULL ,
  `MOVEMENT_ID` INT(11) NOT NULL ,
  `SOURCE_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK5EFD8E07817DE04C` (`SOURCE_ID` ASC) ,
  INDEX `FK5EFD8E0775C78AC6` (`DISPENSING_ID` ASC) ,
  INDEX `FK5EFD8E078D91D34C` (`MOVEMENT_ID` ASC) ,
  INDEX `FK5EFD8E07FBB4E36C` (`MEDICINE_ID` ASC) ,
  CONSTRAINT `FK6B7CA1A71CBB077D`
    FOREIGN KEY (`SOURCE_ID` )
    REFERENCES `etbmanagerd`.`source` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK6B7CA1A74E21F23D`
    FOREIGN KEY (`MOVEMENT_ID` )
    REFERENCES `etbmanagerd`.`movement` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK6B7CA1A7BA967077`
    FOREIGN KEY (`DISPENSING_ID` )
    REFERENCES `etbmanagerd`.`medicinedispensing` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK6B7CA1A7BC45025D`
    FOREIGN KEY (`MEDICINE_ID` )
    REFERENCES `etbmanagerd`.`medicine` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`medicinedispensingbatch`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`medicinedispensingbatch` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`medicinedispensingbatch` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `quantity` INT(11) NOT NULL ,
  `BATCH_ID` INT(11) NULL DEFAULT NULL ,
  `DISPENSINGITEM_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK804922C62005C06` (`DISPENSINGITEM_ID` ASC) ,
  INDEX `FK804922C6DB4F70C8` (`BATCH_ID` ASC) ,
  CONSTRAINT `FK3AC83263B281F77`
    FOREIGN KEY (`BATCH_ID` )
    REFERENCES `etbmanagerd`.`batch` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK3AC8326D4930137`
    FOREIGN KEY (`DISPENSINGITEM_ID` )
    REFERENCES `etbmanagerd`.`medicinedispensingitem` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`medicinedispensingcase`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`medicinedispensingcase` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`medicinedispensingcase` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `quantity` INT(11) NOT NULL ,
  `BATCH_ID` INT(11) NOT NULL ,
  `DISPENSING_ID` INT(11) NOT NULL ,
  `SOURCE_ID` INT(11) NOT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK5EFA8E241CBB077D` (`SOURCE_ID` ASC) ,
  INDEX `FK5EFA8E243B281F77` (`BATCH_ID` ASC) ,
  INDEX `FK5EFA8E24BA967077` (`DISPENSING_ID` ASC) ,
  INDEX `FK5EFA8E24A3F04EEB` (`CASE_ID` ASC) ,
  CONSTRAINT `FK6B79A1C41CBB077D`
    FOREIGN KEY (`SOURCE_ID` )
    REFERENCES `etbmanagerd`.`source` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK6B79A1C43B281F77`
    FOREIGN KEY (`BATCH_ID` )
    REFERENCES `etbmanagerd`.`batch` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK6B79A1C4A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK6B79A1C4BA967077`
    FOREIGN KEY (`DISPENSING_ID` )
    REFERENCES `etbmanagerd`.`medicinedispensing` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`medicineorder`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`medicineorder` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`medicineorder` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `approvingDate` DATETIME NULL DEFAULT NULL ,
  `cancelReason` VARCHAR(200) NULL DEFAULT NULL ,
  `comments` BLOB NULL DEFAULT NULL ,
  `shippingDate` DATE NULL DEFAULT NULL ,
  `numDays` INT(11) NULL DEFAULT NULL ,
  `orderDate` DATETIME NOT NULL ,
  `receivingDate` DATE NULL DEFAULT NULL ,
  `status` INT(11) NULL DEFAULT NULL ,
  `AUTHORIZER_UNIT_ID` INT(11) NULL DEFAULT NULL ,
  `UNIT_FROM_ID` INT(11) NOT NULL ,
  `UNIT_TO_ID` INT(11) NOT NULL ,
  `USER_CREATOR_ID` INT(11) NOT NULL ,
  `legacyId` VARCHAR(50) NULL DEFAULT NULL ,
  `shipAddress` VARCHAR(200) NULL DEFAULT NULL ,
  `shipAddressCont` VARCHAR(200) NULL DEFAULT NULL ,
  `shipContactName` VARCHAR(200) NULL DEFAULT NULL ,
  `shipContactPhone` VARCHAR(200) NULL DEFAULT NULL ,
  `shipInstitutionName` VARCHAR(200) NULL DEFAULT NULL ,
  `shipZipCode` VARCHAR(50) NULL DEFAULT NULL ,
  `SHIP_ADMINUNIT_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKB7E1D347F10C048` (`UNIT_TO_ID` ASC) ,
  INDEX `FKB7E1D3430712779` (`UNIT_FROM_ID` ASC) ,
  INDEX `FKB7E1D341D280684` (`AUTHORIZER_UNIT_ID` ASC) ,
  INDEX `FKB7E1D3482DF27BF` (`USER_CREATOR_ID` ASC) ,
  INDEX `FK7F9C45147F10C048` (`UNIT_TO_ID` ASC) ,
  INDEX `FK7F9C451430712779` (`UNIT_FROM_ID` ASC) ,
  INDEX `FK7F9C45141D280684` (`AUTHORIZER_UNIT_ID` ASC) ,
  INDEX `FK7F9C451482DF27BF` (`USER_CREATOR_ID` ASC) ,
  INDEX `FK7F9C4514DD15FB9F` (`SHIP_ADMINUNIT_ID` ASC) ,
  INDEX `FK51C00554DD15FB9F` (`SHIP_ADMINUNIT_ID` ASC) ,
  CONSTRAINT `FK51C005541A4DE779`
    FOREIGN KEY (`UNIT_TO_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK51C0055464EE6730`
    FOREIGN KEY (`USER_CREATOR_ID` )
    REFERENCES `etbmanagerd`.`sys_user` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK51C00554B8652DB5`
    FOREIGN KEY (`AUTHORIZER_UNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK51C00554CBAE4EAA`
    FOREIGN KEY (`UNIT_FROM_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK51C00554DD15FB9F`
    FOREIGN KEY (`SHIP_ADMINUNIT_ID` )
    REFERENCES `etbmanagerd`.`administrativeunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK7F9C4514DD15FB9F`
    FOREIGN KEY (`SHIP_ADMINUNIT_ID` )
    REFERENCES `etbmanagerd`.`administrativeunit` (`id` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`medicinereceiving_ua`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`medicinereceiving_ua` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`medicinereceiving_ua` (
  `id` INT(11) NOT NULL ,
  `consignmentNumber` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_id_medreceveing` (`id` ASC) ,
  CONSTRAINT `fk_id_medreceveing`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`medicinereceiving` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`medicineregimen`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`medicineregimen` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`medicineregimen` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `defaultDoseUnit` INT(11) NULL DEFAULT NULL ,
  `defaultFrequency` INT(11) NULL DEFAULT NULL ,
  `phase` INT(11) NULL DEFAULT NULL ,
  `SOURCE_ID` INT(11) NOT NULL ,
  `MEDICINE_ID` INT(11) NOT NULL ,
  `REGIMEN_ID` INT(11) NULL DEFAULT NULL ,
  `monthsTreatment` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK924F13A7817DE04C` (`SOURCE_ID` ASC) ,
  INDEX `FK924F13A72F370FE8` (`REGIMEN_ID` ASC) ,
  INDEX `FK924F13A7FBB4E36C` (`MEDICINE_ID` ASC) ,
  CONSTRAINT `FK6A83C3E71CBB077D`
    FOREIGN KEY (`SOURCE_ID` )
    REFERENCES `etbmanagerd`.`source` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK6A83C3E7BC45025D`
    FOREIGN KEY (`MEDICINE_ID` )
    REFERENCES `etbmanagerd`.`medicine` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK6A83C3E7FB9ECED7`
    FOREIGN KEY (`REGIMEN_ID` )
    REFERENCES `etbmanagerd`.`regimen` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 943000
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`medicineunit`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`medicineunit` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`medicineunit` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `measure` INT(11) NULL DEFAULT NULL ,
  `minBufferStock` INT(11) NULL DEFAULT NULL ,
  `numDaysOrder` INT(11) NULL DEFAULT NULL ,
  `MEDICINE_ID` INT(11) NOT NULL ,
  `SOURCE_ID` INT(11) NOT NULL ,
  `UNIT_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKEB5A4AFE817DE04C` (`SOURCE_ID` ASC) ,
  INDEX `FKEB5A4AFEFBB4E36C` (`MEDICINE_ID` ASC) ,
  INDEX `FKEB5A4AFED6A3231A` (`UNIT_ID` ASC) ,
  CONSTRAINT `FKE19D82BE1CBB077D`
    FOREIGN KEY (`SOURCE_ID` )
    REFERENCES `etbmanagerd`.`source` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKE19D82BE71E04A4B`
    FOREIGN KEY (`UNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKE19D82BEBC45025D`
    FOREIGN KEY (`MEDICINE_ID` )
    REFERENCES `etbmanagerd`.`medicine` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`molecularbiology`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`molecularbiology` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`molecularbiology` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comments` VARCHAR(250) NULL DEFAULT NULL ,
  `EVENT_DATE` DATE NOT NULL ,
  `dateRelease` DATE NULL DEFAULT NULL ,
  `result` INT(11) NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  `LABORATORY_ID` INT(11) NULL DEFAULT NULL ,
  `METHOD_ID` INT(11) NULL DEFAULT NULL ,
  `pcr` BIT(1) NOT NULL DEFAULT b'0' ,
  `h` BIT(1) NULL DEFAULT NULL ,
  `r` BIT(1) NULL DEFAULT NULL ,
  `km` BIT(1) NULL DEFAULT NULL ,
  `cm` BIT(1) NULL DEFAULT NULL ,
  `e` BIT(1) NULL DEFAULT NULL ,
  `lfx` BIT(1) NULL DEFAULT NULL ,
  `mfx` BIT(1) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK1484EE8D66A4104C` (`LABORATORY_ID` ASC) ,
  INDEX `FK1484EE8D8861542` (`METHOD_ID` ASC) ,
  INDEX `FK1484EE8D8B327BA` (`CASE_ID` ASC) ,
  INDEX `FK1484EE8DE58A3BF3` (`METHOD_ID` ASC) ,
  CONSTRAINT `FKE388128D43A836FD`
    FOREIGN KEY (`LABORATORY_ID` )
    REFERENCES `etbmanagerd`.`laboratory` (`id` )
    ON DELETE CASCADE,
  CONSTRAINT `FKE388128DA3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE,
  CONSTRAINT `FKE388128DE58A3BF3`
    FOREIGN KEY (`METHOD_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`movements_dispensing`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`movements_dispensing` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`movements_dispensing` (
  `DISPENSING_ID` INT(11) NOT NULL ,
  `MOVEMENT_ID` INT(11) NOT NULL ,
  INDEX `FKAA427C75BA967077` (`DISPENSING_ID` ASC) ,
  INDEX `FKAA427C754E21F23D` (`MOVEMENT_ID` ASC) ,
  INDEX `FK4E4F4C55BA967077` (`DISPENSING_ID` ASC) ,
  INDEX `FK4E4F4C554E21F23D` (`MOVEMENT_ID` ASC) ,
  CONSTRAINT `FKAA427C754E21F23D`
    FOREIGN KEY (`MOVEMENT_ID` )
    REFERENCES `etbmanagerd`.`movement` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKAA427C75BA967077`
    FOREIGN KEY (`DISPENSING_ID` )
    REFERENCES `etbmanagerd`.`medicinedispensing` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`movements_receiving`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`movements_receiving` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`movements_receiving` (
  `RECEIVING_ID` INT(11) NOT NULL ,
  `MOVEMENT_ID` INT(11) NOT NULL ,
  INDEX `FK50CB605D418CD1D` (`RECEIVING_ID` ASC) ,
  INDEX `FK50CB6054E21F23D` (`MOVEMENT_ID` ASC) ,
  CONSTRAINT `FK50CB6054E21F23D`
    FOREIGN KEY (`MOVEMENT_ID` )
    REFERENCES `etbmanagerd`.`movement` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK50CB605D418CD1D`
    FOREIGN KEY (`RECEIVING_ID` )
    REFERENCES `etbmanagerd`.`medicinereceiving` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`orderitem`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`orderitem` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`orderitem` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `approvedQuantity` INT(11) NULL DEFAULT NULL ,
  `comment` VARCHAR(200) NULL DEFAULT NULL ,
  `shippedQuantity` INT(11) NULL DEFAULT NULL ,
  `estimatedQuantity` INT(11) NOT NULL ,
  `numPatients` INT(11) NULL DEFAULT NULL ,
  `receivedQuantity` INT(11) NULL DEFAULT NULL ,
  `requestedQuantity` INT(11) NOT NULL ,
  `MOVEMENT_IN_ID` INT(11) NULL DEFAULT NULL ,
  `MOVEMENT_OUT_ID` INT(11) NULL DEFAULT NULL ,
  `MEDICINE_ID` INT(11) NOT NULL ,
  `ORDER_ID` INT(11) NOT NULL ,
  `SOURCE_ID` INT(11) NOT NULL ,
  `stockQuantity` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK60163F61817DE04C` (`SOURCE_ID` ASC) ,
  INDEX `FK60163F61561ECF86` (`MOVEMENT_IN_ID` ASC) ,
  INDEX `FK60163F618A9A8848` (`ORDER_ID` ASC) ,
  INDEX `FK60163F61FBB4E36C` (`MEDICINE_ID` ASC) ,
  INDEX `FK60163F61CEAF55DD` (`MOVEMENT_OUT_ID` ASC) ,
  CONSTRAINT `FKE8B2AB6116AEEE77`
    FOREIGN KEY (`MOVEMENT_IN_ID` )
    REFERENCES `etbmanagerd`.`movement` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKE8B2AB611CBB077D`
    FOREIGN KEY (`SOURCE_ID` )
    REFERENCES `etbmanagerd`.`source` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKE8B2AB618F3F74CE`
    FOREIGN KEY (`MOVEMENT_OUT_ID` )
    REFERENCES `etbmanagerd`.`movement` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKE8B2AB61BC45025D`
    FOREIGN KEY (`MEDICINE_ID` )
    REFERENCES `etbmanagerd`.`medicine` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKE8B2AB61EA7336F7`
    FOREIGN KEY (`ORDER_ID` )
    REFERENCES `etbmanagerd`.`medicineorder` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`orderbatch`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`orderbatch` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`orderbatch` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `quantity` INT(11) NOT NULL ,
  `receivedQuantity` INT(11) NULL DEFAULT NULL ,
  `BATCH_ID` INT(11) NOT NULL ,
  `ORDERITEM_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKA2469CACDB4F70C8` (`BATCH_ID` ASC) ,
  INDEX `FKA2469CAC5234C888` (`ORDERITEM_ID` ASC) ,
  CONSTRAINT `FK2D37B0AC3B281F77`
    FOREIGN KEY (`BATCH_ID` )
    REFERENCES `etbmanagerd`.`batch` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK2D37B0ACA3A887B7`
    FOREIGN KEY (`ORDERITEM_ID` )
    REFERENCES `etbmanagerd`.`orderitem` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`ordercase`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`ordercase` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`ordercase` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `estimatedQuantity` INT(11) NOT NULL ,
  `ORDERITEM_ID` INT(11) NOT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK60133F7E5234C888` (`ORDERITEM_ID` ASC) ,
  INDEX `FK60133F7E8B327BA` (`CASE_ID` ASC) ,
  CONSTRAINT `FKE8AFAB7EA3A887B7`
    FOREIGN KEY (`ORDERITEM_ID` )
    REFERENCES `etbmanagerd`.`orderitem` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKE8AFAB7EA3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`ordercomment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`ordercomment` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`ordercomment` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comment` LONGTEXT NULL DEFAULT NULL ,
  `date` DATETIME NOT NULL ,
  `statusOnComment` INT(11) NOT NULL ,
  `ORDER_ID` INT(11) NOT NULL ,
  `USER_CREATOR_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKA7A3351EA7336F7` (`ORDER_ID` ASC) ,
  CONSTRAINT `FKA7A3351EA7336F7`
    FOREIGN KEY (`ORDER_ID` )
    REFERENCES `etbmanagerd`.`medicineorder` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`physicalexam`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`physicalexam` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`physicalexam` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `result` INT(11) NULL DEFAULT NULL ,
  `CASEDATA_ID` INT(11) NOT NULL ,
  `EXAM_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKB1DE1DB6EC9AFD44` (`EXAM_ID` ASC) ,
  INDEX `FKB1DE1DB6D3629284` (`CASEDATA_ID` ASC) ,
  INDEX `FKB1DE1DB6F503B113` (`CASEDATA_ID` ASC) ,
  CONSTRAINT `FKA8215576C99F23F5`
    FOREIGN KEY (`EXAM_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE CASCADE,
  CONSTRAINT `FKA8215576F503B113`
    FOREIGN KEY (`CASEDATA_ID` )
    REFERENCES `etbmanagerd`.`casedataph` (`id` )
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`prescribedmedicine`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`prescribedmedicine` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`prescribedmedicine` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `doseUnit` INT(11) NOT NULL ,
  `frequency` INT(11) NOT NULL ,
  `MEDICINE_ID` INT(11) NOT NULL ,
  `SOURCE_ID` INT(11) NOT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  `IniDate` DATE NOT NULL ,
  `EndDate` DATE NOT NULL ,
  `comments` VARCHAR(200) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK85BFA211817DE04C` (`SOURCE_ID` ASC) ,
  INDEX `FK85BFA211FBB4E36C` (`MEDICINE_ID` ASC) ,
  INDEX `FK85BFA2118B327BA` (`CASE_ID` ASC) ,
  INDEX `fkprescmed_lasttx` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK142181D11CBB077D`
    FOREIGN KEY (`SOURCE_ID` )
    REFERENCES `etbmanagerd`.`source` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK142181D1A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK142181D1BC45025D`
    FOREIGN KEY (`MEDICINE_ID` )
    REFERENCES `etbmanagerd`.`medicine` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fkprescmed_lasttx`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`prevtbtreatment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`prevtbtreatment` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`prevtbtreatment` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `TREATMENT_MONTH` INT(11) NULL DEFAULT NULL ,
  `outcome` INT(11) NOT NULL ,
  `TREATMENT_YEAR` INT(11) NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  `OUTCOME_MONTH` INT(11) NULL DEFAULT NULL ,
  `OUTCOME_YEAR` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK988E31778B327BA` (`CASE_ID` ASC) ,
  CONSTRAINT `FK824DD5B7A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`prevtbtreatmentng`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`prevtbtreatmentng` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`prevtbtreatmentng` (
  `healthFacility` INT(11) NULL DEFAULT NULL ,
  `id` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK262F51B044011E6F` (`id` ASC) ,
  CONSTRAINT `FK262F51B044011E6F`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`prevtbtreatment` (`id` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`prevtbtreatmentua`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`prevtbtreatmentua` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`prevtbtreatmentua` (
  `id` INT(11) NOT NULL ,
  `registrationDate` DATE NULL DEFAULT NULL ,
  `registrationCode` VARCHAR(50) NULL DEFAULT NULL ,
  `patientType` INT(11) NULL DEFAULT NULL ,
  `refuse2line` BIT(1) NOT NULL ,
  `prevtreat_id` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_id_case_prevtreat` (`prevtreat_id` ASC) ,
  CONSTRAINT `fk_id_case_prevtreat`
    FOREIGN KEY (`prevtreat_id` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`quarterlyreportdetails`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`quarterlyreportdetails` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`quarterlyreportdetails` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `quarter` INT(11) NOT NULL ,
  `year` INT(11) NOT NULL ,
  `UNIT_ID` INT(11) NOT NULL ,
  `MEDICINE_ID` INT(11) NOT NULL ,
  `outOfStock` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'Store the quarterly report by tbunit, quarter and year';


-- -----------------------------------------------------
-- Table `etbmanagerd`.`regimenunit`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`regimenunit` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`regimenunit` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `numTreatments` INT(11) NOT NULL ,
  `REGIMEN_ID` INT(11) NOT NULL ,
  `UNIT_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK8DB60C652F370FE8` (`REGIMEN_ID` ASC) ,
  INDEX `FK8DB60C65D6A3231A` (`UNIT_ID` ASC) ,
  CONSTRAINT `FK2A5AF06571E04A4B`
    FOREIGN KEY (`UNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK2A5AF065FB9ECED7`
    FOREIGN KEY (`REGIMEN_ID` )
    REFERENCES `etbmanagerd`.`regimen` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`report`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`report` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`report` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `title` VARCHAR(200) NOT NULL ,
  `published` TINYINT(1) NOT NULL ,
  `registrationDate` DATETIME NOT NULL ,
  `owner_id` INT(11) NOT NULL ,
  `dashboard` TINYINT(1) NOT NULL ,
  `data` LONGTEXT NULL DEFAULT NULL ,
  `lastTransaction_id` INT(11) NULL DEFAULT NULL ,
  `workspace_id` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK_report_user` (`owner_id` ASC) ,
  INDEX `FK_report_tx` (`lastTransaction_id` ASC) ,
  INDEX `FK_report_ws` (`workspace_id` ASC) ,
  CONSTRAINT `FK_report_tx`
    FOREIGN KEY (`lastTransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK_report_user`
    FOREIGN KEY (`owner_id` )
    REFERENCES `etbmanagerd`.`sys_user` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK_report_ws`
    FOREIGN KEY (`workspace_id` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`res_prevtbtreatment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`res_prevtbtreatment` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`res_prevtbtreatment` (
  `PREVTBTREATMENT_ID` INT(11) NOT NULL ,
  `SUBSTANCE_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`PREVTBTREATMENT_ID`, `SUBSTANCE_ID`) ,
  INDEX `FK7CFF03D8380E5E08` (`PREVTBTREATMENT_ID` ASC) ,
  INDEX `FK7CFF03D8B7FBE608` (`SUBSTANCE_ID` ASC) ,
  CONSTRAINT `FK972BA41896FA537`
    FOREIGN KEY (`SUBSTANCE_ID` )
    REFERENCES `etbmanagerd`.`substance` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK972BA418E3BBBDF7`
    FOREIGN KEY (`PREVTBTREATMENT_ID` )
    REFERENCES `etbmanagerd`.`prevtbtreatment` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`sequenceinfo`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`sequenceinfo` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`sequenceinfo` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `number` INT(11) NOT NULL ,
  `seq_name` VARCHAR(50) NOT NULL ,
  `WORKSPACE_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK39EADC2FB9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `FK302E13EFE0BC8241` (`createTransaction_ID` ASC) ,
  INDEX `FK302E13EF798D9E5B` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FK302E13EF798D9E5B`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK302E13EFB3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK302E13EFE0BC8241`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`stockposition`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`stockposition` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`stockposition` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `quantity` INT(11) NOT NULL ,
  `totalPrice` FLOAT NOT NULL ,
  `MEDICINE_ID` INT(11) NOT NULL ,
  `SOURCE_ID` INT(11) NOT NULL ,
  `UNIT_ID` INT(11) NOT NULL ,
  `lastMovement` DATE NOT NULL ,
  `amc` INT(11) NULL DEFAULT NULL ,
  `stock_date` DATE NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK191AE3F817DE04C` (`SOURCE_ID` ASC) ,
  INDEX `FK191AE3FFBB4E36C` (`MEDICINE_ID` ASC) ,
  INDEX `FK191AE3FD6A3231A` (`UNIT_ID` ASC) ,
  CONSTRAINT `FKCE181A3F1CBB077D`
    FOREIGN KEY (`SOURCE_ID` )
    REFERENCES `etbmanagerd`.`source` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKCE181A3F71E04A4B`
    FOREIGN KEY (`UNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKCE181A3FBC45025D`
    FOREIGN KEY (`MEDICINE_ID` )
    REFERENCES `etbmanagerd`.`medicine` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`substances_resistpattern`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`substances_resistpattern` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`substances_resistpattern` (
  `ResistancePattern_id` INT(11) NOT NULL ,
  `substances_id` INT(11) NOT NULL ,
  INDEX `FKA0BA320A93BEB595` (`substances_id` ASC) ,
  INDEX `FKA0BA320AD2C6E688` (`ResistancePattern_id` ASC) ,
  CONSTRAINT `FKCFDF2E2A489C06B7`
    FOREIGN KEY (`ResistancePattern_id` )
    REFERENCES `etbmanagerd`.`resistancepattern` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKCFDF2E2AE53274C4`
    FOREIGN KEY (`substances_id` )
    REFERENCES `etbmanagerd`.`substance` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`systemconfig`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`systemconfig` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`systemconfig` (
  `id` INT(11) NOT NULL ,
  `allowRegPage` BIT(1) NOT NULL ,
  `systemMail` VARCHAR(100) NOT NULL ,
  `systemURL` VARCHAR(100) NOT NULL ,
  `TBUNIT_ID` INT(11) NULL DEFAULT NULL ,
  `USERPROFILE_ID` INT(11) NULL DEFAULT NULL ,
  `WORKSPACE_ID` INT(11) NOT NULL ,
  `adminMail` VARCHAR(100) NULL DEFAULT NULL ,
  `buildNumber` INT(11) NULL DEFAULT NULL ,
  `buildVersion` INT(11) NULL DEFAULT NULL ,
  `pageRootURL` VARCHAR(200) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKC533967169D6BFA8` (`USERPROFILE_ID` ASC) ,
  INDEX `FKC5339671B9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `FKC5339671C9D89CEC` (`TBUNIT_ID` ASC) ,
  CONSTRAINT `FKF20356312D576F17`
    FOREIGN KEY (`USERPROFILE_ID` )
    REFERENCES `etbmanagerd`.`userprofile` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKF20356316515C41D`
    FOREIGN KEY (`TBUNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKF2035631B3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`systemparam`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`systemparam` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`systemparam` (
  `param_key` VARCHAR(255) NOT NULL ,
  `param_value` VARCHAR(100) NULL DEFAULT NULL ,
  `WORKSPACE_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`param_key`) ,
  INDEX `FK70D637EB9C757E8` (`WORKSPACE_ID` ASC) ,
  CONSTRAINT `FKA566ABBEB3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tag`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tag` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tag` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `tag_name` VARCHAR(100) NOT NULL ,
  `workspace` TINYBLOB NULL DEFAULT NULL ,
  `WORKSPACE_ID` INT(11) NULL DEFAULT NULL ,
  `sqlCondition` LONGTEXT NULL DEFAULT NULL ,
  `consistencyCheck` TINYINT(1) NOT NULL ,
  `lasttransaction_id` INT(11) NULL DEFAULT NULL ,
  `createtransaction_id` INT(11) NULL DEFAULT NULL ,
  `active` TINYINT(1) NOT NULL ,
  `dailyUpdate` TINYINT(1) NULL DEFAULT '0' ,
  PRIMARY KEY (`id`) ,
  INDEX `FK1477AB9C757E8` (`WORKSPACE_ID` ASC) ,
  INDEX `fk_tag_transactionlog1` (`lasttransaction_id` ASC) ,
  INDEX `fk_tag_transactionlog2` (`createtransaction_id` ASC) ,
  INDEX `FK1BF9AE0BC8241` (`createtransaction_id` ASC) ,
  INDEX `FK1BF9A798D9E5B` (`lasttransaction_id` ASC) ,
  CONSTRAINT `FK1BF9A798D9E5B`
    FOREIGN KEY (`lasttransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `FK1BF9AB3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK1BF9AE0BC8241`
    FOREIGN KEY (`createtransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` ),
  CONSTRAINT `fk_tag_transactionlog1`
    FOREIGN KEY (`lasttransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_tag_transactionlog2`
    FOREIGN KEY (`createtransaction_id` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tags_case`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tags_case` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tags_case` (
  `CASE_ID` INT(11) NOT NULL ,
  `TAG_ID` INT(11) NOT NULL ,
  INDEX `FK4577A7968B327BA` (`CASE_ID` ASC) ,
  INDEX `FK4577A796DAFE1048` (`TAG_ID` ASC) ,
  INDEX `FK4577A796A3F04EEB` (`CASE_ID` ASC) ,
  CONSTRAINT `FK9D9CA796A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK9D9CA796C1409EB7`
    FOREIGN KEY (`TAG_ID` )
    REFERENCES `etbmanagerd`.`tag` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tbcasebd`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tbcasebd` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tbcasebd` (
  `bcgScar` INT(11) NULL DEFAULT NULL ,
  `id` INT(11) NOT NULL ,
  `occupation` INT(11) NULL DEFAULT NULL ,
  `salary` INT(11) NULL DEFAULT NULL ,
  `pulmonaryTypesBD` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKA77F6082DD737C` (`id` ASC) ,
  CONSTRAINT `FKA77F6082DD737C`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tbcasein`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tbcasein` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tbcasein` (
  `SUSPECT_CRITERIA_Complement` VARCHAR(255) NULL DEFAULT NULL ,
  `id` INT(11) NOT NULL ,
  `SUSPECT_CRITERIA` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKA7804382DD737C` (`id` ASC) ,
  INDEX `FKA7804316CAAA61` (`SUSPECT_CRITERIA` ASC) ,
  CONSTRAINT `FKA7804316CAAA61`
    FOREIGN KEY (`SUSPECT_CRITERIA` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` ),
  CONSTRAINT `FKA7804382DD737C`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tbcasekh`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tbcasekh` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tbcasekh` (
  `id` INT(11) NOT NULL ,
  `suspectType` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKA7807B82DD737C` (`id` ASC) ,
  INDEX `fktbcasekh_fieldvalue` (`suspectType` ASC) ,
  CONSTRAINT `FKA7807B82DD737C`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fktbcasekh_fieldvalue`
    FOREIGN KEY (`suspectType` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tbcaseuz`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tbcaseuz` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tbcaseuz` (
  `anothertb_Complement` VARCHAR(255) NULL DEFAULT NULL ,
  `id` INT(11) NOT NULL ,
  `anothertb_id` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKA781C382DD737C` (`id` ASC) ,
  INDEX `FKA781C3F351DA23` (`anothertb_id` ASC) ,
  CONSTRAINT `FKA781C382DD737C`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKA781C3F351DA23`
    FOREIGN KEY (`anothertb_id` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tbcasevi`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tbcasevi` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tbcasevi` (
  `id` INT(11) NOT NULL ,
  `nationalIDNumber` VARCHAR(250) NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_tbcase`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tbcontact`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tbcontact` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tbcontact` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `age` VARCHAR(255) NULL DEFAULT NULL ,
  `examinated` BIT(1) NOT NULL ,
  `gender` INT(11) NULL DEFAULT NULL ,
  `name` VARCHAR(255) NULL DEFAULT NULL ,
  `CONDUCT_ID` INT(11) NULL DEFAULT NULL ,
  `CONTACTTYPE_ID` INT(11) NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  `comments` LONGTEXT NULL DEFAULT NULL ,
  `dateOfExamination` DATETIME NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  `createTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK69755B28B327BA` (`CASE_ID` ASC) ,
  INDEX `FK69755B28A7B0429` (`CONTACTTYPE_ID` ASC) ,
  INDEX `FK69755B21789BDFF` (`CONDUCT_ID` ASC) ,
  INDEX `fk_tbcontact_transactionlog1` (`lastTransaction_ID` ASC) ,
  INDEX `fk_tbcontact_transactionlog2` (`createTransaction_ID` ASC) ,
  CONSTRAINT `FK2BEC9DF2677F2ADA`
    FOREIGN KEY (`CONTACTTYPE_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FK2BEC9DF2A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK2BEC9DF2F48DE4B0`
    FOREIGN KEY (`CONDUCT_ID` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_tbcontact_transactionlog1`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_tbcontact_transactionlog2`
    FOREIGN KEY (`createTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tbcontact_kh`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tbcontact_kh` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tbcontact_kh` (
  `hasTBSymptom` BIT(1) NOT NULL ,
  `id` INT(11) NOT NULL ,
  `sampleSentForCultureTest` BIT(1) NULL DEFAULT NULL ,
  `sampleSentForDSTTest` BIT(1) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK4FEE32AFA32AC6A` (`id` ASC) ,
  CONSTRAINT `FK4FEE32AFA32AC6A`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`tbcontact` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tbcontactbr`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tbcontactbr` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tbcontactbr` (
  `cultureResult` INT(11) NULL DEFAULT NULL ,
  `dateEndTreat` DATETIME NULL DEFAULT NULL ,
  `dateIniTreat` DATETIME NULL DEFAULT NULL ,
  `microscopyResult` INT(11) NULL DEFAULT NULL ,
  `scarBCG` INT(11) NULL DEFAULT NULL ,
  `tbSymptoms` INT(11) NULL DEFAULT NULL ,
  `tuberculinTest` INT(11) NULL DEFAULT NULL ,
  `xray` INT(11) NULL DEFAULT NULL ,
  `id` INT(11) NOT NULL ,
  `tuberculinTestResult` VARCHAR(255) NULL DEFAULT NULL ,
  `XRAY_RESULT_Complement` VARCHAR(255) NULL DEFAULT NULL ,
  `XRAY_RESULT` INT(11) NULL DEFAULT NULL ,
  `TREATMENT_OUTCOME_ILTB_Complement` VARCHAR(255) NULL DEFAULT NULL ,
  `TREATMENT_OUTCOME_ILTB` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKBE18B982A8BEED3B` (`id` ASC) ,
  INDEX `FKBE18B9825D7C8704` (`XRAY_RESULT` ASC) ,
  INDEX `FKE33CF5C286B39ABF` (`TREATMENT_OUTCOME_ILTB` ASC) ,
  CONSTRAINT `FKE33CF5C25D7C8704`
    FOREIGN KEY (`XRAY_RESULT` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `FKE33CF5C286B39ABF`
    FOREIGN KEY (`TREATMENT_OUTCOME_ILTB` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` ),
  CONSTRAINT `FKE33CF5C2FA32AC6A`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`tbcontact` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`tbcontactng`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`tbcontactng` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`tbcontactng` (
  `id` INT(11) NOT NULL ,
  `RISK_GROUP` INT(11) NULL DEFAULT NULL ,
  `contactLastName` VARCHAR(255) NULL DEFAULT NULL ,
  `contactOtherName` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKE33CF72B1F9F7FC9` (`RISK_GROUP` ASC) ,
  INDEX `FKE33CF72BFA32AC6A` (`id` ASC) ,
  CONSTRAINT `FKE33CF72B1F9F7FC9`
    FOREIGN KEY (`RISK_GROUP` )
    REFERENCES `etbmanagerd`.`fieldvalue` (`id` ),
  CONSTRAINT `FKE33CF72BFA32AC6A`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`tbcontact` (`id` ))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`transfer`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`transfer` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`transfer` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `cancelReason` VARCHAR(200) NULL DEFAULT NULL ,
  `commentsFrom` LONGTEXT NULL DEFAULT NULL ,
  `commentsTo` LONGTEXT NULL DEFAULT NULL ,
  `shippingDate` DATE NOT NULL ,
  `receivingDate` DATE NULL DEFAULT NULL ,
  `status` INT(11) NOT NULL ,
  `UNIT_ID_FROM` INT(11) NOT NULL ,
  `UNIT_ID_TO` INT(11) NOT NULL ,
  `USER_FROM_ID` INT(11) NOT NULL ,
  `USER_TO_ID` INT(11) NULL DEFAULT NULL ,
  `consignmentNumber` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK50331C0BB65BA1F7` (`UNIT_ID_FROM` ASC) ,
  INDEX `FK50331C0B7E70BF08` (`UNIT_ID_TO` ASC) ,
  INDEX `FK50331C0B547220A8` (`USER_TO_ID` ASC) ,
  INDEX `FK50331C0B18966219` (`USER_FROM_ID` ASC) ,
  CONSTRAINT `FK4C58B7EB19ADE639`
    FOREIGN KEY (`UNIT_ID_TO` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK4C58B7EB36816019`
    FOREIGN KEY (`USER_TO_ID` )
    REFERENCES `etbmanagerd`.`sys_user` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK4C58B7EB5198C928`
    FOREIGN KEY (`UNIT_ID_FROM` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK4C58B7EBFAA5A18A`
    FOREIGN KEY (`USER_FROM_ID` )
    REFERENCES `etbmanagerd`.`sys_user` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`transfer_ua`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`transfer_ua` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`transfer_ua` (
  `id` INT(11) NOT NULL ,
  `orderNumber` VARCHAR(50) NULL DEFAULT NULL ,
  `orderDate` DATE NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_id_transfer` (`id` ASC) ,
  CONSTRAINT `fk_id_transfer`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`transfer` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`transferitem`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`transferitem` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`transferitem` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `MEDICINE_ID` INT(11) NOT NULL ,
  `MOV_IN_ID` INT(11) NULL DEFAULT NULL ,
  `MOV_OUT_ID` INT(11) NULL DEFAULT NULL ,
  `SOURCE_ID` INT(11) NOT NULL ,
  `TRANSFER_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK8A030DBE8B1DB9CC` (`TRANSFER_ID` ASC) ,
  INDEX `FK8A030DBE817DE04C` (`SOURCE_ID` ASC) ,
  INDEX `FK8A030DBEE64F562B` (`MOV_IN_ID` ASC) ,
  INDEX `FK8A030DBEFBB4E36C` (`MEDICINE_ID` ASC) ,
  INDEX `FK8A030DBE448FA3D8` (`MOV_OUT_ID` ASC) ,
  CONSTRAINT `FK8046457E1CBB077D`
    FOREIGN KEY (`SOURCE_ID` )
    REFERENCES `etbmanagerd`.`source` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK8046457E4BADD8BD`
    FOREIGN KEY (`TRANSFER_ID` )
    REFERENCES `etbmanagerd`.`transfer` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK8046457E51FC2C9`
    FOREIGN KEY (`MOV_OUT_ID` )
    REFERENCES `etbmanagerd`.`movement` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK8046457EA6DF751C`
    FOREIGN KEY (`MOV_IN_ID` )
    REFERENCES `etbmanagerd`.`movement` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK8046457EBC45025D`
    FOREIGN KEY (`MEDICINE_ID` )
    REFERENCES `etbmanagerd`.`medicine` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`transferbatch`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`transferbatch` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`transferbatch` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `quantity` INT(11) NOT NULL ,
  `quantityReceived` INT(11) NULL DEFAULT NULL ,
  `BATCH_ID` INT(11) NOT NULL ,
  `TRANSFERITEM_ID` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKB5F399EF12FB980C` (`TRANSFERITEM_ID` ASC) ,
  INDEX `FKB5F399EFDB4F70C8` (`BATCH_ID` ASC) ,
  CONSTRAINT `FK88175A2F3B281F77`
    FOREIGN KEY (`BATCH_ID` )
    REFERENCES `etbmanagerd`.`batch` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK88175A2FBF90D67D`
    FOREIGN KEY (`TRANSFERITEM_ID` )
    REFERENCES `etbmanagerd`.`transferitem` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`treatmenthealthunit`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`treatmenthealthunit` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`treatmenthealthunit` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `endDate` DATE NULL DEFAULT NULL ,
  `iniDate` DATE NULL DEFAULT NULL ,
  `CASE_ID` INT(11) NOT NULL ,
  `UNIT_ID` INT(11) NOT NULL ,
  `transferring` BIT(1) NULL DEFAULT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKBF99A8788B327BA` (`CASE_ID` ASC) ,
  INDEX `FKBF99A878D6A3231A` (`UNIT_ID` ASC) ,
  INDEX `fktreathu_lasttx` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `FKFE22905871E04A4B`
    FOREIGN KEY (`UNIT_ID` )
    REFERENCES `etbmanagerd`.`tbunit` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FKFE229058A3F04EEB`
    FOREIGN KEY (`CASE_ID` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fktreathu_lasttx`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`treatmentmonitoring`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`treatmentmonitoring` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`treatmentmonitoring` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `case_id` INT(11) NOT NULL ,
  `month_treat` INT(11) NOT NULL ,
  `year_treat` INT(11) NOT NULL ,
  `day1` INT(11) NOT NULL ,
  `day2` INT(11) NOT NULL ,
  `day3` INT(11) NOT NULL ,
  `day4` INT(11) NOT NULL ,
  `day5` INT(11) NOT NULL ,
  `day6` INT(11) NOT NULL ,
  `day7` INT(11) NOT NULL ,
  `day8` INT(11) NOT NULL ,
  `day9` INT(11) NOT NULL ,
  `day10` INT(11) NOT NULL ,
  `day11` INT(11) NOT NULL ,
  `day12` INT(11) NOT NULL ,
  `day13` INT(11) NOT NULL ,
  `day14` INT(11) NOT NULL ,
  `day15` INT(11) NOT NULL ,
  `day16` INT(11) NOT NULL ,
  `day17` INT(11) NOT NULL ,
  `day18` INT(11) NOT NULL ,
  `day19` INT(11) NOT NULL ,
  `day20` INT(11) NOT NULL ,
  `day21` INT(11) NOT NULL ,
  `day22` INT(11) NOT NULL ,
  `day23` INT(11) NOT NULL ,
  `day24` INT(11) NOT NULL ,
  `day25` INT(11) NOT NULL ,
  `day26` INT(11) NOT NULL ,
  `day27` INT(11) NOT NULL ,
  `day28` INT(11) NOT NULL ,
  `day29` INT(11) NOT NULL ,
  `day30` INT(11) NOT NULL ,
  `day31` INT(11) NOT NULL ,
  `lastTransaction_ID` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_treatmonit_case` (`case_id` ASC) ,
  INDEX `fk_treatmonit_translog` (`lastTransaction_ID` ASC) ,
  CONSTRAINT `fk_treatmonit_case`
    FOREIGN KEY (`case_id` )
    REFERENCES `etbmanagerd`.`tbcase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_treatmonit_translog`
    FOREIGN KEY (`lastTransaction_ID` )
    REFERENCES `etbmanagerd`.`transactionlog` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`treatmentmonitoringkh`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`treatmentmonitoringkh` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`treatmentmonitoringkh` (
  `id` INT(11) NOT NULL ,
  `weight` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_treatmonit_treatmonitKH`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`treatmentmonitoring` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`treatmentmonitoringvi`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`treatmentmonitoringvi` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`treatmentmonitoringvi` (
  `id` INT(11) NOT NULL ,
  `weight` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_treatmonit_treatmonitVI`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`treatmentmonitoring` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`userlogin`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`userlogin` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`userlogin` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `Application` VARCHAR(200) NULL DEFAULT NULL ,
  `IpAddress` VARCHAR(16) NULL DEFAULT NULL ,
  `loginDate` DATETIME NOT NULL ,
  `logoutDate` DATETIME NULL DEFAULT NULL ,
  `USER_ID` INT(11) NOT NULL ,
  `WORKSPACE_ID` INT(11) NOT NULL ,
  `sessionId` VARCHAR(32) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK8AA0DA3ED84E76CC` (`USER_ID` ASC) ,
  INDEX `FK8AA0DA3EB9C757E8` (`WORKSPACE_ID` ASC) ,
  CONSTRAINT `FK14F1AA7EB3B1717`
    FOREIGN KEY (`WORKSPACE_ID` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK14F1AA7EBA5DB63D`
    FOREIGN KEY (`USER_ID` )
    REFERENCES `etbmanagerd`.`sys_user` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 992406
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`userpermission`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`userpermission` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`userpermission` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `canChange` BIT(1) NOT NULL ,
  `canExecute` BIT(1) NOT NULL ,
  `grantPermission` BIT(1) NOT NULL ,
  `PROFILE_ID` INT(11) NOT NULL ,
  `ROLE_ID` INT(11) NOT NULL ,
  `caseClassification` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FKD265581AF8AF6C57` (`ROLE_ID` ASC) ,
  INDEX `FKD265581AB3AFD59D` (`PROFILE_ID` ASC) ,
  CONSTRAINT `FK974CAFDA7730850C`
    FOREIGN KEY (`PROFILE_ID` )
    REFERENCES `etbmanagerd`.`userprofile` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK974CAFDAB93F8B48`
    FOREIGN KEY (`ROLE_ID` )
    REFERENCES `etbmanagerd`.`userrole` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 97
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `etbmanagerd`.`workspaceview`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `etbmanagerd`.`workspaceview` ;

CREATE  TABLE IF NOT EXISTS `etbmanagerd`.`workspaceview` (
  `id` INT(11) NOT NULL ,
  `logoImage` VARCHAR(200) NULL DEFAULT NULL ,
  `picture` LONGBLOB NULL DEFAULT NULL ,
  `pictureContentType` VARCHAR(20) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `FK_workspaceview_WS`
    FOREIGN KEY (`id` )
    REFERENCES `etbmanagerd`.`workspace` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

USE `etbmanagerd` ;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
