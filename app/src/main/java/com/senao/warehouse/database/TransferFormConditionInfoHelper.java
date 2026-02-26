package com.senao.warehouse.database;

import java.util.Iterator;
import java.util.List;

public class TransferFormConditionInfoHelper extends BasicHelper {
    private DocumentInfoHelper[] list;
    private String searchText;
    private String type; //P023轉入,P023轉出

    public DocumentInfoHelper[] getList() {
        return list;
    }

    public void setList(List<DocumentInfoHelper> list) {
        this.list = new DocumentInfoHelper[list.size()];
        Iterator<DocumentInfoHelper> iterator = list.iterator();
        int i = 0;

        while (iterator.hasNext()) {
            this.list[i] = iterator.next();
            i++;
        }
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
