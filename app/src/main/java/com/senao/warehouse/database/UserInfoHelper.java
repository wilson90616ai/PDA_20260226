package com.senao.warehouse.database;

public class UserInfoHelper extends BasicHelper {
	private String userName;
	private String password;
	private String palletYN;

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPalletYN() {
		return palletYN;
	}

	public void setPalletYN(String palletYN) {
		this.palletYN = palletYN;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return "UserInfoHelper{" +
				"userName='" + userName + '\'' +
				", password='" + password + '\'' +
				", palletYN='" + palletYN + '\'' +
				'}';
	}
}
