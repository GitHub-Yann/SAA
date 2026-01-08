package org.yann.eureka.client.demo.util;

public class BaseResponse {

	public static final String CODE_BAD_REQUEST = "INVALID_EXPRESSION";
	public static final String CODE_SYSTEM_ERROR = "SYSTEM_ERROR";

	private boolean result = true;
	private String errorCode;
	private String errorMsg;
	private Object data;

	public BaseResponse(boolean result, String errorCode, String errorMsg, Object obj) {
		this.result = result;
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
		this.data = obj;
	}

	public BaseResponse() {
	}

	public static BaseResponse OK(Object obj) {
		return new BaseResponse(true, null, "Api access succeeded", obj);
	}

	public static BaseResponse OK(String msg, Object obj) {
		return new BaseResponse(true, null, msg, obj);
	}

	public static BaseResponse ERROR(String errorMsg) {
		return ERROR(CODE_SYSTEM_ERROR, errorMsg);
	}

	public static BaseResponse ERROR(String errorCode, String errorMsg) {
		return new BaseResponse(false, errorCode, errorMsg, null);
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

}
