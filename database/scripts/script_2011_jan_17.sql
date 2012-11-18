/***************************************************************
* Making HIV date not required (when exam is on going)
* 
* By Ricardo Memoria - 05-jan-2011
****************************************************************/

ALTER TABLE ExamHIV MODIFY COLUMN `EVENT_DATE` DATE, DROP COLUMN `resultDate`;