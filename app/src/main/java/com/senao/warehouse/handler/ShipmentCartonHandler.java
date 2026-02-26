package com.senao.warehouse.handler;

import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.CartonInfoHelper;
import com.senao.warehouse.database.ItemInfoHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;
import com.google.gson.Gson;

public class ShipmentCartonHandler {
	private HttpClient httpClient;
	private Gson gson = new Gson();
	private ItemInfoHelper item;

	public ShipmentCartonHandler(ItemInfoHelper item) {
		this.item = item;
	}

	public CartonInfoHelper getCartonInfo() {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(item), AppController.getProperties("GetCartonInfo"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), CartonInfoHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			CartonInfoHelper carton = new CartonInfoHelper();
			carton.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			carton.setStrErrorBuf(e.getMessage());
			return carton;
		}

		return null;
	}

	public BasicHelper setCartonInfo(CartonInfoHelper carton) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(carton), AppController.getProperties("SetCartonInfo"));
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
