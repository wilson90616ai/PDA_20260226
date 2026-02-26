package com.senao.warehouse.handler;

import com.google.gson.Gson;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ItemInfoHelper;
import com.senao.warehouse.database.PalletInfoHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

public class ShipmentPalletHandler {
	private HttpClient httpClient;
	private Gson gson = new Gson();
	private ItemInfoHelper item;

	public ShipmentPalletHandler(ItemInfoHelper item) {
		this.item = item;
	}

	public PalletInfoHelper getPalletInfo() {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(item), AppController.getProperties("GetPalletInfo"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), PalletInfoHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			PalletInfoHelper palletInfo = new PalletInfoHelper();
			palletInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			palletInfo.setStrErrorBuf(e.getMessage());
			return palletInfo;
		}

		return null;
	}

	public BasicHelper setPalletInfo(PalletInfoHelper palletInfo) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(palletInfo), AppController.getProperties("SetPalletInfo"));
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
}
