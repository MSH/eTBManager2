/***************************************************************
* Inclusion of discriminator field in MedicalExamination to allow
* subclassing of MedicalExamination class
* 
* By Ricardo Memoria - 07-feb-2011
****************************************************************/

ALTER TABLE MedicalExamination ADD COLUMN `DISCRIMINATOR` VARCHAR(31);

update MedicalExamination set discriminator='gen';


update MedicalExamination
set discriminator='br'
where case_id in (select c.id from TbCase c
join Patient p on p.id = c.patient_id
and p.workspace_id=19465);

