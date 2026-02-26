package com.senao.warehouse.database;

public class ApkHelper extends BasicHelper {
	private String strVersionCode;
	private String strVersionName;
	private String strFileName;
	
	public String getStrVersionCode() {
		return strVersionCode;
	}
	
	public void setStrVersionCode(String strVersionCode) {
		this.strVersionCode = strVersionCode;
	}
	
	public String getStrVersionName() {
		return strVersionName;
	}
	
	public void setStrVersionName(String strVersionName) {
		this.strVersionName = strVersionName;
	}
	
	public String getStrFileName() {
		return strFileName;
	}
	
	public void setStrFileName(String strFileName) {
		this.strFileName = strFileName;
	}
}
