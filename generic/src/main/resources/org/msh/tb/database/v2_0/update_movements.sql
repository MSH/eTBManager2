/**
 * This script recalculates the stock position by medicine and batch based on each
 * batch movement registered in the system.
 *
 * By Ricardo Memoria
 * August 6th, 2014
 */
drop table if exists tmpbatchmovement;

create table tmpbatchmovement (
  id int not null auto_increment,
  unit_id int not null,
  source_id int not null,
  medicine_id int not null,
  batch_id int not null,
  mov_date date not null,
  recordDate datetime not null,
  quantity int not null,
  total int not null,
  primary key (id)
);

create index idx_tmp on tmpbatchmovement (unit_id, source_id, medicine_id, batch_id, mov_date);

insert into tmpbatchmovement
  select bm.id, m.unit_id, m.source_id, m.medicine_id, bm.batch_id, m.mov_date, m.recordDate, m.oper * bm.quantity
    ,(select sum(m1.oper * bm1.quantity)
      from movement m1
        inner join batchmovement bm1 on bm1.movement_id=m1.id
      where bm1.BATCH_ID=bm.batch_id
            and m1.UNIT_ID=m.unit_id and m1.source_id=m.source_id and m1.medicine_id=m.medicine_id
            and (m1.mov_date < m.mov_date or (m1.mov_date = m.mov_date and m1.recordDate <= m.recordDate))
     ) total
  from batchmovement bm
    inner join movement m on m.id = bm.MOVEMENT_ID
  order by m.unit_id, m.source_id, bm.batch_id, m.mov_date desc, m.recordDate desc;

/* Update batchmovement */
update batchmovement b
  join tmpbatchmovement b2 on b2.id=b.id
set b.availableQuantity = b2.total;


/* update stockquantity */
delete from batchquantity;

insert into batchquantity (quantity, batch_id, source_id, unit_id)
  select total, batch_id, source_id, unit_id
  from tmpbatchmovement t
  where total <> 0
        and t.id = (select t2.id from tmpbatchmovement t2
  where t2.unit_id=t.unit_id and t2.source_id=t.source_id and t2.batch_id=t.batch_id
                    order by t2.mov_date desc, t2.recordDate desc limit 1);


drop table if exists tmpmovement;

create temporary table tmpmovement (
  id int not null auto_increment,
  moviment_id int not null,
  unit_id int not null,
  source_id int not null,
  medicine_id int not null,
  mov_date date not null,
  quantity int not null,
  totalQuantity int not null,
  totalPrice float not null,
  primary key(id)
);

insert into tmpmovement (moviment_id, unit_id, source_id, medicine_id, mov_date, quantity, totalQuantity, totalPrice)
  select m.id, m.unit_id, m.source_id, m.medicine_id, m.mov_date, m.oper * m.quantity,
     (select sum(m1.oper * m1.quantity)
      from movement m1 where m1.unit_id=m.unit_id and m1.source_id=m.source_id
                             and m1.MEDICINE_ID=m.medicine_id
                             and (m1.mov_date < m.mov_date or (m1.mov_date = m.mov_date and m1.recordDate <= m.recordDate))
     ) totalQuantity
    ,(select sum(m1.oper * m1.totalPrice)
      from movement m1 where m1.unit_id=m.unit_id and m1.source_id=m.source_id
                             and m1.MEDICINE_ID=m.medicine_id
                             and (m1.mov_date < m.mov_date or (m1.mov_date = m.mov_date and m1.recordDate <= m.recordDate))
     ) totalPrice
  from movement m
  order by m.unit_id, m.source_id, m.medicine_id, m.mov_date desc, m.recordDate desc;



update movement m
  join tmpmovement m2 on m2.moviment_id = m.id
set m.availableQuantity = m2.totalQuantity,
  m.totalPriceInventory = m2.totalPrice;


update stockposition sp
set quantity = (select sum(b.quantity) from batchquantity b
  inner join batch on batch.id=b.batch_id
where b.unit_id=sp.unit_id and b.source_id=sp.source_id and batch.medicine_id=sp.medicine_id)
where (select sum(b.quantity) from batchquantity b
  inner join batch on batch.id=b.batch_id
where b.unit_id=sp.unit_id and b.source_id=sp.source_id and batch.medicine_id=sp.medicine_id) <> 0;

drop table if exists tmpbatchmovement;
drop table if exists tmpmovement;