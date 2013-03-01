/***************************************************************
 * 28-feb-2013
 * By A.M.
 * 
 * AZ only!!! Add new userrole for AZ
 * 
****************************************************************/

INSERT INTO `etbmanager`.`userrole` (`id`, `changeable`, `code`, `Role_Name`, `internalUse`, `byCaseClassification`, `messageKey`) VALUES (196, 0, '010413', 'CASE_NEW_TB', 0, 0, 'userrole.CASE_NEW_TB');

INSERT INTO userpermission(canchange, canexecute, grantPermission, profile_id, role_id, caseClassification)
SELECT false, true, true, id, 196, 0 from userprofile
WHERE workspace_id = 8
AND NOT EXISTS(SELECT * FROM userpermission WHERE role_id=196 AND profile_id=userprofile.id);
