/***************************************************************************************
*
****************************************************************************************/

DROP TABLE IF EXISTS `uitheme`;
CREATE TABLE  `etbmanager`.`uitheme` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `systemTheme` bit(1) NOT NULL,
  `url` varchar(255) DEFAULT NULL,
  `defaultTheme` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE sys_user
add THEME_ID int(11) DEFAULT NULL;

ALTER TABLE sys_user
add CONSTRAINT `FK74A81DFD26183FC3` FOREIGN KEY (`THEME_ID`) REFERENCES `uitheme` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;


insert into uitheme (id, name, systemtheme, url, defaulttheme) values (1, 'Default', true, 'green2', true);
insert into uitheme (id, name, systemtheme, url, defaulttheme) values (2, 'Light blue', true, 'blue', false);
