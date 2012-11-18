/***************************************************************
* By Ricardo Memoria
* 
* Update ExamDST table by
*  - Including a new discriminator field
*  - Initializing this field
* 
****************************************************************/

alter table examdst
  add discriminator varchar(100) not null;


update examdst set discriminator='gen'
where case_id in (select a.id from tbcase a join patient b on b.id=a.patient_id
join workspace c on c.id=b.workspace_id where c.extension <> 'vi' or c.discriminator is null);

update examdst set discriminator='vi'
where case_id in (select a.id from tbcase a join patient b on b.id=a.patient_id
join workspace c on c.id=b.workspace_id where c.extension = 'vi');