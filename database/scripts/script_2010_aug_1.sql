insert into UserRole (id, changeable, code, executable, role_name, internaluse)
values (136, false, '044000', true, 'IMPORT', false);

insert into userpermission
(canchange, canexecute, canopen, grantPermission, profile_id, role_id)
select false, true, false, true, id, 136 from userprofile
where name like "%adminis%"
and not exists (select * from userpermission where role_id=136 and profile_id=userprofile.id);

Alter table TbCase add version int(11);

update TbCase set version=1;


ALTER TABLE `etbmanager`.`userlogin` 
	MODIFY COLUMN `Application` VARCHAR(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL;
