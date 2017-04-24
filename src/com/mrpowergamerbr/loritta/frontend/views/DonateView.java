package com.mrpowergamerbr.loritta.frontend.views;

import java.io.PrintWriter;
import java.io.StringWriter;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;
import com.mrpowergamerbr.temmiemercadopago.mp.Payment;
import com.mrpowergamerbr.temmiemercadopago.mp.TemmieItem;
import com.mrpowergamerbr.temmiemercadopago.mp.request.PaymentRequest;

public class DonateView {

	public static Object render(RenderContext context) {
		try {
			if (context.request().param("whodonated").isSet() && context.request().param("grana").isSet()) {
				try {
					String whoDonated = context.request().param("whodonated").value();
					double grana = Double.parseDouble(context.request().param("grana").value().replace(",", ".").replace("R$", "").replace("$", ""));

					grana = Math.max(0.01, grana);
					grana = Math.min(1000, grana);
					Payment payment = Loritta.getTemmieMercadoPago().generatePayment(PaymentRequest.builder()
							.addItem(TemmieItem.builder()
									.title("Doação para a Loritta - " + whoDonated)
									.quantity(1)
									.currencyId("BRL")
									.unitPrice(grana)
									.build()).build());
					
					context.response().redirect(payment.getInitPoint());
				} catch (Throwable e) {}
			}
			PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("donate.html");
			return template;
		} catch (PebbleException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return e.toString();
		}
	}
}