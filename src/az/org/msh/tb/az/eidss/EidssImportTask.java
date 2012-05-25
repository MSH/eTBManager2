package org.msh.tb.az.eidss;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;

import org.jboss.seam.Component;

import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.ValidationState;
import org.omg.PortableServer.POAManagerPackage.State;

import javax.xml.ws.Service;

/**
 * @author Инка
 *
 */
public class EidssImportTask {
	private EntityManager entityManager;
	//ArrayOfHumanCaseListInfo arrayCaseList;
	//private static EidssServiceSoap soap;
	//private List<HumanCaseInfo> caseInfoList;
	List<String> cases;
	List<CaseInfo> InfoForExport=new ArrayList<CaseInfo>();
	CaseImporting ci;
	
	public void execute() {
		test();	
	
	}
	/*
	public void execute1() {	
		Date d=new Date();
		d.setDate(5);
		d.setMonth(0);
		
		 ci=new CaseImporting();
		soap=loginEIDSS("Republican APS", "Administrator", "EIDSS", "en");
		caseInfoList=new 	ArrayList<HumanCaseInfo>();
		arrayCaseList=getHumanCaseList(d,"786360000000");
		cases=ci.CheckIfExistInEtb(ConvertString(arrayCaseList));
		caseInfoList=GetCaseInfoList(soap,cases,"Alive","Refused");
		InfoForExport=AddAll(caseInfoList);
		ci.importRecords(InfoForExport);
	}
	private List<CaseInfo> AddAll(List<HumanCaseInfo> caseInfoList) {
		List<CaseInfo> fe= new ArrayList<CaseInfo>();
		Iterator<HumanCaseInfo> it=caseInfoList.iterator();
		while (it.hasNext()){
			CaseInfo	onecase=convertRecord(it.next());
			fe.add(onecase);
			
		}
		return fe;
	}
	public  EidssServiceSoap loginEIDSS(String server, String login, String password,String language)
	{

		EidssService serv = new EidssService();

		EidssServiceSoap soap = serv.getEidssServiceSoap();

		Map<String, Object> context = ((BindingProvider)soap).getRequestContext();
		context.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
		soap.login(server, login, password, language);


		return soap;
	}


	private CaseInfo convertRecord(HumanCaseInfo EIDSSData) {
		CaseInfo onecase=new CaseInfo();
	
		String firstName = EIDSSData.getFirstName();
		String lastName = EIDSSData.getLastName();
		String fatherName = EIDSSData.getMiddleName();
		onecase.setLastName(lastName);
		onecase.setFirstName(firstName);
		onecase.setMiddleName(fatherName);
		Long gender = EIDSSData.getPatientGender().getId();
		onecase.setDateOfBirth(ConvertToDate(EIDSSData.getDateOfBirth()));
		AddressInfo addr=EIDSSData.getCurrentResidence();
		onecase.setFinalDiagnosisDate(ConvertToDate(EIDSSData.getNotificationDate()));
		String addInfo=addr.getCountry().getName()+"<br/>"+addr.getRegion().getName()+"<br/>"+addr.getSettlement().getName()+"<br/>"+addr.getRayon().getName()+"<br/>"+	addr.getStreet();
		onecase.setAdditionalComment(addInfo);	
		onecase.setCaseID(EIDSSData.getCaseID().toString());	
	
		return onecase;
	}
	 private List<String> ConvertString(ArrayOfHumanCaseListInfo arrayCaseList){
		 List <String> ids=new ArrayList<String>();
		 return ids;
	 }
	/*
	 * getHumanCaseList from EIDSS
	 * ID - tub code in EIDSS 
	 */
	/*
	private static ArrayOfHumanCaseListInfo getHumanCaseList(Date complDate, String id) {
		ArrayOfHumanCaseListInfo cases=null;
		ObjectFactory obj = new ObjectFactory();
		HumanCaseListInfo filter = obj.createHumanCaseListInfo();
		BaseReferenceItem diag = new BaseReferenceItem();
		diag.setId(new Long(id)); // Tuberculosis
		XMLGregorianCalendar dtXML = null;
		try {
			dtXML = DatatypeFactory.newInstance().newXMLGregorianCalendar();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
		dtXML.setDay(complDate.getDate());
		dtXML.setMonth(complDate.getMonth()+1);
		dtXML.setYear(complDate.getYear()+1900);

		dtXML.setTime(0, 0, 0);
		filter.setEnteredDate(dtXML);
		filter.setDiagnosis(diag);
		 cases = soap.getHumanCaseList(filter);
		return cases;
	}
	//TODO  надо еще получать случаи по списку и проверять годятся ли
	public  List<HumanCaseInfo> GetCaseInfoList(EidssServiceSoap soap, List<String> cases,  String patStateName, String caseStateName){
		Integer count=0;
		Iterator <String> it=cases.iterator();
		while (it.hasNext()){
			String item = it.next();
			HumanCaseInfo CI=soap.getHumanCase( item);
			if (suitable(CI, patStateName, caseStateName)){
				caseInfoList.add(CI); }
		}

	count=count+1;

	return caseInfoList;
	}
	
	/* 
	 * is case suitable for export to etb
	 */
	/*
	private static boolean suitable(HumanCaseInfo cI,String patStateName, String caseStateName) {
		   //alive
			if (cI.getPatientState()!=null){
				if (!cI.getPatientState().getName().equalsIgnoreCase(patStateName)) {
					return false;
				}
			}
			//Refused
			if (cI.getCaseClassification()!=null){
				if (cI.getCaseClassification().getName().equalsIgnoreCase(caseStateName)) {
					return false;
				}
			}
			return true;
		}
	
	private Date ConvertToDate(XMLGregorianCalendar param){
		Date tmpDate =new Date();
		int d=1;
		int m=0;
		int y=1;
		if ( param!=null) {
			param.getDay();
			m=param.getMonth();
			y=param.getYear();
			tmpDate.setDate(d);
			tmpDate.setMonth(m-1);
			tmpDate.setYear(y-1900);
		}
		return tmpDate;

	}
*/
	private void test(){
		entityManager = (EntityManager)Component.getInstance("entityManager");
		
		Date d=new Date();
		d.setYear(d.getYear()-30);
		Patient p = new Patient();
		
		p.setLastName("lastName3"+d.getSeconds());
		p.setName("firstName3");
		p.setMiddleName(d.toLocaleString());
		p.setWorkspace(getWorkspace());
		p.setGender(Gender.MALE);
		
		TbCaseAZ tbcase = new TbCaseAZ();
		tbcase.setClassification(CaseClassification.TB);
	tbcase.setLegacyId("legacy"+d.toString());
		//tbcase.setLegacyId("legacyId");
	tbcase.setEIDSSComment("address");	
	tbcase.setInprisonIniDate(new Date());
	
	p.setBirthDate(d);
	tbcase.setDiagnosisDate(new Date()); //TODO change
		tbcase.setRegistrationDate(new Date());
		tbcase.setDiagnosisType(DiagnosisType.CONFIRMED); //TODO change
		tbcase.setValidationState(ValidationState.WAITING_VALIDATION); //TODO check IT
		tbcase.setPatient(p);
		tbcase.setAge(1);
		tbcase.setState(CaseState.DEFAULTED);
		tbcase.setNotifAddressChanged(false);
		entityManager.persist(p);
		entityManager.persist(tbcase);
		entityManager.flush();
	
	}
	private Workspace getWorkspace() {
		return (Workspace)entityManager.find(Workspace.class, 8);
	}
	
	

}
