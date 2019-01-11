package no.valg.eva.admin.common.auditlog;

/**
 * All event types used by {@link AuditLog} annotation (and thus {@link AuditEventFactory}) must be listed in this enumeration, due to the JLS limitation that
 * an annotation value must be a compile-time constant.
 */
public enum AuditEventTypes implements AuditEventType {
	/**
	 * Common event type for save operations.
	 */
	Save,
	/**
	 * Common event type for create operations.
	 */
	Create,
	/**
	 * Common event type for creating a list of objects.
	 */
	CreateAll,
	/**
	 * Common event type for read operations.
	 */
	Read,
	/**
	 * Common event type for update operations.
	 */
	Update,
	UpdateAll,
	/**
	 * Common event type for delete operations.
	 */
	Delete,
	/**
	 * Common event type for deleting a list of objects.
	 */
	DeleteAll,
	StatusChanged,
	/**
	 * Display order has changed in a list of objects.
	 */
	DisplayOrderChanged,
	ContactInfoChanged,
	/**
	 * A parent-children relationship has been created
	 */
	CreateParent,
	/**
	 * Add children to parent-child relation
	 */
	AddChildren,
	/**
	 * Remove children from parent-child relation
	 */
	RemoveChildren,
	SearchElectoralRoll,
	/**
	 * Event type for partial update of an object (such as an election group, election event and so on)
	 */
	PartialUpdate,
	/**
	 * Event type for generating and downloading a report
	 */
	GenerateReport,
	/**
	 * Event type for electoral roll import  and changes
	 */
	FullElectoralImportStarted,
	IncrementalElectoralImportStarted,
	ElectoralRollImportCompleted,
	EntrySkipped,
	DeletedAllWithoutArea,
	/**
	 * Event type for electoral roll import and changes and delete vote counts
	 */
	DeletedAllInArea,
	/**
	 * Hendelsestype for eksport av manntall - valgkort
	 */
	GenererValgkortgrunnlagJobbStartet,
	GenererValgkortgrunnlagJobbFerdig,
	/**
	 * Event type for omraadehiearki-import
	 */
	ImportDistrictsChanges,
	ImportDistrictsChangesCreate,
	ImportDistrictsChangesUpdate,
	ImportDistrictsChangesDelete,
	/**
	 * 
	 * Event type for import av tellinger
	 */
	SaveUploadedCount,
	ImportUploadedCount,
	CandidateVoteSkipped,
	/**
	 * Event type for opptelling
	 */
	RevokeCount,
	SaveCount,
 	ApproveCount,
	ReadyForSettlement,
	ProcessRejectedBallots
}
