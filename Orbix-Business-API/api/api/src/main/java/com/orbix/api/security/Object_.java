             /**
 * 
 */
package com.orbix.api.security;

/**
 * @author GODFREY
 *
 */
public class Object_ {
	
	/**
	 * List of authorities not allowed
	 * ACCESS ALL CREATE READ UPDATE DELETE ACTIVATE APPROVE VERIFY CANCEL
	 * C R U D T V Y X
	 * Format: OBJECT-LIST OF NOT ALLOWED AUTHORITIES
	 */
	
	public static String USER = "USER-ALL CREATE READ UPDATE";
	public static String ADMIN = "ADMIN-ACCESS";	
	public static String ROLE = "ROLE-ALL CREATE READ UPDATE";
	
	public static String RCPTN = "RCPTN-ACCESS";
	public static String FINC = "FINC-ACCESS";
	public static String SHOP = "SHOP-ACCESS";
	public static String PRCMT = "PRCMT-ACCESS";
	public static String MNGNT = "MNGNT-ACCESS";
	
//	public static String BILL = "BILL-ALL CREATE";
//	
//	public static String CASHIER_SERVICE = "CASHIER_SERVICE-ACCESS";
//	
//	public static String WAREHOUSE_SERVICE = "WAREHOUSE_SERVICE-ACCESS";
//	public static String PROCUREMENT_SERVICE = "PROCUREMENT_SERVICE-ACCESS";
//	
//	public static String HR_SERVICE = "HR_SERVICE-ACCESS";
//	public static String EMPLOYEE = "EMPLOYEE-ALL CREATE READ UPDATE";
//	public static String PAYROLL = "PAYROLL-ALL CREATE READ UPDATE DELETE CANCEL VERIFY APPROVE ARCHIVE";
//	
//	public static String STORE_ORDER = "STORE_ORDER-ALL CREATE READ UPDATE DELETE CANCEL VERIFY APPROVE ARCHIVE";
//
//	public static String LOCAL_PURCHASE_ORDER = "LOCAL_PURCHASE_ORDER-ALL CREATE READ UPDATE DELETE CANCEL VERIFY APPROVE ARCHIVE";
//	public static String GOODS_RECEIVED_NOTE = "GOODS_RECEIVED_NOTE-ALL CREATE READ UPDATE DELETE CANCEL VERIFY APPROVE ARCHIVE";
//	public static String SUPPLIER_PRICE_LIST = "SUPPLIER_PRICE_LIST-ALL CREATE READ UPDATE DELETE";
//	
//	public static String MANAGEMENT_GENERAL = "MANAGEMENT_GENERAL-ACCESS";
//	public static String MANAGEMENT_FINANCE = "MANAGEMENT_FINANCE-ACCESS";
//	public static String MANAGEMENT_OPERATIONS = "MANAGEMENT_OPERATIONS-ACCESS";
//	
//	public static String REPORT_SERVICE = "REPORT_SERVICE-ACCESS";
//	
//
//	public static String DEMAND_ORDER = "DEMAND_ORDER";
//	public static String GRN = "GRN";
//	
//	public static String LPO = "LPO";
//	
//	public static String COMPANY_PROFILE = "COMPANY_PROFILE";
//	public static String DAY = "DAY";
//	public static String SUPPLIER = "SUPPLIER";	
}
