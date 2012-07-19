/***************************************************************
* By MSANTOS - 2012-06-12
* 
* Only for SITETB (Brasilian ETBMANAGER)
* It changes the isoniazid and rifampsin monorresistance to monorresistance.
* 
****************************************************************/
UPDATE tbcase
SET drugresistancetype = 0
WHERE drugresistancetype = 5 OR drugresistancetype = 4;
