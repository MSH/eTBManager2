/***************************************************************
 * 08-feb-2013
 * By A.M.
 * 
 * UA only!!! Add new fields to CaseDataUA
 * 
****************************************************************/
ALTER TABLE `etbmanager`.`casedataua` 
ADD COLUMN `dateHospitalization2` DATE NULL DEFAULT NULL  AFTER `treatARTdate` , 
ADD COLUMN `dateHospitalization3` DATE NULL DEFAULT NULL  AFTER `dateHospitalization2` , 
ADD COLUMN `dateHospitalization4` DATE NULL DEFAULT NULL  AFTER `dateHospitalization3` , 
ADD COLUMN `dateHospitalization5` DATE NULL DEFAULT NULL  AFTER `dateHospitalization4` , 
ADD COLUMN `dischargeDate2` DATE NULL DEFAULT NULL  AFTER `dateHospitalization5` , 
ADD COLUMN `dischargeDate3` DATE NULL DEFAULT NULL  AFTER `dischargeDate2` , 
ADD COLUMN `dischargeDate4` DATE NULL DEFAULT NULL  AFTER `dischargeDate3` , 
ADD COLUMN `dischargeDate5` DATE NULL DEFAULT NULL  AFTER `dischargeDate4` ;



