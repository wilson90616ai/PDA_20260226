package com.senao.warehouse.handler;

import com.google.gson.Gson;
import com.senao.warehouse.database.BasicHelper;
import com.senao.warehouse.database.DeliveryInfoHelper;
import com.senao.warehouse.database.ItemInfoHelper;
import com.senao.warehouse.database.ItemPoInfoHelper;
import com.senao.warehouse.database.ItemQtyInfoHelper;
import com.senao.warehouse.database.MerakiPoInfoHelper;
import com.senao.warehouse.database.OobaListHelper;
import com.senao.warehouse.database.PackingQtyInfoHelper;
import com.senao.warehouse.database.PoInfoHelper;
import com.senao.warehouse.database.ReturnInfoHelper;
import com.senao.warehouse.database.SamsaraPoInfoHelper;
import com.senao.warehouse.database.ShipmentPalletInfoHelper;
import com.senao.warehouse.database.SnItemInfoHelper;
import com.senao.warehouse.http.HttpClient;
import com.senao.warehouse.http.ServerResponse;
import com.senao.warehouse.util.ReturnCode;
import com.senao.warehouse.AppController;

public class ShipmentPickingHandler {
    private HttpClient httpClient;
    private Gson gson = new Gson();

    public SnItemInfoHelper setSNItemInfo(SnItemInfoHelper snItem) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(snItem), AppController.getProperties("SetSNItem"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), SnItemInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            SnItemInfoHelper result = new SnItemInfoHelper();
            result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            result.setStrErrorBuf(e.getMessage());
            return result;
        }

        return null;
    }

    public SnItemInfoHelper chkPalletCarton(SnItemInfoHelper snItem) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(snItem), AppController.getProperties("ChkPalletCarton"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), SnItemInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            SnItemInfoHelper result = new SnItemInfoHelper();
            result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            result.setStrErrorBuf(e.getMessage());
            return result;
        }

        return null;
    }

    public BasicHelper returnPicking(ReturnInfoHelper snItem) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(snItem), AppController.getProperties("ReturnPicking"));
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

    public LotcodePermissionHelper getLotcodePermission(LotcodePermissionHelper snItem) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(snItem), AppController.getProperties("GetIsLotcode"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), LotcodePermissionHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            LotcodePermissionHelper result = new LotcodePermissionHelper();
            result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            result.setStrErrorBuf(e.getMessage());
            return result;
        }

        return null;
    }

    public ItemInfoHelper getOutSourcing(ItemInfoHelper itemQty) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(itemQty), AppController.getProperties("GetOutSourcing"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ItemInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            ItemInfoHelper item = new ItemInfoHelper();
            item.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            item.setStrErrorBuf(e.getMessage());
            return item;
        }

        return null;
    }

    public ItemQtyInfoHelper getItemQtyInfo(ItemQtyInfoHelper itemQty) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(itemQty), AppController.getProperties("GetItemQtyInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ItemQtyInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            ItemQtyInfoHelper item = new ItemQtyInfoHelper();
            item.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            item.setStrErrorBuf(e.getMessage());
            return item;
        }

        return null;
    }

    public PackingQtyInfoHelper getPackingQtyInfo(PackingQtyInfoHelper packingQty) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(packingQty), AppController.getProperties("GetPackingQtyInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), PackingQtyInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            packingQty.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            packingQty.setStrErrorBuf(e.getMessage());
            return packingQty;
        }

        return null;
    }

    public MerakiPoInfoHelper getMerakiPoInfo(MerakiPoInfoHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("MerakiPoInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), MerakiPoInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            helper.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            helper.setStrErrorBuf(e.getMessage());
            return helper;
        }

        return null;
    }

    public SamsaraPoInfoHelper getSamsaraPoInfo(SamsaraPoInfoHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("SamsaraPoInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), SamsaraPoInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            helper.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            helper.setStrErrorBuf(e.getMessage());
            return helper;
        }

        return null;
    }

    public PoInfoHelper getPoInfo(PoInfoHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("PoInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), PoInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            helper.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            helper.setStrErrorBuf(e.getMessage());
            return helper;
        }

        return null;
    }

    public ShipmentPalletInfoHelper getShipmentDeliveryInfo(ShipmentPalletInfoHelper printInfo) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(printInfo), AppController.getProperties("GetDNInfoPrint"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ShipmentPalletInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            printInfo = new ShipmentPalletInfoHelper();
            printInfo.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            printInfo.setStrErrorBuf(e.getMessage());
            return printInfo;
        }

        return null;
    }

    public BasicHelper checkOoba(OobaListHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("CheckOoba"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), OobaListHelper.class);
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

    public ReturnInfoHelper checkValue(ReturnInfoHelper snItem) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(snItem), AppController.getProperties("ReturnCheckValue"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ReturnInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            ReturnInfoHelper result = new ReturnInfoHelper();
            result.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            result.setStrErrorBuf(e.getMessage());
            return result;
        }

        return null;
    }

    public BasicHelper checkDn(ReturnInfoHelper info) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(info), AppController.getProperties("ReturnCheckDn"));
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

    public ItemPoInfoHelper getOePoInfo(ItemPoInfoHelper helper) {
        try {
            httpClient = new HttpClient();
            ServerResponse response = httpClient.doPost(gson.toJson(helper), AppController.getProperties("GetOePoInfo"));
            AppController.debug("Server response:" + response.getCode());

            if (response.getCode().equals(ServerResponse.SERVER_RESPONSE_OK)) {
                return gson.fromJson(response.getData(), ItemPoInfoHelper.class);
            }
        } catch (Exception e) {
            AppController.debug("Error to connect the server. " + e.getMessage());
            helper = new ItemPoInfoHelper();
            helper.setIntRetCode(ReturnCode.CONNECTION_ERROR);
            helper.setStrErrorBuf(e.getMessage());
            return helper;
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
}
