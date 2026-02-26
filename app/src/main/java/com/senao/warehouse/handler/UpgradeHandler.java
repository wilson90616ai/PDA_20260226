package com.senao.warehouse.handler;

import com.senao.warehouse.database.ApkHelper;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;
import com.google.gson.Gson;

public class UpgradeHandler {
	private Gson gson = new Gson();

	public BasicHelper doCheckNewVersion(BasicHelper apk) {
		try {
			AppController.debug("Upgrade " + AppController.getServerInfo() + AppController.getProperties("Upgrade"));
			HttpClient httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(apk), AppController.getProperties("Upgrade"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), ApkHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			BasicHelper helper = new BasicHelper();
			helper.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			helper.setStrErrorBuf(e.getMessage());
			return helper;
		}

		return null;
	}

	public BasicHelper downloadApk(BasicHelper apk) {
		try {
			AppController.debug("Upgrade " + AppController.getServerInfo() + AppController.getProperties("Upgrade"));
			HttpClient httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(apk), AppController.getProperties("Upgrade"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), ApkHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			BasicHelper helper = new BasicHelper();
			helper.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			helper.setStrErrorBuf(e.getMessage());
			return helper;
		}

		return null;
	}
}
