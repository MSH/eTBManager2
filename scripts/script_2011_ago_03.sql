/***************************************************************
* By Ricardo Memoria - 30-jul-2011
* - New TbUnit field patientDispensing
****************************************************************/

ALTER TABLE TbUnit ADD COLUMN patientDispensing boolean;

update TbUnit set patientDispensing = true;



