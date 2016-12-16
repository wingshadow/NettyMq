package com.myland.common;

public class ReturnResult implements java.io.Serializable{
	public ReturnResult(boolean isSuc, String errCode, String errMsg) {
		super();
		this.isSuc = isSuc;
		this.errCode = errCode;
		this.errMsg = errMsg;
	}
	public boolean isSuc;
	public String errCode;
	public String errMsg;
	
	public boolean isSuc() {
		return isSuc;
	}
	public void setSuc(boolean isSuc) {
		this.isSuc = isSuc;
	}	
	public String getErrCode() {
		return errCode;
	}
	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}
	public String getErrMsg() {
		return errMsg;
	}
	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

}
