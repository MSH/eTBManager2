ALTER TABLE TbCase DROP COLUMN `CURR_MOBILENUMBER`,
 DROP COLUMN `CURR_PHONENUMBER`,
 CHANGE COLUMN `NOTIF_MOBILENUMBER` `MOBILENUMBER` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
 CHANGE COLUMN `NOTIF_PHONENUMBER` `PHONENUMBER` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL;

ALTER TABLE Workspace ADD COLUMN `useCustomTheme` BIT(1) NOT NULL;
 
update Workspace
set usecustomTheme = 0
where useCustomTheme is null;


ALTER TABLE TbCase ADD COLUMN `ValidationState` INT(11) UNSIGNED DEFAULT 0 AFTER `daysTreatPlanned`;

update TbCase 
set validationState = 0 where state = 0;

update TbCase
set validationState = 1 where state <> 0;

update TbCase
set state = 3 where state < 3;

update TbCase
set state = state - 3;

ALTER TABLE TbCase ADD COLUMN issueCounter INT(11) UNSIGNED DEFAULT 0;

update TbCase
set issueCounter=0
where issueCounter is null;