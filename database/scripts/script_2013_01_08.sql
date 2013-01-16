/***************************************************************
 * 01-JAN-2013
 * By msantos
 * 
 * For all countryes. Incorporate the comments in orderto the new structure of multiples comments.
 * 
****************************************************************/
CREATE TABLE  ordercomment (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `comment` LONGTEXT,
  `date` DATETIME NOT NULL,
  `statusOnComment` int(11) NOT NULL,
  `ORDER_ID` int(11) NOT NULL,
  `USER_CREATOR_ID` int(11) NOT NULL,
  PRIMARY KEY (`id`)
);

ALTER TABLE ordercomment ADD CONSTRAINT `FKA7A3351EA7336F7` FOREIGN KEY `FKA7A3351EA7336F7` (`ORDER_ID`)
    REFERENCES `medicineorder` (`id`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT;
    
INSERT INTO ordercomment(comment, date, statusOnComment, order_id, user_creator_id)
SELECT comments, orderDate, 0, id, user_creator_id FROM medicineorder;

DELETE FROM ordercomment WHERE comment LIKE '';
