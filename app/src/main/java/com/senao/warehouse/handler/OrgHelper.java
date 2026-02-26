package com.senao.warehouse.handler;

import com.senao.warehouse.database.BasicHelper;

import java.util.List;
import java.util.Map;

public class OrgHelper extends BasicHelper {
	//private String orgName;
	private List<String> subs;
	List<OuOrgHelper> list;

	public List<OuOrgHelper> getList() {
		return list;
	}

	public void setList(List<OuOrgHelper> list) {
		this.list = list;
	}

	public List<String> getSubs() {
		return subs;
	}

	public void setSubs(List<String> subs) {
		this.subs = subs;
	}

	//public String getOrgName() { return orgName; }

	//public void setOrgName(String orgName) { this.orgName = orgName; }

	private Map<Integer, String> orMap;

	public Map<Integer, String> getOrMap() {
		return orMap;
	}

	public void setOrMap(Map<Integer, String> orMap) {
		this.orMap = orMap;
	}
}
