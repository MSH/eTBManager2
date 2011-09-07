/***************************************************************
* New model for movements and stock quantity
* By Ricardo Memoria
****************************************************************/

ALTER TABLE movement CHANGE COLUMN `unitPrice` `totalPrice` FLOAT NOT NULL;

create temporary table temp_stockposition
select a.id
from stockposition a
join (select unit_id, source_id, medicine_id, max(stock_date) lastdate
from stockposition group by unit_id,source_id,medicine_id) sp
 on a.unit_id=sp.unit_id and a.source_id=sp.source_id and a.medicine_id=sp.medicine_id and a.stock_date=sp.lastdate;
 
delete from stockposition
where id not in (select id from temp_stockposition);

drop table temp_stockposition;

alter table stockposition drop column stock_date;

delete from batchmovement where quantity is null;

delete from movement
where not exists(select * from batchmovement a
where a.movement_id=movement.id);

update batch
set unitprice = round(unitprice, 2);

update movement
set quantity = (select sum(b.quantity)
from batchmovement b
where b.movement_id = movement.id);

update movement
set totalprice = (select sum(b.quantity * c.unitprice)
from batchmovement b
join batch c on c.id=b.batch_id
where b.movement_id = movement.id);

alter table movement
drop column stockquantity;

delete from stockposition
where not exists(select * from movement b
where b.unit_id=stockposition.unit_id and b.source_id=stockposition.source_id and b.medicine_id=stockposition.medicine_id);

update stockposition
set quantity = (select sum(a.quantity * a.oper)
from movement a
where a.unit_id=stockposition.unit_id and a.source_id=stockposition.source_id and a.medicine_id=stockposition.medicine_id);

update stockposition
set totalprice = (select sum(b.quantity * a.oper * c.unitPrice)
from batchmovement b
join movement a on a.id=b.movement_id
join batch c on c.id=b.batch_id
where a.unit_id=stockposition.unit_id and a.source_id=stockposition.source_id and a.medicine_id=stockposition.medicine_id);

delete from batchquantity;

insert into batchquantity (batch_id, source_id, unit_id, quantity)
select a.batch_id, b.source_id, b.unit_id, sum(a.quantity * b.oper)
from batchmovement a
join movement b on b.id = a.movement_id
group by a.batch_id, b.source_id, b.unit_id;

ALTER TABLE stockposition ADD COLUMN `lastMovement` DATE NOT NULL;

update stockposition
set lastmovement = (select max(mov_date) from movement a 
where a.unit_id=stockposition.unit_id and a.source_id=stockposition.source_id and a.medicine_id=stockposition.medicine_id);

delete from stockposition where quantity=0;

delete from batchquantity where quantity=0;

ALTER TABLE `etbmanager`.`batchdispensing`
 DROP FOREIGN KEY `FK4342D194BA967077`;

ALTER TABLE `etbmanager`.`batchdispensing`
 DROP FOREIGN KEY `FK4342D1941CBB077D`;

ALTER TABLE `etbmanager`.`batchdispensing`
 DROP FOREIGN KEY `FK4342D1943B281F77`;

ALTER TABLE `etbmanager`.`batchdispensing` ADD CONSTRAINT `FK4342D194BA967077` FOREIGN KEY `FK4342D194BA967077` (`DISPENSING_ID`)
    REFERENCES `medicinedispensing` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK4342D1941CBB077D` FOREIGN KEY `FK4342D1941CBB077D` (`SOURCE_ID`)
    REFERENCES `source` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK4342D1943B281F77` FOREIGN KEY `FK4342D1943B281F77` (`BATCH_ID`)
    REFERENCES `batch` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

drop table medicinedispensingbatch;
drop table medicinedispensingitem;

ALTER TABLE `etbmanager`.`medicinedispensingcase`
 DROP FOREIGN KEY `FK6B79A1C4A3F04EEB`;

ALTER TABLE `etbmanager`.`medicinedispensingcase`
 DROP FOREIGN KEY `FK6B79A1C41CBB077D`;

ALTER TABLE `etbmanager`.`medicinedispensingcase`
 DROP FOREIGN KEY `FK6B79A1C43B281F77`;

ALTER TABLE `etbmanager`.`medicinedispensingcase`
 DROP FOREIGN KEY `FK6B79A1C4BA967077`;

ALTER TABLE `etbmanager`.`medicinedispensingcase` ADD CONSTRAINT `FK6B79A1C4A3F04EEB` FOREIGN KEY `FK6B79A1C4A3F04EEB` (`CASE_ID`)
    REFERENCES `tbcase` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK6B79A1C41CBB077D` FOREIGN KEY `FK6B79A1C41CBB077D` (`SOURCE_ID`)
    REFERENCES `source` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK6B79A1C43B281F77` FOREIGN KEY `FK6B79A1C43B281F77` (`BATCH_ID`)
    REFERENCES `batch` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK6B79A1C4BA967077` FOREIGN KEY `FK6B79A1C4BA967077` (`DISPENSING_ID`)
    REFERENCES `medicinedispensing` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

    
ALTER TABLE `etbmanager`.`medicineorder`
 DROP FOREIGN KEY `FK51C00554DD15FB9F`;

ALTER TABLE `etbmanager`.`medicineorder`
 DROP FOREIGN KEY `FK51C005541A4DE779`;

ALTER TABLE `etbmanager`.`medicineorder`
 DROP FOREIGN KEY `FK51C0055464EE6730`;

ALTER TABLE `etbmanager`.`medicineorder`
 DROP FOREIGN KEY `FK51C00554B8652DB5`;

ALTER TABLE `etbmanager`.`medicineorder`
 DROP FOREIGN KEY `FK51C00554CBAE4EAA`;

ALTER TABLE `etbmanager`.`medicineorder`
 DROP FOREIGN KEY `FK7F9C4514DD15FB9F`;

ALTER TABLE `etbmanager`.`medicineorder` ADD CONSTRAINT `FK51C00554DD15FB9F` FOREIGN KEY `FK51C00554DD15FB9F` (`SHIP_ADMINUNIT_ID`)
    REFERENCES `administrativeunit` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK51C005541A4DE779` FOREIGN KEY `FK51C005541A4DE779` (`UNIT_TO_ID`)
    REFERENCES `tbunit` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK51C0055464EE6730` FOREIGN KEY `FK51C0055464EE6730` (`USER_CREATOR_ID`)
    REFERENCES `sys_user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK51C00554B8652DB5` FOREIGN KEY `FK51C00554B8652DB5` (`AUTHORIZER_UNIT_ID`)
    REFERENCES `tbunit` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK51C00554CBAE4EAA` FOREIGN KEY `FK51C00554CBAE4EAA` (`UNIT_FROM_ID`)
    REFERENCES `tbunit` (`id`);

ALTER TABLE errorlog MODIFY COLUMN `exceptionMessage` VARCHAR(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL;

insert into userrole (id, changeable, code, role_name, internaluse, bycaseclassification)
values (167, false, '045103', 'ERRORLOGREP', false, false);

INSERT INTO UserPermission
(canchange, canexecute, grantPermission, profile_id, role_id)
select false, true, true, id, 167 from UserProfile
where name like "%adminis%"
and not exists (select * from UserPermission where role_id=167 and profile_id=UserProfile.id);

 