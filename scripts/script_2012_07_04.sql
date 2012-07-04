/***************************************************************
* By Alex Kurasoff
* 
* Allow see error log report for UA national administrator
* DONT RUN IT ON PRODUCTION BASE! For UA developers only!
* 
****************************************************************/
INSERT INTO `etbmanager`.`userpermission` (`canChange`, `canExecute`, `grantPermission`, `PROFILE_ID`, `ROLE_ID`) VALUES (1, 1, 1, 940356, 167);