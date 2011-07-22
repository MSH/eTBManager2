/***************************************************************
* By Ricardo Memoria - 2011 july 06
****************************************************************/

ALTER TABLE CaseDataPH
 DROP FOREIGN KEY `TBCASEFK`;

ALTER TABLE CaseDataPH ADD CONSTRAINT `TBCASEFK` FOREIGN KEY `TBCASEFK` (`id`)
    REFERENCES TbCase (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

update CaseSideEffect set discriminator='gen';

ALTER TABLE TbCase ADD COLUMN `REGIMEN_INI_ID` INTEGER  DEFAULT NULL;
 
ALTER TABLE TbCase
 ADD CONSTRAINT `FK94DC02DECEBEE212` FOREIGN KEY `FK94DC02DECEBEE212` (`REGIMEN_INI_ID`)
    REFERENCES Regimen (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

update TbCase
set regimen_ini_id = regimen_id;

insert into UserRole (id, changeable, code, Role_Name, internalUse, byCaseClassification)
values (165, false, '010115', 'CASE_TAG', false, true);

update UserRole set internalUse=false where id=155;

INSERT INTO UserPermission
(canchange, canexecute, grantPermission, profile_id, role_id)
select false, false, true, id, 138 from UserProfile
where name like "%adminis%"
and not exists (select * from UserPermission where role_id=138 and profile_id=userprofile.id);

