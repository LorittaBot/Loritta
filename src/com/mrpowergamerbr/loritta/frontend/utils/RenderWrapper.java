package com.mrpowergamerbr.loritta.frontend.utils;

import java.util.Map;

import com.mitchellbosecke.pebble.template.PebbleTemplate;

public class RenderWrapper {
   public Map<String, Object> context;
   public PebbleTemplate pebble;

   public RenderWrapper(PebbleTemplate pebble, Map<String, Object> context) {
      this.context = context;
      this.pebble = pebble;
   }
}
