/**
 * Get_tables_updatedResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.msh.tb.md.symetb.wsdlinterface;

public class Get_tables_updatedResponse  implements java.io.Serializable {
    private Get_tables_updatedResponseGet_tables_updatedResult get_tables_updatedResult;

    public Get_tables_updatedResponse() {
    }

    public Get_tables_updatedResponse(
           Get_tables_updatedResponseGet_tables_updatedResult get_tables_updatedResult) {
           this.get_tables_updatedResult = get_tables_updatedResult;
    }


    /**
     * Gets the get_tables_updatedResult value for this Get_tables_updatedResponse.
     * 
     * @return get_tables_updatedResult
     */
    public Get_tables_updatedResponseGet_tables_updatedResult getGet_tables_updatedResult() {
        return get_tables_updatedResult;
    }


    /**
     * Sets the get_tables_updatedResult value for this Get_tables_updatedResponse.
     * 
     * @param get_tables_updatedResult
     */
    public void setGet_tables_updatedResult(Get_tables_updatedResponseGet_tables_updatedResult get_tables_updatedResult) {
        this.get_tables_updatedResult = get_tables_updatedResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Get_tables_updatedResponse)) return false;
        Get_tables_updatedResponse other = (Get_tables_updatedResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.get_tables_updatedResult==null && other.getGet_tables_updatedResult()==null) || 
             (this.get_tables_updatedResult!=null &&
              this.get_tables_updatedResult.equals(other.getGet_tables_updatedResult())));
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
        if (getGet_tables_updatedResult() != null) {
            _hashCode += getGet_tables_updatedResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Get_tables_updatedResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">get_tables_updatedResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("get_tables_updatedResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "get_tables_updatedResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">>get_tables_updatedResponse>get_tables_updatedResult"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
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
