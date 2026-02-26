package com.senao.warehouse.handler;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ReceivingInfoHelper;
import com.senao.warehouse.database.VendorInfoHelper;
import com.senao.warehouse.database.VendorInfoListHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

public class ReceivingHandler {
    private HttpClient httpClient;
    private Gson gson = new Gson();

    public BasicHelper doReceivingProcess(VendorInfoHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("DoReceivingProcess"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), VendorInfoHelper.class);
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

    public BasicHelper doClearReceivingInfo(ReceivingInfoHelper receivingInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(receivingInfo), AppController.getProperties("ClearReceivingInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ReceivingInfoHelper.class);
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

    public BasicHelper getReceivingInfoList(VendorInfoHelper vendorInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(vendorInfo), AppController.getProperties("GetReceivingInfoList"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), VendorInfoHelper.class);
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

    public BasicHelper checkVendorInfo(VendorInfoHelper vendorInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(vendorInfo), AppController.getProperties("CheckVendorInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), VendorInfoListHelper.class);
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

    public BasicHelper getVendorInfoList(VendorInfoHelper vendorInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(vendorInfo), AppController.getProperties("GetVendorList"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), VendorInfoListHelper.class);
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

    public BasicHelper doCheckProcess(ReceivingInfoHelper receivingInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(receivingInfo), AppController.getProperties("DoCheckProcess"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ReceivingInfoHelper.class);
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

    public BasicHelper doCheckReelID(ReceivingInfoHelper receivingInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(receivingInfo), AppController.getProperties("DoCheckReelID"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ReceivingInfoHelper.class);
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

    public BasicHelper doCheckDateCode(ReceivingInfoHelper receivingInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(receivingInfo), AppController.getProperties("DoCheckDateCode"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ReceivingInfoHelper.class);
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

    public BasicHelper updateInvoiceNo(ReceivingInfoHelper receivingInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(receivingInfo), AppController.getProperties("UpdateInvoiceNo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ReceivingInfoHelper.class);
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


    public BasicHelper getTempReceivingInfoList(VendorInfoHelper vendorInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(vendorInfo), AppController.getProperties("GetTempReceivingInfoList"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), VendorInfoHelper.class);
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

    public BasicHelper doReceivingProcessForTemp(VendorInfoHelper vendorInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(vendorInfo), AppController.getProperties("DoReceivingProcessForTemp"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), VendorInfoHelper.class);
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

    public BasicHelper doDelReceivingData(VendorInfoHelper vendorInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(vendorInfo), AppController.getProperties("DeleteReceivingData"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), VendorInfoHelper.class);
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

    public BasicHelper doCheckInvoiceNo(ReceivingInfoHelper receivingInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(receivingInfo), AppController.getProperties("DoCheckInvoiceNo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ReceivingInfoHelper.class);
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

    public BasicHelper getReceivingInvoiceNoList(VendorInfoHelper vendorInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(vendorInfo), AppController.getProperties("GetReceivingInvoiceNoList"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), VendorInfoHelper.class);
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
