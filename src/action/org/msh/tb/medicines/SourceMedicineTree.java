package org.msh.tb.medicines;

import java.util.ArrayList;
import java.util.List;

import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;

public class SourceMedicineTree<E> {
	private List<SourceNode> sources = new ArrayList<SourceNode>();

	public interface ItemTraversing<E> {
		void traverse(Source source, Medicine medicine, E item);
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
	public SourceNode addSource(Source source) {
		for (SourceNode it: sources) {
			if (it.getSource().equals(source))
				return it;
		}
		
		SourceNode grp = new SourceNode(source);
		sources.add(grp);
		return grp;
	}


	/**
	 * Add a new medicine
	 * @param source
	 * @param med
	 * @return
	 */
	public MedicineNode addMedicine(Source source, Medicine med) {
		SourceNode node = addSource(source);

		MedicineNode mednode = node.nodeByMedicine(med);
		if (mednode != null)
			return mednode;
		
		mednode = new MedicineNode(med);
		node.getMedicines().add(mednode);
		
		return mednode;
	}
	
	
	/**
	 * Add an item to the tree
	 * @param source
	 * @param medicine
	 * @param item
	 */
	public void addItem(Source source, Medicine medicine, E item) {
		MedicineNode mg = addMedicine(source, medicine);
		mg.getBatches().add(item);
	}

	
	/**
	 * Search for the medicine node by the source and medicine id
	 * @param sourceId
	 * @param medId
	 * @return instance of the {@link MedicineNode} class, otherwise null if the node was not found
	 */
	public MedicineNode findNodeByMedicineId(Integer sourceId, Integer medId) {
		SourceNode sourceNode = findNodeBySourceId(sourceId);
		if (sourceNode == null)
			return null;
		
		for (MedicineNode node: sourceNode.getMedicines()) {
			if (node.getMedicine().getId().equals(medId))
				return node;
		}
		return null;
	}


	/**
	 * Search for the source node by the source id
	 * @param sourceId
	 * @return instance of the {@link SourceNode} class, otherwise null if the source was not found
	 */
	public SourceNode findNodeBySourceId(Integer sourceId) {
		for (SourceNode node: sources) {
			if (node.getSource().getId().equals(sourceId))
				return node;
		}
		return null;
	}

	/**
	 * Traverse the list
	 * @param t
	 */
	public void traverse(ItemTraversing<E> t) {
		for (SourceNode sourceNode: sources) {
			for (MedicineNode medNode: sourceNode.getMedicines()) {
				for (Object item: medNode.getBatches()) {
					t.traverse(sourceNode.getSource(), medNode.getMedicine(), (E)item);
				}
			}
		}
	}


	/**
	 * @author Ricardo Memoria
	 *
	 */
	public class SourceNode {
		private Source source;
		private Object item;
		private List<MedicineNode> medicines = new ArrayList<MedicineNode>();

		public SourceNode(Source source) {
			this.source = source;
		}

		/**
		 * Search for a node by its medicine
		 * @param med
		 * @return
		 */
		public MedicineNode nodeByMedicine(Medicine med) {
			for (MedicineNode node: medicines) {
				if (node.getMedicine().equals(med))
					return node;
			}
			return null;
		}
		/**
		 * @return the item
		 */
		public Object getItem() {
			return item;
		}
		/**
		 * @param item the item to set
		 */
		public void setItem(Object item) {
			this.item = item;
		}
		/**
		 * @return the medicines
		 */
		public List<MedicineNode> getMedicines() {
			return medicines;
		}

		/**
		 * @return the source
		 */
		public Source getSource() {
			return source;
		}
	}


	/**
	 * @author Ricardo Memoria
	 *
	 */
	public class MedicineNode {
		private Medicine medicine;
		private Object item;
		private List<E> batches = new ArrayList<E>();
		
		public MedicineNode(Medicine medicine) {
			this.medicine = medicine;
		}
		/**
		 * @return the medicine
		 */
		public Medicine getMedicine() {
			return medicine;
		}
		/**
		 * @param medicine the medicine to set
		 */
		public void setMedicine(Medicine medicine) {
			this.medicine = medicine;
		}
		/**
		 * @return the item
		 */
		public Object getItem() {
			return item;
		}
		/**
		 * @param item the item to set
		 */
		public void setItem(Object item) {
			this.item = item;
		}
		/**
		 * @return the batches
		 */
		public List<E> getBatches() {
			return batches;
		}
	}


	/**
	 * @return the sources
	 */
	public List<SourceNode> getSources() {
		return sources;
	}
}
