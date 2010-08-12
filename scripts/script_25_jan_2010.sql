delete from tbcase
where pulmonary_id not in (select id from fieldvalue);

delete from tbcase
where extrapulmonary_id not in (select id from fieldvalue);

delete from tbcase
where extrapulmonary2_id not in (select id from fieldvalue);

ALTER TABLE TbCase ADD CONSTRAINT `FK94DC02DED9B34E64` FOREIGN KEY `FK94DC02DED9B34E64` (`EXTRAPULMONARY2_ID`)
    REFERENCES `fieldvalue` (`id`)
    ON DELETE SET NULL
    ON UPDATE RESTRICT,
 ADD CONSTRAINT `FK94DC02DE47681D40` FOREIGN KEY `FK94DC02DE47681D40` (`PULMONARY_ID`)
    REFERENCES `fieldvalue` (`id`)
    ON DELETE SET NULL
    ON UPDATE RESTRICT,
 ADD CONSTRAINT `FK94DC02DE82A21950` FOREIGN KEY `FK94DC02DE82A21950` (`EXTRAPULMONARY_ID`)
    REFERENCES `fieldvalue` (`id`)
    ON DELETE SET NULL
    ON UPDATE RESTRICT;
