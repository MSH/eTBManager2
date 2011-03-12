/***************************************************************
* Adjustments in forecasting
* 
* By Ricardo Memoria - 15-feb-2011
****************************************************************/

ALTER TABLE ForecastingBatch
	add quantityAvailable int(11);

UPDATE ForecastingBatch
	set quantityAvailable = 0;

ALTER TABLE ForecastingBatch 
	CHANGE COLUMN `quantityToExpire` `quantityExpired` INT(11) NOT NULL;

ALTER TABLE ForecastingMedicine
	add quantityMissingLT int(11);

UPDATE ForecastingMedicine
	set quantityMissingLT = 0;

ALTER TABLE ForecastingResult
 CHANGE COLUMN `quantityCasesOnTreatment` `consumptionCases` INT(11) NOT NULL,
 CHANGE COLUMN `quantityNewCases` `consumptionNewCases` FLOAT NOT NULL,
 CHANGE COLUMN `quantityOnOrder` `stockOnOrder` INT(11) NOT NULL;

update TbCase
set TbCase.treatment_unit_id = (select max(unit_id)
from TreatmentHealthUnit
where case_id = TbCase.id
and enddate = (select max(aux.enddate) from TreatmentHealthUnit aux where aux.case_id = TbCase.id));


/* CHANGES IN 01-mar-2011 */
alter table Forecasting add publicView BIT(1);

update Forecasting set publicview=0;
