update examculture set sample_id = null;
update exammicroscopy set sample_id = null;
update examdst set sample_id = null;
update examxpert set sample_id = null;

delete from patientsample;

/* Create samples from microscopy */
insert into patientsample (datecollected, sampleNumber, case_id)
select datecollected, sampleNumber, case_id
from exammicroscopy
where (datecollected is not null or (samplenumber is not null and samplenumber <> ''))
group by datecollected, samplenumber, case_id;

/* Update sample ID in microscopy */
update exammicroscopy
set sample_id = (select id from patientsample s
where s.dateCollected = exammicroscopy.dateCollected
and s.sampleNumber = exammicroscopy.sampleNumber
and s.case_id = exammicroscopy.CASE_ID limit 1);

/* Update sample ID in microscopy */
update exammicroscopy
set sample_id = (select id from patientsample s
where s.dateCollected = exammicroscopy.dateCollected
and s.sampleNumber is null
and s.case_id = exammicroscopy.CASE_ID limit 1)
where sampleNumber is null;

/* create samples from culture */
insert into patientsample (datecollected, sampleNumber, case_id)
select datecollected, sampleNumber, case_id
from examculture s
where not exists (select * from patientsample p where p.datecollected=s.datecollected
and p.sampleNumber=s.sampleNumber and p.case_id=s.case_id)
and (datecollected is not null or (samplenumber is not null and samplenumber <> ''))
group by datecollected, samplenumber, case_id;

/* Update sample ID in culture */
update examculture
set sample_id = (select id from patientsample s
where s.dateCollected = examculture.dateCollected
and s.sampleNumber = examculture.sampleNumber
and s.case_id = examculture.CASE_ID limit 1);

/* Update sample ID in culture where sample number is null */
update examculture
set sample_id = (select id from patientsample s
where s.dateCollected = examculture.dateCollected
and s.sampleNumber is null
and s.case_id = examculture.CASE_ID limit 1)
where sampleNumber is null;

/* create samples from dst */
insert into patientsample (datecollected, sampleNumber, case_id)
select datecollected, sampleNumber, case_id
from examdst s
where not exists (select * from patientsample p where p.datecollected=s.datecollected
and p.sampleNumber=s.sampleNumber and p.case_id=s.case_id)
and (datecollected is not null or (samplenumber is not null and samplenumber <> ''))
group by datecollected, samplenumber, case_id;

/* Update sample ID in dst */
update examdst
set sample_id = (select id from patientsample s
where s.dateCollected = examdst.dateCollected
and s.sampleNumber = examdst.sampleNumber
and s.case_id = examdst.CASE_ID limit 1);

/* Update sample ID in dst */
update examdst
set sample_id = (select id from patientsample s
where s.dateCollected = examdst.dateCollected
and s.sampleNumber is null
and s.case_id = examdst.CASE_ID limit 1)
where sampleNumber is null;

/* create samples from xpert */
insert into patientsample (datecollected, sampleNumber, case_id)
select datecollected, sampleNumber, case_id
from examxpert s
where not exists (select * from patientsample p where p.datecollected=s.datecollected
and p.sampleNumber=s.sampleNumber and p.case_id=s.case_id)
and (datecollected is not null or (samplenumber is not null and samplenumber <> ''))
group by datecollected, samplenumber, case_id;

/* Update sample ID in xpert exam */
update examxpert
set sample_id = (select id from patientsample s
where s.dateCollected = examxpert.dateCollected
and s.sampleNumber = examxpert.sampleNumber
and s.case_id = examxpert.CASE_ID limit 1);


/* Update sample ID in xpert exam */
update examxpert
set sample_id = (select id from patientsample s
where s.dateCollected = examxpert.dateCollected
and s.sampleNumber is null
and s.case_id = examxpert.CASE_ID limit 1)
where sampleNumber is null;