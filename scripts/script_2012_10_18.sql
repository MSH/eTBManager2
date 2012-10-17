/****************************************************************
 * 18-10-2012
 * By MSANTOS
 * 
 * ONLY FOR BRASIL
 * Clean the field tiporesistencia when the case is a TB or NMT case.
 ****************************************************************/

update casedatabr
set tiporesistencia = null
where id in (select id from tbcase where classification in (0,2));

