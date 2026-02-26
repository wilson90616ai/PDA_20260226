package com.senao.warehouse.handler;

import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ClosedDateSetHelper;
import com.senao.warehouse.database.ItemInfoHelper;
import com.senao.warehouse.database.SerialNoHelper;
import com.senao.warehouse.database.StockInNoHelper;
import com.senao.warehouse.database.StockInInfoHelper;
import com.senao.warehouse.database.StockInNoListHelper;
import com.senao.warehouse.database.StockInProcessInfoHelper;
import com.senao.warehouse.database.SubinventoryInfoHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;
import com.google.gson.Gson;

public class StockInHandler {
	private HttpClient httpClient;
	private Gson gson = new Gson();

	public BasicHelper getStockInfo(BasicHelper stockInfo) {
		try {
			AppController.debug("Get StockInfo from " + AppController.getServerInfo() + AppController.getProperties("GetStockInfo"));
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(stockInfo), AppController.getProperties("GetStockInfo"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInInfoHelper.class);
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

	public BasicHelper getStockNoList(StockInNoListHelper stockNoList) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(stockNoList), AppController.getProperties("GetStockNoList"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInNoListHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			StockInNoListHelper result = new StockInNoListHelper();
			result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			result.setStrErrorBuf(e.getMessage());
			return result;
		}

		return null;
	}

	public BasicHelper clearStockInfo(BasicHelper partItem) {
		try {
			AppController.debug("Delete StockInfo to " + AppController.getServerInfo() + AppController.getProperties("ClearStockInfo"));
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(partItem), AppController.getProperties("ClearStockInfo"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), ItemInfoHelper.class);
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

	public BasicHelper getSubinventory(SubinventoryInfoHelper item) {
		try {
			httpClient = new HttpClient();
			//ServerResponse response = httpClient.doPost(gson.toJson(item), AppController.getProperties("GetSubinventory"));
			ServerResponse response = httpClient.doPost(gson.toJson(item), AppController.getProperties("GetSubinventoryNew"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), SubinventoryInfoHelper.class);
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

	public BasicHelper getStockInNo(StockInNoHelper stockInNo) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(stockInNo), AppController.getProperties("GetStockInNo"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInNoHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			StockInNoListHelper result = new StockInNoListHelper();
			result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			result.setStrErrorBuf(e.getMessage());
			return result;
		}

		return null;
	}

	public BasicHelper getDateCodeList(StockInNoListHelper stockNOs) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(stockNOs), AppController.getProperties("GetDcList"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInNoListHelper.class);
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

	public BasicHelper getDateCode(StockInNoHelper stockInNO) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(stockInNO), AppController.getProperties("GetDC"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInNoHelper.class);
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

	public BasicHelper checkSN(SerialNoHelper serialNo) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(serialNo), AppController.getProperties("CheckSN"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), SerialNoHelper.class);
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

	public BasicHelper checkPallet(SerialNoHelper serialNo) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(serialNo), AppController.getProperties("CheckPallet"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), SerialNoHelper.class);
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

	public BasicHelper checkSnBox(SerialNoHelper serialNo) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(serialNo), AppController.getProperties("CheckSnBox"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), SerialNoHelper.class);
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

	public BasicHelper checkBoxNo(SerialNoHelper serialNo) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(serialNo), AppController.getProperties("CheckBoxNo"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), SerialNoHelper.class);
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

	public BasicHelper doStockIn(BasicHelper helper) {
		try {
			AppController.debug("Do StockIn from " + AppController.getServerInfo() + AppController.getProperties("DoStockIn"));
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("DoStockIn"));
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

	public BasicHelper checkSubInventory(SubinventoryInfoHelper subinventory) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(subinventory), AppController.getProperties("CheckSubinventory"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), SubinventoryInfoHelper.class);
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

	public BasicHelper doDcMergePn(StockInProcessInfoHelper processInfoHelper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("DcMergePn"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInProcessInfoHelper.class);
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

	public BasicHelper doDcPn(StockInProcessInfoHelper processInfoHelper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("DcPn"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInProcessInfoHelper.class);
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

	public BasicHelper doDCReelID(StockInProcessInfoHelper processInfoHelper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("DcReelID"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInProcessInfoHelper.class);
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

	public BasicHelper doNoNotMerge(StockInProcessInfoHelper processInfoHelper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("NoNotMerge"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInProcessInfoHelper.class);
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

	public BasicHelper doSnPallet(StockInProcessInfoHelper processInfoHelper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("SnPallet"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInProcessInfoHelper.class);
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

	public BasicHelper doSnBox(StockInProcessInfoHelper processInfoHelper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("SnBox"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInProcessInfoHelper.class);
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

	public BasicHelper doSnSn(StockInProcessInfoHelper processInfoHelper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("SnSn"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInProcessInfoHelper.class);
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

	public BasicHelper doSnQRCode(StockInProcessInfoHelper processInfoHelper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("SnQRCode"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInProcessInfoHelper.class);
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

	public BasicHelper doDCMergeReelID(StockInProcessInfoHelper processInfoHelper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("DcMergeReelID"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInProcessInfoHelper.class);
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

	public BasicHelper doNoMerge(StockInProcessInfoHelper processInfoHelper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("NoMerge"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInProcessInfoHelper.class);
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

	public BasicHelper doSnMergePallet(StockInProcessInfoHelper processInfoHelper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("SnMergePallet"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInProcessInfoHelper.class);
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

	public BasicHelper doSnMergeSn(StockInProcessInfoHelper processInfoHelper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("SnMergeSn"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInProcessInfoHelper.class);
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

	public BasicHelper doSnMergeQRCode(StockInProcessInfoHelper processInfoHelper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("SnMergeQRCode"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInProcessInfoHelper.class);
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

	public BasicHelper doSnMergeBox(StockInProcessInfoHelper processInfoHelper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("SnMergeBox"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), StockInProcessInfoHelper.class);
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

	public BasicHelper checkPalletBoxNo(SerialNoHelper serialNoHelper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(serialNoHelper), AppController.getProperties("CheckPalletBoxNo"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), SerialNoHelper.class);
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

    public ClosedDateSetHelper getClosedDate(ClosedDateSetHelper helper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetClosedDate"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), ClosedDateSetHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			ClosedDateSetHelper result = new ClosedDateSetHelper();
			result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			result.setStrErrorBuf(e.getMessage());
			return result;
		}

		return null;
    }

	public ClosedDateSetHelper setClosedDate(ClosedDateSetHelper helper) {
		try {
			httpClient = new HttpClient();
			ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("SetClosedDate"));
			AppController.debug("Server response:" + response.getCode());

			if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
				return gson.fromJson(response.getData(), ClosedDateSetHelper.class);
			}
		} catch (Exception e) {
			AppController.debug("Error to connect the server. " + e.getMessage());
			ClosedDateSetHelper result = new ClosedDateSetHelper();
			result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
			result.setStrErrorBuf(e.getMessage());
			return result;
		}

		return null;
	}
}
