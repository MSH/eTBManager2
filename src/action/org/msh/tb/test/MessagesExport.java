package org.msh.tb.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.Messages;
import org.msh.utils.ItemSelect;
import org.msh.utils.ItemSelectHelper;


@Name("messagesExport")
@Scope(ScopeType.CONVERSATION)
public class MessagesExport {

	@In(create=true) LocaleSelector localeSelector;
	
	private List<Locale> langs;

	public class MessageRow {
		private String key;
		private List<String> messages = new ArrayList<String>();
		/**
		 * @return the key
		 */
		public String getKey() {
			return key;
		}
		/**
		 * @param key the key to set
		 */
		public void setKey(String key) {
			this.key = key;
		}
		/**
		 * @return the messages
		 */
		public List<String> getMessages() {
			return messages;
		}
		/**
		 * @param messages the messages to set
		 */
		public void setMessages(List<String> messages) {
			this.messages = messages;
		}
	}
	
	private List<ItemSelect<Locale>> languages;
	private List<MessageRow> messages;
	
	public List<ItemSelect<Locale>> getLanguages() {
		if (languages == null) 
			createLanguageList();
		return languages;
	}


	/**
	 * Create the list of languages to be selected by the user
	 */
	protected void createLanguageList() {
		Iterator<Locale> locales = FacesContext.getCurrentInstance().getApplication().getSupportedLocales();
		
		languages = new ArrayList<ItemSelect<Locale>>();
		
		while (locales.hasNext()) {
			Locale locale = locales.next();
			ItemSelect item = new ItemSelect();

			item.setItem(locale);
			item.setSelected(false);
			languages.add(item);
		}
	}

	@Begin(join=true)
	public String export() {
		if (getSelectedLanguages().size() == 0)
			return "error";
		return "export";
	}
	
	public List<MessageRow> getMessages() {
		if (messages == null)
			createMessages();
		return messages;
	}

	private MessageRow findRow(String key) {
		for (MessageRow row: messages) {
			if (row.getKey().equals(key))
				return row;
		}
		
		MessageRow row = new MessageRow();
		row.setKey(key);
		// initialize messages
		for (int i = 0; i < langs.size(); i++)
			row.getMessages().add("");
		messages.add(row);
		return row;
	}


	/**
	 * Create the list of messages to be exported to the user
	 */
	protected void createMessages() {
		langs = ItemSelectHelper.createItemsList(getLanguages(), true);
		
		if (langs.size() == 0)
			return;

		// get list of locale code names
		List<String> langsStr = new ArrayList<String>();
		List<String> langsSelStr = new ArrayList<String>();
		for (ItemSelect<Locale> it: getLanguages()) 
			langsStr.add( it.getItem().toString() );
		for (Locale loc: langs) {
			langsSelStr.add( loc.toString() );
		}
		
		
		messages = new ArrayList<MessageRow>();
		
		// save the current locale
		Locale currentLocale = localeSelector.getLocale();
		
		try {
			int index = 0;
			List<String> keys = new ArrayList<String>();
			
			// create list of keys
			for (Locale locale: langs) {
				localeSelector.setLocale(locale);
				localeSelector.select();

				Map<String, String> msgs = Messages.instance();
				for (String key: msgs.keySet()) {
					String prefix = getKeyPrefix(key);
					boolean isLanguagePrefix = (prefix != null) && (langsStr.contains(prefix));
					if (isLanguagePrefix) {
						// language prefix is part of the selected languages
						if (langsSelStr.contains(prefix))
							keys.add(key);
					}
					else keys.add(key);
				}
			}

			// create table of messages
			for (Locale locale: langs) {
				localeSelector.setLocale(locale);
				localeSelector.select();
				
				Map<String, String> msgs = Messages.instance();
				for (String key: keys) {
					MessageRow row = findRow(key);
					row.getMessages().set(index, msgs.get(key));
				}
				index++;
			}
			
			Collections.sort(messages, new Comparator<MessageRow>() {

				public int compare(MessageRow row1, MessageRow row2) {
					return row1.getKey().compareTo(row2.getKey());
				}
			});
			
			
		} finally {
			// resume the current locale
			localeSelector.setLocale(currentLocale);
			localeSelector.select();
		}
	}
	
	public List<Locale> getSelectedLanguages() {
		if (langs == null)
			createMessages();
		return langs;
	}

	
	/**
	 * Return the prefix of the message key
	 * @param key
	 * @return
	 */
	private String getKeyPrefix(String key) {
		int pos = key.indexOf('.');
		if (pos == -1)
			return null;
		return key.substring(0, pos);
	}
	
/*	private String getLocaleCode(Locale loc) {
		String s = loc.getLanguage();
		String aux = loc.getCountry();
		if ((aux != null) && (!aux.isEmpty()))
			s += "_" + aux;
		return s;
	}
*/}
