/***************************************************************************************
* Include discriminator column in exammicroscopy table
* by Ricardo Memoria
* 29/09/2011
****************************************************************************************/

alter table exammicroscopy
add discriminator varchar(20) not null;

update exammicroscopy
 set discriminator = 'gen';

update exammicroscopy
 set discriminator='br'
 where case_id in (select a.id from tbcase a inner join patient p on p.id=a.patient_id
 inner join workspace w on w.id=p.workspace_id
 where w.extension='br');
 