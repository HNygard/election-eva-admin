package no.evote.service;

import java.io.Serializable;

import no.evote.model.BaseEntity;
import no.evote.security.UserData;

public interface GenericService extends Serializable {
	BaseEntity findByPk(UserData userData, Class<?> clazz, Long pk);
}
