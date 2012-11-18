/***************************************************************
* By Vani Rao
* 
* Country - Cambodia
* Contact Evaluation
* @Table TbContact_Kh
* Setting the bits to 0 for the newly introduced columns  
* 

****************************************************************/

ALTER TABLE tbcontact_kh drop column sampleSentForTest;

UPDATE tbcontact_kh SET sampleSentForCultureTest = 0;
UPDATE tbcontact_kh SET sampleSentForDSTTest = 0;