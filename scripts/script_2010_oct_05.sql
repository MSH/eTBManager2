INSERT INTO UserRole (id, changeable, code, executable, role_name, internalUse) VALUES (138, false, '020900', true, 'MED_INIT', false);

INSERT INTO UserPermission
(canchange, canexecute, canopen, grantPermission, profile_id, role_id)
select false, true, false, true, id, 138 from userprofile
where name like "%adminis%"
and not exists (select * from UserPermission where role_id=138 and profile_id=userprofile.id);
