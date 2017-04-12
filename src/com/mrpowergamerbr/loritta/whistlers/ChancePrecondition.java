package com.mrpowergamerbr.loritta.whistlers;

import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.utils.MathUtils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.core.entities.Message;

@AllArgsConstructor
@NoArgsConstructor
public class ChancePrecondition implements IPrecondition {
	double chance;
	
	@Override
	public boolean isValid(ServerConfig conf, Message message) {
		return MathUtils.chance(chance);
	}
}
