package org.yann.eureka.client.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Provides a single place for converting uncaught exceptions to friendly responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(IllegalArgumentException.class)
	public BaseResponse handleIllegalArgument(IllegalArgumentException ex) {
		LOGGER.warn("Request validation failed: {}", ex.getMessage());
		return BaseResponse.ERROR(BaseResponse.CODE_BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public BaseResponse handleUnexpected(Exception ex) {
		LOGGER.error("Unhandled server error", ex);
		return BaseResponse.ERROR(BaseResponse.CODE_SYSTEM_ERROR, "系统繁忙，请稍后再试");
	}
}