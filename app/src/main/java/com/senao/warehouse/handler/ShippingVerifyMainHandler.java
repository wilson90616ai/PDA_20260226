package com.senao.warehouse.handler;

import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ChkDeliveryInfoHelper;
import com.senao.warehouse.database.ChkSnItemInfoHelper;
import com.senao.warehouse.database.DnShipWayHelper;
import com.senao.warehouse.database.PalletCartonInfoHelper;
import com.senao.warehouse.database.PrintShipDocHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;
import com.google.gson.Gson;

public class ShippingVerifyMainHandler {
	private HttpClient httpClient;
	private Gson gson = new Gson();
	private ChkDeliveryInfoHelper dnInfo;

	public ShippingVerifyMainHandler(ChkDeliveryInfoHelper dnInfo) {
		this.dnInfo = dnInfo;
	}

	public ChkDeliveryInfoHelper getDNInfo() {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(dnInfo), AppController.getProperties("ChkDNInfo"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), ChkDeliveryInfoHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			ChkDeliveryInfoHelper dnInfo = new ChkDeliveryInfoHelper();
			dnInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			dnInfo.setStrErrorBuf(e.getMessage());
			return dnInfo;
		}

		return null;
	}

	public BasicHelper setDNShipWay(DnShipWayHelper dnshipway) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(dnshipway), AppController.getProperties("SetDNShipWay"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), DnShipWayHelper.class);
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

	public BasicHelper chkSNItemInfo(ChkSnItemInfoHelper snitem) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(snitem), AppController.getProperties("ChkSNItemInfo"));
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

	public BasicHelper printShipDoc(PrintShipDocHelper shipdoc) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(shipdoc), AppController.getProperties("PrintShipDoc"));
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

	public BasicHelper getPalletCartonInfo(PalletCartonInfoHelper palletCartonInfo) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(palletCartonInfo), AppController.getProperties("GetPalletCartonInfo"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), PalletCartonInfoHelper.class);
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
