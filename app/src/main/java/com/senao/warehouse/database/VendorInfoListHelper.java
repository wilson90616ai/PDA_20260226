package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class VendorInfoListHelper extends BasicHelper {
	private VendorInfoHelper[] vendorList;

	public VendorInfoHelper[] getVendorList() {
		return vendorList;
	}

	public void setVendorList(List<VendorInfoHelper> list) {
		this.vendorList = new VendorInfoHelper[list.size()];
		Iterator<VendorInfoHelper> iter = list.iterator();
		int i = 0;

		while (iter.hasNext()) {
			vendorList[i] = iter.next();
			i++;
		}
	}
}
