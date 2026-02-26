package com.senao.warehouse.handler;

import com.senao.warehouse.database.BasicHelper;

public class LotcodePermissionHelper extends BasicHelper {
	private int itemId;
	private int permission; //1顯示lotcode 0 不顯示

	public int getPermission() {
		return permission;
	}

	public void setPermission(int permission) {
		this.permission = permission;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
}
