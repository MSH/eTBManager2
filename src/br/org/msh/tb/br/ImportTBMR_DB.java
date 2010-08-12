package org.msh.tb.br;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.entities.CaseDataBR;
import org.msh.entities.enums.FailureType;
import org.msh.entities.enums.TipoResistencia;
import org.msh.mdrtb.entities.Address;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.CountryStructure;
import org.msh.mdrtb.entities.ExamCulture;
import org.msh.mdrtb.entities.ExamHIV;
import org.msh.mdrtb.entities.ExamSputumSmear;
import org.msh.mdrtb.entities.ExamSusceptibilityResult;
import org.msh.mdrtb.entities.ExamSusceptibilityTest;
import org.msh.mdrtb.entities.ExamXRay;
import org.msh.mdrtb.entities.FieldValue;
import org.msh.mdrtb.entities.HealthSystem;
import org.msh.mdrtb.entities.Laboratory;
import org.msh.mdrtb.entities.MedicalExamination;
import org.msh.mdrtb.entities.Patient;
import org.msh.mdrtb.entities.PrevTBTreatment;
import org.msh.mdrtb.entities.Regimen;
import org.msh.mdrtb.entities.Substance;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.CultureResult;
import org.msh.mdrtb.entities.enums.DiagnosisType;
import org.msh.mdrtb.entities.enums.DrugResistanceType;
import org.msh.mdrtb.entities.enums.Gender;
import org.msh.mdrtb.entities.enums.HIVResult;
import org.msh.mdrtb.entities.enums.InfectionSite;
import org.msh.mdrtb.entities.enums.Nationality;
import org.msh.mdrtb.entities.enums.PatientType;
import org.msh.mdrtb.entities.enums.PrevTBTreatmentOutcome;
import org.msh.mdrtb.entities.enums.SputumSmearResult;
import org.msh.mdrtb.entities.enums.SusceptibilityResultTest;
import org.msh.mdrtb.entities.enums.TbField;
import org.msh.mdrtb.entities.enums.ValidationState;
import org.msh.mdrtb.entities.enums.YesNoType;
import org.msh.tb.adminunits.AdminUnitHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.ExamCultureHome;
import org.msh.tb.cases.ExamSputumHome;
import org.msh.tb.cases.ExamSusceptHome;
import org.msh.tb.cases.treatment.StartTreatmentHome;
import org.msh.tb.misc.FieldsQuery;
import org.msh.utils.date.DateUtils;


/**
 * Import case data from the Brazilian MDR-TB system "Sistema TBMR" to the e-TB Manager database.
 * The connection to the Br MDR-TB is done using a jndi lookup to the "java:tbmrDatasource" name. This name is 
 * supposed to return a {@link DataSource} instance, where the class uses this datasource 
 * to read data from the Br MDR-TB database.
 * @author Ricardo Memoria
 *
 */
@Name("importTBMRDB")
public class ImportTBMR_DB {

	/**
	 * Standard regimen used during importing
	 */
	private static final int regimenId = 940665;
	
	@In(create=true) EntityManager entityManager;
	@In(create=true) FacesMessages facesMessages;
	@In(required=true) Workspace defaultWorkspace;
	@In(create=true) CaseDataBRHome caseDataBRHome;
	@In(create=true) CaseHome caseHome;
	@In(create=true) AdminUnitHome adminUnitHome;
	@In(create=true) ExamCultureHome examCultureHome;
	@In(create=true) ExamSputumHome examSputumHome;
	@In(create=true) ExamSusceptHome examSusceptHome;
	@In(create=true) FieldsQuery fieldsQuery;
	@In(create=true) StartTreatmentHome startTreatmentHome;
//	@In(create=true) TreatmentHealthUnitHome treatmentHealthUnitHome;

	private DataSource tbmrDataSource;
	private Connection connection;
	private ResultSet rsCases;
	private String uf;
	private List<SelectItem> ufList;
	private List<CountryStructure> structures;
	private TbCase tbcase;
	private CaseDataBR caseData;
	private HealthSystem healthSystem;
	private Regimen regimen;
	private List<Substance> substances;
	
	public String execute() throws Exception {
		loadCasesToImport();

		try {
			int counter = 0;
			while (rsCases.next()) {
				importCase();
				counter++;
				
				// refresh entity manager each 40 cases imported
				if (counter % 40 == 19) {
					refreshEntityManager();
				}
			}
			facesMessages.add(Integer.toString(counter) + " casos importados.");
		} finally {
			rsCases.getStatement().close();
			connection.close();
		}

		return "success";
	}

	
	/**
	 * Import a case
	 * @throws Exception
	 */
	protected void importCase() throws Exception {
		Patient p = importPatient();

		String numFicha = rsCases.getString("COD_CASO");
		String sql = "select c.id from TbCase c where c.legacyId = :id and c.patient.workspace.id = #{defaultWorkspace.id}";
		List<Integer> lst = entityManager.createQuery(sql)
			.setParameter("id", numFicha)
			.getResultList();
		if (lst.size() > 0) {
			caseHome.setId(lst.get(0));
		}
		else caseHome.setId(null);

		tbcase = caseHome.getInstance();
		caseData = caseDataBRHome.getCaseDataBR();
		
		tbcase.setPatient(p);
		tbcase.setLegacyId(rsCases.getString("COD_CASO"));
		tbcase.setRegistrationDate(rsCases.getDate("DATA_NOTIFICACAO"));
		tbcase.setDiagnosisDate(rsCases.getDate("DATA_NOTIFICACAO"));
		tbcase.setAge(DateUtils.yearsBetween(tbcase.getRegistrationDate(), p.getBirthDate()));
		tbcase.setCaseNumber(rsCases.getInt("NUM_CASO"));
		if (tbcase.getCaseNumber() == 0)
			tbcase.setCaseNumber(null);
		tbcase.setClassification(CaseClassification.MDRTB_DOCUMENTED);
		
		int val = rsCases.getInt("COD_CASO_TBMR");
		if (val == 1)
			 tbcase.setDiagnosisType(DiagnosisType.CONFIRMED);
		else tbcase.setDiagnosisType(DiagnosisType.SUSPECT);
		
		tbcase.setDrugResistanceType(DrugResistanceType.MONO_RESISTANCE);

		importInfectionSite();
		importComorbidities();
		importHIV();

		tbcase.setNationality(Nationality.NATIVE);
		tbcase.setNotifAddressChanged(false);
		
		int numCasosAnt = rsCases.getInt("NUM_TRATAMENTOS_ANTERIORES");
		if (numCasosAnt == 0)
			 tbcase.setPatientType(PatientType.NEW);
		else tbcase.setPatientType(PatientType.RELAPSE);
		
		importPatientAddress();
		
		Tbunit unit = loadTBUnit(rsCases.getInt("COD_US_TRATAMENTO"));
		tbcase.setNotificationUnit(unit);
		
		tbcase.setValidationState(ValidationState.WAITING_VALIDATION);
		
		String codMun = rsCases.getString("COD_MUN_US_ORIGEM");
		AdministrativeUnit municipio = loadMunicipio(codMun);
		caseData.setAdminUnitUsOrigem(municipio);
		caseData.setUsOrigem(rsCases.getString("NOME_US_ORIGEM"));

		FieldValue fieldValue = getFieldValue(TbField.CONTAG_PLACE, rsCases.getString("COD_LOCAL_CONTAGIO"));  
		caseData.getContagPlace().setValue(fieldValue);
		
		fieldValue = getFieldValue(TbField.EDUCATIONAL_DEGREE, rsCases.getString("ESCOLARIDADE"));
		caseData.getEducationalDegree().setValue(fieldValue);

		fieldValue = getFieldValue(TbField.POSITION, rsCases.getString("OCUPACAO"));
		caseData.getPosition().setValue(fieldValue);
		
		fieldValue = getFieldValue(TbField.SKINCOLOR, rsCases.getString("COR"));
		caseData.getSkinColor().setValue(fieldValue);

		if (numCasosAnt == 0)
			 caseData.setFailureType(FailureType.FIRST_TREATMENT);
		else caseData.setFailureType(FailureType.RETREATMENT);
		
		val = rsCases.getInt("TIPO_RESISTENCIA");
		if (val == 1)
			 caseData.setTipoResistencia(TipoResistencia.PRIMARIA);
		else caseData.setTipoResistencia(TipoResistencia.ADQUIRIDA);
		
		caseData.setNumSinan(rsCases.getString("NUM_SINAN"));
		
		updateCaseOutcome();
		updateCaseTreatment();
		importMedicalExamination();
		
		caseHome.setTransactionLogActive(false);
		caseHome.setDisplayMessage(false);
		caseHome.persist();
		
		caseData.setTbcase(tbcase);
		caseData.setId(tbcase.getId());
		caseDataBRHome.setDisplayMessage(false);
		caseDataBRHome.persist();
		
		importExams();
		importPrevTBTreatment();
	}


	/**
	 * Import exams results from notification form
	 * @throws Exception
	 */
	protected void importExams() throws Exception {
		Laboratory lab = loadLaboratory(rsCases.getInt("COD_LABORATORIO"));

		Date dtColeta = rsCases.getDate("DATA_CULTURA_ESCARRO");
		
		int res = rsCases.getInt("RES_CULTURA_ESCARRO");
		CultureResult resCulture = null;
		switch (res) {
		case 0:
			resCulture = CultureResult.NEGATIVE;
			break;
		case 1:
			resCulture = CultureResult.PLUS;
			break;
		case 2:
			resCulture = CultureResult.PLUS2;
			break;
		case 3:
			resCulture = CultureResult.PLUS3;
			break;
		case 6:
			resCulture = CultureResult.CONTAMINATED;
			break;
		}

		if (resCulture != null) {
			
			examCultureHome.setId(null);
			ExamCulture culture = examCultureHome.getInstance();
			culture.setResult(resCulture);
			culture.setLaboratory(lab);
			
			// get method
			String cod = rsCases.getString("COD_METODO");
			if ("5".equals(cod)) {
				cod = "6";
			}
			FieldValue method = getFieldValue(TbField.CULTURE_METHOD, cod);
			culture.setMethod(method);

			examCultureHome.getSample().setDateCollected(dtColeta);
			examCultureHome.setDisplayMessage(false);
			examCultureHome.persist();
		}
		
		res = rsCases.getInt("RES_BACILOSCOPIA_ESCARRO");
		SputumSmearResult resSputum = null;
		switch (res) {
		case 0:
			resSputum = SputumSmearResult.NEGATIVE;
			break;
		case 1:
			resSputum = SputumSmearResult.PLUS;
			break;
		case 2:
			resSputum = SputumSmearResult.PLUS2;
			break;
		case 3:
			resSputum = SputumSmearResult.PLUS3;
		}
		
		if (resSputum != null) {
			examSputumHome.setId(null);
			ExamSputumSmear sputum = examSputumHome.getInstance();
			sputum.setResult(resSputum);
			sputum.setLaboratory(lab);
			examSputumHome.getSample().setDateCollected(dtColeta);
			examSputumHome.setDisplayMessage(false);
			examSputumHome.persist();
		}
		
		examSusceptHome.setId(null);
		examSusceptHome.setInstance(null);
		for (ExamSusceptibilityResult resTest: examSusceptHome.getItems())
			resTest.setResult(SusceptibilityResultTest.NOTDONE);
		checkResultDST("COD_PADRAO_RES_RIFAMPICINA", "R");
		checkResultDST("COD_PADRAO_RES_PIRAZINAMIDA", "Z");
		checkResultDST("COD_PADRAO_RES_ETIONAMIDA", "Et");
		checkResultDST("COD_PADRAO_RES_AMICACINA", "Am");
		checkResultDST("COD_PADRAO_RES_CAPREOMICINA", "Cp");
		checkResultDST("COD_PADRAO_RES_OFLOXACINO", "Ofx");
		checkResultDST("COD_PADRAO_RES_MOXIFLOXACINO", "Mfx");
		checkResultDST("COD_PADRAO_RES_ISONIAZIDA", "H");
		checkResultDST("COD_PADRAO_RES_ETAMBUTOL", "E");
		checkResultDST("COD_PADRAO_RES_ESTREPTOMICINA", "S");
		checkResultDST("COD_PADRAO_RES_KANAMICINA", "Km");
		checkResultDST("COD_PADRAO_RES_CIPROFLOXACINO", "Cfx");
		checkResultDST("COD_PADRAO_RES_LEVOFLOXACINO", "Lfx");
		checkResultDST("COD_PADRAO_RES_TERIZIDONA", "Tzd");
		
		ExamSusceptibilityTest dst = examSusceptHome.getExamSusceptibilityTest();
		dst.setLaboratory(lab);
		examSusceptHome.getSample().setDateCollected(dtColeta);
		examSusceptHome.setDisplayMessage(false);
		examSusceptHome.persist();
	}

	
	/**
	 * Check the result of a substance in the DST exam
	 * @param field
	 * @param subAbbrevName
	 * @throws Exception
	 */
	private void checkResultDST(String field, String subAbbrevName) throws Exception {
		int resDst = rsCases.getInt(field);

		if (resDst == 2)
			return;
		
		SusceptibilityResultTest resTest;
		if (resDst == 1)
			 resTest = SusceptibilityResultTest.RESISTANT;
		else resTest = SusceptibilityResultTest.SUSCEPTIBLE;
		
		ExamSusceptibilityResult res = null;
		for (ExamSusceptibilityResult aux: examSusceptHome.getItems()) {
			if (aux.getSubstance().getAbbrevName().toString().equals(subAbbrevName)) {
				res = aux;
			}
		}
		
		if (res != null)
			res.setResult(resTest);
	}

	
	/**
	 * Import previous TB treatment
	 * @throws Exception
	 */
	protected void importPrevTBTreatment() throws Exception {
		Connection conn = getConnection();
		try {
			String sql = "select * from TRATAMENTOS_ANTERIORES where num_ficha = " + rsCases.getString("NUM_FICHA");
			
			ResultSet rs = conn.createStatement().executeQuery(sql);
			
			while (rs.next()) {
				PrevTBTreatment aux = new PrevTBTreatment();
				checkMedicinePrevTreatment(rs, aux, "DROGA_R", "R");
				checkMedicinePrevTreatment(rs, aux, "DROGA_Z", "Z");
				checkMedicinePrevTreatment(rs, aux, "DROGA_H", "H");
				checkMedicinePrevTreatment(rs, aux, "DROGA_E", "E");
				checkMedicinePrevTreatment(rs, aux, "DROGA_S", "S");
				checkMedicinePrevTreatment(rs, aux, "DROGA_ET", "Et");
				checkMedicinePrevTreatment(rs, aux, "DROGA_OFX", "Ofx");
				checkMedicinePrevTreatment(rs, aux, "DROGA_CS", "Cs");
				checkMedicinePrevTreatment(rs, aux, "DROGA_CLZ", "Cfz");
				checkMedicinePrevTreatment(rs, aux, "DROGA_AM", "Am");
				
				aux.setYear(rs.getInt("ANO_INICIO"));
				int val = rs.getInt("RES_TRATAMENTO");
				PrevTBTreatmentOutcome res = null;
				switch (val) {
				case 1: res = PrevTBTreatmentOutcome.CURED;
					break;
				case 2: res = PrevTBTreatmentOutcome.DEFAULTED;
					break;
				case 3: res = PrevTBTreatmentOutcome.FAILURE;
					break;
				case 4: res = PrevTBTreatmentOutcome.SCHEME_CHANGED;
					break;
				}
				aux.setOutcome(res);
				aux.setTbCase(tbcase);
				
				entityManager.persist(aux);
				entityManager.flush();
			}
		} finally {
			conn.close();
		}
	}
	
	
	private void checkMedicinePrevTreatment(ResultSet rs, PrevTBTreatment prev, String fieldName, String subAbbrevName) throws Exception {
		int val = rs.getInt(fieldName);
		if (val == 0)
			return;
		
		Substance s = substanceByAbbrevName(subAbbrevName);
		if (s == null)
			return;
		
		prev.getSubstances().add(s);
	}


	/**
	 * Return substance by its abbreviated name
	 * @param abbrevName
	 * @return {@link Substance} with same abbreviated name, or null, if it doesn't exist
	 */
	protected Substance substanceByAbbrevName(String abbrevName) {
		for (Substance s: getSubstances()) {
			if (abbrevName.equalsIgnoreCase(s.getAbbrevName().toString())) {
				return s;
			}
		}
		return null;
	}


	/**
	 * Import medical examination
	 * @throws Exception
	 */
	protected void importMedicalExamination() throws Exception {
		MedicalExamination medExam = new MedicalExamination();
		
		medExam.setTbcase(tbcase);
		medExam.setWeight(rsCases.getDouble("PESO_ATUAL"));
		medExam.setDate(tbcase.getRegistrationDate());
		medExam.setResponsible(rsCases.getString("NOME_RESP_PREENCHIMENTO"));
		medExam.setPositionResponsible(rsCases.getString("PROF_RESP_PREENCHIMENTO"));
		medExam.setComments(rsCases.getString("OUTRAS_INFORMACOES"));
		int val = rsCases.getInt("TRATAMENTO_SUPERVISIONADO");
		YesNoType resp = null;
		if (val == 1) 
			 resp = YesNoType.YES;
		else resp = YesNoType.NO;
		medExam.setSupervisedTreatment(resp);
		medExam.setSupervisionUnitName(rsCases.getString("UNIDADE_SUPERVISAO"));
		
		tbcase.getExaminations().add(medExam);
	}

	/**
	 * Update treatment of the current case
	 */
	protected void updateCaseTreatment() {
		CaseState st = tbcase.getState();
		tbcase.getHealthUnits().clear();
		
		Date dtIni = tbcase.getRegistrationDate();
		Date dtEnd = tbcase.getOutcomeDate();
		if (dtEnd != null)
			System.out.println("Outcome");

		startTreatmentHome.setIniTreatmentDate(dtIni);
		startTreatmentHome.setEndTreatmentDate(dtEnd);

		startTreatmentHome.getTbunitselection().setTbunit(tbcase.getNotificationUnit());
		
		startTreatmentHome.setUseDefaultDoseUnit(true);
		startTreatmentHome.setRegimen(getRegimen());
		startTreatmentHome.updatePhases();
		
		
		tbcase.setState(st);
	}


	/**
	 * Import results from HIV exam
	 * @throws Exception
	 */
	protected void importHIV() throws Exception {
		int val = rsCases.getInt("RES_HIV");
		HIVResult res = null;
		if (val == 1)
			 res = HIVResult.POSITIVE;
		else res = HIVResult.NEGATIVE;
		
		if (res == null)
			return;
		ExamHIV exam = new ExamHIV();
		exam.setDate(tbcase.getRegistrationDate());
		exam.setResult(res);
		exam.setTbcase(tbcase);
		tbcase.getResHIV().add(exam);
	}
	
	/**
	 * Import comorbidities
	 * @throws Exception 
	 */
	protected void importComorbidities() throws Exception {
		checkComorbidity("AIDS", "1");
		checkComorbidity("DIABETES", "2");
		checkComorbidity("SILICOSE", "3");
		checkComorbidity("NEOPLASIA", "4");
		checkComorbidity("CORTICOTERAPIA", "5");
		checkComorbidity("TRANSPLANTADO", "6");
		checkComorbidity("HEMODIALISE", "7");
		checkComorbidity("ALCOOLISMO", "8");
		checkComorbidity("DROGA", "9");
		checkComorbidity("DOENCA_MENTAL", "10");
		checkComorbidity("OUTRA_COMORBIDADE", "11");
	}
	
	protected void checkComorbidity(String fieldName, String id) throws Exception {
		int res = rsCases.getInt(fieldName);
		if (res == 2)
			return;
		
		FieldValue value = getFieldValue(TbField.COMORBIDITY, id);
		tbcase.getComorbidities().add(value);
	}


	/**
	 * Check if case is pulmonary or extrapulmonary and import forms
	 * @throws Exception
	 */
	protected void importInfectionSite() throws Exception {
		switch (rsCases.getInt("COD_FORMA")) {
		case 1:
			tbcase.setInfectionSite( InfectionSite.PULMONARY );
			break;
		case 3:
			tbcase.setInfectionSite( InfectionSite.EXTRAPULMONARY );
			break;
		default:
			tbcase.setInfectionSite( InfectionSite.BOTH );
		}

		// get pulmonary type
		int val = rsCases.getInt("COD_RAIOX_TORAX");
		FieldValue fieldValue = null;
		FieldValue resXRay = null;
		if (tbcase.isPulmonary()) {
			String code = null;
			if (val == 1) // unilateral cavitária
				code = "UC";
			else
			if (val == 2) // unilateral não cavitária
				code = "UNC";
			else
			if (val == 3) // bilateral cavitária
				code = "BC";
			else
			if (val == 4) // bilateral não cavitária
				code = "BNC";
			else
			if (val == 5) // normal
				code = "N";
			fieldValue = getFieldValue(TbField.PULMONARY_TYPES, code);
			resXRay = getFieldValue(TbField.XRAYPRESENTATION, code);
		}
		tbcase.setPulmonaryType(fieldValue);
		
		if (resXRay != null) {
			ExamXRay xray = new ExamXRay();
			xray.setDate(tbcase.getRegistrationDate());
			xray.setPresentation(resXRay);
			xray.setTbcase(tbcase);
			tbcase.getResXRay().add(xray);
		}

		// get extrapulmonary types
		if (tbcase.isExtrapulmonary()) {
			fieldValue = getFieldValue(TbField.EXTRAPULMONARY_TYPES, Integer.toString( rsCases.getInt("TIPO_EXTRAPULMONAR1") ));
			tbcase.setExtrapulmonaryType(fieldValue);
			
			fieldValue = getFieldValue(TbField.EXTRAPULMONARY_TYPES, Integer.toString( rsCases.getInt("TIPO_EXTRAPULMONAR2") ));
			tbcase.setExtrapulmonaryType2(fieldValue);
		}
	}

	/**
	 * Import patient address
	 * @param tbcase
	 */
	protected void importPatientAddress() {
		try {
			Integer codEnder = rsCases.getInt("COD_ENDERECO");
			
			if (codEnder == null)
				return;
			
			String sql = "select * from ENDERECO_PACIENTE where COD_ENDERECO = " + codEnder.toString();

			Connection conn = getConnection();
			try {
				ResultSet rs = conn.createStatement().executeQuery(sql);
				
				if (!rs.next()) {
					rs.getStatement().close();
					return;
				}
				
				Address aux = tbcase.getNotifAddress();
				aux.setAddress(rs.getString("ENDERECO"));

				String codMunicipio = rs.getString("COD_MUNICIPIO");
				String codUf = rs.getString("SIGLA_UF");
				AdministrativeUnit adm;
				if ((codMunicipio == null) || (codMunicipio.isEmpty()))
					 adm = loadUF(codUf);
				else adm = loadMunicipio(codMunicipio);
				aux.setAdminUnit(adm);

				aux.setComplement(rs.getString("COMPLEMENTO"));
				aux.setZipCode(rs.getString("CEP"));
				caseData.setNotifAddressNumber(rs.getString("NUMERO_ENDERECO"));
				caseData.setNotifDistrict(rs.getString("BAIRRO"));
				tbcase.setPhoneNumber(rs.getString("TELEFONE"));
				tbcase.setMobileNumber(rs.getString("CELULAR"));
				
				rs.getStatement().close();
			}
			finally {
				conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	/**
	 * Load a TB Unit by its legacyId
	 * @param legacyID
	 * @return
	 */
	public Tbunit loadTBUnit(Integer legacyID) {
		List<Tbunit> lst = entityManager.createQuery("from Tbunit u where u.legacyId = :id and u.workspace.id = #{defaultWorkspace.id}")
			.setParameter("id", legacyID.toString())
			.getResultList();
		
		if (lst.size() > 0)
			return lst.get(0);
		try {
			Connection conn = getConnection();
			try {
				ResultSet rs = conn.createStatement().executeQuery("select * from UNIDADE_SAUDE where COD_US = " + legacyID.toString());

				if (!rs.next()) {
					rs.getStatement().close();
					return null;
				}
				
				Tbunit unit = new Tbunit();
				
				unit.getName().setName1(rs.getString("NOME"));
				unit.setHealthSystem(getHealthSystem());
				unit.setAddress(rs.getString("ENDERECO"));
				unit.setBatchControl(true);
				unit.setChangeEstimatedQuantity(true);
				unit.setDistrict(rs.getString("COMPLEMENTO"));
				unit.setLegacyId(legacyID.toString());
				unit.setMdrHealthUnit(true);
				unit.setTbHealthUnit(false);
				unit.setNotifHealthUnit(true);
				unit.setNumDaysOrder(90);
				unit.setMedicineStorage(true);
				AdministrativeUnit adm = loadMunicipio(rs.getString("COD_MUNICIPIO"));
				unit.setAdminUnit(adm);
				unit.setWorkspace(defaultWorkspace);
				
				entityManager.persist(unit);
				entityManager.flush();
				rs.getStatement().close();
				System.out.println("NOVA UNIDADE: " + unit.getName().toString());

				return unit;
			}
			finally {
				conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * Load a laboratory by its legacyId
	 * @param legacyID
	 * @return
	 */
	public Laboratory loadLaboratory(Integer legacyID) {
		List<Laboratory> lst = entityManager.createQuery("from Laboratory u where u.legacyId = :id and u.workspace.id = #{defaultWorkspace.id}")
			.setParameter("id", legacyID.toString())
			.getResultList();
		
		if (lst.size() > 0)
			return lst.get(0);
		try {
			Connection conn = getConnection();
			try {
				ResultSet rs = conn.createStatement().executeQuery("select * from LABORATORIO where COD_LABORATORIO = " + legacyID.toString());

				if (!rs.next()) {
					rs.getStatement().close();
					return null;
				}
				
				Laboratory lab = new Laboratory();
				
				lab.setName(rs.getString("NOME"));
				lab.setAbbrevName(lab.getName());
				AdministrativeUnit adm = loadUF(rs.getString("SIGLA_UF"));
				lab.setAdminUnit(adm);
				lab.setLegacyId(legacyID.toString());
				lab.setWorkspace(defaultWorkspace);
				
				entityManager.persist(lab);
				entityManager.flush();
				rs.getStatement().close();
				System.out.println("NOVO LAB: " + lab.getName());

				return lab;
			}
			finally {
				conn.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}



	/**
	 * Load an administrative unit "Município" by its legacyId
	 * @param legacyId
	 * @return
	 */
	protected AdministrativeUnit loadMunicipio(String legacyId) {
		List<AdministrativeUnit> lst = entityManager.createQuery("from AdministrativeUnit a " +
				"where a.legacyId = :id and a.workspace.id = #{defaultWorkspace.id}")
				.setParameter("id", legacyId)
				.getResultList();
		
		if (lst.size() > 0)
			return lst.get(0);
		
		try {
			Connection conn = getConnection();
			try {
				ResultSet rs = conn.createStatement().executeQuery("select * from MUNICIPIO where COD_MUNICIPIO = '" + legacyId + "'");

				if (!rs.next()) {
					rs.getStatement().close();
					return null;
				}
				
				AdministrativeUnit adm = loadUF(rs.getString("SIGLA_UF"));
				Integer parentId = adm.getId();
				
				adminUnitHome.setId(null);
				adminUnitHome.setParentId(parentId);

				adm = adminUnitHome.getInstance();
				adm.setCountryStructure(getMunicipioStructure());
				adm.getName().setName1(rs.getString("NOME"));
				adm.setLegacyId(legacyId);
				adminUnitHome.persist();
				
				System.out.println("NOVO MUNICIPIO: " + adm.getName().toString());
				rs.getStatement().close();
				
				return adm;
			}
			finally {
				conn.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	


	/**
	 * Load an administrative unit "UF" by its legacyId
	 * @param legacyId
	 * @return
	 */
	protected AdministrativeUnit loadUF(String legacyId) {
		List<AdministrativeUnit> lst = entityManager.createQuery("from AdministrativeUnit a " +
				"where a.legacyId = :id and a.workspace.id = #{defaultWorkspace.id}")
				.setParameter("id", legacyId)
				.getResultList();
		
		if (lst.size() > 0)
			return lst.get(0);
		
		try {
			Connection conn = getConnection();
			try {
				ResultSet rs = conn.createStatement().executeQuery("select * from UF where sigla_uf = '" + legacyId + "'");

				if (!rs.next()) {
					rs.getStatement().close();
					return null;
				}
				
				adminUnitHome.setId(null);
				AdministrativeUnit adm = adminUnitHome.getInstance();
				adm.setCountryStructure(getUFStructure());
				adm.getName().setName1(rs.getString("NOME"));
				adm.setLegacyId(legacyId);
				adminUnitHome.persist();
				
				System.out.println("NOVA UF: " + adm.getName());
				rs.getStatement().close();
				
				return adm;
			}
			finally {
				conn.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

	/**
	 * Import patient data
	 * @return
	 * @throws Exception
	 */
	public Patient importPatient() throws Exception {
		String pacid = rsCases.getString("Cod_Paciente");
		List<Patient> lst = entityManager
			.createQuery("from Patient p where p.legacyId = :id and p.workspace.id = #{defaultWorkspace.id})")
			.setParameter("id", pacid)
			.getResultList();
		
		Patient p;
		if (lst.size() > 0)
			 p = lst.get(0);
		else {
			p = new Patient();
			p.setLegacyId(pacid);
			p.setWorkspace(defaultWorkspace);
		}
		
		p.setName(rsCases.getString("nome"));
		p.setBirthDate(rsCases.getDate("data_nascimento"));
		
		Gender gender = null;
		int sexo = rsCases.getInt("SEXO");
		if (sexo == 1)
			gender = Gender.MALE;
		else 
		if (sexo == 2)
			gender = Gender.FEMALE;
		
		p.setGender(gender);
		p.setMotherName(rsCases.getString("NOME_MAE"));
		p.setSecurityNumber(rsCases.getString("NUM_CARTAO_SUS"));
		p.setRecordNumber(rsCases.getInt("NUM_TBMR"));
		if (p.getRecordNumber() == 0)
			p.setRecordNumber(null);
		
		entityManager.persist(p);
		entityManager.flush();
		System.out.println("Caso importado: " + p.getName());
		
		return p;
	}


	/**
	 * Update case outcome based on the latest quartely form
	 * @throws SQLException 
	 */
	protected void updateCaseOutcome() throws SQLException {
		String sql = "select ft.cod_resultado_tratamento, f.data " +
				"from ficha_trimestral ft " + 
				"inner join ficha f on f.num_ficha = ft.num_ficha " + 
				"where f.cod_caso = " + rsCases.getString("COD_CASO") +  
				" and f.data = (select max(f2.data) from ficha f2 " +
				"where f2.cod_caso = f.cod_caso)";
		
		Connection conn = getConnection();
		try {
			ResultSet rs = conn.createStatement().executeQuery(sql);
			
			CaseState st = CaseState.ONTREATMENT;
			if (rs.next()) {
				int val = rs.getInt("COD_RESULTADO_TRATAMENTO");
				switch (val) {
				case 2:
					st = CaseState.FAILED;
					break;
				case 3:
					st = CaseState.CURED;
					break;
				case 4:
					st = CaseState.DEFAULTED;
					break;
				case 5:
					st = CaseState.DIED;
					break;
				case 6:
					st = CaseState.DIED_NOTTB;
					break;
				case 8:
					st = CaseState.OTHER;
				}
			}
			tbcase.setState(st);
			if (st != CaseState.ONTREATMENT) {
				tbcase.setOutcomeDate(rs.getDate("DATA"));
			}
		}
		finally {
			conn.close();
		}
	}
	
	/**
	 * Load cases to import
	 * @throws SQLException 
	 */
	protected void loadCasesToImport() throws SQLException {
		if (uf == null)
			return;
		
		String sql = "select * from ficha f " +
			"inner join caso_tbmr c on c.cod_caso = f.cod_caso " +
        	"inner join ficha_notificacao fn on fn.num_ficha = f.num_ficha " +
        	"inner join paciente p on p.cod_paciente = f.cod_paciente " +
        	"where exists(select * from unidade_saude u where u.cod_us = f.cod_us_tratamento and u.sigla_uf = '" + uf + "')";
		connection = getConnection();
		Statement stm = connection.createStatement();
		rsCases = stm.executeQuery(sql);
	}


	/**
	 * Return a JDBC connection to Brazilian "Sistema TBMR" database
	 * @return
	 */
	protected Connection getConnection() {
		if (tbmrDataSource == null) {
			try {
				InitialContext initialContext = new InitialContext();
				tbmrDataSource = (DataSource)initialContext.lookup("java:tbmrDatasource");
			} catch (NamingException e) {
				return null;
			}
		}

		try {
			return tbmrDataSource.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}			
	}


	public String getUf() {
		return uf;
	}


	public void setUf(String uf) {
		this.uf = uf;
	}


	public List<SelectItem> getUfList() {
		if (ufList == null) {
			ufList = new ArrayList<SelectItem>();
			
			List<Object[]> lst = entityManager.createQuery("select a.legacyId, a.name.name1 " +
					"from AdministrativeUnit a where a.parent is null " +
					"and a.workspace.id = #{defaultWorkspace.id}")
					.getResultList();

			ufList.add(new SelectItem(null, "-"));
			for (Object[] vals: lst) {
				ufList.add(new SelectItem(vals[0].toString(), vals[1].toString()));
			}
		}
		return ufList;
	}
	
	
	/**
	 * Return country structure of level 2 from Brazil
	 * @return
	 */
	protected CountryStructure getMunicipioStructure() {
		for (CountryStructure aux: getStructures()) {
			if (aux.getLevel() == 2)
				return aux;
		}
		return null;
	}


	/**
	 * Return country structure of level 1 from brazil
	 * @return
	 */
	protected CountryStructure getUFStructure() {
		for (CountryStructure aux: getStructures()) {
			if (aux.getLevel() == 1)
				return aux;
		}
		return null;
	}
	
	
	/**
	 * Return list of country structure
	 * @return
	 */
	public List<CountryStructure> getStructures() {
		if (structures == null) {
			structures = entityManager
				.createQuery("from CountryStructure where workspace.id = #{defaultWorkspace.id}")
				.getResultList();
		}

		return structures;
	}

	
	/**
	 * Return field value of the given TB field type and custom ID 
	 * @param fieldType
	 * @param customId
	 * @return
	 */
	public FieldValue getFieldValue(TbField fieldType, String customId) {
		for (FieldValue fld: getFieldOptions(fieldType)) {
			String s = fld.getCustomId(); 
			if ((s != null) && (s.equals(customId))) {
				return fld;
			}
		}
		return null;
	}

	/**
	 * Return options for field
	 * @param fieldType
	 * @return
	 */
	public List<FieldValue> getFieldOptions(TbField fieldType) {
		fieldsQuery.setField(fieldType);
		return fieldsQuery.getValues();
	}
	
	
	protected HealthSystem getHealthSystem() {
		if (healthSystem == null) {
			List<HealthSystem> lst = entityManager.createQuery("from HealthSystem a where a.workspace.id = #{defaultWorkspace.id}").getResultList();
			if (lst.size() == 0)
				throw new RuntimeException("Nenhuma unidade de saúde cadastrada");
			healthSystem = lst.get(0);
		}
		return healthSystem;
	}


	protected Regimen getRegimen() {
		if (regimen == null) {
			regimen = entityManager.find(Regimen.class, regimenId);
		}
		return regimen;
	}
	
	/**
	 * Refresh entity manager and force entities to be reloaded
	 */
	private void refreshEntityManager() {
		entityManager.flush();
		entityManager.clear();
		structures = null;
		healthSystem = null;
		regimen = null;
		substances = null;
		fieldsQuery.refreshLists();
	}
	
	
	/**
	 * Return list of substances
	 * @return
	 */
	protected List<Substance> getSubstances() {
		if (substances == null) {
			substances = entityManager
				.createQuery("from Substance s where s.workspace.id = #{defaultWorkspace.id}")
				.getResultList();
		}
		return substances;
	}
}
