insert into UserRole (id, changeable, code, executable, role_name, internaluse)
values (137, true, '010300', false, 'NMTCASES', false);

insert into userpermission
(canchange, canexecute, canopen, grantPermission, profile_id, role_id)
select false, false, false, true, id, 137 from userprofile
where name like "%adminis%"
and not exists (select * from userpermission where role_id=137 and profile_id=userprofile.id);
