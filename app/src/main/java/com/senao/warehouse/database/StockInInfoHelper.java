package com.senao.warehouse.database;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StockInInfoHelper extends BasicHelper{
	private ItemInfoHelper[] itemInfo;
	private String searchText;
	private String CustmerId;

	public String getCustmerId() {
		return CustmerId;
	}

	public void setCustmerId(String custmerId) {
		CustmerId = custmerId;
	}

	public ItemInfoHelper[] getItemInfo() {
		return itemInfo;
	}
	
	public void setItemInfo(List<ItemInfoHelper> list) {
		this.itemInfo = new ItemInfoHelper[list.size()];
		Iterator<ItemInfoHelper> iter = list.iterator();
		int i = 0;

		while (iter.hasNext()) {
			itemInfo[i] = iter.next();
			i++;
		}
	}
	
	public String getSearchText() {
		return searchText;
	}
	
	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	@Override
	public String toString() {
		return "StockInInfoHelper{" +
				"itemInfo=" + Arrays.toString(itemInfo) +
				", searchText='" + searchText + '\'' +
				", CustmerId='" + CustmerId + '\'' +
				'}';
	}
}
