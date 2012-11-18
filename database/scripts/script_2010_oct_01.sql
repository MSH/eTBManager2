ALTER TABLE ExamCulture
 ADD COLUMN `CASE_ID` INT(11) AFTER `sampleType`,
 ADD COLUMN `sampleNumber` VARCHAR(50) AFTER `CASE_ID`,
 ADD COLUMN `dateCollected` DATE AFTER `sampleNumber`;

ALTER TABLE ExamCulture
 ADD CONSTRAINT `FKB2F04C6F8B327BA` FOREIGN KEY `FKB2F04C6F8B327BA` (`CASE_ID`)
    REFERENCES TbCase (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;
    
update ExamCulture, PatientSample
set ExamCulture.sampleNumber = PatientSample.sampleNumber,
ExamCulture.dateCollected = PatientSample.dateCollected,
ExamCulture.case_id = PatientSample.case_id
where ExamCulture.id = PatientSample.examCulture_id;

ALTER TABLE ExamSputumSmear
 ADD COLUMN `CASE_ID` INT(11) AFTER `sampleType`,
 ADD COLUMN `sampleNumber` VARCHAR(50) AFTER `CASE_ID`,
 ADD COLUMN `dateCollected` DATE AFTER `sampleNumber`;

ALTER TABLE ExamSputumSmear
 ADD CONSTRAINT `FKC6929FC38B327BA` FOREIGN KEY `FKA5F78C098B327BA` (`CASE_ID`)
    REFERENCES TbCase (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FKC6929FC366A4104C` FOREIGN KEY `FKC6929FC366A4104C` (`LABORATORY_ID`)
    REFERENCES Laboratory (`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FKC6929FC38861542` FOREIGN KEY `FKC6929FC38861542` (`METHOD_ID`)
    REFERENCES FieldValue (`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
 DROP FOREIGN KEY `FKA5F78C0966A4104C`,
 DROP FOREIGN KEY `FKA5F78C098861542`;

update ExamSputumSmear, PatientSample
set ExamSputumSmear.sampleNumber = PatientSample.sampleNumber,
ExamSputumSmear.dateCollected = PatientSample.dateCollected,
ExamSputumSmear.case_id = PatientSample.case_id
where ExamSputumSmear.id = PatientSample.examSputumSmear_id;

ALTER TABLE ExamSusceptibilityTest
 ADD COLUMN `CASE_ID` INT(11),
 ADD COLUMN `sampleNumber` VARCHAR(50) ,
 ADD COLUMN `dateCollected` DATE ;

ALTER TABLE ExamSusceptibilityTest
 ADD CONSTRAINT `FK145D01068B327BA` FOREIGN KEY `FKD76A756C8B327BA` (`CASE_ID`)
    REFERENCES TbCase (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK145D010666A4104C` FOREIGN KEY `FK145D010666A4104C` (`LABORATORY_ID`)
    REFERENCES Laboratory (`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK145D01068861542` FOREIGN KEY `FK145D01068861542` (`METHOD_ID`)
    REFERENCES FieldValue (`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
 DROP FOREIGN KEY `FKD76A756C66A4104C`,
 DROP FOREIGN KEY `FKD76A756C8861542`;

ALTER TABLE ExamSusceptibilityResult
 ADD CONSTRAINT `FK4FF058C3B7FBE608` FOREIGN KEY `FK4FF058C3B7FBE608` (`SUBSTANCE_ID`)
    REFERENCES Substance (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 ADD CONSTRAINT `FK4FF058C3CB45202F` FOREIGN KEY `FK4FF058C3CB45202F` (`EXAM_ID`)
    REFERENCES ExamSusceptibilityTest (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
 DROP FOREIGN KEY `FKA3392A7779C49EF9`,
 DROP FOREIGN KEY `FKA3392A77B7FBE608`;
 
update ExamSusceptibilityTest, PatientSample
set ExamSusceptibilityTest.sampleNumber = PatientSample.sampleNumber,
ExamSusceptibilityTest.dateCollected = PatientSample.dateCollected,
ExamSusceptibilityTest.case_id = PatientSample.case_id
where ExamSusceptibilityTest.id = PatientSample.examSusceptibilityTest_id;


ALTER TABLE ExamSusceptibilityTest RENAME TO ExamDST;
ALTER TABLE ExamSusceptibilityResult RENAME TO ExamDSTResult;
ALTER TABLE ExamSputumSmear RENAME TO ExamMicroscopy;

DROP TABLE PatientSample;
