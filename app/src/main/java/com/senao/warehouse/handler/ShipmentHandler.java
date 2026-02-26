package com.senao.warehouse.handler;

import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.DeliveryInfoHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;
import com.google.gson.Gson;

public class ShipmentHandler {
	private HttpClient httpClient;
	private Gson gson = new Gson();
	private DeliveryInfoHelper dnInfo;

	public ShipmentHandler(DeliveryInfoHelper dnInfo) {
		this.dnInfo = dnInfo;
	}

	public DeliveryInfoHelper getDNInfo() {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(dnInfo), AppController.getProperties("GetDNInfo"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), DeliveryInfoHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			DeliveryInfoHelper dnInfo = new DeliveryInfoHelper();
			dnInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			dnInfo.setStrErrorBuf(e.getMessage());
			return dnInfo;
		}

		return null;
	}

	public BasicHelper delPackingInfo(DeliveryInfoHelper dnitem) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(dnitem), AppController.getProperties("DelPackingInfo"));
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

	public BasicHelper checkOnHandInfo() {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(dnInfo), AppController.getProperties("CheckOnHandInfo"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), DeliveryInfoHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			DeliveryInfoHelper dnInfo = new DeliveryInfoHelper();
			dnInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			dnInfo.setStrErrorBuf(e.getMessage());
			return dnInfo;
		}

		return null;
	}
}
