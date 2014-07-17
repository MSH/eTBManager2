package org.msh.tb;

import org.msh.tb.entities.Source;

import java.util.ArrayList;
import java.util.List;

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
