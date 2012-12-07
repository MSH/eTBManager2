/***************************************************************
 * 07-dec-2012
 * By Ricardo Memoria
 * 
 * Including new field in tag table - Enabled true/false to enable or disable tags
 * 
****************************************************************/

ALTER TABLE `tag` ADD COLUMN `active` BOOLEAN NOT NULL;

update tag set active = true;
