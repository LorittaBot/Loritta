package com.mrpowergamerbr.loritta.whistlers;

import java.util.ArrayList;
import java.util.List;

import lombok.NoArgsConstructor;

// Code Block = Trecho de código
@NoArgsConstructor
public class CodeBlock implements ICode {
	public List<IPrecondition> preconditions = new ArrayList<IPrecondition>(); // Preconditions desse CodeBlock
	// Só será executado os códigos desse CodeBlock caso todos os preconditions retornarem OK
	
	public List<ICode> codes = new ArrayList<ICode>(); // Mais código...
}
