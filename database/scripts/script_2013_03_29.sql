/***************************************************************
 * 29-feb-2013
 * By A.M.
 * 
 * AZ only!!! Add new userrole for AZ
 * 
****************************************************************/

INSERT INTO `etbmanager`.`userrole` (`id`, `changeable`, `code`, `Role_Name`, `internalUse`, `byCaseClassification`, `messageKey`) VALUES (198, 0, '010416', 'EDT_HIV_TB', 0, 0, 'userrole.EDT_HIV_TB');

INSERT INTO userpermission(canchange, canexecute, grantPermission, profile_id, role_id, caseClassification)
SELECT false, true, true, id, 198, 0 from userprofile
WHERE workspace_id = 8;

INSERT INTO userpermission(canchange, canexecute, grantPermission, profile_id, role_id, caseClassification)
SELECT false, true, true, id, 198, 1 from userprofile
WHERE workspace_id = 8;