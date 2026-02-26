package com.senao.warehouse.handler;

import com.google.gson.Gson;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.ExtremePalletInfoHelper;
import com.senao.warehouse.database.InvoiceHelper;
import com.senao.warehouse.database.MaterialLabelHelper;
import com.senao.warehouse.database.PnItemInfoHelper;
import com.senao.warehouse.database.PnItemInfoListHelper;
import com.senao.warehouse.database.RmaBoxInfoHelper;
import com.senao.warehouse.database.RmaBoxListInfoHelper;
import com.senao.warehouse.database.RmaPalletInfoHelper;
import com.senao.warehouse.database.RmaSnInfoHelper;
import com.senao.warehouse.database.ShipmentPalletInfoHelper;
import com.senao.warehouse.database.ShipmentPalletSnInfoHelper;
import com.senao.warehouse.database.ShipmentPalletSnInfoHelperSophos;
import com.senao.warehouse.database.NewPrintData;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

public class PrintLabelHandler {
    private HttpClient httpClient;
    private Gson gson = new Gson();

    public PnItemInfoHelper getPNInfo(PnItemInfoHelper pnInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(pnInfo), AppController.getProperties("GetPNInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), PnItemInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            pnInfo = new PnItemInfoHelper();
            pnInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            pnInfo.setStrErrorBuf(e.getMessage());
            return pnInfo;
        }

        return null;
    }

    public PnItemInfoListHelper getPNInfoFromSfis(PnItemInfoHelper pnInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(pnInfo), AppController.getProperties("GetPNInfoSfis"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), PnItemInfoListHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            PnItemInfoListHelper pnInfoList = new PnItemInfoListHelper();
            pnInfoList.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            pnInfoList.setStrErrorBuf(e.getMessage());
            return pnInfoList;
        }

        return null;
    }

    public ShipmentPalletInfoHelper getShipmentPalletInfo(ShipmentPalletInfoHelper palletInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(palletInfo), AppController.getProperties("GetShipmentPalletInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ShipmentPalletInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            palletInfo = new ShipmentPalletInfoHelper();
            palletInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            palletInfo.setStrErrorBuf(e.getMessage());
            return palletInfo;
        }

        return null;
    }

    public ShipmentPalletSnInfoHelper printShipmentPalletSnInfo01(ShipmentPalletSnInfoHelper palletSnInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(palletSnInfo), AppController.getProperties("PrintShipmentPalletSnInfo01"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ShipmentPalletSnInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            palletSnInfo = new ShipmentPalletSnInfoHelper();
            palletSnInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            palletSnInfo.setStrErrorBuf(e.getMessage());
            return palletSnInfo;
        }

        return null;
    }

    public ShipmentPalletSnInfoHelper NewprintShipmentPalletSnInfo01(ShipmentPalletSnInfoHelper palletSnInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(palletSnInfo), AppController.getProperties("PrintShipmentPalletSnInfo01"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ShipmentPalletSnInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            palletSnInfo = new ShipmentPalletSnInfoHelper();
            palletSnInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            palletSnInfo.setStrErrorBuf(e.getMessage());
            return palletSnInfo;
        }

        return null;
    }

    public ShipmentPalletSnInfoHelper printShipmentPalletSnInfo(ShipmentPalletSnInfoHelper palletSnInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(palletSnInfo), AppController.getProperties("PrintShipmentPalletSnInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ShipmentPalletSnInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            palletSnInfo = new ShipmentPalletSnInfoHelper();
            palletSnInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            palletSnInfo.setStrErrorBuf(e.getMessage());
            return palletSnInfo;
        }

        return null;
    }

    //GetPalletSnInfoByDnPallet
    public ShipmentPalletSnInfoHelperSophos printShipmentPalletSnInfo_SOPHOS(ShipmentPalletSnInfoHelperSophos palletSnInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(palletSnInfo), AppController.getProperties("GetPalletSnInfoByDnPallet_SOPHOS"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ShipmentPalletSnInfoHelperSophos.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            palletSnInfo = new ShipmentPalletSnInfoHelperSophos();
            palletSnInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            palletSnInfo.setStrErrorBuf(e.getMessage());
            return palletSnInfo;
        }

        return null;
    }

    public BasicHelper getPalletSnInfo(ShipmentPalletSnInfoHelper palletSnInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(palletSnInfo), AppController.getProperties("GetPalletSnInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ShipmentPalletSnInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            palletSnInfo = new ShipmentPalletSnInfoHelper();
            palletSnInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            palletSnInfo.setStrErrorBuf(e.getMessage());
            return palletSnInfo;
        }

        return null;
    }

    public RmaPalletInfoHelper doPrintLabelByPallet(RmaPalletInfoHelper palletInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(palletInfo), AppController.getProperties("PrintLabelByPallet"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), RmaPalletInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            palletInfo = new RmaPalletInfoHelper();
            palletInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            palletInfo.setStrErrorBuf(e.getMessage());
            return palletInfo;
        }

        return null;
    }

    public RmaBoxInfoHelper doCheckRmaBoxSn(RmaBoxInfoHelper boxInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(boxInfo), AppController.getProperties("CheckRmaBoxSnInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), RmaBoxInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            boxInfo = new RmaBoxInfoHelper();
            boxInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            boxInfo.setStrErrorBuf(e.getMessage());
            return boxInfo;
        }

        return null;
    }

    public RmaPalletInfoHelper doCheckRmaPalletSn(RmaPalletInfoHelper palletInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(palletInfo), AppController.getProperties("CheckRmaPalletSnInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), RmaPalletInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            palletInfo = new RmaPalletInfoHelper();
            palletInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            palletInfo.setStrErrorBuf(e.getMessage());
            return palletInfo;
        }

        return null;

    }

    public RmaBoxListInfoHelper doPrintLabelByBox(RmaBoxListInfoHelper boxInfoList) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(boxInfoList), AppController.getProperties("PrintLabelByBox"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), RmaBoxListInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            boxInfoList = new RmaBoxListInfoHelper();
            boxInfoList.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            boxInfoList.setStrErrorBuf(e.getMessage());
            return boxInfoList;
        }

        return null;
    }

    public RmaBoxListInfoHelper doPrintLabelByBox01(RmaBoxListInfoHelper boxInfoList) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(boxInfoList), AppController.getProperties("PrintLabelByBox01"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), RmaBoxListInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            boxInfoList = new RmaBoxListInfoHelper();
            boxInfoList.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            boxInfoList.setStrErrorBuf(e.getMessage());
            return boxInfoList;
        }

        return null;
    }

    public RmaSnInfoHelper getRmaSnInfo(RmaSnInfoHelper snInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(snInfo), AppController.getProperties("GetRmaSnInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), RmaSnInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            snInfo = new RmaSnInfoHelper();
            snInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            snInfo.setStrErrorBuf(e.getMessage());
            return snInfo;
        }

        return null;
    }

    public ShipmentPalletSnInfoHelper doPrintPalletLabel(ShipmentPalletSnInfoHelper snInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(snInfo), AppController.getProperties("PrintTemboPalletLabel"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ShipmentPalletSnInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            snInfo = new ShipmentPalletSnInfoHelper();
            snInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            snInfo.setStrErrorBuf(e.getMessage());
            return snInfo;
        }

        return null;
    }

    public ExtremePalletInfoHelper getExtremePalletInfo(ExtremePalletInfoHelper info) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(info), AppController.getProperties("ExtremePalletInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ExtremePalletInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            info = new ExtremePalletInfoHelper();
            info.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            info.setStrErrorBuf(e.getMessage());
            return info;
        }

        return null;
    }

    public MaterialLabelHelper getReelIdList(MaterialLabelHelper mlInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(mlInfo), AppController.getProperties("GetReelIdList"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), MaterialLabelHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            mlInfo = new MaterialLabelHelper();
            mlInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            mlInfo.setStrErrorBuf(e.getMessage());
            return mlInfo;
        }

        return null;
    }

    public MaterialLabelHelper getReelIdSubList(MaterialLabelHelper mlInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(mlInfo), AppController.getProperties("GetReelIdSubList"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), MaterialLabelHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            mlInfo = new MaterialLabelHelper();
            mlInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            mlInfo.setStrErrorBuf(e.getMessage());
            return mlInfo;
        }

        return null;
    }

    public InvoiceHelper getInvoiceSeq(InvoiceHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetInvoiceSeq"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), InvoiceHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            helper = new InvoiceHelper();
            helper.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            helper.setStrErrorBuf(e.getMessage());
            return helper;
        }

        return null;
    }

    public InvoiceHelper qryInvoiceSeq(InvoiceHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("QryInvoiceSeq"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), InvoiceHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            helper = new InvoiceHelper();
            helper.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            helper.setStrErrorBuf(e.getMessage());
            return helper;
        }

        return null;
    }
}
