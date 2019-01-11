package no.valg.eva.admin.configuration.domain.model.mapper;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Mapper {
	private Mapper() {
	}

	@SafeVarargs
	public static <T, R> List<R> map(List<T> list, Function<T, R> mapper, Predicate<T>... filtre) {
		return list.stream()
				.filter(stream(filtre).reduce(o -> true, Predicate::and))
				.map(mapper)
				.collect(toList());
	}
}
