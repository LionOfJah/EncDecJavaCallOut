package com.icicibank.apimngmnt.javaCallOut;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.apigee.flow.execution.Action;
import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;
import com.google.gson.Gson;

public class EncJavaCallOut implements Execution {

	private Map<String, String> properties; // read-only

	public EncJavaCallOut(Map<String, String> properties) {
		this.properties = properties;
	}

	private static String initVector = "encryptionIntVec";
	/*
	 * public static void main(String[] args) { EncJavaCallOut obj = new
	 * EncJavaCallOut(); String result = obj.maskedAndEncryptedData(
	 * "[\"000405010255\",\"000405010310\",\"000405009777\",\"000401109537\",\"000401113460\",\"000401116861\",\"001805015288\",\"628801537715\",\"000319000184\",\"000401193586\",\"000405111139\",\"000405501186\",\"000760004994\",\"000805015689\",\"000813001320\",\"001105024982\",\"001801080623\",\"001810042371\",\"001825075268\",\"004214109637\",\"004214109638\",\"010310000278\",\"032501004151\",\"044205008102\",\"105701000275\",\"630010075140\"]",
	 * "thisismysupersceretkeyey"); System.out.println(result); }
	 */

	@Override
	public ExecutionResult execute(MessageContext messageContext, ExecutionContext executionContext) {

		try {
			String strOne = resolveVariable(this.properties.get("accountNumber"), messageContext);
			String secretKey = resolveVariable(this.properties.get("key"), messageContext);
			String mode = resolveVariable(this.properties.get("mode"), messageContext);

			messageContext.setVariable("accountNumber", strOne);
			messageContext.setVariable("key", secretKey);
			messageContext.setVariable("mode", mode);
			String result = maskedAndEncryptedData(strOne, secretKey);

			messageContext.setVariable("encryptedResult", result);
			return ExecutionResult.SUCCESS;
		} catch (Exception ex) {
			ExecutionResult executionResult = new ExecutionResult(false, Action.ABORT);
			executionResult.setErrorResponse(ex.getMessage());
			executionResult.addErrorResponseHeader("ExceptionClass", ex.getClass().getName());
			// messageContext.setVariable("stage", stage);
			messageContext.setVariable("JAVA_ERROR", ex.getMessage());
			messageContext.setVariable("JAVA_STACKTRACE", ex.getClass().getName());
			return ExecutionResult.ABORT;
		}
	}

	private String resolveVariable(String variable, MessageContext msgContext) {
		if (variable.isEmpty())
			return "";
		if (!variable.startsWith("{") || !variable.endsWith("}"))
			return variable;
		String value = msgContext.getVariable(variable.substring(1, variable.length() - 1)).toString();
		if (value.isEmpty())
			return variable;
		return value;
	}

	public String maskedAndEncryptedData(String accountNumber, String secretKey) {

		String accountNoString = accountNumber.substring(accountNumber.indexOf("[") + 1,
				accountNumber.lastIndexOf("]"));
		String[] accountNumbers = accountNoString.split("\\,");
		// StringBuilder arrayOfAccounts = new StringBuilder("[");

		ArrayList<ResponseModel> responseModelList = new ArrayList<ResponseModel>();
		for (String acNo : accountNumbers) {
			// System.out.println("acNo "+acNo);
			ResponseModel responsemodel = new ResponseModel();

			StringBuilder maskedAccountNumber = new StringBuilder();
			acNo = acNo.replaceAll("\"", "");
			int maskLength = acNo.length() - 4;

			for (int i = 0; i < maskLength; i++) {
				maskedAccountNumber.append("X");

			}
			maskedAccountNumber.append(acNo.substring(maskLength, acNo.length()));

			SecretKeySpec secretKey1 = new SecretKeySpec(secretKey.getBytes(), "AES");
			Cipher cipher;
			String encryptedAcNo = "";
			try {

				IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
				cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

				cipher.init(Cipher.ENCRYPT_MODE, secretKey1, iv);

				encryptedAcNo = Base64.getEncoder().encodeToString(cipher.doFinal(acNo.getBytes()));
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
					| BadPaddingException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {

				e.printStackTrace();
			}
			responsemodel.setOriginalAccountNo(acNo);
			responsemodel.setMaskedAccountNo(maskedAccountNumber.toString());
			responsemodel.setEncryptedAccountno(encryptedAcNo);
			responseModelList.add(responsemodel);

		}

		return new Gson().toJson(responseModelList);
	}

	public EncJavaCallOut() {

	}
}
