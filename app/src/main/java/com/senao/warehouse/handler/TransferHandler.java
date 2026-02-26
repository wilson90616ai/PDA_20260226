package com.senao.warehouse.handler;

import com.google.gson.Gson;
import com.senao.warehouse.AppController;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.DocumentInfoHelper;
import com.senao.warehouse.database.RevokeInfoHelper;
import com.senao.warehouse.database.TempTransferProcessInfoHelper;
import com.senao.warehouse.database.TransferFormConditionInfoHelper;
import com.senao.warehouse.database.TransferInfoHelper;
import com.senao.warehouse.database.TransferItemInfoHelper;
import com.senao.warehouse.database.TransferProcessInfoHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;

public class TransferHandler {
    private HttpClient httpClient;
    private Gson gson = new Gson();

    public BasicHelper doTransferItemSn(TransferProcessInfoHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("TransferItemSn"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), TransferProcessInfoHelper.class);
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

    public BasicHelper doCheckSn(TransferProcessInfoHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("CheckSn"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), TransferProcessInfoHelper.class);
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

    public BasicHelper doTransferItemNoDc(TransferProcessInfoHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("TransferItemNoDc"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), TransferProcessInfoHelper.class);
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

    public BasicHelper getTransferInfo(BasicHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("TransferInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), TransferProcessInfoHelper.class);
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

    public BasicHelper triggerTransferLocator(BasicHelper helper) {
        try {
            AppController.debug("Trigger Transfer Locator from " + AppController.getServerInfo() + AppController.getProperties("TriggerTransferLocator"));
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("TriggerTransferLocator"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), TransferInfoHelper.class);
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

    public BasicHelper processTempTransferInfo(BasicHelper helper) {
        try {
            AppController.debug("Process Temp Transfer Info from " + AppController.getServerInfo() + AppController.getProperties("ProcessTempTransferInfo"));
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("ProcessTempTransferInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), TempTransferProcessInfoHelper.class);
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

    public BasicHelper triggerTempTransferInfo(BasicHelper helper) {
        try {
            AppController.debug("Get On Hand Inventory Info from " + AppController.getServerInfo() + AppController.getProperties("TriggerTempTransferInfo"));
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("TriggerTempTransferInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), TransferInfoHelper.class);
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

    public BasicHelper getOnHandInventoryInfo(BasicHelper helper) {
        try {
            AppController.debug("Get On Hand Inventory Info from " + AppController.getServerInfo() + AppController.getProperties("OnHandInventoryInfo"));
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("OnHandInventoryInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), TransferInfoHelper.class);
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

    public BasicHelper getTempTransferInfo(BasicHelper helper) {
        try {
            AppController.debug("Get Temp Transfer Info from " + AppController.getServerInfo() + AppController.getProperties("TempTransferInfo"));
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("TempTransferInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), TransferInfoHelper.class);
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

    public BasicHelper getItemOnHandInventoryInfo(BasicHelper helper) {
        try {
            AppController.debug("Get On Hand Inventory Info from " + AppController.getServerInfo() + AppController.getProperties("ItemOnHandInventoryInfo"));
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("ItemOnHandInventoryInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), TransferItemInfoHelper.class);
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

    public BasicHelper delTempTransferInfo(BasicHelper helper) {
        try {
            AppController.debug("Delete Temp Transfer Info from " + AppController.getServerInfo() + AppController.getProperties("DeleteTempTransferInfo"));
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("DeleteTempTransferInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), TransferItemInfoHelper.class);
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

    public BasicHelper revokeEzflowDoc(BasicHelper helper) {
        try {
            AppController.debug("Revoke Ezflow Doc from " + AppController.getServerInfo() + AppController.getProperties("RevokeEzflowDoc"));
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("RevokeEzflowDoc"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), DocumentInfoHelper.class);
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

    public BasicHelper getEzFlowDocumentListForRevoke(BasicHelper helper) {
        try {
            AppController.debug("Get EzFlow Document List For Revoke from " + AppController.getServerInfo() + AppController.getProperties("GetApplicantListForRevoke"));
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetApplicantListForRevoke"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), RevokeInfoHelper.class);
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

    public BasicHelper getEzflowDocNoList(BasicHelper helper) {
        try {
            AppController.debug("Get EzFlow Document No List from " + AppController.getServerInfo() + AppController.getProperties("GetEzflowDocNoList"));
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetEzflowDocNoList"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), TransferFormConditionInfoHelper.class);
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

    public BasicHelper getProcessNoList(BasicHelper helper) {
        try {
            AppController.debug("Get Process No List from " + AppController.getServerInfo() + AppController.getProperties("GetProcessNoList"));
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetProcessNoList"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), TransferFormConditionInfoHelper.class);
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

    public BasicHelper getApplicationDeptList(BasicHelper helper) {
        try {
            AppController.debug("Get Application Dept List from " + AppController.getServerInfo() + AppController.getProperties("GetApplicationDeptList"));
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetApplicationDeptList"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), TransferFormConditionInfoHelper.class);
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

    public BasicHelper getApplicantList(BasicHelper helper) {
        try {
            AppController.debug("Get Applicant List from " + AppController.getServerInfo() + AppController.getProperties("GetApplicantList"));
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetApplicantList"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), TransferFormConditionInfoHelper.class);
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
