package no.valg.eva.admin.common.rbac.service;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.Person;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.common.rbac.BuypassOperator;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.RoleAssociation;
import no.valg.eva.admin.common.rbac.RoleItem;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AdminOperatorService extends Serializable {

	/**
	 * Finds operator or voter with the given person id. If operator is found, the operator is returned, else a search for voter is performed.
	 *
	 * @param userData
	 *            context object
	 * @param operatorId
	 *            personId for operator
	 * @return operator instance
	 */
	Operator operatorOrVoterById(UserData userData, PersonId operatorId);

	/**
	 * Finds users in operator table or in voter table. Only adds instances from voter table if not an operator with same person id already exists.
	 *
	 * @param userData
	 *            context object
	 * @param name
	 *            name to search for
	 * @return list of person instances
	 */
	Collection<Person> operatorsByName(UserData userData, String name);

	/**
	 * Finds all operators in a selected area.
	 * <p/>
	 * If the user invoking this operation has a role with access, then all operators at and below
	 * the area is returned. If the user does not have this access, only the operators at the specified area is returned. This may be used to restrict view for
	 * county users, while allowing municipality users to see users at area levels below the municipality.
	 * <p/>
	 * The election event is derived from {@code AreaPath} parameter.
	 *
	 * @param userData
	 *            context object
	 * @param area
	 *            the selected area to find operators within
	 * @return all operators in, and below, the selected area. Returns empty list if nothing is found.
	 * @throws java.lang.NullPointerException
	 *             if any arguments are null
	 * @throws no.evote.exception.EvoteException
	 *             if the operator or area is unknown
	 */
	List<Operator> operatorsInArea(UserData userData, AreaPath area);

	/**
	 * Finner alle brukere i et område med navn som matcher søkestrengen
	 */
	List<no.valg.eva.admin.rbac.domain.model.Operator> operatorsInAreaByName(UserData userData, AreaPath area, String nameSearchString);
	
	/**
	 * Updates operator, and role associations within an area. Creates operator if operator doesn't exist.
	 * <p/>
	 * The election event is found in UserData.
	 *
	 * @param userData
	 *            context object
	 * @param operator
	 *            the operator to update. Allows updating e-mail and phone number.
	 * @param addedRoleAssociations
	 * @param deletedRoleAssociations
	 *            role associations to delete. If the role association does not exist in the backend, it is ignored. If the association exists in both
	 *            newRoleAssociations and deletedRoleAssociations parameters, it is ignored. @throws java.lang.NullPointerException if any arguments are null
	 * @throws no.evote.exception.EvoteException
	 *             if the operator or area is unknown
	 */
	Operator updateOperator(
			UserData userData, Operator operator, AreaPath area, Collection<RoleAssociation> addedRoleAssociations,
			Collection<RoleAssociation> deletedRoleAssociations);

	/**
	 * Deletes operator. Only information within the given area is affected.
	 * <p/>
	 * The election event is found in UserData.
	 *
	 * @param userData
	 *            context object
	 * @param operator
	 *            the operator to delete
	 * @throws java.lang.NullPointerException
	 *             if any arguments are null
	 * @throws no.evote.exception.EvoteException
	 *             if the operator is unknown
	 */
	void deleteOperator(UserData userData, Operator operator);

	/**
	 * Finds all roles, as RoleItem instances, for a given election event and area. If area is county, only roles that can be assigned on county level are
	 * returned. If area is municipality, roles that can be assigned on municipality, polling district or polling place level are returned.
	 *
	 * @param userData
	 *            context object
	 * @param areaPath
	 *            path to area, used for finding area level
	 * @return collection of role items
	 */
	Collection<RoleItem> assignableRolesForArea(UserData userData, AreaPath areaPath);

	/**
	 * Finds roles that can be assigned to a given area or subareas defined by areaPath. In addition, subareas each role can be valid for is returned.
	 *
	 * @param userData
	 *            context object
	 * @param areaPath
	 *            path defining area
	 * @return map of role to area lists
	 */
	Map<RoleItem, List<PollingPlaceArea>> areasForRole(UserData userData, AreaPath areaPath);

	ContactInfo contactInfoForOperator(UserData userData);

	void updateContactInfoForOperator(UserData userData, ContactInfo contactInfo);

	/**
	 * 
	 * @param userData
	 *            context object
	 * @param contents
	 *            content of imported file
	 * @return list of updated operators
	 */

	List<BuypassOperator> updateBuypassKeySerialNumbers(UserData userData, byte[] contents);
}
