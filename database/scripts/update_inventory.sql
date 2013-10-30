/***************************************************************
* By Ricardo Memoria
*
* Update the quantity available of medicine and batches in the inventory 
* 
****************************************************************/

delete from batchquantity;

insert into batchquantity (batch_id, source_id, unit_id, quantity)
select bm.batch_id, m.source_id, m.unit_id, sum(bm.quantity * m.oper)
from batchmovement bm
inner join movement m on m.id=bm.movement_id
group by bm.batch_id, m.source_id, m.unit_id
having sum(bm.quantity * m.oper) <> 0;

update stockposition
set quantity = (select sum(m.quantity * m.oper)
from movement m where m.source_id=stockposition.source_id
and m.unit_id=stockposition.unit_id
and m.medicine_id=stockposition.medicine_id);