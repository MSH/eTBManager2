package org.msh.tb.medicines;

import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ricardo Memoria
 *
 */
public class SourceMedicineGroup<E> {

	private List<SourceGroup> sources = new ArrayList<SourceGroup>();
	private Class<? extends MedicineGroup> medClazz;
	
	public interface ItemTraversing<E> {
		void traverse(Source source, Medicine medicine, E item);
	}

	public SourceMedicineGroup() {
		super();
	}
	
	public SourceMedicineGroup(Class<? extends MedicineGroup> medClazz) {
		super();
		this.medClazz = medClazz;
	}


	/**
	 * Return the sources
	 * @return
	 */
	public List<SourceGroup> getSources() {
		return sources;
	}
	
	
	/**
	 * Return a list with all items children of medicines
	 * @return
	 */
	public List<E> getItems() {
		final List<E> items = new ArrayList<E>();

		traverse(new ItemTraversing<E>() {
			public void traverse(Source source, Medicine medicine, E item) {
				items.add(item);
			}
		});

		return items;
	}


	/**
	 * Add a new source to the tree
	 * @param source
	 * @return
	 */
	public SourceGroup addSource(Source source) {
		for (SourceGroup it: sources) {
			if (it.getSource().equals(source))
				return it;
		}
		
		SourceGroup grp = new SourceGroup();
		grp.setSource(source);
		sources.add(grp);
		return grp;
	}


	/**
	 * Add a new medicine
	 * @param source
	 * @param med
	 * @return
	 */
	public MedicineGroup addMedicine(Source source, Medicine med) {
		SourceGroup grp = addSource(source);
		
		for (MedicineGroup medGrp: grp.getMedicines()) {
			if (medGrp.getMedicine().equals(med))
				return medGrp;
		}
		
		MedicineGroup medGrp = createMedicineGroup();
		medGrp.setMedicine(med);
		grp.getMedicines().add(medGrp);
		
		return medGrp;
	}
	
	
	/**
	 * Add an item to the tree
	 * @param source
	 * @param medicine
	 * @param item
	 */
	public void addItem(Source source, Medicine medicine, E item) {
		MedicineGroup mg = addMedicine(source, medicine);
		mg.getItems().add(item);
	}
	

	/**
	 * Traverse the list
	 * @param t
	 */
	public void traverse(ItemTraversing<E> t) {
		for (SourceGroup sourceGrp: sources) {
			for (MedicineGroup medGrp: sourceGrp.getMedicines()) {
				for (Object item: medGrp.getItems()) {
					t.traverse(sourceGrp.getSource(), medGrp.getMedicine(), (E)item);
				}
			}
		}
	}


	/**
	 * Create new instance of {@link MedicineGroup} class
	 * @return
	 */
	protected MedicineGroup createMedicineGroup() {
		if (medClazz == null)
			return new MedicineGroup();

		try {
			return (MedicineGroup)medClazz.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
