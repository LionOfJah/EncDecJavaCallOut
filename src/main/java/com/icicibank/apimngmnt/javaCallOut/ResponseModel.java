package com.icicibank.apimngmnt.javaCallOut;

public class ResponseModel {

	private String originalAccountNo;
	
	private String maskedAccountNo;
	
	private String encryptedAccountno;

	public ResponseModel() {
		
	}

	public String getOriginalAccountNo() {
		return originalAccountNo;
	}

	public void setOriginalAccountNo(String originalAccountNo) {
		this.originalAccountNo = originalAccountNo;
	}

	public String getMaskedAccountNo() {
		return maskedAccountNo;
	}

	public void setMaskedAccountNo(String maskedAccountNo) {
		this.maskedAccountNo = maskedAccountNo;
	}

	public String getEncryptedAccountno() {
		return encryptedAccountno;
	}

	public void setEncryptedAccountno(String encryptedAccountno) {
		this.encryptedAccountno = encryptedAccountno;
	}

	@Override
	public String toString() {
		return "ResponseModel [originalAccountNo=" + originalAccountNo + ", maskedAccountNo=" + maskedAccountNo
				+ ", encryptedAccountno=" + encryptedAccountno + "]";
	}
	
	
}
