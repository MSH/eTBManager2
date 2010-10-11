/**
 * Get_cases_classicResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.msh.tb.md.symeta;

public class Get_cases_classicResponse  implements java.io.Serializable {
    private org.msh.tb.md.symeta.Get_cases_classicResponseGet_cases_classicResult get_cases_classicResult;

    public Get_cases_classicResponse() {
    }

    public Get_cases_classicResponse(
           org.msh.tb.md.symeta.Get_cases_classicResponseGet_cases_classicResult get_cases_classicResult) {
           this.get_cases_classicResult = get_cases_classicResult;
    }


    /**
     * Gets the get_cases_classicResult value for this Get_cases_classicResponse.
     * 
     * @return get_cases_classicResult
     */
    public org.msh.tb.md.symeta.Get_cases_classicResponseGet_cases_classicResult getGet_cases_classicResult() {
        return get_cases_classicResult;
    }


    /**
     * Sets the get_cases_classicResult value for this Get_cases_classicResponse.
     * 
     * @param get_cases_classicResult
     */
    public void setGet_cases_classicResult(org.msh.tb.md.symeta.Get_cases_classicResponseGet_cases_classicResult get_cases_classicResult) {
        this.get_cases_classicResult = get_cases_classicResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Get_cases_classicResponse)) return false;
        Get_cases_classicResponse other = (Get_cases_classicResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.get_cases_classicResult==null && other.getGet_cases_classicResult()==null) || 
             (this.get_cases_classicResult!=null &&
              this.get_cases_classicResult.equals(other.getGet_cases_classicResult())));
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
        if (getGet_cases_classicResult() != null) {
            _hashCode += getGet_cases_classicResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Get_cases_classicResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">get_cases_classicResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("get_cases_classicResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "get_cases_classicResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">>get_cases_classicResponse>get_cases_classicResult"));
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
