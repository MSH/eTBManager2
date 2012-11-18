/***************************************************************
* By Mauricio Santos - 2012 mar 30th
****************************************************************/

/*ONLY FOR BRAZIL
**SCRIPT Direcionado ao sistema que roda no Brasil
**Workspace Brasil (Aqueles que utilizam a tabela casedatabr)

**Confira a lista abaixo.
**O primeiro numero correponde ao id do tipo de resistencia
**dentro do enum, o segundo representa ao id no casedatabr.
**ATENÇÃO: Antes de executar o script verificar se o segundo
**numero abaixo corresponde ao id cadastrado no fieldvalue
**para tal tipo de resistencia, caso não corresponda, a
**cláusula WHERE dos scripts abaixo precisarão ser atualizadas.

***1 - 938342 e 939027	Poliresistência,
***2 - 938343 e 939028	Multiresistência,
***3 - 938344 e 939029	Resistência extensiva,
***4 - 938340 e 939025  Monoresistencia à Rifampicina,
***5 - 938341 e 939026	Monoresistencia à Isoniasida,

**Utilize o primeiro SELECT para conferir os ids.

*/;

SELECT f.id as IdResistencia, f.name1 as Resistencia, w.name1 as Workspace
FROM fieldvalue f
INNER JOIN workspace w ON w.id = f.WORKSPACE_ID
WHERE field = 20;

/*A coluno caseResistencia é a coluna que será populada,
**Neste momento ela deve aparecer nula ou com 0.
**Para conferir o sucesso do script, o total de registros
**que este SELECT retorna deve ser igual a soma do
**total de registros que cada UPDATE vai retornar.
*/
SELECT c.id as caseID, c.drugResistanceType as caseRESISTENCIA, cbr.id as brID, cbr.RESISTANCETYPE as brRESISTENCIA, f.name1 as fieldNOMERESISTENCIA
FROM casedatabr cbr
INNER JOIN fieldvalue f ON f.id = cbr.RESISTANCETYPE
INNER JOIN tbcase c ON c.id = cbr.id;

UPDATE tbcase c
INNER JOIN casedatabr cbr ON cbr.id = c.id
SET c.drugResistanceType = 1
WHERE cbr.RESISTANCETYPE = 938342 OR cbr.RESISTANCETYPE = 939027
/*1 - 938342 e 939027	POLY_RESISTANCE*/;

UPDATE tbcase c
INNER JOIN casedatabr cbr ON cbr.id = c.id
SET c.drugResistanceType = 2
WHERE cbr.RESISTANCETYPE = 938343 OR cbr.RESISTANCETYPE = 939028
/*2 - 938343 e 939028	MULTIDRUG_RESISTANCE,*/;

UPDATE tbcase c
INNER JOIN casedatabr cbr ON cbr.id = c.id
SET c.drugResistanceType = 3
WHERE cbr.RESISTANCETYPE = 938344 OR cbr.RESISTANCETYPE = 939029
/*3 - 938344 e 939029	EXTENSIVEDRUG_RESISTANCE*/;

UPDATE tbcase c
INNER JOIN casedatabr cbr ON cbr.id = c.id
SET c.drugResistanceType = 4
WHERE cbr.RESISTANCETYPE = 938340 OR cbr.RESISTANCETYPE = 939025
/*4 - 938340 e 939025 	RIFAMPICIN_MONO_RESISTANCE*/;

UPDATE tbcase c
INNER JOIN casedatabr cbr ON cbr.id = c.id
SET c.drugResistanceType = 5
WHERE cbr.RESISTANCETYPE = 938341 OR cbr.RESISTANCETYPE = 939026
/*5 - 938341 e 939026	ISONIAZID_MONO_RESISTANCE;*/;

SELECT c.id as caseID, c.drugResistanceType as caseRESISTENCIA, cbr.id as brID, cbr.RESISTANCETYPE as brRESISTENCIA, f.name1 as fieldNOMERESISTENCIA
FROM casedatabr cbr
INNER JOIN fieldvalue f ON f.id = cbr.RESISTANCETYPE
INNER JOIN tbcase c ON c.id = cbr.id;


/*OUTCOME_RESISTANCETYPE
**Atualizando a coluna que armazena o tipo de resistencia
**caso o desfeicho do caso tenha sido uma evolução para TBDR
*/
SELECT *
FROM casedatabr
WHERE OUTCOME_RESISTANCETYPE IS NOT NULL;

ALTER TABLE casedatabr
ADD COLUMN OUTCOME_RESISTANCETYPE2 INT(11);

UPDATE casedatabr cbr
SET cbr.OUTCOME_RESISTANCETYPE2 = 1
WHERE cbr.OUTCOME_RESISTANCETYPE = 938342 OR cbr.OUTCOME_RESISTANCETYPE = 939027
/*1 - 938342 e 939027	POLY_RESISTANCE*/;

UPDATE casedatabr cbr
SET cbr.OUTCOME_RESISTANCETYPE2 = 2
WHERE cbr.OUTCOME_RESISTANCETYPE = 938343 OR cbr.OUTCOME_RESISTANCETYPE = 939028
/*2 - 938343 e 939028	MULTIDRUG_RESISTANCE,*/;

UPDATE casedatabr cbr
SET cbr.OUTCOME_RESISTANCETYPE2 = 3
WHERE cbr.OUTCOME_RESISTANCETYPE = 938344 OR cbr.OUTCOME_RESISTANCETYPE = 939029
/*3 - 938344 e 939029	EXTENSIVEDRUG_RESISTANCE*/;

UPDATE casedatabr cbr
SET cbr.OUTCOME_RESISTANCETYPE2 = 4
WHERE cbr.OUTCOME_RESISTANCETYPE = 938340 OR cbr.OUTCOME_RESISTANCETYPE = 939025
/*4 - 938340 e 939025 	RIFAMPICIN_MONO_RESISTANCE*/;

UPDATE casedatabr cbr
SET cbr.OUTCOME_RESISTANCETYPE2 = 5
WHERE cbr.OUTCOME_RESISTANCETYPE = 938341 OR cbr.OUTCOME_RESISTANCETYPE = 939026
/*5 - 938341 e 939026	ISONIAZID_MONO_RESISTANCE;*/;

SELECT *
FROM casedatabr
WHERE OUTCOME_RESISTANCETYPE2 IS NOT NULL;

/*Alterando a estrutura*/
ALTER TABLE casedatabr
DROP FOREIGN KEY FKDF8310AAFF7F75AD;

ALTER TABLE casedatabr
DROP COLUMN RESISTANCETYPE;

ALTER TABLE casedatabr
DROP FOREIGN KEY FKDF8310AA7E2A0E3A;

ALTER TABLE casedatabr
DROP COLUMN OUTCOME_RESISTANCETYPE;

ALTER TABLE casedatabr
CHANGE OUTCOME_RESISTANCETYPE2 OUTCOME_RESISTANCETYPE INT(11);

DELETE FROM fieldvalue
WHERE field LIKE '20';

