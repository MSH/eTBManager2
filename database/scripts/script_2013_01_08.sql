/***************************************************************
 * 01-JAN-2013
 * By msantos
 * 
 * For all countryes. Incorporate the comments in orderto the new structure of multiples comments.
 * 
****************************************************************/
INSERT INTO ordercomment(comment, date, statusOnComment, order_id, user_creator_id)
SELECT comments, orderDate, 0, id, user_creator_id FROM medicineorder;

DELETE FROM ordercomment WHERE comment LIKE '';
