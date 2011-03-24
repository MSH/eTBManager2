package org.msh.tb.test.dbgen;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.msh.mdrtb.entities.Address;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.CaseComorbidity;
import org.msh.mdrtb.entities.CaseSideEffect;
import org.msh.mdrtb.entities.ExamCulture;
import org.msh.mdrtb.entities.ExamDST;
import org.msh.mdrtb.entities.ExamDSTResult;
import org.msh.mdrtb.entities.ExamHIV;
import org.msh.mdrtb.entities.ExamMicroscopy;
import org.msh.mdrtb.entities.ExamXRay;
import org.msh.mdrtb.entities.FieldValue;
import org.msh.mdrtb.entities.Laboratory;
import org.msh.mdrtb.entities.MedicalExamination;
import org.msh.mdrtb.entities.MedicineComponent;
import org.msh.mdrtb.entities.MedicineRegimen;
import org.msh.mdrtb.entities.Patient;
import org.msh.mdrtb.entities.PrescribedMedicine;
import org.msh.mdrtb.entities.Regimen;
import org.msh.mdrtb.entities.Substance;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.TbContact;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.TreatmentHealthUnit;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.CultureResult;
import org.msh.mdrtb.entities.enums.DiagnosisType;
import org.msh.mdrtb.entities.enums.DrugResistanceType;
import org.msh.mdrtb.entities.enums.DstResult;
import org.msh.mdrtb.entities.enums.Gender;
import org.msh.mdrtb.entities.enums.HIVResult;
import org.msh.mdrtb.entities.enums.InfectionSite;
import org.msh.mdrtb.entities.enums.LocalityType;
import org.msh.mdrtb.entities.enums.MedAppointmentType;
import org.msh.mdrtb.entities.enums.MicroscopyResult;
import org.msh.mdrtb.entities.enums.Nationality;
import org.msh.mdrtb.entities.enums.NumTreatments;
import org.msh.mdrtb.entities.enums.RegimenPhase;
import org.msh.mdrtb.entities.enums.ValidationState;
import org.msh.mdrtb.entities.enums.XRayEvolution;
import org.msh.mdrtb.entities.enums.YesNoType;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


/**
 * Generates cases for database testing from a set of preferences
 * @author Ricardo Memoria
 *
 */
@Name("caseGenerator")
public class CaseGenerator {

	@In(create=true) EntityManager entityManager;
	@In(create=true) SaveCaseAction saveCase;

	private Random random = new Random();
	private Patient patient;
	private TbCase tbcase;
	private int caseNum;
	private int counter;
	private AgeRangeInfo ageRangeInfo;
	private Workspace defaultWorkspace;
	private GeneratorPreferences preferences;
	private Date endDate;
	private Date dateFirstCulture;

	/**
	 * Execute the database generator based on the preferences in the preferenceGenerator property
	 * @return "success" if executed successfully
	 */
	@Asynchronous
	public String execute(Workspace defaultWorkspace, GeneratorPreferences preferences) {
		this.defaultWorkspace = entityManager.merge(defaultWorkspace);
		this.preferences = preferences;
		
		if (preferences.isRemExistingCases())
			saveCase.removeCases(preferences, defaultWorkspace);
		
		for (CaseInfo caseInfo: preferences.getCases()) {
			generateCases(caseInfo);
		}
		return "success";
	}

	
	/**
	 * Generates TB and MDR cases for the corresponding year
	 * @param caseInfo - Information about quantity of cases to be generated
	 */
	protected void generateCases(CaseInfo caseInfo) {
		counter = 0;

		Map<CaseClassification, Integer> caseType = new HashMap<CaseClassification, Integer>();
		caseType.put(CaseClassification.TB, caseInfo.getNumTBCases());
		caseType.put(CaseClassification.DRTB, caseInfo.getNumMDRTBCases());
		
		while ((!caseInfo.isAllMDRTBGenerated()) || (!caseInfo.isAllTBGenerated())) {
			// choose a case type
			CaseClassification cla = newValue(caseType);
			if (cla == null)
				cla = CaseClassification.TB;
			
			if (caseInfo.isAllMDRTBGenerated())
				cla = CaseClassification.TB;
			else
			if (caseInfo.isAllTBGenerated())
				cla = CaseClassification.DRTB;

			generatePatientCases(caseInfo.getYear(), cla);
		}
	}

	/**
	 * Generates cases for a new patient for a given year
	 * @param caseInfo - Base year
	 * @param classif - TB or MDR-TB case
	 */
	protected void generatePatientCases(int year, CaseClassification classif) {
		patient = createPatient(year);
		NumTreatments numTreats = newValue(preferences.getNumTreatments());
		
		int num = 0;
		switch (numTreats) {
		case ONE_TREATMENT:
			num = 1;
			break;
		case TWO_TREATMENTS:
			num = 2;
			break;
		case THREE_TREATMENTS:
			num = 3;
			break;
		default:
			num = 1;
			break;
		}
		
		if (num == 0)
			return;
		
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, random.nextInt(12));
		c.set(Calendar.DAY_OF_MONTH, random.nextInt(28)+1);
		Date dt = c.getTime();
		
		for (int i = 0; i < num; i++) {
			caseNum = i + 1;
			if (generateCase(dt, classif)) {
				// if patient died, doens't create any other case for him
				if (tbcase.getState() == CaseState.DIED)
					return;
				dt = tbcase.getOutcomeDate();
				if (dt == null)
					break;
			}
			
			int months;
			if (classif == CaseClassification.TB)
				 months = random.nextInt(12) + 3;
			else months = random.nextInt(24) + 3;
			DateUtils.incMonths(dt, months);
		}
	}
	


	/**
	 * Generate a new case
	 * @param year - year the case will be generated
	 * @param classif - type of case (TB or MDR-TB)
	 */
	@Transactional
	protected boolean generateCase(Date caseDate, CaseClassification classif) {
		int year = DateUtils.yearOf(caseDate);
		CaseInfo caseInfo = findCaseInfo(year);
		if (caseInfo == null)
			return false;
		
		tbcase = new TbCase();

		if (patient.getId() != null)
			patient = entityManager.merge(patient);

		// generate registration date
		int numDays = newRangeValue(preferences.getVarDaysRegDiag());
		Date dt = DateUtils.incDays(caseDate, -numDays);
		tbcase.setRegistrationDate(dt);
		
		tbcase.setDiagnosisDate(caseDate);
		tbcase.setPatient(patient);
		tbcase.setClassification(classif);
		tbcase.setAge(DateUtils.yearsBetween(patient.getBirthDate(), caseDate));
		tbcase.setCaseNumber(caseNum);
		tbcase.setValidationState(ValidationState.VALIDATED);
		
		if (classif == CaseClassification.TB)
			tbcase.setPatientType(newValue(preferences.getPatientTypesTB()));
		else tbcase.setPatientType(newValue(preferences.getPatientTypesMDR()));
		tbcase.setInfectionSite(newValue(preferences.getInfectionSites()));
		
		if (classif == CaseClassification.DRTB)
			 tbcase.setDiagnosisType(newValue(preferences.getMdrDiagnosis()));
		else tbcase.setDiagnosisType(DiagnosisType.CONFIRMED);
		
		if (tbcase.getInfectionSite() == InfectionSite.PULMONARY)
			 tbcase.setPulmonaryType(newValue(preferences.getPulmonaryForms()));
		else tbcase.setExtrapulmonaryType(newValue(preferences.getExtrapulmonaryForms()));
		
		// days to start treatment after diagnosis
		RangeValue startTreat;
		if (classif == CaseClassification.TB)
			 startTreat = preferences.getStartTreatmentTB();
		else startTreat = preferences.getStartTreatmentMDR();
		int daysStartTreat = newRangeValue(startTreat);
		Date dtIniTreat = DateUtils.incDays(caseDate, daysStartTreat);

		Date dtEndTreat = null;
		endDate = new Date();

		// region of patient address
		Tbunit unit = null;
		AdministrativeUnit region = null;
		
		int count = 0;
		while (unit == null) {
			region = newValue(preferences.getRegions());
			// notification health unit
			unit = unitFromRegion(region);
			tbcase.setNotificationUnit(unit);
			count++;
			if (count > 30)
				throw new RuntimeException("Exceded the number of times to find for an available unit. Register more health TB Units");
		}

		region = entityManager.merge(region);
		AdministrativeUnit locality = localityFromRegion(region);
		// locality type of patient
		LocalityType localityType = newValue(preferences.getLocalityTypes());
		
		// mount patient address
		Address address = new Address();
		address.setAddress("XPTO Street");
		address.setLocalityType(localityType);
		address.setAdminUnit(locality);
		tbcase.setNotifAddress(address);
				
		// nationality
		Nationality nationality = newValue(preferences.getNationalities());
		tbcase.setNationality(nationality);

		CaseState outcome;
		Date outcomeDate = null;
		Regimen reg = null;

		// treatment starts in the future ??
		if (dtIniTreat.after(new Date())) {
			outcome = CaseState.WAITING_TREATMENT;
			dtIniTreat = null;
			dtEndTreat = null;
		}
		else {
			// regimen
			if (classif == CaseClassification.TB)
				reg = newValue(preferences.getRegimensTB());
			else reg = newValue(preferences.getRegimensMDR());
			reg = entityManager.merge(reg);

			// outcome
			if (classif == CaseClassification.TB)
				outcome = newValue(preferences.getOutcomesTB());
			else outcome = newValue(preferences.getOutcomesMDR());
			
			if (outcome == CaseState.CURED) {
				int months = reg.getMonthsPhase(RegimenPhase.INTENSIVE) + reg.getMonthsPhase(RegimenPhase.CONTINUOUS);
				outcomeDate = DateUtils.incMonths(dtIniTreat, months);
			}
			else {
				int months;
				if (classif == CaseClassification.TB)
					 months = random.nextInt(8);
				else months = random.nextInt(24);
				outcomeDate = DateUtils.incMonths(dtIniTreat, months);
			}
			
			// outcome is before the treatment start ?
			if (!outcomeDate.after(dtIniTreat)) {
				dtIniTreat = null;
				dtEndTreat = null;
			}
			else {
				dtEndTreat = outcomeDate;
			}

			// checks if outcome is in the future
			if (outcomeDate.after(new Date())) {
				outcomeDate = null;
				outcome = CaseState.ONTREATMENT;
			}
		}
		
		if ((outcomeDate != null) && (outcomeDate.before(endDate)))
			endDate = outcomeDate;
		
		if ((dtIniTreat == null) || (dtEndTreat == null)) {
			System.out.println("Tratamento nulo");
		}
		else tbcase.setTreatmentPeriod(new Period(dtIniTreat, dtEndTreat));
		tbcase.setOutcomeDate(outcomeDate);
		tbcase.setState(outcome);

		if (reg != null)
			applyStandardRegimen(reg);

		createMedicalExamination();
		createMicroscopyExams();
		createCultureExams();
		createDSTExams();
		createHIVExams();
		createXRays();
		createSideEffects();
		createComorbidities();
		createContacts();
		
		saveCase.save(tbcase);

		// each 20 cases clear memory
		if (counter % 20 == 0) {
			entityManager.clear();
		}
		
		// increments the number of cases
		if (classif == CaseClassification.TB)
			 caseInfo.setNumTBGenerated(caseInfo.getNumTBGenerated() + 1);
		else caseInfo.setNumMDRTBGenerated(caseInfo.getNumMDRTBGenerated() + 1);
		
		return true;
	}


	/**
	 * Create DST exams
	 */
	private void createDSTExams() {
		Date dt = newExamFirstDate(preferences.getDstFirst());
		Laboratory laboratory = selectLaboratory();		
		FieldValue method = selectMethod();

		List<Substance> resPattern;
		int freq;
		if (tbcase.getClassification() == CaseClassification.TB) {
			 resPattern = newValue(preferences.getResPatternsTB());
			 freq = preferences.getDstFreqTB();
		}
		else {
			resPattern = newValue(preferences.getResPatternsMDR());
			freq = preferences.getDstFreqMDR();
		}

		boolean bFirst = true;
		
		while ((dt.before(new Date())) && (dt.before(endDate))) {
			ExamDST exam = new ExamDST();
			exam.setMethod(method);
			exam.setLaboratory(laboratory);
			
			exam.setTbcase(tbcase);
			exam.setDateCollected(dt);
			tbcase.getExamsDST().add(exam);

			// set resistance pattern
			for (Substance sub: resPattern) {
				ExamDSTResult res = new ExamDSTResult();
				DstResult test = DstResult.values()[random.nextInt(3)+1];
				res.setResult(test);
//				res.setResult(SusceptibilityResultTest.RESISTANT);
				res.setSubstance(sub);
				res.setExam(exam);
				exam.getResults().add(res);
			}

			// complete the test with other substances
			for (Substance sub: preferences.getSubstances()) {
				ExamDSTResult res = exam.findResultBySubstance(sub);
				if (res == null) {
					res = new ExamDSTResult();
					if (tbcase.getClassification() == CaseClassification.DRTB)
						 res.setResult(newValue(preferences.getDstResultsMDR()));
					else res.setResult(newValue(preferences.getDstResultsTB()));
					res.setSubstance(sub);
					
					res.setExam(exam);
					exam.getResults().add(res);
				}
			}
			
			// remove results that are resistant to medicines prescribed 
			for (ExamDSTResult res: exam.getResults()) {
				if ((DstResult.RESISTANT.equals(res.getResult())) && (isSubstancePrescribed(res.getSubstance())))
					res.setResult(DstResult.SUSCEPTIBLE);
			}

			// remove results that are not done
			int i = 0;
			while (i < exam.getResults().size()) {
				ExamDSTResult res = exam.getResults().get(i);
				if (DstResult.NOTDONE.equals(res.getResult())) {
					exam.getResults().remove(i);
				}
				else {
					// register the number of results
					switch (res.getResult()) {
						case CONTAMINATED: exam.setNumContaminated( exam.getNumContaminated() + 1);
						case RESISTANT: exam.setNumResistant( exam.getNumResistant() + 1);
						case SUSCEPTIBLE: exam.setNumSusceptible( exam.getNumSusceptible() + 1);
					}
					i++;
				}
			}
			
			if (bFirst) {
				checkDrugResistanceType(exam);
			}
			
			if (freq == 0)
				break;
			dt = nextExam(dt, freq);
		}
	}

	
	/**
	 * Check if substance is prescribed to the patient
	 * @param sub
	 * @return
	 */
	private boolean isSubstancePrescribed(Substance sub) {
		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			if (pm.getMedicine().getAbbrevName().equals(sub.getAbbrevName().toString()))
				return true;
			for (MedicineComponent medComp: pm.getMedicine().getComponents()) {
				if (medComp.getSubstance().equals(sub))
					return true;
			}
		}
		return false;
	}
	
	private void checkDrugResistanceType(ExamDST dstTest) {
		int resCount = 0;
		boolean resH = false;
		boolean resR = false;
		boolean resFluoq = false;
		boolean resInjec = false;
		
		for (ExamDSTResult res: dstTest.getResults()) {
			if (res.getResult() == DstResult.RESISTANT) {
				resCount++;
				String subname = res.getSubstance().getAbbrevName().toString();
				if (subname.equals("H"))
					resH = true;
				if (subname.equals("R"))
					resR = true;
				if ((subname.equals("Ofx")) || (subname.equals("Mfx")) || (subname.equals("Lfx")))
					resFluoq = true;
				if ((subname.equals("Cm")) || (subname.equals("Km")) || (subname.equals("Am")))
					resInjec = true;
			}
		}
		
		if (resCount == 0)
			return;
		
		if (resCount == 1) {
			tbcase.setDrugResistanceType(DrugResistanceType.MONO_RESISTANCE);
			return;
		}

		if (resFluoq && resInjec && (resH || resR)) {
			tbcase.setDrugResistanceType(DrugResistanceType.EXTENSIVEDRUG_RESISTANCE);
			return;
		}
		
		if (!(resH && resR)) {
			tbcase.setDrugResistanceType(DrugResistanceType.POLY_RESISTANCE);
			return;
		}
		
		tbcase.setDrugResistanceType(DrugResistanceType.MULTIDRUG_RESISTANCE);
	}

	/**
	 * Create culture exams
	 */
	private void createCultureExams() {
		Date dt = newExamFirstDate(preferences.getCultureFirst());
		dateFirstCulture = dt;
		Laboratory laboratory = selectLaboratory();

		int freq;
		if (tbcase.getClassification() == CaseClassification.TB)
			freq = preferences.getCultureFreqTB();
		else freq = preferences.getCultureFreqMDR();
		
		// months to negativate
		int negmonths = random.nextInt(12);
		Date dtneg = DateUtils.incMonths(dt, negmonths);

		CultureResult res = CultureResult.NEGATIVE;
		while ((dt.before(new Date())) && (dt.before(endDate))) {
			// generates exam result
			if (dt.after(dtneg))
				res = CultureResult.NEGATIVE;
			else {
				while (res == CultureResult.NEGATIVE)
					res = newValue(preferences.getCultureResults());
			}
			
			ExamCulture exam = new ExamCulture();
			exam.setLaboratory(laboratory);
			exam.setResult(res);
			exam.setDateCollected(dt);
			exam.setTbcase(tbcase);
			
			tbcase.getExamsCulture().add(exam);
			
			if (freq == 0)
				break;

			dt = nextExam(dt, freq);
		}
	}
	

	/**
	 * Generates a new exam first date
	 * @param range
	 * @return
	 */
	protected Date newExamFirstDate(RangeValue range) {
		Date dt = tbcase.getDiagnosisDate();
		int dx = newRangeValue(range);
		dt = DateUtils.incDays(dt, -dx);
		return dt;
	}
	
	
	/**
	 * Calculate next exam date based on previous exam date and number of days for next exam
	 * @param prevExam - Date of previous exam
	 * @param dx - number of days for next exam
	 * @return date of next exam
	 */
	protected Date nextExam(Date prevExam, int dx) {
		int var = random.nextInt(preferences.getVarDateExam());
		if (random.nextInt(2) == 0)
			var = -var;
		dx = dx + (dx * var / 100);

		Date dt = DateUtils.incDays(prevExam, dx);
		return dt;
	}


	/**
	 * Create microscopy exams
	 */
	private void createMicroscopyExams() {
		int freq = 0;
		if (tbcase.getClassification() == CaseClassification.TB)
			freq = preferences.getMicroscopyFreqTB();
		else freq = preferences.getMicroscopyFreqMDR();
		
		Date dt = newExamFirstDate(preferences.getMicroscopyFirst());
		Laboratory laboratory = selectLaboratory();
		
		// months to negativate
		int negmonths = random.nextInt(12);
		Date dtneg = DateUtils.incMonths(dt, negmonths);

		MicroscopyResult res = MicroscopyResult.NEGATIVE;
		while ((dt.before(new Date())) && (dt.before(endDate))) {
			// generates exam result
			if (dt.after(dtneg))
				res = MicroscopyResult.NEGATIVE;
			else {
				while (res == MicroscopyResult.NEGATIVE)
					res = newValue(preferences.getMicroscopyResults());
			}
			
			ExamMicroscopy exam = new ExamMicroscopy();
			exam.setLaboratory(laboratory);
			exam.setResult(res);

			exam.setDateCollected(dt);
			exam.setTbcase(tbcase);
			tbcase.getExamsMicroscopy().add(exam);

			if (freq == 0)
				break;
			
			dt = nextExam(dt, freq);
		}
	}

	
	/**
	 * Create HIV exam results 
	 */
	protected void createHIVExams() {
		int freq = 0;
		if (tbcase.getClassification() == CaseClassification.TB)
			freq = preferences.getHivFreqTB();
		else freq = preferences.getHivFreqMDR();
		
		Date dt = newExamFirstDate(preferences.getHivFirst());
		
		HIVResult res = HIVResult.NEGATIVE;
		while ((dt.before(new Date())) && (dt.before(endDate))) {
			// generates exam result
			res = newValue(preferences.getHivResults());
			
			ExamHIV exam = new ExamHIV();
			exam.setDate(dt);
			exam.setResult(res);
			
			tbcase.getResHIV().add(exam);
			exam.setTbcase(tbcase);

			if (res == HIVResult.POSITIVE)
				break;
			
			if (freq == 0)
				break;
			
			dt = nextExam(dt, freq);
		}
	}


	/**
	 * Create X-Ray results
	 */
	protected void createXRays() {
		if (dateFirstCulture == null)
			return;
		
		Date dt = dateFirstCulture;
		Date today = new Date();
		boolean firstExam = true;
		// indicate if the x-ray will improve or decrease the case
		boolean dirImprove;
		if (CaseState.CURED.equals(tbcase.getState()))
			dirImprove = true;
		else
		if ((CaseState.FAILED.equals(tbcase.getState())) || (CaseState.DIED.equals(tbcase.getState())))
			 dirImprove = false;
		else dirImprove = random.nextInt(2) == 0;

		FieldValue presentation = newValue(preferences.getXrayPresentation());
		
		while ((dt.before(today)) && (dt.before(endDate))) {
			ExamXRay xray = new ExamXRay();
			xray.setPresentation(presentation);
			xray.setDate(dt);
			
			if (!firstExam) {
				XRayEvolution evolution;
				boolean sit = random.nextInt(2) == 0;
				if (sit)
					 evolution = XRayEvolution.STABLE;
				else evolution = (dirImprove? XRayEvolution.IMPROVED: XRayEvolution.PROGRESSED);
				xray.setEvolution(evolution);
				
				if (!XRayEvolution.STABLE.equals(evolution)) {
					Integer val = preferences.getXrayPresentationProgress().get(presentation);
					if (val != null) {
						// moves to the next presentation, according to the evolution
						FieldValue pres = presentation;
						Integer num2 = null;
						for (FieldValue fld: preferences.getXrayPresentationProgress().keySet()) {
							Integer num = preferences.getXrayPresentationProgress().get(fld);
							if (XRayEvolution.IMPROVED.equals(evolution)) {
								if ((num > val) && ((num2 == null) || (num <= num2))) {
									pres = fld;
									num2 = num;
								}
							}
							else {
								if ((num < val) && ((num2 == null) || (num >= num2))) {
									pres = fld;
									num2 = num;
								}
							}
						}
						presentation = pres;
					}
					xray.setPresentation(presentation);
				}
			}
			
			tbcase.getResXRay().add(xray);
			xray.setTbcase(tbcase);
			
			// select next date
			int days = newRangeValue(preferences.getXrayNextResult());
			dt = DateUtils.incDays(dt, days);
			firstExam = false;
		}
	}

	
	/**
	 * Create side effects to cases
	 */
	protected void createSideEffects() {
		// will case have adverse reaction ?
		if (!randomWillHappen( preferences.getPercAdverseReactions() ))
			return;
		
		for (FieldValue field: preferences.getAdverseReactions().keySet()) {
			int perc = preferences.getAdverseReactions().get(field);
			// has this adverse reaction ?
			if (randomWillHappen(perc)) {
				CaseSideEffect se = new CaseSideEffect();
				se.setResolved(YesNoType.YES);
				
				field = entityManager.merge(field);
				se.setSideEffect(field);

				int months;
				if (tbcase.getTreatmentPeriod().isEmpty())
					 months = tbcase.getTreatmentPeriod().getMonths();
				else months = 4;
				se.setMonth(random.nextInt(months+1) + 1);
				
				se.setTbcase(tbcase);
				tbcase.getSideEffects().add(se);
			}
		}
	}


	/**
	 * Create comorbidities
	 */
	protected void createComorbidities() {
		// will case have adverse reaction ?
		if (!randomWillHappen( preferences.getPercComorbidities() ))
			return;
		
		for (FieldValue field: preferences.getComorbidities().keySet()) {
			int perc = preferences.getComorbidities().get(field);
			// has this comorbidity ?
			if (randomWillHappen(perc)) {
				CaseComorbidity com = new CaseComorbidity();
				com.setComorbidity(entityManager.merge(field));
				com.setTbcase(tbcase);
				tbcase.getComorbidities().add(com);
			}
		}
	}
	
	
	/**
	 * Create contacts
	 */
	protected void createContacts() {
		// will case have adverse reaction ?
		if (!randomWillHappen( preferences.getPercContacts() ))
			return;
		
		int num = newRangeValue( preferences.getContactsRange() );
		
		for (int i = 0; i < num; i++) {
			TbContact cont = new TbContact();
			cont.setAge(random.nextInt(30) + 12);
			
			Gender gender = newValue( preferences.getGenders());
			cont.setGender(gender);

			String name;
			if (gender == Gender.MALE)
				 name = newValue(preferences.getFirstNamesMale());
			else name = newValue(preferences.getFirstNamesFemale());				
			
			String lastName = newValue(preferences.getLastNames());
			name = name + " " + lastName;
			cont.setName(name);
			cont.setTbcase(tbcase);
			cont.setContactType( newValue(preferences.getContactType()) );
			cont.setConduct( newValue(preferences.getContactConduct()) );
			tbcase.getContacts().add(cont);
		}
	}


	/**
	 * Apply the standard regimen to the treatment of the current case
	 * @param reg - Standard regimen to be applied to the treatment
	 */
	protected void applyStandardRegimen(Regimen reg) {
		// if the treatment period is not set, there is nothing to do
		if (tbcase.getTreatmentPeriod().getIniDate() == null)
			return;

		Period treatPeriod = tbcase.getTreatmentPeriod();

		// defines treatment unit
		TreatmentHealthUnit hu = new TreatmentHealthUnit();
		hu.getPeriod().set(treatPeriod);
		hu.setTbCase(tbcase);
		hu.setTbunit(tbcase.getNotificationUnit());
		tbcase.getHealthUnits().add(hu);

		// initialize case regimen
		tbcase.setRegimen(reg);
		Date iniCont = DateUtils.incMonths(treatPeriod.getIniDate(), reg.getMonthsPhase(RegimenPhase.INTENSIVE));
		if (iniCont.after(treatPeriod.getEndDate()))
			iniCont = null;
		tbcase.setIniContinuousPhase(iniCont);
		
		// create list of medicines for the intensive phase
		Date dt = tbcase.getTreatmentPeriod().getIniDate();
		for (MedicineRegimen medReg: reg.getIntensivePhaseMedicines()) {
			PrescribedMedicine pm = new PrescribedMedicine();
			pm.initializeFromRegimen(medReg, dt);
			if (pm.getPeriod().intersect(treatPeriod)) {
				pm.setTbcase(tbcase);
				tbcase.getPrescribedMedicines().add(pm);
			}
		}
		
		// create list of medicines for the continuous phase
		if ((iniCont != null) && (!iniCont.after(treatPeriod.getEndDate()))) {
			dt = DateUtils.incMonths(dt, reg.getMonthsPhase(RegimenPhase.INTENSIVE));
			for (MedicineRegimen medReg: reg.getContinuousPhaseMedicines()) {
				PrescribedMedicine pm = new PrescribedMedicine();
				pm.initializeFromRegimen(medReg, dt);
				if (pm.getPeriod().intersect(treatPeriod)) {
					pm.setTbcase(tbcase);
					tbcase.getPrescribedMedicines().add(pm);
				}
			}
		}
		
		tbcase.updateDaysTreatPlanned();
	}
	

	/**
	 * Creates a new patient
	 * @return - patient
	 */
	protected Patient createPatient(int caseYear) {
		patient = new Patient();

		// picks a gender
		Gender gender = newValue(preferences.getGenders());
		patient.setGender(gender);
		
		String name;
		String lastName;
		int count = 0;
		
		while (true) {
			// picks a name
			name = null;
			while (name == null) {
				if (gender == Gender.MALE)
					 name = newValue(preferences.getFirstNamesMale());
				else name = newValue(preferences.getFirstNamesFemale());				
			}
			
			lastName = newValue(preferences.getLastNames());
			name = name + " " + lastName;
			if (!nameExists(name))
				break;

			count++;
			// if it tries more than 20 times to get a new name, so finish it 
			if (count > 20) {
				throw new RuntimeException("Unable to generate a new name for the patient. Create more combinations of names and last names");
			}
		}
		
		patient.setName(name);

		// set mother name
		patient.setMotherName(newValue(preferences.getFirstNamesFemale()) + " " + lastName);

		patient.setBirthDate(newBirthDate(caseYear));
		patient.setRecordNumber(random.nextInt(100000) + 100000);
		patient.setWorkspace(defaultWorkspace);

		return patient;
	}

	
	/**
	 * Checks if the patient name exists
	 * @param name - Patient name to check
	 * @return - true if the patient with the given name exists, false if there is no patient with the given name
	 */
	protected boolean nameExists(String name) {
		Integer count = ((BigInteger) entityManager
			.createNativeQuery("select count(*) from Patient p where upper(p.patient_name) = :name")
			.setParameter("name", name.toUpperCase())
			.getSingleResult()).intValue();
		
		return count > 0;
	}


	/**
	 * Generates new birth date
	 * @return - Date
	 */
	protected Date newBirthDate(int caseYear) {
		ageRangeInfo = newValue(preferences.getAgeRanges());
		int dx;
		int endRange = ageRangeInfo.getRange().getEndAge();
		if ((endRange > 100) || (endRange == 0))
			 dx = 20;
		else dx = ageRangeInfo.getRange().getEndAge() - ageRangeInfo.getRange().getIniAge();
		int year = random.nextInt(dx);
		
		Calendar c = Calendar.getInstance();
		
		year = caseYear - ageRangeInfo.getRange().getIniAge() - year;
		c.clear();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, random.nextInt(12));
		c.set(Calendar.DAY_OF_MONTH, random.nextInt(28) + 1);
		return c.getTime();
	}

	
	/**
	 * Sort the change of something to happen, in a given percentage of chances to happen
	 * @param perc
	 * @return
	 */
	protected boolean randomWillHappen(int perc) {
		int val = random.nextInt(100);
		return (val < perc);
	}
	
	/**
	 * Picks a random item from a list
	 * @param values
	 * @return - an item of the list
	 */
	protected <E> E newValue(List<E> values) {
		E res = null;
		while (res == null) {
			int max = values.size();
			int val = random.nextInt(max);			
			res = values.get(val);
		}
		return res;
	}


	/**
	 * Generates a random value according to the weight-factor of each key in the map
	 * @param values
	 * @return - a key of the map
	 */
	protected <E> E newValue(Map<E, Integer> values) {
		if (values.keySet().size() == 0)
			return null;
		
		int max = 0;
		for (Object key: values.keySet()) {
			max += values.get(key);
		}
		
		int val = random.nextInt(max) + 1;
		for (E key: values.keySet()) {
			val -= values.get(key);
			if (val <= 0)
				return key;
		}
		return null;
	}


	/**
	 * Select one locality from a region
	 * @param reg - Region where the locality will be chosen
	 * @return locality selected
	 */
	protected AdministrativeUnit localityFromRegion(AdministrativeUnit reg) {
		List<Integer> ids = entityManager
			.createQuery("select loc.id from AdministrativeUnit loc where loc.parent.id = :id)")
			.setParameter("id", reg.getId())
			.getResultList();
		
		if (ids.size() == 0)
			return null;
		
		int index = random.nextInt(ids.size());
		int id = ids.get(index);
		return entityManager.find(AdministrativeUnit.class, id);
	}


	/**
	 * Selects one unit from a region
	 * @param reg - Region where the unit will be chosen
	 * @return Tbunit selected from the region
	 */
	protected Tbunit unitFromRegion(AdministrativeUnit reg) {
		List<Integer> ids = entityManager
			.createQuery("select unit.id from Tbunit unit where unit.adminUnit.code like :id " +
					"and unit.treatmentHealthUnit = true " +
					"and unit.workspace.id = :wsid")
			.setParameter("id", reg.getCode() + '%')
			.setParameter("wsid", defaultWorkspace.getId())
			.getResultList();
	
		if (ids.size() == 0)
			return null;
	
		int index = random.nextInt(ids.size());
		int id = ids.get(index);
		return entityManager.find(Tbunit.class, id);		
	}

	
	/**
	 * Selects a laboratory
	 * @return - Laboratory selected
	 */
	protected Laboratory selectLaboratory() {
		List<Integer> ids = entityManager
			.createQuery("select lab.id from Laboratory lab where lab.workspace.id = :id")
			.setParameter("id", defaultWorkspace.getId())
			.getResultList();
		
		if (ids.size() == 0)
			throw new RuntimeException("No laboratory found in the system");
		
		int index = random.nextInt(ids.size());
		int id = ids.get(index);
		return entityManager.find(Laboratory.class, id);
	}
	
	/**
	 * Create a medical examination record before the diagnosis date
	 */
	protected void createMedicalExamination() {
		MedicalExamination medExam = new MedicalExamination();
		medExam.setAppointmentType(MedAppointmentType.SCHEDULLED);
		medExam.setDate(DateUtils.incDays(tbcase.getDiagnosisDate(), -3));
		medExam.setTbcase(tbcase);
		medExam.setUsingPrescMedicines(YesNoType.YES);
		
		int w = newRangeValue(ageRangeInfo.getWeightRange());
		int h = newRangeValue(ageRangeInfo.getHeigthRange());
		
		if (w != 0)
			medExam.setWeight((double) w);
		
		if (h != 0)
			medExam.setHeight((float)h);

		medExam.setResponsible("Dr. " + newValue(preferences.getFirstNamesMale()) + " " + newValue(preferences.getLastNames()));
		tbcase.getExaminations().add(medExam);
	}
	

	/**
	 * Generates a new value inside a range
	 * @param rangeValue - range of values
	 * @return integer value
	 */
	protected int newRangeValue(RangeValue rangeValue) {
		int dx = rangeValue.getEndValue() - rangeValue.getIniValue();
		if (dx == 0)
			return 0;

		int fator;
		if (dx < 0)
			fator = -1;
		else fator = 1;
		int val = random.nextInt(dx * fator);
		return rangeValue.getIniValue() + (val * fator);
	}


	/**
	 * Selects a new method
	 * @return selected method
	 */
	protected FieldValue selectMethod() {
		List<Integer> ids = entityManager
			.createQuery("select f.id from FieldValue f where f.workspace.id = :id")
			.setParameter("id", defaultWorkspace.getId())
			.getResultList();
	
		if (ids.size() == 0)
			throw new RuntimeException("No method found in the system");
	
		int index = random.nextInt(ids.size());
		int id = ids.get(index);
		return entityManager.find(FieldValue.class, id);
	}
	
	/**
	 * Find case information by an specific year
	 * @param year
	 * @return PrescriptionInfo instance of the year param
	 */
	protected CaseInfo findCaseInfo(int year) {
		for (CaseInfo caseInfo: preferences.getCases()) {
			if (caseInfo.getYear() == year)
				return caseInfo;
		}
		return null;
	}

	/**
	 * @return the defaultWorkspace
	 */
	public Workspace getDefaultWorkspace() {
		return defaultWorkspace;
	}

	/**
	 * @param defaultWorkspace the defaultWorkspace to set
	 */
	public void setDefaultWorkspace(Workspace defaultWorkspace) {
		this.defaultWorkspace = defaultWorkspace;
	}
}
