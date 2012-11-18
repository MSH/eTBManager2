/***************************************************************
* By MSANTOS - 2012-06-14
* 
* Only for SITETB (Brasilian ETBMANAGER)
* It updates the diagnosis date up to the collection date of the first microscopy exam or to the date of the first
* medical examination, if there is no microscopy exam.
* 
****************************************************************/
UPDATE tbcase
SET diagnosisdate = (select min(em.dateCollected)
                      FROM exammicroscopy em
                      WHERE em.dateCollected < tbcase.iniTreatmentDate
                        AND em.result != 0 AND em.result != 6
                        AND em.dateCollected IS NOT NULL
                        AND em.CASE_ID = tbcase.id)
WHERE diagnosisdate is NULL;

UPDATE tbcase
SET diagnosisdate = (select min(me.EVENT_DATE)
                        FROM medicalexamination me
                      WHERE me.EVENT_DATE is not NULL
                        AND me.CASE_ID = tbcase.id)
WHERE diagnosisdate IS NULL;
