package com.senao.warehouse.handler;

import com.senao.warehouse.AppController;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.UserInfoHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;

public class LoginHandler extends  BasicHandler{

	public BasicHelper doLogin(BasicHelper user) {
		try {
			AppController.debug("Login from " + AppController.getServerInfo() + AppController.getProperties("Login"));
			HttpClient httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(user), AppController.getProperties("Login"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), UserInfoHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			BasicHelper result = new BasicHelper();
			result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			result.setStrErrorBuf(e.getMessage());
			return user;
		}

		return null;
	}
}
