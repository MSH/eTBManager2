/***************************************************************
* By Alex Kurasoff
* 
* Set urban and rural locality types for Azerbaijan patients, if possible
* DONT RUN IT ON PRODUCTION BASE! For AZ developers only!
* This file MUST be in UTF-8
* 
****************************************************************/
UPDATE `etbmanager`.`tbcase` c
inner join etbmanager.administrativeunit au on au.id=c.NOTIF_ADMINUNIT_ID
inner join etbmanager.patient p on p.id = c.PATIENT_ID
SET `NOTIF_LOCALITYTYPE`=1
where p.WORKSPACE_ID ='8' and 
(c.NOTIF_LOCALITYTYPE is NULL) and
((c.NOTIF_ADDRESS like '%kənd%') or
(c.NOTIF_COMPLEMENT like '%kənd%')
);
UPDATE `etbmanager`.`tbcase` c
inner join etbmanager.administrativeunit au on au.id=c.NOTIF_ADMINUNIT_ID
inner join etbmanager.patient p on p.id = c.PATIENT_ID
SET `NOTIF_LOCALITYTYPE`=0
where p.WORKSPACE_ID ='8' and 
(c.NOTIF_LOCALITYTYPE is null) and
(
(c.NOTIF_ADDRESS like '%şəhər%') or
(c.NOTIF_COMPLEMENT like '%şəhər%') or
(c.NOTIF_ADDRESS like '% ş.%') or
(c.NOTIF_COMPLEMENT like '% ş.%') or
(c.NOTIF_ADDRESS like '% şəh.%') or
(c.NOTIF_COMPLEMENT like '% şəh.%')
);