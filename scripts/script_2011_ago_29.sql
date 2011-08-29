/***************************************************************
* New model for movements and stock quantity
* By Ricardo Memoria
****************************************************************/

ALTER TABLE movement CHANGE COLUMN `unitPrice` `totalPrice` FLOAT NOT NULL;

create temporary table temp_stockposition
select a.id
from stockposition a
join (select unit_id, source_id, medicine_id, max(stock_date) lastdate
from stockposition group by unit_id,source_id,medicine_id) sp
 on a.unit_id=sp.unit_id and a.source_id=sp.source_id and a.medicine_id=sp.medicine_id and a.stock_date=sp.lastdate;
 
delete from stockposition
where id not in (select id from temp_stockposition);

drop table temp_stockposition;

alter table stockposition drop column stock_date;

delete from batchmovement where quantity is null

delete from movement
where not exists(select * from batchmovement a
where a.movement_id=movement.id);

update movement
set quantity = (select sum(b.quantity)
from batchmovement b
where b.movement_id = movement.id);

update movement
set totalprice = (select sum(b.quantity * c.unitprice)
from batchmovement b
join batch c on c.id=b.batch_id
where b.movement_id = movement.id);

alter table movement
drop column stockquantity;

delete from stockposition
where not exists(select * from movement b
where b.unit_id=stockposition.unit_id and b.source_id=stockposition.source_id and b.medicine_id=stockposition.medicine_id);

update stockposition
set quantity = (select sum(a.quantity * a.oper)
from movement a
where a.unit_id=stockposition.unit_id and a.source_id=stockposition.source_id and a.medicine_id=stockposition.medicine_id);

update stockposition
set totalprice = (select sum(a.quantity * a.oper * c.unitPrice)
from batchmovement b
join movement a on a.id=b.movement_id
join batch c on c.id=b.batch_id
where a.unit_id=stockposition.unit_id and a.source_id=stockposition.source_id and a.medicine_id=stockposition.medicine_id);

ALTER TABLE stockposition ADD COLUMN `lastMovement` DATE NOT NULL;

update stockposition
set lastmovement = (select max(mov_date) from movement a 
where a.unit_id=stockposition.unit_id and a.source_id=stockposition.source_id and a.medicine_id=stockposition.medicine_id);
