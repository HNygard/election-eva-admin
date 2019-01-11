package no.valg.eva.admin.configuration.domain.model;

import static org.hibernate.annotations.CacheConcurrencyStrategy.NONSTRICT_READ_WRITE;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.constants.SQLConstants;
import no.evote.model.VersionedEntity;
import no.evote.persistence.AntiSamyEntityListener;
import no.evote.validation.AntiSamy;
import no.evote.validation.LettersOrDigits;
import no.evote.validation.StringNotNullEmptyOrBlanks;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Immutable;

/**
 * Text id for translation
 */
@Entity
@Immutable
@Cacheable
@Cache(usage = NONSTRICT_READ_WRITE, region = "no.valg.eva.admin.configuration.domain.model.TextId")
@Table(name = "text_id", uniqueConstraints = { @UniqueConstraint(columnNames = { SQLConstants.ELECTION_EVENT_PK, "text_id" }) })
@AttributeOverride(name = "pk", column = @Column(name = "text_id_pk"))
@EntityListeners({ AntiSamyEntityListener.class })
@NamedQueries({
		@NamedQuery(name = "TextId.findByElectionEvent", query = "SELECT  ti from  TextId ti where election_event_pk = :electionEventPk order by textId"),
		@NamedQuery(name = "TextId.findGlobal", query = "SELECT  ti from  TextId ti where election_event_pk is null order by textId"),
		@NamedQuery(name = "TextId.findByElectionEventAndId", query = "SELECT  ti from  TextId ti where election_event_pk = :electionEventPk"
				+ " and text_id = :textId"),
		@NamedQuery(name = "TextId.findGlobalById", query = "SELECT  ti from  TextId ti where election_event_pk is null and text_id = :textId")
})
public class TextId extends VersionedEntity implements java.io.Serializable {

	private ElectionEvent electionEvent;
	private String textId;
	@AntiSamy
	private String infoText;
	
	public TextId() {
	}

	public TextId(ElectionEvent electionEvent, String textId, String infoText) {
		this.electionEvent = electionEvent;
		this.textId = textId;
		this.infoText = infoText;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = SQLConstants.ELECTION_EVENT_PK)
	public ElectionEvent getElectionEvent() {
		return this.electionEvent;
	}

	public void setElectionEvent(final ElectionEvent electionEvent) {
		this.electionEvent = electionEvent;
	}

	@Column(name = "text_id", nullable = false, length = 100)
	@StringNotNullEmptyOrBlanks
	@LettersOrDigits(extraChars = "@[]._-")
	@Size(max = 100)
	public String getTextId() {
		return this.textId;
	}

	public void setTextId(final String textId) {
		this.textId = textId;
	}

	@Column(name = "info_text", length = 150)
	@Size(max = 150)
	public String getInfoText() {
		return this.infoText;
	}

	public void setInfoText(final String infoText) {
		this.infoText = infoText;
	}

}
