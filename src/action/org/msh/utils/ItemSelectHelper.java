/*
 * ItemSelectHelper.java
 *
 * Created on 1 de Fevereiro de 2007, 22:51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.msh.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Classe usada para manter uma lista de objetos ItemSelect.
 * Inicialmente foi criada para ser usada em componentes JSF DataTable, mas
 * pode ser utilizada em qualquer outra situacao.
 * @author Ricardo
 */
public class ItemSelectHelper {
    
    /**
     * Cria uma lista de objetos ItemSelect a partir de uma lista de objetos.
     */
    public static <E> List<ItemSelect<E>> createList(List<E> lst) {
        ArrayList<ItemSelect<E>> ret = new ArrayList<ItemSelect<E>>();
        
        for (int i=0; i < lst.size(); i++) {
            ItemSelect it = new ItemSelect();
            it.setItem(lst.get(i));
            it.setSelected(false);
            ret.add(it);
        }
        
        return ret;
    }
    
    public static <E> void selectItems(List<ItemSelect<E>> lst, List<E> items, boolean select) {
    	for (int i=0; i < lst.size(); i++) {
    		ItemSelect it = lst.get(i);
    		boolean bSel = items.contains(it.getItem()) && (select);
    		it.setSelected(bSel);
    	}
    }
    
    /**
     * cria uma lista de objetos a partir de uma lista de objetos ItemSelect.
     * <I>List</I> eh uma lista de objetos ItemSelect, e <I>selected</I> indica se a 
     * lista sera montada com objetos selecionados ou nao.
     * 
     * <b>Retorno</b>
     *    Lista de objetos referenciados por ItemSelect
     */
	public static <E> List<E> createItemsList(List<ItemSelect<E>> selItems, boolean aSelected) {
        ArrayList<E> lst = new ArrayList<E>();
        
        for (int i=0; i < selItems.size(); i++) {
            ItemSelect<E> it = selItems.get(i);
            if (it.isSelected() == aSelected) {
                lst.add(it.getItem());
            }
        }
        return lst;
    }


    /**
     * Filtra uma lista de objetos ItemSelect deixando apenas objetos selecionados ou 
     * objetos nao selecionados, de acordo com o parametro selected.
     */
    public static <E> void filter(List<ItemSelect<E>> lst, boolean selected) {
        Iterator<ItemSelect<E>> i = lst.iterator();
        
        while (i.hasNext()) {
            ItemSelect it = i.next();
            if (it.isSelected() == selected)
                i.remove();
        }
    }
    
    /**
     * Adiciona um objeto a uma lista de objetos ItemSelect
     */
    public static <E> ItemSelect add(List<ItemSelect<E>> items, Object item, boolean selected) {
        ItemSelect it = new ItemSelect();
        
        it.setItem(item);
        it.setSelected(selected);
        
        items.add(it);
        
        return it;
    }
    
    /**
     * Remove um objeto de uma lista de objetos ItemSelect
     */
    public static <E> void remove(List<E> items, Object aItem) {
        for (int i=0; i < items.size(); i++) {
            ItemSelect it = (ItemSelect)items.get(i);
            if (it.getItem() == aItem) {
                items.remove(it);
                break;
            }
        }
    }
    
    /**
     * Check or uncheck all items of a list of {@link ItemSelect} objects according to the argument select
     */
    public static <E> void selectAll(List<E> items, boolean select) {
        for (int i=0; i < items.size(); i++) {
            ItemSelect it = (ItemSelect)items.get(i);
            it.setSelected(select);
        }
    }
}
