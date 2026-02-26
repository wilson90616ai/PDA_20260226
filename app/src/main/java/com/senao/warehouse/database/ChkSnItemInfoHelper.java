package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class ChkSnItemInfoHelper extends BasicHelper {
	private int deliveryID;
	private int typeFlag;
	private String palletNo;
	private String cartonNo;
	private String serialNo;
	private String[] snList;

	public int getDeliveryID() {
		return deliveryID;
	}

	public void setDeliveryID(int deliveryID) {
		this.deliveryID = deliveryID;
	}

	public int getTypeFlag() {
		return typeFlag;
	}

	public void setTypeFlag(int typeFlag) {
		this.typeFlag = typeFlag;
	}

	public String getPalletNo() {
		return palletNo;
	}

	public void setPalletNo(String palletNo) {
		this.palletNo = palletNo;
	}

	public String getCartonNo() {
		return cartonNo;
	}

	public void setCartonNo(String cartonNo) {
		this.cartonNo = cartonNo;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String[] getSnList() {
		return snList;
	}

	public void setSnList(List<String> list) {
		this.snList = new String[list.size()];
		Iterator<String> iter = list.iterator();
		int i = 0;
		while (iter.hasNext()) {
			snList[i] = iter.next();
			i++;
		}
	}
}
