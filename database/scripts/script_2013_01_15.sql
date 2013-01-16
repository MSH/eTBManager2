/************************************************
 *  Generic + All countries
 * Jan-15-2013
 * End User License Implementation
*************************************************/

alter table sys_user add ulaAccepted boolean;
alter table workspace add ulaActive boolean;

update sys_user SET ulaAccepted = 0;

update workspace SET ulaActive = 0;