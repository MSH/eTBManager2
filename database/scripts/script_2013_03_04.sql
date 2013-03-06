/***************************************************************
 * 04-mar-2013
 * By A.M.
 * 
 * AZ only!!! Add new userrole for AZ
 * 
****************************************************************/

UPDATE `etbmanager`.`userrole` SET `Role_Name`='PATIENT_NEW_TB', `messageKey`='userrole.PATIENT_NEW_TB' WHERE `id`='196';

INSERT INTO `etbmanager`.`userrole` (`id`, `changeable`, `code`, `Role_Name`, `internalUse`, `byCaseClassification`, `messageKey`) VALUES (197, 0, '010414', 'CASE_NEW_TB', 0, 0, 'userrole.CASE_NEW_TB');

INSERT INTO userpermission(canchange, canexecute, grantPermission, profile_id, role_id, caseClassification)
SELECT false, true, true, id, 197, 0 from userprofile
WHERE workspace_id = 8

/* and for DR-TB */
INSERT INTO userpermission(canchange, canexecute, grantPermission, profile_id, role_id, caseClassification)
SELECT false, true, true, id, 196, 1 from userprofile
WHERE workspace_id = 8

INSERT INTO userpermission(canchange, canexecute, grantPermission, profile_id, role_id, caseClassification)
SELECT false, true, true, id, 197, 1 from userprofile
WHERE workspace_id = 8