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

import org.msh.mdrtb.entities.enums.LogValueType;

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
