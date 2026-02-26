package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class StockInNoListHelper extends BasicHelper {
	private String searchText;
	private String partNo;
	private StockInNoHelper[] stockInNoList;
	
	public StockInNoHelper[] getStockInNoList() {
		return stockInNoList;
	}
	public void setStockNoList(List<StockInNoHelper> list) {
		this.stockInNoList = new StockInNoHelper[list.size()];
		Iterator<StockInNoHelper> iter = list.iterator();
		int i = 0;

		while (iter.hasNext()) {
			stockInNoList[i] = iter.next();
			i++;
		}
	}
	
	public String getPartNo() {
		return partNo;
	}

	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}
}
