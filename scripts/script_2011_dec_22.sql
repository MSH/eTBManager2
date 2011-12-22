/***************************************************************************************
* Adjust transaction log in the computer
* by Ricardo Memoria
* 24/11/2011
****************************************************************************************/

DELIMITER $$

CREATE PROCEDURE sp_add_new_col()
BEGIN
IF NOT EXISTS
(
SELECT NULL
FROM information_schema.columns
WHERE table_schema ='etbmanager' AND table_name ='transactionlog' AND column_name
='comments'
)
THEN
ALTER TABLE transactionlog ADD COLUMN `comments` LONGTEXT ;
END IF;
END$$

DELIMITER ;

CALL sp_add_new_col();

DROP PROCEDURE sp_add_new_col;

alter table transactionlog
   add column UNIT_ID int,
   add column ADMINUNIT_ID int;

alter table transactionlog
   drop column entityClass,
   drop column hasPrevValues,
   drop column numValues,
   drop column caseClassification;
   
ALTER TABLE transactionlog ADD CONSTRAINT `FK3C3C7BA671E04A4B` FOREIGN KEY `FK3C3C7BA671E04A4B` (`UNIT_ID`)
    REFERENCES `tbunit` (`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK3C3C7BA6CEDEEA7C` FOREIGN KEY `FK3C3C7BA6CEDEEA7C` (`ADMINUNIT_ID`)
    REFERENCES `administrativeunit` (`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;


update transactionlog
set unit_id =
(select tbunit_id from userworkspace a
where a.user_id=transactionlog.userlog_id and a.workspace_id=transactionlog.workspacelog_id limit 1)
where unit_id is null;

update transactionlog
set adminunit_id =
(select b.adminunit_id from userworkspace a
join tbunit b on b.id = a.tbunit_id
where a.user_id=transactionlog.userlog_id and a.workspace_id=transactionlog.workspacelog_id limit 1)
where adminunit_id is null;

insert into 