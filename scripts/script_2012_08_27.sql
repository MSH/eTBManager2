/***************************************************************
* By Mauricio Santos
* 
* Add the new role that defines if the profile can or can't update the receiving date of a order.
* 
****************************************************************/
INSERT INTO userrole(id, code,role_name,internalUse,byCaseClassification,changeable,messageKey) 
values(193, '020406', 'ORDER_DTREC', false, false, false, 'adm.modifyReceivingDate');

INSERT INTO userpermission(canchange, canexecute, grantPermission, profile_id, role_id)
SELECT false, false, true, id, 193 from userprofile
WHERE name like "%administ%"
AND NOT EXISTS(SELECT * FROM userpermission WHERE role_id=193 AND profile_id=userprofile.id);