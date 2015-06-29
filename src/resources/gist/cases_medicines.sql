/**
 * Return the list of patients and its prescribed medicines in a colunar way.
 *
 * Country: Namibia
 */
select p.lastname, p.PATIENT_NAME, p.securityNumber as caseNumber, c.caseNumber as caseNumberSeq,
p.birthDate, p.gender, u.name1, r.regimen_name,
(exists (select * from prescribedmedicine pm where pm.medicine_id=940908 and pm.case_id=c.id)) as 'Km IGm inj',
(exists (select * from prescribedmedicine pm where pm.medicine_id=940910 and pm.case_id=c.id)) as 'Eto 250 mg',
(exists (select * from prescribedmedicine pm where pm.medicine_id=940911 and pm.case_id=c.id)) as 'Cs 250 mg',
(exists (select * from prescribedmedicine pm where pm.medicine_id=940912 and pm.case_id=c.id)) as 'PAS 4 gm',
(exists (select * from prescribedmedicine pm where pm.medicine_id=940915 and pm.case_id=c.id)) as 'CM 1 gm',
(exists (select * from prescribedmedicine pm where pm.medicine_id=940916 and pm.case_id=c.id)) as 'Amx/Clv 1000 mg',
(exists (select * from prescribedmedicine pm where pm.medicine_id=940917 and pm.case_id=c.id)) as 'Lfx 250 mg',
(exists (select * from prescribedmedicine pm where pm.medicine_id=940918 and pm.case_id=c.id)) as 'E 400 mg',
(exists (select * from prescribedmedicine pm where pm.medicine_id=940919 and pm.case_id=c.id)) as 'Z 500 mg',
(exists (select * from prescribedmedicine pm where pm.medicine_id=940920 and pm.case_id=c.id)) as 'R 450 mg',
(exists (select * from prescribedmedicine pm where pm.medicine_id=940921 and pm.case_id=c.id)) as 'Pyr 25 mg',
(exists (select * from prescribedmedicine pm where pm.medicine_id=940933 and pm.case_id=c.id)) as 'Clr 500 mg',
(exists (select * from prescribedmedicine pm where pm.medicine_id=940934 and pm.case_id=c.id)) as 'Cfz 100 mg',
(exists (select * from prescribedmedicine pm where pm.medicine_id=940935 and pm.case_id=c.id)) as 'Z 400 mg',
(exists (select * from prescribedmedicine pm where pm.medicine_id=940936 and pm.case_id=c.id)) as 'R 150 mg',
(exists (select * from prescribedmedicine pm where pm.medicine_id=941004 and pm.case_id=c.id)) as 'H 300mg 1',
(exists (select * from prescribedmedicine pm where pm.medicine_id=941268 and pm.case_id=c.id)) as 'Mfx 400mg tablet'
from tbcase c
inner join patient p on p.id=c.patient_id
inner join tbunit u on u.id=c.owner_unit_id
left join regimen r on r.id=c.REGIMEN_ID
where p.workspace_id=24;


/** Gera o SQL que deve ficar no SELECT, para geração das colunas dos medicamentos  */
select concat('(exists (select * from prescribedmedicine pm where pm.medicine_id=',id,' and pm.case_id=c.id)) as ',
'\'', abbrevname, ' ', strength, ' ', strengthUnit, '\''),
id, abbrevname, strength, strengthunit
from medicine;
