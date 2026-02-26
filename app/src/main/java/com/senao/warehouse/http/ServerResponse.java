package com.senao.warehouse.http;

public class ServerResponse {
	public final static String SERVER_RESPONSE_OK = "OK";
	private String Code;
	private String Message;
	private String Data;

	public String getCode() {
		return Code;
	}

	public void setCode(String code) {
		Code = code;
	}

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}

	public String getData() {
		return Data;
	}

	public void setData(String data) {
		Data = data;
	}

	@Override
	public String toString() {
		return "ServerResponse{" +
				"Code='" + Code + '\'' +
				", Message='" + Message + '\'' +
				", Data='" + Data + '\'' +
				'}';
	}
}
