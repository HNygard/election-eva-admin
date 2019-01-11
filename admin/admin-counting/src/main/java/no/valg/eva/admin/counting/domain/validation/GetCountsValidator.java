package no.valg.eva.admin.counting.domain.validation;

import static java.lang.String.format;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountContext;

public class GetCountsValidator {

	/**
	 * 
	 * @param context
	 * @param countingAreaPath
	 * @param operatorAreaPath
	 */
	public void validate(CountContext context, AreaPath countingAreaPath, AreaPath operatorAreaPath) {

		context.validate();
		countingAreaPath.validateAreaPath(AreaLevelEnum.MUNICIPALITY, AreaLevelEnum.BOROUGH, AreaLevelEnum.POLLING_DISTRICT);
		String electionEventIdFromCountingAreaPath = countingAreaPath.getElectionEventId();
		ElectionPath contestPath = context.getContestPath();
		String electionEventIdFromContestPath = contestPath.getElectionEventId();
		if (!electionEventIdFromCountingAreaPath.equals(electionEventIdFromContestPath)) {
			throw new IllegalArgumentException(format(
					"expected area path to match contest path (area path=<%s>, contest path=<%s>)",
					countingAreaPath,
					contestPath));
		}
		if (!countingAreaPath.equals(operatorAreaPath) && !countingAreaPath.isSubpathOf(operatorAreaPath)) {
			throw new IllegalArgumentException(format(
					"exptected area path to match selected area path (area path=<%s>, selected area path=<%s>",
					countingAreaPath,
                    operatorAreaPath));
		}
		
	}
	
	
}
