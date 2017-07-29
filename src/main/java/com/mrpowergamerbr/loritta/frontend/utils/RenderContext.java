package com.mrpowergamerbr.loritta.frontend.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jooby.Request;
import org.jooby.Response;

import java.util.Map;

@Getter
@Setter
@Accessors(fluent = true)
/**
 * From PaniniCMS: https://github.com/PaniniCMS/Panini/blob/master/src/com/paninicms/utils/RenderContext.java
 */
public class RenderContext {
	public Request request;
	public Response response;
	public Map<String, Object> contextVars;
	public String[] arguments;
}
