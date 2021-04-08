package com.icicibank.apimngmnt.javaCallOut;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

public class DecJavaCallOut implements Execution {

	private Map<String, String> properties; // read-only

	public DecJavaCallOut(Map<String, String> properties) {
		this.properties = properties;
	}

	
	/*
	 * public static void main(String[] args) {
	 * 
	 * DecJavaCallOut callOut = new DecJavaCallOut();
	 * System.out.println(callOut.decryptData("wITXXZxNDU2XWA6RTtJbsw\u003d\u003d",
	 * "thisismysupersceretkeyey")); }
	 */

	private static String initVector = "encryptionIntVec";

	@Override
	public ExecutionResult execute(MessageContext messageContext, ExecutionContext executionContext) {
		try {
			String strOne = resolveVariable(this.properties.get("encrptedData"), messageContext);
			String secretKey = resolveVariable(this.properties.get("key"), messageContext);
			String mode = resolveVariable(this.properties.get("mode"), messageContext);

			messageContext.setVariable("encrptedData", strOne);
			messageContext.setVariable("key", secretKey);
			messageContext.setVariable("mode", mode);
			String result = decryptData(strOne, secretKey);

			messageContext.setVariable("decryptedResult", result);
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

	public String decryptData(String encryptedData, String secretKey) {

		SecretKeySpec secretKey1 = new SecretKeySpec(secretKey.getBytes(), "AES");
		Cipher cipher;
		String dataBytes = "";
		try {
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

			cipher.init(Cipher.DECRYPT_MODE, secretKey1, iv);

			dataBytes = new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));

		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {

			e.printStackTrace();
		}

		return dataBytes;
	}


	public DecJavaCallOut() {
		super();
		// TODO Auto-generated constructor stub
	}

}
