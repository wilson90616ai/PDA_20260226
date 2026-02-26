package com.senao.warehouse.handler;

import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.DcNoItemInfoHelper;
import com.senao.warehouse.database.ItemInfoHelper;
import com.senao.warehouse.database.ItemQtyInfoHelper;
import com.senao.warehouse.database.PackingQtyInfoHelper;
import com.senao.warehouse.database.ShipmentPalletInfoHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;
import com.google.gson.Gson;

public class ShipmentCreatePalletHandler {
	private HttpClient httpClient;
	private Gson gson = new Gson();
	private ItemInfoHelper item;

	public ShipmentCreatePalletHandler(ItemInfoHelper item) {
		this.item = item;
	}

	public LotcodePermissionHelper getLotcodePermission(LotcodePermissionHelper snItem) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(snItem), AppController.getProperties("GetIsLotcode"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), LotcodePermissionHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			LotcodePermissionHelper result = new LotcodePermissionHelper();
			result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			result.setStrErrorBuf(e.getMessage());
			return result;
		}

		return null;
	}

	public BasicHelper setDCItemInfo(DcNoItemInfoHelper dcItem) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(dcItem), AppController.getProperties("SetDCItem"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), BasicHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			BasicHelper result = new BasicHelper();
			result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			result.setStrErrorBuf(e.getMessage());
			return result;
		}

		return null;
	}

	public BasicHelper setNOItemInfo(DcNoItemInfoHelper noItem) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(noItem), AppController.getProperties("SetNOItem"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), BasicHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			BasicHelper result = new BasicHelper();
			result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			result.setStrErrorBuf(e.getMessage());
			return result;
		}

		return null;
	}

	public ItemQtyInfoHelper getItemQtyInfo(ItemQtyInfoHelper itemQty) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(itemQty), AppController.getProperties("GetItemQtyInfo"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), ItemQtyInfoHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			ItemQtyInfoHelper item = new ItemQtyInfoHelper();
			item.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			item.setStrErrorBuf(e.getMessage());
			return item;
		}

		return null;
	}

	public PackingQtyInfoHelper getPackingQtyInfo(PackingQtyInfoHelper packingQty) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(packingQty), AppController.getProperties("GetPackingQtyInfo"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), PackingQtyInfoHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			packingQty.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			packingQty.setStrErrorBuf(e.getMessage());
			return packingQty;
		}

		return null;
	}

	public ShipmentPalletInfoHelper getShipmentDeliveryInfo(ShipmentPalletInfoHelper printInfo) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(printInfo), AppController.getProperties("GetDNInfoPrint"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), ShipmentPalletInfoHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			printInfo = new ShipmentPalletInfoHelper();
			printInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			printInfo.setStrErrorBuf(e.getMessage());
			return printInfo;
		}

		return null;
	}
}
