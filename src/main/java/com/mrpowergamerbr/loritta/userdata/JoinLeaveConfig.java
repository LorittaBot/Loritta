package com.mrpowergamerbr.loritta.userdata;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinLeaveConfig {
	public boolean isEnabled = false;
	public boolean tellOnJoin = true;
	public boolean tellOnLeave = true;
	public String joinMessage = "👉 {@user} entrou no servidor!";
	public String leaveMessage = "👈 {nickname} saiu do servidor!";
	public String canalJoinId = null;
	public String canalLeaveId = null;
}
