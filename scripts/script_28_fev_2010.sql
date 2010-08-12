insert into WorkspaceView (id)
select id from workspace;

update WorkspaceView set logoimage = null;

update WorkspaceView set logoimage = 'sitetb.gif'
where id = 19465;

/* adjust new state -> transferring */
update TbCase set state = state + 1
where state > 1;

alter table TreatmentHealthUnit
add column transferring BIT(1);

update TreatmentHealthUnit
set transferring = 0;
