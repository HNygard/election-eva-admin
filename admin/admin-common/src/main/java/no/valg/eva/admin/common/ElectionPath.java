package no.valg.eva.admin.common;

import static java.lang.String.format;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.constants.ElectionLevelEnum.ELECTION;
import static no.evote.constants.ElectionLevelEnum.ELECTION_EVENT;
import static no.evote.constants.ElectionLevelEnum.ELECTION_GROUP;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.evote.constants.ElectionLevelEnum;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

/**
 * Value object for election path: EEVENT.EG.EL.CONTES where EEVENT is election event, EG country, EL election and CONTES contest.
 */
public class ElectionPath implements Serializable {

	public static final String ROOT_ELECTION_EVENT_ID = "000000";

	public static final String ELECTION_PATH_REGEX = "^(\\d{6})(\\.(\\d{2})(\\.(\\d{2})(\\.(\\d{6}))?)?)?$";
	// regex groups
	public static final int GROUP_NO_ELECTION_EVENT = 1;
	public static final int GROUP_NO_ELECTION_GROUP = 3;
	public static final int GROUP_NO_ELECTION = 5;
	public static final int GROUP_NO_CONTEST = 7;
	private static final Pattern ELECTION_PATH_PATTERN = Pattern.compile(ELECTION_PATH_REGEX);
	private final String path;
	private final ElectionLevelEnum level;
	private final String electionEventId;
	private final String electionGroupId;
	private final String electionId;
	private final String contestId;

	public ElectionPath(final String path) {
		this.path = path;

		Matcher pathMatcher = ELECTION_PATH_PATTERN.matcher(path);
		if (!pathMatcher.matches()) {
			throw new IllegalArgumentException(format("illegal path <%s>, must match <%s>", path, ELECTION_PATH_REGEX));
		}

		electionEventId = pathMatcher.group(GROUP_NO_ELECTION_EVENT);
		ElectionLevelEnum electionLevelEnum = ElectionLevelEnum.ELECTION_EVENT;
		electionGroupId = pathMatcher.group(GROUP_NO_ELECTION_GROUP);
		if (electionGroupId != null) {
			electionLevelEnum = ElectionLevelEnum.ELECTION_GROUP;
		}
		electionId = pathMatcher.group(GROUP_NO_ELECTION);
		if (electionId != null) {
			electionLevelEnum = ELECTION;
		}
		contestId = pathMatcher.group(GROUP_NO_CONTEST);
		if (contestId != null) {
			electionLevelEnum = CONTEST;
		}
		this.level = electionLevelEnum;
	}

	public static ElectionPath from(String path) {
		return new ElectionPath(path);
	}

	public static ElectionPath from(String electionEventId, String electionGroupId) {
		return from(electionEventId + "." + electionGroupId);
	}

	public static ElectionPath from(String electionEventId, String electionGroupId, String electionId) {
		return from(electionEventId + "." + electionGroupId + "." + electionId);
	}

	public static ElectionPath from(String electionEventId, String electionGroupId, String electionId, String contestId) {
		return from(electionEventId + "." + electionGroupId + "." + electionId + "." + contestId);
	}

	public String path() {
		return path;
	}

	public ElectionLevelEnum getLevel() {
		return level;
	}

	public void assertContestLevel() {
		if (getLevel() != CONTEST) {
			throw new IllegalArgumentException(
					format("expected <%s> election path, but got <%s>", CONTEST, getLevel()));
		}
	}

	public void assertElectionGroupLevel() {
		if (getLevel() != ELECTION_GROUP) {
			throw new IllegalArgumentException(
					format("expected <%s> election path, but got <%s>", ELECTION_GROUP, getLevel()));
		}
	}

	public void assertElectionLevel() {
		if (getLevel() != ELECTION) {
			throw new IllegalArgumentException(
					format("expected <%s> election path, but got <%s>", ELECTION, getLevel()));
		}
	}

	public void assertElectionEventLevel() {
		if (getLevel() != ELECTION_EVENT) {
			throw new IllegalArgumentException(
					format("expected <%s> election path, but got <%s>", ELECTION_EVENT, getLevel()));
		}
	}

	public void assertLevel(ElectionLevelEnum electionLevelEnum) {
		if (getLevel() != electionLevelEnum) {
			String levelDescription = electionLevelEnum.getLevelDescription();
			throw new IllegalArgumentException(format("illegal %s path: %s", levelDescription, this));
		}
	}

	public void assertContestOrElectionLevel() {
		if (getLevel() != CONTEST && getLevel() != ELECTION) {
			throw new IllegalArgumentException(
					format("expected <%s> or <%s> election path, but got <%s>", CONTEST, ELECTION, getLevel()));
		}
	}

	public void assertLevels(ElectionLevelEnum first, ElectionLevelEnum... rest) {
		EnumSet<ElectionLevelEnum> levelsExpected = EnumSet.of(first, rest);
		if (!levelsExpected.contains(getLevel())) {
			throw new IllegalArgumentException(format("expected level to be one of <%s>, but was <%s>", levelsExpected, getLevel()));
		}
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getElectionGroupId() {
		return electionGroupId;
	}

	public String getElectionId() {
		return electionId;
	}

	public String getContestId() {
		return contestId;
	}

	/**
	 * @return true if this contains subPath, that is this is a parent of subPath
	 */
	public boolean contains(final ElectionPath subPath) {
		return subPath.path.startsWith(path);
	}

	/**
	 * @return true if this is part of parentPath
	 */
	public boolean isSubpathOf(final ElectionPath parentPath) {
		return path.startsWith(parentPath.path);
	}

	public ElectionPath toElectionGroupPath() {
		if (electionGroupId == null) {
			throw new IllegalStateException(format("path to election group can not be found for <%s>", path));
		}
		return new ElectionPath(electionEventId + "." + electionGroupId);
	}

	public ElectionPath add(String id) {
		return ElectionPath.from(path() + "." + id);
	}

	public ElectionPath toElectionPath() {
		if (electionId == null) {
			throw new IllegalStateException(format("path to election can not be found for <%s>", path));
		}
		return new ElectionPath(electionEventId + '.' + electionGroupId + '.' + electionId);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ElectionPath)) {
			return false;
		}
		ElectionPath areaPath = (ElectionPath) o;
		return path.equals(areaPath.path);
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	public String toString() {
		return path;
	}

	public ElectionPath toContestSubPath(String contestId) {
		return from(electionEventId, electionGroupId, electionId, contestId);
	}

	public ElectionPath toElectionSubPath(String electionId) {
		return from(electionEventId, electionGroupId, electionId);
	}

	public ElectionPath toElectionGroupSubPath(String electionGroupId) {
		return from(electionEventId, electionGroupId);
	}

	public ElectionPath toElectionEventPath() {
		return from(electionEventId);
	}

	public String lastId() {
		if (contestId != null) {
			return contestId;
		}
		if (electionId != null) {
			return electionId;
		}
		if (electionGroupId != null) {
			return electionGroupId;
		}
		return electionEventId;
	}

	public ValghierarkiSti tilValghierarkiSti() {
		return ValghierarkiSti.fra(this);
	}
}
