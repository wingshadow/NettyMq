package com.myland.intfc;

import com.myland.common.ReturnResult;

public interface MsgConsumerService<T> {
	ReturnResult consume();
}
