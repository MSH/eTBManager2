package org.msh.tb.medicines.dispensing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.login.UserSession;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.LocaleDateConverter;

public abstract class AbstractDispensigUIHome {

	private List<SourceItem> sources;
	private Tbunit unit;
	private DispensingHome dispensingHome;


	/**
	 * Return the list of sources to be displayed to the user
	 * @return
	 */
	public List<SourceItem> getSources() {
		if (sources == null) {
			sources = new ArrayList<SourceItem>(); 
			loadSources(sources);
			for (SourceItem item: sources)
				item.getTable().updateLayout();
			Collections.sort(sources, new Comparator<SourceItem>() {
				@Override
				public int compare(SourceItem o1, SourceItem o2) {
					return o1.getSource().getAbbrevName().toString().compareTo(o2.getSource().getAbbrevName().toString());
				}
			});
		}
		return sources;
	}


	/**
	 * Create list of sources to be used in the displaying of dispensing data
	 * @return
	 */
	protected abstract void loadSources(List<SourceItem> sources);



	/**
	 * Save the dispensing data
	 * @return
	 */
	public String saveDispensing() {
		Tbunit unit = getUnit();

		// is dispensing by patient ?
		TbCase tbcase;
		if (unit.isPatientDispensing()) {
			// so get the patient
			CaseHome ch = (CaseHome)Component.getInstance("caseHome");
			if (!ch.isManaged())
				throw new RuntimeException("No patient defined for this dispensing");
			tbcase = ch.getInstance();
		}
		else tbcase = null;

		// validate 
		if (unit.getMedManStartDate() == null)
			throw new RuntimeException("Unit not in medicine management control");

		FacesMessages facesMessages = FacesMessages.instance();

		Date dispDate = getDispensingHome().getInstance().getDispensingDate();
		
		if (dispDate.before( unit.getMedManStartDate() )) {
			facesMessages.addToControlFromResourceBundle("edtdate", "meds.movs.datebefore", LocaleDateConverter.getDisplayDate( unit.getMedManStartDate(), false ));
			return "error";
		}

		DispensingHome dispensingHome = getDispensingHome();

		dispensingHome.getInstance().setTbunit(unit);

		// inform DispensingHome about the batches to be dispensed
		for (SourceItem it: getSources())
			for (DispensingRow row: it.getTable().getRows()) {
				Integer qtd = row.getDispensingQuantity();
				if ((qtd != null) && (qtd > 0)) {
					if (tbcase != null)
						 dispensingHome.addPatientDispensing(tbcase, row.getBatch(), it.getSource(), qtd);
					else dispensingHome.addDispensing(row.getBatch(), it.getSource(), qtd);
				}
			}

		// error during saving
		if (!dispensingHome.saveDispensing()) {
			// handle error messages
			dispensingHome.traverseErrors(new DispensingHome.ErrorTraverser() {
				public void traverse(Source source, Medicine medicine, Batch batch, String errorMessage) {
					SourceItem it = findSourceItem(source);
					if (batch != null) {
						DispensingRow row = it.getTable().findRowByBatch(source, batch);
						if (row != null)
							row.setBatchErrorMessage(errorMessage);
					}
					else {
						DispensingRow row = it.getTable().findFirstMedicineRow(medicine);
						if (row != null)
							row.setErrorMessage(errorMessage);
					}
				}
			});
			return "error";
		}

		// select the default month and year as the one entered by the dispensing date
		Date dt = dispensingHome.getMedicineDispensing().getDispensingDate();
		DispensingSelection dispensingSel = (DispensingSelection)Component.getInstance("dispensingSelection", true);
		dispensingSel.setMonth(DateUtils.monthOf(dt));
		dispensingSel.setYear(DateUtils.yearOf(dt));
		
		return "persisted";
	}


	/**
	 * Return a managed instance of {@link Tbunit} selected by the user in the {@link UserSession} component
	 * @return
	 */
	public Tbunit getUnit() {
		if (unit == null) {
			UserSession userSession = (UserSession)Component.getInstance("userSession", true);
			unit = getEntityManager().find(Tbunit.class, userSession.getTbunit().getId());
		}
		return unit;
	}


	/**
	 * Return an instance of the {@link EntityManager} class as a SEAM component
	 * @return
	 */
	public EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}


	/**
	 * Return an instance of the component {@link DispensingHome}
	 * @return
	 */
	protected DispensingHome getDispensingHome() {
		if (dispensingHome == null)
			dispensingHome = (DispensingHome)Component.getInstance("dispensingHome");
		return dispensingHome;
	}


	/**
	 * Return an instance of {@link SourceItem} by its {@link Source}
	 * @param source
	 * @return
	 */
	protected SourceItem findSourceItem(Source source) {
		for (SourceItem sg: sources) {
			if (sg.getSource().equals(source))
				return sg;
		}

		SourceItem item = new SourceItem(source);
		sources.add(item);
		return item;
	}

	
	protected DispensingRow addSourceRow(Source source, Batch batch) {
		SourceItem si = findSourceItem(source);
		
		DispensingRow row = si.getTable().addRow(source, batch);
		
		return row;
	}

	/**
	 * Add a row to the table located in the instance of {@link SourceItem} pointing to the {@link SourceItem} source
	 * @param item
	 * @return
	 */
	protected DispensingRow addSourceRow(BatchQuantity item) {
		DispensingRow row = addSourceRow(item.getSource(), item.getBatch());
		row.setQuantity(item.getQuantity());
		
		return row;
	}

	/**
	 * @return the dispensingDate
	 */
	public Date getDispensingDate() {
		return getDispensingHome().getInstance().getDispensingDate();
	}

	/**
	 * @param dispensingDate the dispensingDate to set
	 */
	public void setDispensingDate(Date dispensingDate) {
		getDispensingHome().getInstance().setDispensingDate(dispensingDate);
	}


	/**
	 * @return the medicineDispensingId
	 */
	public Integer getMedicineDispensingId() {
		return getDispensingHome().getInstance().getId();
	}


	/**
	 * @param medicineDispensingId the medicineDispensingId to set
	 */
	public void setMedicineDispensingId(Integer medicineDispensingId) {
		getDispensingHome().setId(medicineDispensingId);
	}
	
}
