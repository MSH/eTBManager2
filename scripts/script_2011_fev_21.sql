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
