package no.valg.eva.admin.util;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public final class StringUtil {

	private StringUtil() {
	}

	public static String capitalize(final String s) {
		if (StringUtils.isEmpty(s)) {
			return s;
		}
		return s.charAt(0) + s.substring(1, s.length()).toLowerCase();
	}

	public static String prefixString(final String formatted, final int length, final char prefixChar) {
		int len = formatted.length();
		StringBuilder sb = new StringBuilder("");
		while (len++ < length) {
			sb.append(prefixChar);
		}
		return sb.append(formatted).toString();
	}

	public static String join(final Object[] arr) {
		StringBuilder s = new StringBuilder();
		int len = arr.length;
		if (len > 0) {
			for (int j = 0; j < len - 1; j++) {
				s.append(arr[j]).append(",");
			}
			s.append(arr[len - 1]);
		}
		return s.toString();
	}

	public static String join(final String... strings) {
		return StringUtils.join(strings, " ");
	}

	public static boolean isInSplittedString(final String name, final String query) {
		if (name.startsWith(query)) {
			return true;
		}

		for (String subString : name.split(" ")) {
			if (subString.startsWith(query)) {
				return true;
			}
		}
		return false;
	}

	public static String[] convert(Object[] arr) {
		if (arr == null) {
			return null;
		} else if (arr.length == 0) {
			return new String[0];
		} else {
			return Lists.newArrayList(Iterables.transform(Arrays.asList(arr),
					Functions.toStringFunction())).toArray(new String[arr.length]);
		}
	}

	public static boolean isSet(String... values) {
		for (String value : values) {
			if (StringUtils.isEmpty(value)) {
				return false;
			}
		}
		return true;
	}

	public static String joinOnlyNonNullAndNonEmpty(String... args) {
		return joinOnlyNonNullAndNonEmpty(' ', args);
	}

	public static String joinOnlyNonNullAndNonEmpty(Character separator, String... args) {
		List<String> nonNullAndNonEmptyArgs = new ArrayList<>();
		for (String arg : args) {
			addIfNotEmpty(nonNullAndNonEmptyArgs, arg);
		}
		return StringUtils.join(nonNullAndNonEmptyArgs.toArray(), separator);
	}

	private static void addIfNotEmpty(List<String> list, String value) {
		if (!isEmpty(value)) {
			list.add(value);
		}
	}
	
	public static boolean isNotBlank(String... values) {
		return values != null && stream(values).allMatch(StringUtils::isNotBlank);
	}
}
