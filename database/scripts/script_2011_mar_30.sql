/***************************************************************
* By Ricardo Memoria - 30-mar-2011
****************************************************************/

insert into userrole (id, changeable, code, role_name, internaluse, bycaseclassification)
values (162, true, '042000', 'TAGS', false, false);


INSERT INTO UserPermission
(canchange, canexecute, grantPermission, profile_id, role_id)
select true, true, true, id, 162 from userprofile
where name like "%adminis%"
and not exists (select * from UserPermission where role_id=162 and profile_id=userprofile.id);


CREATE TABLE  Tag (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tag_name` varchar(100) NOT NULL,
  `workspace` tinyblob,
  `WORKSPACE_ID` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1477AB9C757E8` (`WORKSPACE_ID`),
  CONSTRAINT `FK1477AB9C757E8` FOREIGN KEY (`WORKSPACE_ID`) REFERENCES `workspace` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;


CREATE TABLE  `TAGS_CASE` (
  `CASE_ID` int(11) NOT NULL,
  `TAG_ID` int(11) NOT NULL,
  KEY `FK4577A7968B327BA` (`CASE_ID`),
  KEY `FK4577A796DAFE1048` (`TAG_ID`),
  CONSTRAINT `FK4577A796DAFE1048` FOREIGN KEY (`TAG_ID`) REFERENCES Tag (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK4577A7968B327BA` FOREIGN KEY (`CASE_ID`) REFERENCES TbCase (`id`)
);

