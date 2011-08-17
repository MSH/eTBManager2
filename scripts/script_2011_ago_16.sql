/***************************************************************
*
* By Ricardo Memoria
****************************************************************/

update tbcase
set treatment_unit_id = (select max(a.unit_id)
from treatmenthealthunit a
where a.endDate = (select max(b.endDate) from treatmenthealthunit b where b.case_id = tbcase.id)
and a.case_id = tbcase.id)

ALTER TABLE `etbmanager`.`medicineorder` MODIFY COLUMN `approvingDate` DATETIME DEFAULT NULL,
 MODIFY COLUMN `orderDate` DATETIME NOT NULL;
