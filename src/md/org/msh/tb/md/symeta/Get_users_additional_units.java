/**
 * Get_users_additional_units.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.msh.tb.md.symeta;

public class Get_users_additional_units  implements java.io.Serializable {
    private java.util.Calendar last_update;

    public Get_users_additional_units() {
    }

    public Get_users_additional_units(
           java.util.Calendar last_update) {
           this.last_update = last_update;
    }


    /**
     * Gets the last_update value for this Get_users_additional_units.
     * 
     * @return last_update
     */
    public java.util.Calendar getLast_update() {
        return last_update;
    }


    /**
     * Sets the last_update value for this Get_users_additional_units.
     * 
     * @param last_update
     */
    public void setLast_update(java.util.Calendar last_update) {
        this.last_update = last_update;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Get_users_additional_units)) return false;
        Get_users_additional_units other = (Get_users_additional_units) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.last_update==null && other.getLast_update()==null) || 
             (this.last_update!=null &&
              this.last_update.equals(other.getLast_update())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getLast_update() != null) {
            _hashCode += getLast_update().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Get_users_additional_units.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">get_users_additional_units"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("last_update");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "last_update"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
