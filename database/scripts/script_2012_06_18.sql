/***************************************************************
* By Ricardo Memoria
* 
* Including a new theme
* Update ntm health units
* 
****************************************************************/

insert into uitheme (name, systemTheme, defaultTheme, path) values ('Light Gray', true, false, 'gray');

update tbunit set ntmHealthUnit=0 where ntmHealthUnit is null;