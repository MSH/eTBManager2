/***************************************************************
*
* By Ricardo Memoria
****************************************************************/
alter table TbCase
add TREATMENT_UNIT_ID int(11) null,
add constraint FKCB85A29E45C57184 foreign key (treatment_unit_id) references TbUnit(id);

update TbCase
set treatment_unit_id = (select max(a.unit_id)
from treatmenthealthunit a
where a.endDate = (select max(b.endDate) from treatmenthealthunit b where b.case_id = tbcase.id)
and a.case_id = tbcase.id);

ALTER TABLE `etbmanager`.`medicineorder` MODIFY COLUMN `approvingDate` DATETIME DEFAULT NULL,
 MODIFY COLUMN `orderDate` DATETIME NOT NULL;

drop table MedicineDispensingBatch;

drop table MedicineDispensingItem;
