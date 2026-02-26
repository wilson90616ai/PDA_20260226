package com.senao.warehouse.database;

/**
 * Created by 102069 on 2017/6/15.
 */

public enum TRANSFER_FORM_TYPE {
    TO_P023("P023轉入"), FROM_P023("P023轉出");

    private final String name;

    TRANSFER_FORM_TYPE(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        //(otherName == null) check is not needed because name.equals(null) returns false
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
