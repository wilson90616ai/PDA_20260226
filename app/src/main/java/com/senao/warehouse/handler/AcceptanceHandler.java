package com.senao.warehouse.handler;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.database.AcceptanceConditionHelper;
import com.senao.warehouse.database.AcceptanceInfoHelper;
import com.senao.warehouse.database.AcceptanceNoHelper;
import com.senao.warehouse.database.AcceptanceNoListHelper;
import com.senao.warehouse.database.AcceptanceProcessInfoHelper;
import com.senao.warehouse.database.AcceptanceSerialNoHelper;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ItemInfoHelper;
import com.senao.warehouse.database.StockInInfoHelper;
import com.senao.warehouse.database.SubinventoryInfoHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;

public class AcceptanceHandler {
    private HttpClient httpClient;
    private Gson gson = new Gson();

    public BasicHelper getAcceptanceInfo(AcceptanceConditionHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetAcceptanceInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceConditionHelper.class);
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

    public BasicHelper getAcceptanceInfoOsp(AcceptanceConditionHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetAcceptanceInfoOsp"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceConditionHelper.class);
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

    public BasicHelper doDeleteTempInfo(AcceptanceInfoHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("DoDeleteTempInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceInfoHelper.class);
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

    public BasicHelper getStockInfo(StockInInfoHelper stockInfo) {
        try {
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

    public BasicHelper getReceiptNoList(AcceptanceInfoHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetReceiptNoList"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceInfoHelper.class);
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


    public BasicHelper getReceiptNoListOsp(AcceptanceInfoHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetReceiptNoListOsp"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceInfoHelper.class);
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

    public BasicHelper clearStockInfo(ItemInfoHelper partItem) {
        try {
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
            ServerResponse response = httpClient.doPost(gson.toJson(item), AppController.getProperties("GetSubinventory"));
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

    public BasicHelper getReceiptNo(AcceptanceConditionHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetReceiptNo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceConditionHelper.class);
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

    public BasicHelper getReceiptNoOsp(AcceptanceConditionHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetReceiptNoOsp"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceConditionHelper.class);
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

    public BasicHelper getDateCodeList(AcceptanceNoListHelper stockNOs) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(stockNOs), AppController.getProperties("GetDcList"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceNoListHelper.class);
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

    public BasicHelper getDateCode(AcceptanceNoHelper stockInNO) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(stockInNO), AppController.getProperties("GetDC"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceNoHelper.class);
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

    public BasicHelper checkSN(AcceptanceSerialNoHelper serialNo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(serialNo), AppController.getProperties("AcceptanceCheckSN"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceSerialNoHelper.class);
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

    public BasicHelper checkPallet(AcceptanceSerialNoHelper serialNo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(serialNo), AppController.getProperties("AcceptanceCheckPallet"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceSerialNoHelper.class);
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

    public BasicHelper checkSnBox(AcceptanceSerialNoHelper serialNo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(serialNo), AppController.getProperties("AcceptanceCheckSnBox"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceSerialNoHelper.class);
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

    public BasicHelper checkBoxNo(AcceptanceSerialNoHelper serialNo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(serialNo), AppController.getProperties("AcceptanceCheckBoxNo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceSerialNoHelper.class);
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

    public BasicHelper doStockInBatch(AcceptanceConditionHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("AcceptanceDoStockInBatch"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceConditionHelper.class);
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

    public BasicHelper doStockIn(AcceptanceConditionHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("AcceptanceDoStockIn"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceConditionHelper.class);
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

    public BasicHelper doStockInForSn(ItemInfoHelper selectedStockInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(selectedStockInfo), AppController.getProperties("DoStockInForSnControl"));
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

    public BasicHelper doStockInForNo(ItemInfoHelper selectedStockInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(selectedStockInfo), AppController.getProperties("DoStockInForNoControl"));
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

    public BasicHelper doStockInForDc(ItemInfoHelper selectedStockInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(selectedStockInfo), AppController.getProperties("DoStockInForDcControl"));
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

    public BasicHelper checkSubInventory(SubinventoryInfoHelper subinventory) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(subinventory), AppController.getProperties("AcceptanceCheckSubinventory"));
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

    public BasicHelper doDcMergePn(AcceptanceProcessInfoHelper processInfoHelper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("AcceptanceDcMergePn"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceProcessInfoHelper.class);
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

    public BasicHelper doDcPn(AcceptanceProcessInfoHelper processInfoHelper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("AcceptanceDcPn"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceProcessInfoHelper.class);
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

    public BasicHelper doDCReelID(AcceptanceProcessInfoHelper processInfoHelper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("AcceptanceDcReelID"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceProcessInfoHelper.class);
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

    public BasicHelper doNoNotMerge(AcceptanceProcessInfoHelper processInfoHelper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("AcceptanceNoNotMerge"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceProcessInfoHelper.class);
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

    public BasicHelper doSnPallet(AcceptanceProcessInfoHelper processInfoHelper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("AcceptanceSnPallet"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceProcessInfoHelper.class);
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

    public BasicHelper doSnBox(AcceptanceProcessInfoHelper processInfoHelper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("AcceptanceSnBox"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceProcessInfoHelper.class);
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

    public BasicHelper doSnSn(AcceptanceProcessInfoHelper processInfoHelper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("AcceptanceSnSn"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceProcessInfoHelper.class);
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

    public BasicHelper doDCMergeReelID(AcceptanceProcessInfoHelper processInfoHelper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("AcceptanceDcMergeReelID"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceProcessInfoHelper.class);
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

    public BasicHelper doNoMerge(AcceptanceProcessInfoHelper processInfoHelper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("AcceptanceNoMerge"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceProcessInfoHelper.class);
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

    public BasicHelper doSnMergePallet(AcceptanceProcessInfoHelper processInfoHelper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("AcceptanceSnMergePallet"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceProcessInfoHelper.class);
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

    public BasicHelper doSnMergeSn(AcceptanceProcessInfoHelper processInfoHelper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("AcceptanceSnMergeSn"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceProcessInfoHelper.class);
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

    public BasicHelper doSnMergeBox(AcceptanceProcessInfoHelper processInfoHelper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(processInfoHelper), AppController.getProperties("AcceptanceSnMergeBox"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceProcessInfoHelper.class);
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

    public BasicHelper checkPalletBoxNo(AcceptanceSerialNoHelper serialNoHelper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(serialNoHelper), AppController.getProperties("AcceptanceCheckPalletBoxNo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), AcceptanceSerialNoHelper.class);
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
