/***************************************************************
* By Mauricio Santos
* 
* Fix the database problem caused by a bug in the dispensing module.
* This bug caused a problem that maintain in the database movements registries related to dispenses that doesn’t exists.
* 
****************************************************************/
delete from movement
where type = 3 and id not in (select movement_id from movements_dispensing);

update stockposition
set quantity = (select sum(a.quantity * a.oper)
from movement a
where a.unit_id=stockposition.unit_id and a.source_id=stockposition.source_id and a.medicine_id=stockposition.medicine_id);

update stockposition
set totalprice = (select sum(b.quantity * a.oper * c.unitPrice)
from batchmovement b
join movement a on a.id=b.movement_id
join batch c on c.id=b.batch_id
where a.unit_id=stockposition.unit_id and a.source_id=stockposition.source_id and a.medicine_id=stockposition.medicine_id);

delete from batchquantity;

insert into batchquantity (batch_id, source_id, unit_id, quantity)
select a.batch_id, b.source_id, b.unit_id, sum(a.quantity * b.oper)
from batchmovement a
join movement b on b.id = a.movement_id
group by a.batch_id, b.source_id, b.unit_id;

delete from batchquantity
where quantity = 0;