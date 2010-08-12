package org.msh.tb;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.WeeklyFrequency;
import org.msh.mdrtb.entities.Workspace;

@Name("weeklyFrequencyHome")
public class WeeklyFrequencyHome {

	@In(create=true) Workspace defaultWorkspace;
	@In(create=true) EntityManager entityManager;
	
	public class WeekDaySelection {
		private boolean selected;

		public boolean isSelected() {
			return selected;
		}
		public void setSelected(boolean selected) {
			this.selected = selected;
		}
	}
	
	public class Item {
		private Integer index;
		private List<WeekDaySelection> days = new ArrayList<WeekDaySelection>();

		public List<WeekDaySelection> getDays() {
			return days;
		}

		public void setDays(List<WeekDaySelection> days) {
			this.days = days;
		}

		public Integer getIndex() {
			return index;
		}

		public void setIndex(Integer index) {
			this.index = index;
		}
	}
	
	private List<Item> items;
	
	/**
	 * Save changes in the weekly frequency
	 * @return
	 */
	public String save() {
		WeeklyFrequency[] freqs = defaultWorkspace.getWeeklyFrequencies();

		items = getItems();
		for (int i = 0; i < 7; i++) {
			for (int day = 0; day < 7; day++) {
				WeekDaySelection wds = items.get(i).getDays().get(day);
				freqs[i].setDay(day+1, wds.isSelected());
			}
		}
		
		// update in-memory items
		defaultWorkspace.setWeeklyFrequency(freqs);

		// update database
		Workspace ws = entityManager.find(Workspace.class, defaultWorkspace.getId());
		ws.setWeeklyFrequency(freqs);
		
		entityManager.persist(ws);
		entityManager.flush();
		
		return "success";
	}
	
	public List<Item> getItems() {
		if (items == null) {
			items = new ArrayList<Item>();

			WeeklyFrequency[] vals = defaultWorkspace.getWeeklyFrequencies();
			for (int i = 0; i < 7; i++) {
				Item item = new Item();
				item.setIndex(i + 1);
				items.add(item);
				
				for (int day = 0; day < 7; day++) {
					WeekDaySelection wds = new WeekDaySelection();
					wds.setSelected(vals[i].getDay(day+1));
					item.getDays().add(wds);
				}
			}
		}
		
		return items;
	}
	
	public void setItems(List<Item> vals) {
		items = vals;
	}
}
