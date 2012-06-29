--update patient SET DISCRIMINATOR = 'gen';
--
--update patient SET DISCRIMINATOR = 'bd' where WORKSPACE_ID = 940352;

insert into tbcasebd (bcgScar,id)
select c.bcgScar, c.id from tbcase c, patient p where c.PATIENT_ID = p.id and p.WORKSPACE_ID = 940352;

--update tbcasebd bd SET bd.PATIENT_ID = (select c.PATIENT_ID from tbcase c where bd.id = c.id);

update medicalexamination m set m.DISCRIMINATOR = 'bd' 
where m.CASE_ID in ( select c.id from tbcase c, patient p where p.id = c.patient_id and p.workspace_id = 940352);

drop table medicalexaminationbd;

--alter table casecomment  drop column section;
  

update tbcase c, caseregimen r, patient p SET
  c.iniTreatmentDate = r.iniDate -- date
  ,c.endTreatmentDate = r.endDate
  ,c.iniContinuousPhase = r.iniContPhase -- date
  ,c.REGIMEN_ID = r.REGIMEN_ID -- int(11)
  ,c.REGIMEN_INI_ID = r.REGIMEN_ID -- int(11)
WHERE c.id = r.CASE_ID -- int(11)
and c.PATIENT_ID = p.id
and p.WORKSPACE_ID = 940352



insert into systemconfig(id,allowRegPage,systemMail,systemURL,TBUNIT_ID,USERPROFILE_ID,WORKSPACE_ID,adminMail,buildNumber,buildVersion) values (1,1,'vrao@msh.org','http://www.etbmanager.org',941111,940391,940352,'',null,null);
