/***************************************************************
* By Mauricio Santos
* 
* Fix the comments problem in the form of adding medicine to the treatment of a case.
* 
****************************************************************/
ALTER TABLE prescribedmedicine
MODIFY COLUMN comments TEXT CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL;