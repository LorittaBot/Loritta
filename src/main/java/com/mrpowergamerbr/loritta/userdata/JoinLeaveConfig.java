package com.mrpowergamerbr.loritta.userdata;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinLeaveConfig {
	private boolean isEnabled = false;
	private boolean tellOnJoin = true;
	private boolean tellOnLeave = true;
	private String joinMessage = "👉 {@user} entrou no servidor!";
	private String leaveMessage = "👈 {nickname} saiu do servidor!";
	private String canalJoinId = null;
	private String canalLeaveId = null;
}
