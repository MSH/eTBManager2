/***************************************************************
* By Ricardo Memoria - 30-jul-2011
* - New discriminator column for ExamXRay table
****************************************************************/

ALTER TABLE ExamXRay ADD COLUMN `DISCRIMINATOR` VARCHAR(100) NOT NULL;

update ExamXRay set discriminator='gen';



