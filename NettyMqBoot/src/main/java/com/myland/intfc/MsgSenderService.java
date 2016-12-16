package com.myland.intfc;

import com.myland.common.ReturnResult;

public interface MsgSenderService<T> {
	ReturnResult send(T message);
}
