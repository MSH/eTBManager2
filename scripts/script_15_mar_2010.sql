ALTER TABLE `etbmanager`.`orderitem` CHANGE COLUMN `deliveredQuantity` `shippedQuantity` INT(11) DEFAULT NULL;

ALTER TABLE `etbmanager`.`medicineorder` CHANGE COLUMN `deliveryDate` `shippingDate` DATE DEFAULT NULL;

ALTER TABLE `etbmanager`.`transfer` CHANGE COLUMN `deliveryDate` `shippingDate` DATE NOT NULL;

insert into userrole (id, changeable, code, executable, role_name, internaluse)
values (135, false, '040902', true, 'WSCOPY', true);

delete from casedispensingdays
where exists (select * from casedispensing where id = casedispensingdays.id);

