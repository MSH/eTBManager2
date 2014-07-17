package org.msh.tb.entities;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;


public class WeeklyFrequencyType implements UserType {

	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached;
	}

	public Object deepCopy(Object obj) throws HibernateException {
		WeeklyFrequency wf = new WeeklyFrequency();
		wf.setValue(((WeeklyFrequency)obj).getValue());
		return wf;
	}

	public Serializable disassemble(Object obj) throws HibernateException {
		return (Serializable)obj;
	}

	public boolean equals(Object val1, Object val2) throws HibernateException {
		return ((val1 == val2) || (val1 != null && val2 != null && (val1.equals(val2))));
	}

	public int hashCode(Object obj) throws HibernateException {
		return obj.hashCode();
	}

	public boolean isMutable() {
		return false;
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
		int val = rs.getInt(names[0]);
		WeeklyFrequency wf = new WeeklyFrequency();
		wf.setValue(val);
		return wf;
	}

	public void nullSafeSet(PreparedStatement ps, Object value, int index) throws HibernateException, SQLException {
		ps.setInt(index, ((WeeklyFrequency)value).getValue());
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

	public Class returnedClass() {
		return WeeklyFrequency.class;
	}

	public int[] sqlTypes() {
		return new int[] { Types.INTEGER };
	}

}
