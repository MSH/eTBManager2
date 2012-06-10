insert into userrole (id, changeable, code, executable, role_name, internalUse)
values (130, true, '011001', true, 'EXAM_CULTURE', true);

insert into userrole (id, changeable, code, executable, role_name, internalUse)
values (131, true, '011002', true, 'EXAM_MICROSC', true);

insert into userrole (id, changeable, code, executable, role_name, internalUse)
values (132, true, '011003', true, 'EXAM_DST', true);

insert into userrole (id, changeable, code, executable, role_name, internalUse)
values (133, true, '011006', true, 'EXAM_HIV', true);

insert into userrole (id, changeable, code, executable, role_name, internalUse)
values (134, true, '011005', true, 'EXAM_XRAY', true);

ALTER TABLE `etbmanager`.`examxray` MODIFY COLUMN `result` INT(11) DEFAULT NULL;