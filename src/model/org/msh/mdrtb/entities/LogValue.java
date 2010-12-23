package org.msh.mdrtb.entities;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.jboss.seam.international.Messages;
import org.msh.mdrtb.entities.enums.LogValueType;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.LocaleDateConverter;

@Entity
public class LogValue {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name="TRANSACTIONLOG_ID")
	private TransactionLog transactionLog;

	@Column(length=100, name="KEYVALUE")
	private String key;
	
	@Column(length=100)
	private String prevValue;
	
	@Column(length=100)
	private String newValue;
	
	private LogValueType type = LogValueType.TEXT;


	public Object getPrevValueObject() {
		return getValueAsObject(prevValue);
	}
	
	public Object getNewValueObject() {
		return getValueAsObject(newValue);
	}


	/**
	 * Return a string representation of the previous value ready for displaying
	 * @return
	 */
	public String getPrevDisplayValue() {
		return getDisplayValue(prevValue);
	}


	/**
	 * Return a string representation of the new value ready for displaying
	 * @return
	 */
	public String getNewDisplayValue() {
		return getDisplayValue(newValue);
	}


	/**
	 * Return value as its display representation
	 * @param value
	 * @return
	 */
	protected String getDisplayValue(String value) {
		if (type == null)
			return value;

		if ((value == null) || (value.isEmpty()))
			return null;
		
		switch (type) {
		case MESSAGE: return Messages.instance().get(value);
		case BOOLEAN: return ("1".equals(value)? "(x)": "( )");
		case DATE: return LocaleDateConverter.getDisplayDate(parseDateValue(value));
		}
		return value;
	}


	/**
	 * Return value as an object according to its type
	 * @param value
	 * @return
	 */
	protected Object getValueAsObject(String value) {
		if (type == null)
			return value;
		
		if ((value == null) || (value.isEmpty()))
			return null;
		
		switch (type) {
		case BOOLEAN: return ("1".equals(value)? true: false);
		case MESSAGE: return Messages.instance().get(value);
		case NUMBER: return Float.parseFloat(value);
		case DATE: return parseDateValue(value);
		}
		
		return value;
	}

	
	/**
	 * Parse a value from string to a date value
	 * @param value
	 * @return
	 */
	protected Date parseDateValue(String value) {
		try {
			int year = Integer.parseInt(value.substring(0, 4));
			int month = Integer.parseInt(value.substring(4, 6));
			int day = Integer.parseInt(value.substring(6, 8));
			return DateUtils.newDate(year, month, day);
		}
		catch (Exception e) {
			return null;
		}
	}

	public void truncateValues() {
		if ((prevValue != null) && (prevValue.length() > 100))
			prevValue = prevValue.substring(0, 96) + "...";

		if ((newValue != null) && (newValue.length() > 100))
			newValue = newValue.substring(0, 96) + "...";
	}
	
	/**
	 * Return the key to be displayed using the list of messages
	 * @return String containing the key in the messages file
	 */
	public String getMessageKey() {
		if (key == null)
			return null;
		if (key.startsWith("."))
			 return transactionLog.getEntityClass() + key;
		else return key;
	}


	public void setNewObjectValue(Object value) {
		newValue = convertToString(value);
	}


	public void setPrevObjectValue(Object value) {
		prevValue = convertToString(value);
	}


	public String convertToString(Object value) {
		if (value == null)
			return null;
		
		if (value instanceof String) {
			type = LogValueType.TEXT;
			return (String)value;
		}
		
		if (value instanceof Date) {
			type = LogValueType.DATE;
			return dateToString((Date)value);
		}
		
		if (value instanceof Boolean) {
			type = LogValueType.BOOLEAN;
			return (Boolean)value? "1": "0";
		}
		
		if (value instanceof Enum) {
			type = LogValueType.ENUM;
			return Integer.toString(((Enum) value).ordinal());
		}

		type = LogValueType.TEXT;
		return value.toString();
	}


	/**
	 * Convert from date to a string representation
	 * @param dt
	 * @return
	 */
	public String dateToString(Date dt) {
		Calendar c = Calendar.getInstance();
		int ano = c.get(Calendar.YEAR);
		int mes = c.get(Calendar.MONTH);
		int dia = c.get(Calendar.DAY_OF_MONTH);
		
		NumberFormat nf = new DecimalFormat("00");
		return Integer.toString(ano) + nf.format(mes) + nf.format(dia);
	}
	
	/**
	 * @return the transactionLog
	 */
	public TransactionLog getTransactionLog() {
		return transactionLog;
	}

	/**
	 * @param transactionLog the transactionLog to set
	 */
	public void setTransactionLog(TransactionLog transactionLog) {
		this.transactionLog = transactionLog;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

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
	 * @return the prevValue
	 */
	public String getPrevValue() {
		return prevValue;
	}

	/**
	 * @param prevValue the prevValue to set
	 */
	public void setPrevValue(String prevValue) {
		this.prevValue = prevValue;
	}

	/**
	 * @return the newValue
	 */
	public String getNewValue() {
		return newValue;
	}

	/**
	 * @param newValue the newValue to set
	 */
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	

	/**
	 * @return the type
	 */
	public LogValueType getType() {
		return type;
	}


	/**
	 * @param type the type to set
	 */
	public void setType(LogValueType type) {
		this.type = type;
	}
}
