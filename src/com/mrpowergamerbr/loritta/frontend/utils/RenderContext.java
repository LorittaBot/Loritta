package com.mrpowergamerbr.loritta.frontend.utils;

import java.util.Map;

import org.jooby.Request;
import org.jooby.Response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
/**
 * From PaniniCMS: https://github.com/PaniniCMS/Panini/blob/master/src/com/paninicms/utils/RenderContext.java
 */
public class RenderContext {
	private Request request;
	private Response response;
	private Map<String, Object> contextVars;
	private String[] arguments;
}
