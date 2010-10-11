/**
 * MDR_TBMISSSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.msh.tb.md.symeta;

public interface MDR_TBMISSSoap extends java.rmi.Remote {
    public java.lang.String helloWorld() throws java.rmi.RemoteException;
    public Get_deleted_recordResponseGet_deleted_recordResult get_deleted_record(java.lang.String table_name, java.util.Calendar last_update) throws java.rmi.RemoteException;
    public Get_institutionsResponseGet_institutionsResult get_institutions(java.util.Calendar last_update) throws java.rmi.RemoteException;
    public Get_regionsResponseGet_regionsResult get_regions(java.util.Calendar last_update) throws java.rmi.RemoteException;
    public Get_usersResponseGet_usersResult get_users(java.util.Calendar last_update) throws java.rmi.RemoteException;
    public Get_users_additional_unitsResponseGet_users_additional_unitsResult get_users_additional_units(java.util.Calendar last_update) throws java.rmi.RemoteException;
    public Get_casesResponseGet_casesResult get_cases(java.util.Calendar last_update) throws java.rmi.RemoteException;
    public Get_cases_unitsResponseGet_cases_unitsResult get_cases_units(java.util.Calendar last_update) throws java.rmi.RemoteException;
    public Get_tables_updatedResponseGet_tables_updatedResult get_tables_updated(java.lang.String table_name, java.util.Calendar last_update) throws java.rmi.RemoteException;
    public Get_cases_classicResponseGet_cases_classicResult get_cases_classic(java.util.Calendar last_update) throws java.rmi.RemoteException;
    public Get_localitiesResponseGet_localitiesResult get_localities(java.util.Calendar last_update) throws java.rmi.RemoteException;
}
