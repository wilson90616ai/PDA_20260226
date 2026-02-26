package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class AcceptanceNoHelper extends BasicHelper {
	private String no;
	private AcceptanceItemInfoHelper itemInfo;
	private AcceptanceDateCodeInfo[] dateDodeInfo;
	
	public AcceptanceDateCodeInfo[] getDateCodeInfo() {
		return dateDodeInfo;
	}
	
	public void setDateCodeInfo(List<AcceptanceDateCodeInfo> list) {
		this.dateDodeInfo = new AcceptanceDateCodeInfo[list.size()];
		Iterator<AcceptanceDateCodeInfo> iter = list.iterator();
		int i = 0;
		while (iter.hasNext()) {
			dateDodeInfo[i] = iter.next();
			i++;
		}
	}
	
	public String getNo() {
		return no;
	}
	
	public void setNo(String no) {
		this.no = no;
	}
	
	public AcceptanceItemInfoHelper getItemInfo() {
		return itemInfo;
	}
	
	public void setItemInfo(AcceptanceItemInfoHelper itemInfo) {
		this.itemInfo = itemInfo;
	}
}
