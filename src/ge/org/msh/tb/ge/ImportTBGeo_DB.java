package org.msh.tb.ge;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.adminunits.AdminUnitHome;
import org.msh.tb.br.CaseDataBRHome;
import org.msh.tb.br.entities.MedicalExaminationBR;
import org.msh.tb.br.entities.TbCaseBR;
import org.msh.tb.br.entities.enums.FailureType;
import org.msh.tb.br.entities.enums.TipoResistencia;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.exams.ExamCultureHome;
import org.msh.tb.cases.exams.ExamDSTHome;
import org.msh.tb.cases.exams.ExamMicroscopyHome;
import org.msh.tb.cases.treatment.StartTreatmentHome;
import org.msh.tb.entities.Address;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.CaseComorbidity;
import org.msh.tb.entities.CountryStructure;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.ExamHIV;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.ExamXRay;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.HealthSystem;
import org.msh.tb.entities.Laboratory;
import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.PrevTBTreatment;
import org.msh.tb.entities.Regimen;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.TreatmentHealthUnit;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.DrugResistanceType;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.entities.enums.Nationality;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.PrevTBTreatmentOutcome;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.entities.enums.XRayEvolution;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.tb.misc.FieldsQuery;
import org.msh.utils.TransactionalBatchComponent;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

import com.sun.rowset.CachedRowSetImpl;


/**
 * Import case data from the Brazilian MDR-TB system "Sistema TBMR" to the e-TB Manager database.
 * The connection to the Br MDR-TB is done using a jndi lookup to the "java:tbmrDatasource" name. This name is 
 * supposed to return a {@link DataSource} instance, where the class uses this datasource 
 * to read data from the Br MDR-TB database.
 * @author Ricardo Memoria
 *
 */
@Name("importTBGeo_DB")
public class ImportTBGeo_DB extends TransactionalBatchComponent {

	/**
	 * Standard regimen used during importing
	 */
	private static final int regimenId = 940665;
	
	private static final int sourceId = 22107;
	
	@In(create=true) FacesMessages facesMessages;
	@In(create=true) CaseDataBRHome caseDataBRHome;
	@In(create=true) CaseHome caseHome;
	@In(create=true) AdminUnitHome adminUnitHome;
	@In(create=true) ExamCultureHome examCultureHome;
	@In(create=true) ExamMicroscopyHome examMicroscopyHome;
	@In(create=true) ExamDSTHome examDSTHome;
	@In(create=true) FieldsQuery fieldsQuery;
	@In(create=true) StartTreatmentHome startTreatmentHome;
//	@In(create=true) TreatmentHealthUnitHome treatmentHealthUnitHome;

	private Workspace defaultWorkspace;
	private EntityManager entityManager;
	private DataSource tbmrDataSource;
	private Connection connection;
	private CachedRowSet rsCases;
	private String uf;
	private List<CountryStructure> structures;
	private TbCaseBR tbcase;
	private HealthSystem healthSystem;
	private Regimen regimen;
	private Source source;
	private List<Substance> substances;
	private List<Medicine> medicines;
	private Date refDate;
	private List<Integer> fichas;
	
	/**
	 * Number of cases imported
	 */
	private int counter;

	
	@Asynchronous
	public boolean importCases(String uf, Workspace workspace, UserLogin userLogin) throws SQLException {
		defaultWorkspace = workspace;
		this.uf = uf;
		Contexts.getEventContext().set("userLogin", userLogin);
		Contexts.getEventContext().set("defaultWorkspace", defaultWorkspace);

		connection = getConnection();
		try {
			return execute();
		}
		finally {
			connection.close();
		}
	}


	/* (non-Javadoc)
	 * @see org.msh.utils.TransactionalBatchComponent#executeIteration()
	 */
	@Override
	public boolean executeIteration() throws Exception {
		entityManager = getEntityManager();
		Integer numFicha = fichas.get(counter);

		// initialize variables for the next loop
		structures = null;
		healthSystem = null;
		regimen = null;
		substances = null;
		source = null;
		medicines = null;
		fieldsQuery.refreshLists();
		
		loadCaseToImport(numFicha);
		try {
			if (rsCases.next())
				importCase();
		}
		finally {
			rsCases.close();
		}

		counter++;
		return (counter < fichas.size());
	}


	/* (non-Javadoc)
	 * @see org.msh.utils.TransactionalBatchComponent#startExecution()
	 */
	@Override
	protected void startExecution() throws Exception {
		counter = 0;

		// carrega o numero das fichas
		String sql = "select num_ficha from ficha f " +
        	"where f.tipo_ficha=1 and exists(select * from unidade_saude u where u.cod_us = f.cod_us_tratamento and u.sigla_uf = '" + uf + "')";

		Statement stm = connection.createStatement();
		ResultSet rs = stm.executeQuery(sql);

		try {
			fichas = new ArrayList<Integer>();
			while (rs.next()) {
				fichas.add(rs.getInt("NUM_FICHA"));
			}
			System.out.println( Integer.toString(fichas.size()) + " casos para importar...");
		}
		finally {
			rs.getStatement().close();
		}
	}

	
	/* (non-Javadoc)
	 * @see org.msh.utils.TransactionalBatchComponent#finishExecution()
	 */
	@Override
	protected void finishExecution() throws Exception {
		if (connection != null)
			connection.close();
		System.out.println(Integer.toString(counter - 1) + " casos importados.");
	}

	
	/**
	 * Import a case
	 * @throws Exception
	 */
	protected void importCase() throws Exception {
		Patient p = importPatient();

		System.out.println("FICHA = " + rsCases.getString("NUM_FICHA"));
		
		String numFicha = rsCases.getString("COD_CASO");
		String sql = "select c.id from TbCase c where c.legacyId = :id and c.patient.workspace.id = #{defaultWorkspace.id}";
		List<Integer> lst = entityManager.createQuery(sql)
			.setParameter("id", numFicha)
			.getResultList();

		caseHome.setTransactionLogActive(false);
		caseHome.setDisplayMessage(false);
		caseHome.setCheckSecurityOnOpen(false);

		if (lst.size() > 0) {
			entityManager.createQuery("delete from TbCase c where c.id = :id").setParameter("id", lst.get(0)).executeUpdate();
//			caseHome.setId(lst.get(0));
//			caseHome.remove();
		}

		// initialize object
		caseHome.clearInstance();
		caseHome.setId(null);

		tbcase = (TbCaseBR)caseHome.getInstance();
		
		tbcase.setPatient(p);
		tbcase.setLegacyId(rsCases.getString("COD_CASO"));

		refDate = rsCases.getDate("DATA_NOTIFICACAO");
		tbcase.setRegistrationDate(refDate);
		tbcase.setDiagnosisDate(refDate);
		tbcase.setAge(DateUtils.yearsBetween(tbcase.getRegistrationDate(), p.getBirthDate()));
		tbcase.setCaseNumber(rsCases.getInt("NUM_CASO"));
		if (tbcase.getCaseNumber() == 0)
			tbcase.setCaseNumber(null);
		tbcase.setClassification(CaseClassification.DRTB);
		
/*		int val = rsCases.getInt("COD_CASO_TBMR");
		if (val == 1)
			 tbcase.setDiagnosisType(DiagnosisType.CONFIRMED);
		else tbcase.setDiagnosisType(DiagnosisType.SUSPECT);
*/		
		tbcase.setDiagnosisType(DiagnosisType.CONFIRMED);
		
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
		tbcase.setOwnerUnit(unit);
		
		tbcase.setValidationState(ValidationState.WAITING_VALIDATION);
		
		String codMun = rsCases.getString("COD_MUN_US_ORIGEM");
		AdministrativeUnit municipio = loadMunicipio(codMun);
		tbcase.setAdminUnitUsOrigem(municipio);
		tbcase.setUsOrigem(rsCases.getString("NOME_US_ORIGEM"));

		FieldValue fieldValue = getFieldValue(TbField.CONTAG_PLACE, rsCases.getString("COD_LOCAL_CONTAGIO"));  
		tbcase.getContagPlace().setValue(fieldValue);
		
		fieldValue = getFieldValue(TbField.EDUCATIONAL_DEGREE, rsCases.getString("ESCOLARIDADE"));
		tbcase.getEducationalDegree().setValue(fieldValue);

		fieldValue = getFieldValue(TbField.POSITION, rsCases.getString("OCUPACAO"));
		tbcase.getPosition().setValue(fieldValue);
		
		fieldValue = getFieldValue(TbField.SKINCOLOR, rsCases.getString("COR"));
		tbcase.getSkinColor().setValue(fieldValue);

		if (numCasosAnt == 0)
			 tbcase.setFailureType(FailureType.FIRST_TREATMENT);
		else tbcase.setFailureType(FailureType.RETREATMENT);
		
		int val = rsCases.getInt("TIPO_RESISTENCIA");
		if (val == 1)
			 tbcase.setTipoResistencia(TipoResistencia.PRIMARIA);
		else tbcase.setTipoResistencia(TipoResistencia.ADQUIRIDA);
		
		tbcase.setNumSinan(rsCases.getString("NUM_SINAN"));
		
		updateCaseOutcome();
		importMedicalExamination(rsCases);
		updateCaseTreatment();
		
		caseHome.persist();
		tbcase = (TbCaseBR)caseHome.getInstance();
		
		System.out.println("case id = " + tbcase.getId() + " (" + tbcase.getPatient().getFullName() + ")");
		
//		caseData.setTbcase(tbcase);
//		caseData.setId(tbcase.getId());
//		caseDataBRHome.setDisplayMessage(false);
//		caseDataBRHome.persist();
		
		importExams(rsCases);
		importDSTExam(rsCases);
		importPrevTBTreatment();

		importQuarterlyData();
	}


	/**
	 * Import exams results from notification form
	 * @throws Exception
	 */
	protected void importExams(ResultSet rs) throws Exception {
		int res = rs.getInt("RES_CULTURA_ESCARRO");
		int res2 = rs.getInt("RES_BACILOSCOPIA_ESCARRO");
		
		if ((res == 5) && (res2 == 4))
			return;
		
		Laboratory lab = loadLaboratory(rs.getInt("COD_LABORATORIO"));

		Date dtColeta = rs.getDate("DATA_CULTURA_ESCARRO");
		if (dtColeta == null)
			return;
		
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
			String cod = rs.getString("COD_METODO");
			if ("5".equals(cod)) {
				cod = "6";
			}
			FieldValue method = getFieldValue(TbField.CULTURE_METHOD, cod);
			culture.setMethod(method);

			examCultureHome.getInstance().setDateCollected(dtColeta);
			examCultureHome.getInstance().setTbcase(tbcase);
			examCultureHome.setDisplayMessage(false);
			examCultureHome.setTransactionLogActive(false);
			examCultureHome.persist();
		}
		
		MicroscopyResult resMicroscopy = null;
		switch (res2) {
		case 0:
			resMicroscopy = MicroscopyResult.NEGATIVE;
			break;
		case 1:
			resMicroscopy = MicroscopyResult.PLUS;
			break;
		case 2:
			resMicroscopy = MicroscopyResult.PLUS2;
			break;
		case 3:
			resMicroscopy = MicroscopyResult.PLUS3;
		}
		
		if (resMicroscopy != null) {
			examMicroscopyHome.setId(null);
			ExamMicroscopy sputum = examMicroscopyHome.getInstance();
			sputum.setResult(resMicroscopy);
			sputum.setLaboratory(lab);
			sputum.setDateCollected(dtColeta);
			examMicroscopyHome.setDisplayMessage(false);
			examMicroscopyHome.setTransactionLogActive(false);
			examMicroscopyHome.persist();
		}
	}

	
	private void importDSTExam(ResultSet rs) throws Exception {
		Date dtColeta = rs.getDate("DATA_CULTURA_ESCARRO");
		if (dtColeta == null)
			return;

		Laboratory lab = loadLaboratory(rs.getInt("COD_LABORATORIO"));

		examDSTHome.setId(null);
		examDSTHome.setInstance(null);
		for (ExamDSTResult resTest: examDSTHome.getItems())
			resTest.setResult(DstResult.NOTDONE);
		checkResultDST(rs, "COD_PADRAO_RES_RIFAMPICINA", "R");
		checkResultDST(rs, "COD_PADRAO_RES_PIRAZINAMIDA", "Z");
		checkResultDST(rs, "COD_PADRAO_RES_ETIONAMIDA", "Et");
		checkResultDST(rs, "COD_PADRAO_RES_AMICACINA", "Am");
		checkResultDST(rs, "COD_PADRAO_RES_CAPREOMICINA", "Cp");
		checkResultDST(rs, "COD_PADRAO_RES_OFLOXACINO", "Ofx");
		checkResultDST(rs, "COD_PADRAO_RES_MOXIFLOXACINO", "Mfx");
		checkResultDST(rs, "COD_PADRAO_RES_ISONIAZIDA", "H");
		checkResultDST(rs, "COD_PADRAO_RES_ETAMBUTOL", "E");
		checkResultDST(rs, "COD_PADRAO_RES_ESTREPTOMICINA", "S");
		checkResultDST(rs, "COD_PADRAO_RES_KANAMICINA", "Km");
		checkResultDST(rs, "COD_PADRAO_RES_CIPROFLOXACINO", "Cfx");
		checkResultDST(rs, "COD_PADRAO_RES_LEVOFLOXACINO", "Lfx");
		checkResultDST(rs, "COD_PADRAO_RES_TERIZIDONA", "Tzd");

		System.out.println("***** data coleta = " + dtColeta);
		
		ExamDST dst = examDSTHome.getInstance();
		dst.setLaboratory(lab);
		examDSTHome.getInstance().setDateCollected(dtColeta);
		examDSTHome.setDisplayMessage(false);
		examDSTHome.setTransactionLogActive(false);
		examDSTHome.persist();		
	}
	
	
	/**
	 * Check the result of a substance in the DST exam
	 * @param field
	 * @param subAbbrevName
	 * @throws Exception
	 */
	private void checkResultDST(ResultSet rs, String field, String subAbbrevName) throws Exception {
		String resdst = rs.getString(field);
		if ((resdst == null) || (resdst.isEmpty())) 
			return;
		
		int resDst = rs.getInt(field);

		if ((resDst != 0) && (resDst != 1))
			return;
		
		DstResult resTest;
		if (resDst == 1)
			 resTest = DstResult.RESISTANT;
		else resTest = DstResult.SUSCEPTIBLE;
		
		ExamDSTResult res = null;
		for (ExamDSTResult aux: examDSTHome.getItems()) {
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
		String sql = "select * from TRATAMENTOS_ANTERIORES where num_ficha = " + rsCases.getString("NUM_FICHA");
		
		ResultSet rs = connection.createStatement().executeQuery(sql);
		
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
			aux.setTbcase(tbcase);
			
			entityManager.persist(aux);
			entityManager.flush();
		}
		rs.getStatement().close();
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
	protected void importMedicalExamination(ResultSet rs) throws Exception {
		MedicalExamination medExam = new MedicalExaminationBR();
		
		medExam.setTbcase(tbcase);
		medExam.setWeight(rs.getDouble("PESO_ATUAL"));
		medExam.setDate(refDate);
		medExam.setResponsible(rs.getString("NOME_RESP_PREENCHIMENTO"));
		medExam.setPositionResponsible(rs.getString("PROF_RESP_PREENCHIMENTO"));
		medExam.setComments(rs.getString("OUTRAS_INFORMACOES"));
		int val = rs.getInt("TRATAMENTO_SUPERVISIONADO");
		YesNoType resp = null;
		if (val == 1) 
			 resp = YesNoType.YES;
		else resp = YesNoType.NO;
		medExam.setSupervisedTreatment(resp);
		medExam.setSupervisionUnitName(rs.getString("UNIDADE_SUPERVISAO"));
		
		tbcase.getExaminations().add(medExam);
	}


	/**
	 * Update treatment of the current case
	 * @throws Exception 
	 * @throws SQLException 
	 */
	protected void updateCaseTreatment() throws Exception {
		tbcase.getHealthUnits().clear();
		
		Date dtIni = tbcase.getRegistrationDate();
		Date dtEnd = tbcase.getOutcomeDate();
		if (dtEnd == null)
			dtEnd = DateUtils.incMonths(dtIni, 18);			

		Period p = new Period();
		p.setIniDate(dtIni);
		p.setEndDate(dtEnd);
		tbcase.setTreatmentPeriod(p);
		tbcase.setRegimen(getRegimen());
		tbcase.setIniContinuousPhase( DateUtils.incMonths(p.getIniDate(), 6) );
		
		tbcase.getPrescribedMedicines().clear();
		List<PrescribedMedicine> lst = getMedicinesPrescribed(rsCases.getInt("NUM_FICHA"));
		for (PrescribedMedicine pm: lst) {
			pm.setPeriod(new Period(p));
			pm.setTbcase(tbcase);
			pm.setSource(getSource());
			tbcase.getPrescribedMedicines().add(pm);
		}
		
		TreatmentHealthUnit hu = new TreatmentHealthUnit();
		hu.setPeriod(new Period(p));
		hu.setTbCase(tbcase);
		hu.setTbunit(tbcase.getNotificationUnit());
		hu.setTransferring(false);
		tbcase.getHealthUnits().add(hu);
		
/*		startTreatmentHome.setIniTreatmentDate(dtIni);
		startTreatmentHome.setEndTreatmentDate(dtEnd);

		startTreatmentHome.getTbunitselection().setTbunit(tbcase.getNotificationUnit());
		
		startTreatmentHome.setUseDefaultDoseUnit(true);
		startTreatmentHome.setRegimen(getRegimen());
		startTreatmentHome.updatePhases();
		
		startTreatmentHome.startStandardRegimen();
*/		

		CaseState st = tbcase.getState();
		if ((st == null) || (st.ordinal() <= CaseState.ONTREATMENT.ordinal()))
			tbcase.setState(CaseState.ONTREATMENT);
	}

	
	/**
	 * Return a ResultSet containing the medicines prescribed in a specific form
	 * @param numFicha
	 * @return
	 * @throws Exception 
	 */
	protected List<PrescribedMedicine> getMedicinesPrescribed(Integer numFicha) throws Exception {
		String sql = "select p.cod_produto, am.cod_medicamento, am.cod_apresentacao, quantidade, fator_multiplicativo " +
			"from MEDICAMENTOS_FICHA mf " +
			"inner join apresentacao_medicamento am on am.cod_medicamento=mf.cod_medicamento and am.cod_apresentacao=mf.cod_apresentacao " +
			"inner join produto_medicamento p on p.cod_produto=am.cod_produto " +
			"where mf.num_ficha = " + numFicha.toString();

		List<PrescribedMedicine> lst = new ArrayList<PrescribedMedicine>();
		
		ResultSet rs = connection.createStatement().executeQuery(sql);
		while (rs.next()) {
			String codProd = rs.getString("COD_PRODUTO");
			Medicine med = findMedicineByLegacyId(codProd);
			if (med != null) {
				PrescribedMedicine pm = new PrescribedMedicine();
				pm.setMedicine(med);
				pm.setDoseUnit(rs.getInt("FATOR_MULTIPLICATIVO"));
				pm.setFrequency(7);
				lst.add(pm);
			}
		}
		rs.getStatement().close();

		return lst;
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
		CaseComorbidity caseComorbidity = new CaseComorbidity();
		caseComorbidity.setTbcase(tbcase);
		caseComorbidity.setComorbidity(value);
		tbcase.getComorbidities().add(caseComorbidity);
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
//		FieldValue resXRay = null;
		if (tbcase.isPulmonary()) {
			String code = fieldCodePulmonaryForm(val);
			fieldValue = getFieldValue(TbField.PULMONARY_TYPES, code);
//			resXRay = getFieldValue(TbField.XRAYPRESENTATION, code);
		}
		tbcase.setPulmonaryType(fieldValue);

		importXRay(rsCases, true);
/*		if (resXRay != null) {
			ExamXRay xray = new ExamXRay();
			xray.setDate(tbcase.getRegistrationDate());
			xray.setPresentation(resXRay);
			xray.setTbcase(tbcase);
			tbcase.getResXRay().add(xray);
		}
*/
		// get extrapulmonary types
		if (tbcase.isExtrapulmonary()) {
			fieldValue = getFieldValue(TbField.EXTRAPULMONARY_TYPES, Integer.toString( rsCases.getInt("TIPO_EXTRAPULMONAR1") ));
			tbcase.setExtrapulmonaryType(fieldValue);
			
			fieldValue = getFieldValue(TbField.EXTRAPULMONARY_TYPES, Integer.toString( rsCases.getInt("TIPO_EXTRAPULMONAR2") ));
			tbcase.setExtrapulmonaryType2(fieldValue);
		}
	}

	
	protected String fieldCodePulmonaryForm(int val) {
		if (val == 1) // unilateral cavitária
			return "UC";
		else
		if (val == 2) // unilateral não cavitária
			return "UNC";
		else
		if (val == 3) // bilateral cavitária
			return "BC";
		else
		if (val == 4) // bilateral não cavitária
			return "BNC";
		else
		if (val == 5) // normal
			return "N";
		return null; 
	}

	
	/**
	 * Import X-Ray exam
	 * @throws SQLException
	 */
	protected void importXRay(ResultSet rs, boolean primeiro) throws SQLException {
		if (!tbcase.isPulmonary())
			return;
		
		int val = 0;
		
		XRayEvolution evolution = null;
		
		if (primeiro)
			 val = rs.getInt("COD_RAIOX_TORAX");
		else {
			if (rs.getInt("EXAME_RADIOLOGICO") == 2)
				return;
			val = rs.getInt("COD_APRESENTACAO_RADIOLOGICA");
			switch (rs.getInt("COD_EVOLUCAO_RADIOLOGICA")) {
			case 1: evolution = XRayEvolution.IMPROVED; break;
			case 2: evolution = XRayEvolution.PROGRESSED; break;
			case 3: evolution = XRayEvolution.STABLE; break;
			}
		}
		
		String code = fieldCodePulmonaryForm(val);
		FieldValue resXRay = getFieldValue(TbField.XRAYPRESENTATION, code);
		
		if (resXRay != null) {
			ExamXRay xray = new ExamXRay();
			xray.setDate(refDate);
			xray.setPresentation(resXRay);
			xray.setTbcase(tbcase);
			xray.setEvolution(evolution);
			tbcase.getResXRay().add(xray);
		}
	}


	/**
	 * Import patient address
	 * @param tbcase
	 * @throws Exception 
	 */
	protected void importPatientAddress() throws Exception {
		Integer codEnder = rsCases.getInt("COD_ENDERECO");
		
		if (codEnder == null)
			return;
		
		String sql = "select * from ENDERECO_PACIENTE where COD_ENDERECO = " + codEnder.toString();
		ResultSet rs = connection.createStatement().executeQuery(sql);
		
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
		tbcase.setNotifAddressNumber(rs.getString("NUMERO_ENDERECO"));
		tbcase.setNotifDistrict(rs.getString("BAIRRO"));
		tbcase.setPhoneNumber(rs.getString("TELEFONE"));
		tbcase.setMobileNumber(rs.getString("CELULAR"));
		
		rs.getStatement().close();
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
			ResultSet rs = connection.createStatement().executeQuery("select * from UNIDADE_SAUDE where COD_US = " + legacyID.toString());

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
			unit.setAddressCont(rs.getString("COMPLEMENTO"));
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
			ResultSet rs = connection.createStatement().executeQuery("select * from LABORATORIO where COD_LABORATORIO = " + legacyID.toString());

			if (!rs.next()) {
				rs.getStatement().close();
				return null;
			}
			
			Laboratory lab = new Laboratory();
			
			lab.setName(rs.getString("NOME"));
			String s = lab.getName();
			if (s.length() > 20)
				s = s.substring(0, 19);
			lab.setAbbrevName(s);
			AdministrativeUnit adm = loadUF(rs.getString("SIGLA_UF"));
			lab.setAdminUnit(adm);
			lab.setLegacyId(legacyID.toString());
			lab.setWorkspace(defaultWorkspace);
			
			entityManager.persist(lab);
			entityManager.flush();
			rs.getStatement().close();
			System.out.println("NOVO LAB: " + lab.getName());

			return lab;
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
			ResultSet rs = connection.createStatement().executeQuery("select * from MUNICIPIO where COD_MUNICIPIO = '" + legacyId + "'");

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
			adminUnitHome.setDisplayMessage(false);
			adminUnitHome.setTransactionLogActive(false);
			adminUnitHome.persist();
			
			System.out.println("NOVO MUNICIPIO: " + adm.getName().toString());
			rs.getStatement().close();
			
			return adm;
			
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
			ResultSet rs = connection.createStatement().executeQuery("select * from UF where sigla_uf = '" + legacyId + "'");

			if (!rs.next()) {
				rs.getStatement().close();
				return null;
			}
			
			adminUnitHome.setId(null);
			AdministrativeUnit adm = adminUnitHome.getInstance();
			adm.setCountryStructure(getUFStructure());
			adm.getName().setName1(rs.getString("NOME"));
			adm.setLegacyId(legacyId);
			adminUnitHome.setDisplayMessage(false);
			adminUnitHome.setTransactionLogActive(false);
			adminUnitHome.persist();
			
			System.out.println("NOVA UF: " + adm.getName());
			rs.getStatement().close();
			
			return adm;
			
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
		
		ResultSet rs = connection.createStatement().executeQuery(sql);
		
		if (rs.next()) {
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

				tbcase.setState(st);
				if (CaseState.ONTREATMENT.equals(st)) {
					tbcase.setOutcomeDate(rs.getDate("DATA"));
				}
			}
		}
		
		rs.getStatement().close();
	}
	
	/**
	 * Load cases to import
	 * @throws SQLException 
	 */
	protected void loadCaseToImport(Integer numFicha) throws SQLException {
		if (uf == null)
			return;
		
		String sql = "select * from ficha f " +
			"inner join caso_tbmr c on c.cod_caso = f.cod_caso " +
        	"inner join ficha_notificacao fn on fn.num_ficha = f.num_ficha " +
        	"inner join paciente p on p.cod_paciente = f.cod_paciente " +
        	"where exists(select * from unidade_saude u where u.cod_us = f.cod_us_tratamento and u.sigla_uf = '" + uf + "') " +
        	"and f.num_ficha = " + numFicha.toString();

		rsCases = new CachedRowSetImpl();
		rsCases.setDataSourceName("java:tbmrDatasource");
		rsCases.setCommand(sql);
		rsCases.execute(connection);
/*		casesConnection = getConnection();
		Statement stm = casesConnection.createStatement();
		rsCases = stm.executeQuery(sql);		
*/	}

	
	/**
	 * Import information from quarterly forms 
	 * @throws Exception 
	 */
	protected void importQuarterlyData() throws Exception {
		Integer codCaso = rsCases.getInt("COD_CASO");
		String sql = "select * from ficha f " +
				"inner join ficha_trimestral ft on ft.num_ficha = f.num_ficha " +
				"where f.cod_caso = " + codCaso.toString();
		
		CachedRowSet rs = new CachedRowSetImpl();
		rs.setCommand(sql);
		rs.execute(connection);
//		ResultSet rs = connection.createStatement().executeQuery(sql);
		while (rs.next()) {
			refDate = rs.getDate("DATA");
			
			int novodst = rs.getInt("COD_EXAME_CONFIRMATORIO");

			if (novodst == 1)
				importDSTExam(rs);
			importExams(rs);
			importXRay(rs, false);
			importMedicalExamination(rs);
		}
		rs.close();
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
	
	protected Source getSource() {
		if (source == null)
			source = entityManager.find(Source.class, sourceId);
		return source;
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

	public int getCounter() {
		return counter;
	}

	
	/**
	 * Return list of medicines to be used during importing
	 * @return
	 */
	public List<Medicine> getMedicines() {
		if (medicines == null) {
			medicines = entityManager
				.createQuery("from Medicine m where m.workspace.id = #{defaultWorkspace.id}")
				.getResultList();
		}
		return medicines;
	}


	public Medicine findMedicineByLegacyId(String legacyId) {
		for (Medicine m: getMedicines()) {
			if ((m.getLegacyId() != null) && (m.getLegacyId().equals(legacyId)))
				return m;
		}
		return null;
	}
}
