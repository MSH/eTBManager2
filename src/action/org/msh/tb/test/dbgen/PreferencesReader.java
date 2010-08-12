package org.msh.tb.test.dbgen;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.AgeRange;
import org.msh.mdrtb.entities.Regimen;
import org.msh.mdrtb.entities.Substance;
import org.msh.tb.AgeRangeHome;
import org.msh.tb.RegimensQuery;
import org.msh.tb.SubstancesQuery;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.misc.FieldsQuery;

/**
 * Reads the preferences of the text file
 * @author Ricardo Memoria
 *
 */
@Name("preferencesReader")
public class PreferencesReader extends TokenReader {
	
	@In(create=true) FacesMessages facesMessages;
	
	@In(create=true) RegimensQuery regimens;
	@In(create=true) SubstancesQuery substances;
	@In(create=true) AgeRangeHome ageRangeHome;
	@In(create=true) FieldsQuery fieldsQuery;
	
	private AdminUnitSelection auselection = new AdminUnitSelection();
	
	/**
	 * Read the dbgen preference file (inputStream)
	 * @return - "success" if ok
	 * @throws TokenReaderException
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public String read(GeneratorPreferences preferences) { 
		if (getTextFile() == null)
			return "error";

		initialize();
		
		try {
			while (getTokenType() != TokenType.EOF) {
				readPropertyValue(preferences);
				if (!expectPropertySeparator()) {
					if (getTokenType() != TokenType.EOF)
						throwTokenReaderException(", or EOL expected");
				}
			}
			
		} catch (Exception e) {
		    facesMessages.add(e.getMessage());
//			facesMessages.add(new FacesMessage(e.getMessage()));
			return "error";
		}
		
		return "success";
	}


	
	/**
	 * Reads the property value in the preference file. System reads the next property name in the preference file and read
	 * its contents according to its property type 
	 * @param obj - object to receive property value
	 * @throws TokenReaderException
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	protected void readPropertyValue(Object obj) throws TokenReaderException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String propName = readPropertyName();

		PropertyDescriptor desc = PropertyUtils.getPropertyDescriptor(obj, propName);
		if (desc == null)
			throw new TokenReaderException("property " + propName + " doesn't exist");

		handleComments();
		
		Class propClass = desc.getPropertyType();
		
		Object value = null;
		if (propClass == boolean.class)
			value = readBooleanValue();
		else
		if (propClass == int.class)
			value = readIntegerValue();
		else
		if (propClass.isEnum())
			value = readEnumValue(propClass);
		else 
		if (propClass ==  Map.class)
			value = readMapValues(obj, propName);
		else 
		if (inInterfaces(propClass, Collection.class))
			value = readListValue(obj, propName);
		else
		if (propClass == FactorValue.class)
			value = readFactorValue();
		else
		if (propClass == RangeValue.class)
			value = readRangeValue();
		else value = readBeanFromList(propClass, propName);

		System.out.println(propName + " = " + value);
		
		PropertyUtils.setProperty(obj, propName, value);
	}

	
	/**
	 * Checks if an interface is present in a class
	 * @param interfaces
	 * @param intf
	 * @return
	 */
	protected boolean inInterfaces(Class<?> beanClass, Class<?> intf) {
		for (Class<?> aux: beanClass.getInterfaces()) {
			if (aux == intf)
				return true;
		}
		return false;
	}

	
	/**
	 * Reads a Map list
	 * @param object
	 * @param propName
	 * @return
	 * @throws TokenReaderException
	 */
	protected Object readMapValues(Object object, String propName) throws TokenReaderException {		
		expectToken("[");

		Type aux = getGenericType(object, propName);
		if (aux instanceof ParameterizedType) {
			return readParameterizedMap((ParameterizedType)aux, propName);
		}
		else {
			Class tclass = (Class)aux;

			if (tclass.isEnum())
				 return readEnumMap(tclass);
			else
			if (inInterfaces(tclass, BeanSerialize.class))
				 return readBeanMap(tclass, propName);
			else return readItemMap(tclass, propName);
		}
	}


	protected Map readParameterizedMap(ParameterizedType ptype, String propName) throws TokenReaderException {
		Map map = new HashMap();
		
		if (ptype.getRawType() != List.class)
			throwExpectedException("not supported");

		Class tclass = (Class)ptype.getActualTypeArguments()[0];

		while (true) {
			expectToken("[");

			List lst = null;
			if (tclass == String.class)
				 lst = readStringList();
			else 
			if (inInterfaces(tclass, BeanSerialize.class))
				lst = readBeanList(tclass, propName);
			else lst = readItemList(tclass, propName);
			
			if (lst == null)
				throwTokenReaderException("Generic in list not supported");
			expectToken("=");
			Integer val = readIntegerValue();
			
			System.out.println(lst);
			map.put(lst, val);
			
			if (!expectPropertySeparator()) {
				expectToken("]");
				break;
			}
			if (isToken("]")) {
				nextToken();
				break;
			}
		}
		return map;
	}
	
	/**
	 * Reads a map composed of bean properties and its weights
	 * @param beanClass
	 * @return
	 * @throws TokenReaderException
	 */
	protected Map readBeanMap(Class<?> beanClass, String propName) throws TokenReaderException {
		Map map = new HashMap();
		
		while (true) {
			Object obj = newInstance(beanClass);
			readBeanItem(obj);
			expectToken("=");
			Integer val = (Integer)readIntegerValue();
		
			map.put(obj, val);

			// check end of line or end of map
			if (!expectPropertySeparator()) {
				if ((getTokenType() == TokenType.OPERATOR) && (getToken().equals("]")))
					break;
				throwTokenReaderException(", or ] expected");
			}

			if ((getTokenType() == TokenType.OPERATOR) && (getToken().equals("]")))
				break;
		}

		nextToken();
		return map;
	}
	
	
	
	/**
	 * Reads a list of objects and its weights in a Map form
	 * @param beanClass
	 * @return
	 * @throws TokenReaderException
	 */
	protected Map readItemMap(Class<?> beanClass, String propName) throws TokenReaderException {
		List lst = getBeanList(beanClass, propName);
		Map map = new HashMap();
		
		while (true) {
			handleComments();
			String name = readString("'");
			
			// recovers object
			Object obj = null;
			for (Object item: lst) {
				if (item.toString().trim().equalsIgnoreCase(name)) {
					obj = item;
					break;
				}
			}
			
			if (obj == null)
				throwTokenReaderException("Name not recognized: " + name);
			expectToken("=");
			Integer val = (Integer)readIntegerValue();
			
			map.put(obj, val);
			
			// check end of line or end of map
			if (!expectPropertySeparator()) {
				if ((getTokenType() == TokenType.OPERATOR) && (getToken().equals("]")))
					break;
			throwTokenReaderException(", or ] expected");
			}

			if ((getTokenType() == TokenType.OPERATOR) && (getToken().equals("]")))
				break;
		}

		nextToken();
		return map;
	}
	
	
	/**
	 * Reads an enumeration value
	 * @param enumClass - Enumeration class
	 * @return - enumeration object
	 * @throws TokenReaderException
	 */
	protected Object readEnumValue(Class enumClass) throws TokenReaderException {
		List lst = Arrays.asList( enumClass.getEnumConstants() );
		String s = readString("'");
		for (Object obj: lst) {
			if (obj.toString().equals(s))
				return obj;
		}
		throwTokenReaderException(s + " not recognized as a valid value");
		return null;
	}
	
	
	/**
	 * Read bean from its representation as a string value
	 * @param beanClass
	 * @return
	 * @throws TokenReaderException 
	 */
	protected Object readBeanFromList(Class beanClass, String propName) throws TokenReaderException {
		List lst = getBeanList(beanClass, propName);
		
		String beanStr = readString("'");
		
		if (lst == null)
			return null;
		
		
		for (Object obj: lst) {
			if (obj.toString().equalsIgnoreCase(beanStr)) {
				return obj;
			}
		}
		
		throw new TokenReaderException("Item not found: " + beanStr);
	}
	
	/**
	 * Reads a list of enumeration and its values
	 * @param enumClass
	 * @return
	 * @throws TokenReaderException
	 */
	protected Map readEnumMap(Class<?> enumClass) throws TokenReaderException {
		List lst = Arrays.asList( enumClass.getEnumConstants());
		Map map = new HashMap();
		
		while (true) {
			// read the enumeration
			handleComments();
			String name = readString("'");
			
			Object enumVal = null;
			for (Object obj: lst) {
				if (obj.toString().equals(name)) {
					enumVal = obj;
					break;
				}
			}
			if (enumVal == null)
				throwTokenReaderException("Name not recognized: " + name);

			// get enumeration value
			expectToken("=");
			Integer val = (Integer)readIntegerValue();
			
			map.put(enumVal, val);
			
			// check end of line or end of map
			if (!expectPropertySeparator()) {
				if ((getTokenType() == TokenType.OPERATOR) && (getToken().equals("]")))
					break;
				throwTokenReaderException(", or ] expected");
			}

			if ((getTokenType() == TokenType.OPERATOR) && (getToken().equals("]")))
				break;
		}

		nextToken();
		return map;
	}
	
	
	/**
	 * Reads the identifier property
	 * @return
	 * @throws TokenReaderException
	 */
	protected String readPropertyName() throws TokenReaderException {
		handleComments();
		String prop = expectString();
		handleComments();

		if ((getTokenType() != TokenType.OPERATOR) || (!getToken().equals("=")))
			throwExpectedException("=");
		nextToken();
			
		return prop;
	}


	/**
	 * Handle comments in the preferences file 
	 */
	protected boolean handleComments() {
		boolean b = false;
		while ((getTokenType() == TokenType.EOL) || 
				((getTokenType() == TokenType.OPERATOR) && (getToken().equals(";")))) {
			b = true;
			nextLine();
			nextToken();
		}
		return b;
	}


	/**
	 * Read a list value
	 * @return - Object that implements a List interface
	 * @throws TokenReaderException 
	 */
	protected Object readListValue(Object object, String propName) throws TokenReaderException {
		expectToken("[");

		Class tclass = (Class)getGenericType(object, propName);
		if (tclass == String.class)
			 return readStringList();
		else 
		if (inInterfaces(tclass, BeanSerialize.class))
			return readBeanList(tclass, propName);
		else return readItemList(tclass, propName);
	}

	/**
	 * Reads a list of strings
	 * @return object of ArrayList<String> instance
	 * @throws TokenReaderException 
	 */
	protected List<String> readStringList() throws TokenReaderException {
		List<String> lst = new ArrayList<String>();
		
		while (true) {
			handleComments();
			String s = readString("'");
			if (s == null)
				break;
			lst.add(s);
			
			if (!expectPropertySeparator()) {
				if ((getTokenType() == TokenType.OPERATOR) && (getToken().equals("]")))
					break;
			}
		}
		
		nextToken();
		return lst;
	}
	
	
	/**
	 * Reads a list of beans and its properties
	 * @param tclass - Bean class to be read
	 * @return List of objects of tclass class
	 * @throws TokenReaderException if any problem occurs
	 */
	protected List readBeanList(Class tclass, String propName) throws TokenReaderException {
		List lst = new ArrayList();
		
		while (true) {
			handleComments();
			Object obj = newInstance(tclass);
			readBeanItem(obj);

			lst.add(obj);
			
			if (!expectPropertySeparator()) {
				if (isToken("]"))
					break;
				throwTokenReaderException("Unexpected " + getToken());
			}
			if (isToken("]"))
				break;
		}
		
		nextToken();
		return lst;
	}
	
	/**
	 * Reads a list of beans
	 * @return
	 * @throws TokenReaderException 
	 */
	protected List readItemList(Class tclass, String propName) throws TokenReaderException {
		List lst = new ArrayList();
		List items = getBeanList(tclass, propName);
	
		Object obj;
		try {
			while (true) {
				handleComments();
				String name = readString("'");
				
				// recovers object
				obj = null;
				for (Object item: items) {
					if (item.toString().equals(name)) {
						obj = item;
						break;
					}
				}
				
				if (obj == null)
					throwTokenReaderException("Name not recognized: " + name);

				lst.add(obj);

				if (!expectPropertySeparator()) {
					if (getToken().equals("]"))
						break;
					throwTokenReaderException(getToken() + " unexpected");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (!(e instanceof TokenReaderException))
				throwTokenReaderException(e.getMessage());
			else throw (TokenReaderException)e;
		}
		nextToken();
		
		return lst;
	}


	/**
	 * Read bean properties enclosed by { }
	 * @param obj
	 * @throws TokenReaderException
	 */
	protected void readBeanItem(Object obj) throws TokenReaderException {
		expectToken("{");

		try {
			while (true) {
				readPropertyValue(obj);
				if (!expectPropertySeparator()) {
					if (getTokenType() != TokenType.OPERATOR)
						throwTokenReaderException("Unexpected " + getTokenType() + ": " + getToken());
					if (getToken().equals("}"))
						break;
					throwExpectedException("}");
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
			if (!(e instanceof TokenReaderException))
				throwTokenReaderException(e.getMessage());
			else throw (TokenReaderException)e;
		}
		nextToken();
	}


	/**
	 * Reads boolean value from the current token
	 * @return
	 */

	protected Object readBooleanValue() {
		String s = readString("'");
		if (s.toLowerCase().equals("true"))
			 return Boolean.TRUE;
		else return Boolean.FALSE;
	}

	@Override
	public void expectToken(String token) throws TokenReaderException {
		handleComments();
		super.expectToken(token);
	}

	/**
	 * Reads integer value from the current token
	 * @return
	 * @throws TokenReaderException
	 */
	protected Integer readIntegerValue() throws TokenReaderException {
		// checks if value is signaled
		int dx = 1;
		if (isToken("-")) {
			dx = -1;
			nextToken();
		}
		
		expectTokenType(TokenType.NUMBER);
		
		String s = getToken();
		Integer val = Integer.parseInt(s) * dx;
		nextToken();
		return val;
	}


	/**
	 * Reads FactorValue object in the format NumberA/NumberB
	 * @return instance of FactorValue object
	 * @throws TokenReaderException
	 */
	protected Object readFactorValue() throws TokenReaderException {
		Integer val1 = (Integer)readIntegerValue();
		if ((!isToken("/")) && (!isToken("/-")))
			expectToken("/");

		int dx = 1;
		if (isToken("/-"))
			dx = -1;
		nextToken();
		
		Integer val2 = (Integer)readIntegerValue() * dx;
		
		FactorValue val = new FactorValue();
		val.setValueA(val1);
		val.setValueB(val2);

		return val;
	}
	
	
	/**
	 * Reads a RangeValue object in the format iniValue..endValue
	 * @return instance of RangeValue object
	 * @throws TokenReaderException
	 */
	protected Object readRangeValue() throws TokenReaderException {
		Integer val1 = (Integer)readIntegerValue();
		if ((!isToken("..-")) && (!isToken(".."))) 
			expectToken("..");
		nextToken();

		Integer val2 = (Integer)readIntegerValue();

		RangeValue val = new RangeValue();
		val.setIniValue(val1);
		val.setEndValue(val2);
		
		return val;
	}

	/**
	 * Handle separator between properties. Can be a comma (,) or a EOL
	 * @throws TokenReaderException
	 */
	protected boolean expectPropertySeparator() {
		if (handleComments())
			return true;
		
		if (((getTokenType() == TokenType.OPERATOR) && (getToken().equals(","))) ||
				(getTokenType() == TokenType.EOL)) {
			nextToken();
			return true;
		}
		
		return false;
	}
	
	protected List getBeanList(Class<?> beanClass, String propName) {
		if (beanClass == Regimen.class)
			return regimens.getResultList();
		else
		if (beanClass == AdministrativeUnit.class)
			return auselection.getOptionsLevel1();
		else
		if (beanClass == Substance.class)
			return substances.getResultList();
		else
		if (beanClass == AgeRange.class)
			return ageRangeHome.getItems();
		else
		if (propName.compareToIgnoreCase("pulmonaryForms") == 0) 
			return fieldsQuery.getPulmonaryTypes();
		else
		if (propName.compareToIgnoreCase("comorbidities") == 0) 
			return fieldsQuery.getComorbidities();
		else
		if (propName.compareToIgnoreCase("adverseReactions") == 0) 
			return fieldsQuery.getSideEffects();
		else
		if (propName.compareToIgnoreCase("contactType") == 0) 
			return fieldsQuery.getContactTypes();
		else
		if (propName.compareToIgnoreCase("contactConduct") == 0) 
			return fieldsQuery.getContactConducts();
		else
		if ((propName.compareToIgnoreCase("xrayPresentation") == 0) || 
			(propName.compareToIgnoreCase("xrayPresentationProgress") == 0)) 
			return fieldsQuery.getXRayPresentations();
		else
			if (propName.compareToIgnoreCase("extrapulmonaryForms") == 0) 
				return fieldsQuery.getExtrapulmonaryTypes();
		
		return null;
	}
	
	/**
	 * Create a new instance of the beanClass class argument
	 * @param beanClass - Class of the new instance to be created
	 * @return instance of beanClass
	 * @throws TokenReaderException if it's not possible to create the class
	 */
	protected Object newInstance(Class<?> beanClass) throws TokenReaderException {
		try {
			return beanClass.newInstance();
		} catch (Exception e) {
			throwTokenReaderException(e.getMessage());
		}
		return null;
	}


	
	/**
	 * Return the generic type of the property (property List or Map)
	 * @param object
	 * @param propName
	 * @return
	 * @throws TokenReaderException
	 */
	protected Type getGenericType(Object object, String propName) throws TokenReaderException {
		ParameterizedType ptype = null;
		try {
			Field fld = object.getClass().getDeclaredField(propName);
			ptype = (ParameterizedType)fld.getGenericType();
		} catch (Exception e) {
			e.printStackTrace();
			throwTokenReaderException("field " + propName + " not accessible");
		}

		if (ptype.getActualTypeArguments().length == 0)
			throw new TokenReaderException("No generic type defined to list in property " + propName);
		
		return ptype.getActualTypeArguments()[0];
	}
	
} 
