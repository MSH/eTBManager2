/***************************************************************
 * 15-feb-2013
 * By A.M.
 * 
 * AZ only!!! Replace some duplicate recordNumbers in table Patient to next by order in base
 * 
****************************************************************/
UPDATE `etbmanager`.`patient` SET `recordNumber`=(select number from `etbmanager`.`sequenceinfo` where workspace_id = 8 and seq_name like 'CASE_NUMBER')+1 WHERE `id`='1069430';
UPDATE `etbmanager`.`patient` SET `recordNumber`=(select number from `etbmanager`.`sequenceinfo` where workspace_id = 8 and seq_name like 'CASE_NUMBER')+2 WHERE `id`='1069596';
UPDATE `etbmanager`.`patient` SET `recordNumber`=(select number from `etbmanager`.`sequenceinfo` where workspace_id = 8 and seq_name like 'CASE_NUMBER')+3 WHERE `id`='1069620';
UPDATE `etbmanager`.`sequenceinfo` SET `number`=`number`+3 WHERE `id`='940640';




