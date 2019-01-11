package no.valg.eva.admin.common.configuration.service;

import java.io.Serializable;

import no.evote.constants.GenererValgkortgrunnlagStatus;
import no.evote.security.UserData;

public interface ValgkortgrunnlagService extends Serializable {
	
	void genererValgkortgrunnlag(UserData userData, boolean tillatVelgereIkkeTilknyttetValgdistrikt);

	GenererValgkortgrunnlagStatus sjekkForutsetningerForGenerering(UserData userData);
}
