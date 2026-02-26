package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class ReceivingInfoListHelper extends BasicHelper {
	private ReceivingInfoHelper[] receivingInfoList;

	public ReceivingInfoHelper[] getReceivingInfoList() {
		return receivingInfoList;
	}

	public void setReceivingInfoList(List<ReceivingInfoHelper> list) {
		this.receivingInfoList = new ReceivingInfoHelper[list.size()];
		Iterator<ReceivingInfoHelper> iter = list.iterator();
		int i = 0;
		while (iter.hasNext()) {
			receivingInfoList[i] = iter.next();
			i++;
		}
	}
}
