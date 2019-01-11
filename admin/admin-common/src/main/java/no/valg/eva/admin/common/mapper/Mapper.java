package no.valg.eva.admin.common.mapper;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Mapper {
	public <T, R> List<R> map(List<T> list, Function<T, R> mapper) {
		return list.stream().map(mapper).collect(Collectors.toList());
	}
}
