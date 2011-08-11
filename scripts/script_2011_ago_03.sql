/***************************************************************
* By Ricardo Memoria - 30-jul-2011
* - New TbUnit field patientDispensing
****************************************************************/

ALTER TABLE TbUnit ADD COLUMN patientDispensing boolean;

update TbUnit set patientDispensing = true;

update medicineReceiving set totalprice=(select sum(mov.unitPrice * mov.quantity) from medicineReceivingItem it
join movement mov on mov.id=it.movement_id
where it.receiving_id=medicineReceiving.id);

update medicineReceiving set totalPrice=0 where totalPrice is null;

insert into movements_receiving (receiving_id, movement_id)
select receiving_id, movement_id
from medicineReceivingItem
where receiving_id not in (select distinct receiving_id from movements_receiving);

update UserRole set internalUse=false where id=155;

ALTER TABLE movements_dispensing
 DROP FOREIGN KEY `FKAA427C754E21F23D`;

ALTER TABLE movements_dispensing
 DROP FOREIGN KEY `FKAA427C75BA967077`;

ALTER TABLE movements_dispensing ADD CONSTRAINT `FKAA427C754E21F23D` FOREIGN KEY `FKAA427C754E21F23D` (`MOVEMENT_ID`)
    REFERENCES `movement` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FKAA427C75BA967077` FOREIGN KEY `FKAA427C75BA967077` (`DISPENSING_ID`)
    REFERENCES `medicinedispensing` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

ALTER TABLE movements_receiving
 DROP FOREIGN KEY `FK50CB6054E21F23D`;

ALTER TABLE movements_receiving
 DROP FOREIGN KEY `FK50CB605D418CD1D`;

ALTER TABLE movements_receiving ADD CONSTRAINT `FK50CB6054E21F23D` FOREIGN KEY `FK50CB6054E21F23D` (`MOVEMENT_ID`)
    REFERENCES `movement` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK50CB605D418CD1D` FOREIGN KEY `FK50CB605D418CD1D` (`RECEIVING_ID`)
    REFERENCES `medicinereceiving` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

    
ALTER TABLE Tbunit MODIFY COLUMN `address` VARCHAR(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
 CHANGE COLUMN `district` `addressCont` VARCHAR(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
 DROP COLUMN `shipAddress`,
 DROP COLUMN `shipAddressCont`,
 DROP COLUMN `shipContactName`,
 DROP COLUMN `shipContactPhone`,
 ADD COLUMN `zipCode` VARCHAR(50) AFTER `patientDispensing`;
