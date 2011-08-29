package org.msh.tb.medicines;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.SourceGroup;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.StockPosition;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.medicines.movs.StockPositionList;
import org.msh.utils.ItemSelect;


@Name("medicineStockSelection")
@Scope(ScopeType.CONVERSATION)
public class MedicineStockSelection {

	@In(create=true) StockPositionList stockPositionList;
	
	public class StockSource extends SourceGroup<ItemSelect> {};
	
	private Tbunit tbunit;
	private List<StockSource> sources;

// TODO remove this property and check impact
	private Date date;
	

	/**
	 * Returns a structured list of sources and stock position of medicines. The tbunit is required
	 * @return
	 */
	public List<StockSource> getSources() {
		if (sources == null)
			createSources();
		return sources;
	}


	/**
	 * Return a list of stock position elements (medicines in stock) that were selected.
	 * @return
	 */
	public List<StockPosition> getSelectedMedicines() {
		if (sources == null)
			return null;
		
		List<StockPosition> lst = new ArrayList<StockPosition>();
		
		for (StockSource ss: sources) {
			for (ItemSelect it: ss.getItems()) {
				if (it.isSelected()) {
					lst.add((StockPosition)it.getItem());
				}
			}
		}
		
		return lst;
	}


	/**
	 * Remove a item with the corresponding source and medicine
	 * @param s
	 * @param m
	 */
	public void removeItem(Source s, Medicine m) {
		StockSource sitem = null;
		for (StockSource ss: getSources()) {
			if (ss.getSource().getId().equals(s.getId())) {
				sitem = ss;
				break;
			}
		}
		
		if (sitem == null)
			return;
		
		ItemSelect item = null;
		for (ItemSelect it: sitem.getItems()) {
			StockPosition sp = (StockPosition)it.getItem();
			if (sp.getMedicine().getId().equals(m.getId())) {
				item = it;
			}
		}
		
		if (item == null)
			return;
		
		sitem.getItems().remove(item);
		if (sitem.getItems().size() == 0) {
			sources.remove(sitem);
		}
	}
	
	
	/**
	 * Creates a structured list of sources and stock position of medicines. 
	 */
	public void createSources() {
		if (tbunit == null)
			return;
		
		List<StockPosition> lst = stockPositionList.generate(tbunit, null);
	
		sources = new ArrayList<StockSource>();
		for (StockPosition sp: lst) {
			StockSource ss = findSource(sp.getSource());
			ItemSelect it = new ItemSelect();
			it.setItem(sp);
			ss.getItems().add(it);
		}
	}

	protected StockSource findSource(Source s) {
		for (StockSource ss: sources) {
			if (ss.getSource().getId().equals(s.getId())) {
				return ss;
			}
		}
		
		StockSource ss = new StockSource();
		ss.setSource(s);
		sources.add(ss);
		return ss;
	}
	
	public Tbunit getTbunit() {
		return tbunit;
	}

	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
		sources = null;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
