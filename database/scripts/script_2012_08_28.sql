/***************************************************************
* By Mauricio Santos
* 
* Fix the role that gives the access to the history of modifications.
* 
****************************************************************/
UPDATE userrole
SET messageKey = 'form.logreport'
WHERE id = 118;

DELETE FROM userpermission
WHERE role_id = 118;

INSERT INTO userpermission(canchange, canexecute, grantPermission, profile_id, role_id)
SELECT false, false, true, id, 118 from userprofile
WHERE name like "%administ%"
AND NOT EXISTS(SELECT * FROM userpermission WHERE role_id=118 AND profile_id=userprofile.id);