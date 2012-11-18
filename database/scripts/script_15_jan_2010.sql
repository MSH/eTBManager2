alter table ExamXRay modify result int null;


ALTER TABLE CaseRegimen
 DROP FOREIGN KEY `FKC2387DF12F370FE8`;

ALTER TABLE CaseRegimen ADD CONSTRAINT `FKC2387DF12F370FE8` FOREIGN KEY `FKC2387DF12F370FE8` (`REGIMEN_ID`)
    REFERENCES Regimen (`id`)
    ON DELETE SET NULL
    ON UPDATE RESTRICT;

delete from CaseDispensingDays
where not exists (select * from CaseDispensing where CaseDispensing.id = CaseDispensingDays.id);

ALTER TABLE CaseDispensingDays ADD CONSTRAINT `FK_CASEDISPENSING_CSD` FOREIGN KEY `FK_CASEDISPENSING_CSD` (`id`)
    REFERENCES CaseDispensing (`id`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT;

update Patient
set middlename=''
where middlename='0';
