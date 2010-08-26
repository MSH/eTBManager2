/**
 * Get_regionsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.msh.tb.md.symeta;

public class Get_regionsResponse  implements java.io.Serializable {
    private org.msh.tb.md.symeta.Get_regionsResponseGet_regionsResult get_regionsResult;

    public Get_regionsResponse() {
    }

    public Get_regionsResponse(
           org.msh.tb.md.symeta.Get_regionsResponseGet_regionsResult get_regionsResult) {
           this.get_regionsResult = get_regionsResult;
    }


    /**
     * Gets the get_regionsResult value for this Get_regionsResponse.
     * 
     * @return get_regionsResult
     */
    public org.msh.tb.md.symeta.Get_regionsResponseGet_regionsResult getGet_regionsResult() {
        return get_regionsResult;
    }


    /**
     * Sets the get_regionsResult value for this Get_regionsResponse.
     * 
     * @param get_regionsResult
     */
    public void setGet_regionsResult(org.msh.tb.md.symeta.Get_regionsResponseGet_regionsResult get_regionsResult) {
        this.get_regionsResult = get_regionsResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Get_regionsResponse)) return false;
        Get_regionsResponse other = (Get_regionsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.get_regionsResult==null && other.getGet_regionsResult()==null) || 
             (this.get_regionsResult!=null &&
              this.get_regionsResult.equals(other.getGet_regionsResult())));
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
        if (getGet_regionsResult() != null) {
            _hashCode += getGet_regionsResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Get_regionsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">get_regionsResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("get_regionsResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "get_regionsResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">>get_regionsResponse>get_regionsResult"));
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
