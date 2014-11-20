/**
 * Get_cases_unitsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.msh.tb.md.symetb.wsdlinterface;

public class Get_cases_unitsResponse  implements java.io.Serializable {
	private static final long serialVersionUID = -3507438454478513795L;
	
	private Get_cases_unitsResponseGet_cases_unitsResult get_cases_unitsResult;

    public Get_cases_unitsResponse() {
    }

    public Get_cases_unitsResponse(
           Get_cases_unitsResponseGet_cases_unitsResult get_cases_unitsResult) {
           this.get_cases_unitsResult = get_cases_unitsResult;
    }


    /**
     * Gets the get_cases_unitsResult value for this Get_cases_unitsResponse.
     * 
     * @return get_cases_unitsResult
     */
    public Get_cases_unitsResponseGet_cases_unitsResult getGet_cases_unitsResult() {
        return get_cases_unitsResult;
    }


    /**
     * Sets the get_cases_unitsResult value for this Get_cases_unitsResponse.
     * 
     * @param get_cases_unitsResult
     */
    public void setGet_cases_unitsResult(Get_cases_unitsResponseGet_cases_unitsResult get_cases_unitsResult) {
        this.get_cases_unitsResult = get_cases_unitsResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Get_cases_unitsResponse)) return false;
        Get_cases_unitsResponse other = (Get_cases_unitsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.get_cases_unitsResult==null && other.getGet_cases_unitsResult()==null) || 
             (this.get_cases_unitsResult!=null &&
              this.get_cases_unitsResult.equals(other.getGet_cases_unitsResult())));
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
        if (getGet_cases_unitsResult() != null) {
            _hashCode += getGet_cases_unitsResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Get_cases_unitsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">get_cases_unitsResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("get_cases_unitsResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "get_cases_unitsResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">>get_cases_unitsResponse>get_cases_unitsResult"));
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
