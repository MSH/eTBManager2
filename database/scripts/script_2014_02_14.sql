/***************************************************************
 * 14-feb-2014
 * By MSANTOS
 * 
 * BD only!!!
 * 
 * Changes an incompatible FK
****************************************************************/
ALTER TABLE casedispensing DROP FOREIGN KEY FKE4CB224A8B327BA;

ALTER TABLE casedispensing 
  ADD CONSTRAINT FKA9B27A0AA3F04EEB
  FOREIGN KEY (CASE_ID)
  REFERENCES tbcase(id)
  ON DELETE CASCADE
  ON UPDATE RESTRICT;


