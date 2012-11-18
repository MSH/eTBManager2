/***************************************************************
* By Ricardo Memoria - 2012 mar 8th
****************************************************************/

insert into userrole (id, changeable, code, role_name, messagekey, internaluse, bycaseclassification) 
	values (190, false, '010402', 'TREATMENT_UNDO', 'cases.treat.undo', true, true);

delete from userrole where role_name='REM_COM';

update userrole set messageKey='meds.orders.shipment' where id=14;
update userrole set messageKey='meds.orders.receive' where id=15;

update userrole set messageKey='meds.dispensing' where id=16;
update userrole set messageKey='meds.transfer' where id=46;
update userrole set messageKey='meds.transfer.new' where id=145;
update userrole set messageKey='meds.transfer.receive' where id=28;
update userrole set messageKey='meds.transfer.cancel' where id=156;

update userrole set messageKey='meds.start.remove' where id=155;
update userrole set messageKey='meds.start' where id=138;
update userrole set
  messagekey='meds.movs.newadjust',
  code='020100',
  changeable=false
  where id=37;

update userrole set messagekey='meds.movs' where id=27;
update userrole set messagekey='manag.rel2' where id=31;
update userrole set messagekey='manag.rel3' where id=32;
update userrole set messagekey='manag.rel5' where id=35;
update userrole set messagekey='manag' where id=10;
update userrole set messagekey='manag.rel1' where id=30;
update userrole set messagekey='manag.rel4' where id=34;
update userrole set messagekey='manag.case.reports' where id=151;
update userrole set messagekey='manag.export' where id=59;
update userrole set messagekey='manag.forecast.med' where id=60;

update userrole set messagekey='admin.reports' where id=62;
update userrole set messagekey='admin.auorg' where id=120;
