/**
 * Get_localitiesResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.msh.tb.md.symetb.wsdlinterface;

public class Get_localitiesResponse  implements java.io.Serializable {
    private Get_localitiesResponseGet_localitiesResult get_localitiesResult;

    public Get_localitiesResponse() {
    }

    public Get_localitiesResponse(
           Get_localitiesResponseGet_localitiesResult get_localitiesResult) {
           this.get_localitiesResult = get_localitiesResult;
    }


    /**
     * Gets the get_localitiesResult value for this Get_localitiesResponse.
     * 
     * @return get_localitiesResult
     */
    public Get_localitiesResponseGet_localitiesResult getGet_localitiesResult() {
        return get_localitiesResult;
    }


    /**
     * Sets the get_localitiesResult value for this Get_localitiesResponse.
     * 
     * @param get_localitiesResult
     */
    public void setGet_localitiesResult(Get_localitiesResponseGet_localitiesResult get_localitiesResult) {
        this.get_localitiesResult = get_localitiesResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Get_localitiesResponse)) return false;
        Get_localitiesResponse other = (Get_localitiesResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.get_localitiesResult==null && other.getGet_localitiesResult()==null) || 
             (this.get_localitiesResult!=null &&
              this.get_localitiesResult.equals(other.getGet_localitiesResult())));
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
        if (getGet_localitiesResult() != null) {
            _hashCode += getGet_localitiesResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Get_localitiesResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">get_localitiesResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("get_localitiesResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "get_localitiesResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">>get_localitiesResponse>get_localitiesResult"));
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
