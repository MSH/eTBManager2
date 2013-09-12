package org.msh.tb.az;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import jxl.Cell;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.jboss.seam.annotations.Name;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.PatientType;

@Name("importIMM")
public class ImportIMM {
	private InputStream file;
	
	public boolean execute() {
		try {
			Workbook workbook = Workbook.getWorkbook(new File("c://test.xls"));
			Sheet sheet = workbook.getSheet(0);
			for (int i = 2; i < sheet.getRows(); i++) {
				Cell[] r = sheet.getRow(i);
				importRecord(r);
			}
		
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	private void importRecord(Cell[] r) {
		TbCaseAZ tc = new TbCaseAZ();
		Patient p = new Patient();
		p.setSecurityNumber(r[1].getContents());
		//SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", LocaleSelector.instance().getLocale());
		
		try {
			DateCell dc = (DateCell)r[2];
			p.setBirthDate(dc.getDate());
			
			dc = (DateCell)r[3];
			tc.setRegistrationDate(dc.getDate());
			tc.setDiagnosisDate(dc.getDate());
			
			p.setLastName(r[4].getContents());
			p.setName(r[5].getContents());
			p.setMiddleName(r[6].getContents());
			
			p.setMotherName(r[7].getContents());
			
			String s = r[8].getContents();
			if ("M".equals(s))
				p.setGender(Gender.MALE);
			if ("F".equals(s))
				p.setGender(Gender.FEMALE);
			
			s = r[9].getContents();
			if ("N".equals(s))
				tc.setPatientType(PatientType.NEW);
			if ("T".equals(s))
				tc.setPatientType(PatientType.AFTER_DEFAULT);
			if ("R".equals(s))
				tc.setPatientType(PatientType.RELAPSE);
			if ("F".equals(s))
				tc.setPatientType(PatientType.FAILURE_FT);
			if ("O".equals(s))
				tc.setPatientType(PatientType.OTHER);
			
			//TODO r[10] notifUnit
			
			s = r[11].getContents();
			if (s.length() == 2)
				tc.setReferToOtherTBUnit(false);
			else
				tc.setReferToOtherTBUnit(true);
			
			//TODO r[12] infectionSite
			
			
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setFile(InputStream file) {
		this.file = file;
	}

	public InputStream getFile() {
		return file;
	}
}
