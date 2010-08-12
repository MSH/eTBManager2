package org.msh.tb;

import java.util.ArrayList;
import java.util.List;

import org.msh.mdrtb.entities.Source;

public class SourceGroup<E> {

	private Source source;
	private List<E> items = new ArrayList<E>();
	
	public Source getSource() {
		return source;
	}
	public void setSource(Source source) {
		this.source = source;
	}
	public List<E> getItems() {
		return items;
	}
	public void setItems(List<E> items) {
		this.items = items;
	}
}
