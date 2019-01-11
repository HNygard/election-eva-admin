package no.valg.eva.admin.configuration.domain.model;

import static org.hibernate.annotations.CacheConcurrencyStrategy.NONSTRICT_READ_WRITE;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.model.VersionedEntity;
import no.evote.persistence.AntiSamyEntityListener;
import no.evote.validation.AntiSamy;
import no.evote.validation.StringNotNullOrEmpty;

import org.hibernate.annotations.Cache;

/**
 * Translated text
 */
@Entity
@Cache(usage = NONSTRICT_READ_WRITE, region = "no.valg.eva.admin.configuration.domain.model.LocaleText")
@EntityListeners({ AntiSamyEntityListener.class })
@Table(name = "locale_text", uniqueConstraints = @UniqueConstraint(columnNames = { "locale_pk", "text_id_pk" }))
@AttributeOverride(name = "pk", column = @Column(name = "locale_text_pk"))
@NamedQueries({
		@NamedQuery(name = "LocaleText.findByElectionEvent", query = "select ti.textId, lt.localeText from LocaleText lt join lt.textId ti "
				+ "where ti.electionEvent.pk = :electionEventPk and lt.locale.pk = :localePk"),
		@NamedQuery(name = "LocaleText.findGlobal", query = "select ti.textId, lt.localeText from LocaleText lt join lt.textId ti "
				+ "where ti.electionEvent is null and lt.locale.pk = :localePk"),
		@NamedQuery(name = "LocaleText.findByElectionEventAndTextId", query = "select lt from LocaleText lt join lt.textId ti "
				+ "where ti.electionEvent.pk = :electionEventPk and lt.locale.pk = :localePk"),
		@NamedQuery(
				name = "LocaleText.findByElectionEventLocaleAndTextId",
				query = "select lt from LocaleText lt inner join lt.textId ti where"
				+ " ti.electionEvent.pk = :electionEventPk and lt.locale.pk = :localePk and ti.textId = :textId",
				hints = { @QueryHint(name = "org.hibernate.cacheable", value = "true") }),
		@NamedQuery(name = "LocaleText.findGlobalByLocaleAndTextId", query = "select lt from LocaleText lt inner join lt.textId ti"
				+ " where ti.electionEvent is null and lt.locale.pk = :localePk and ti.textId = :textId"),
		@NamedQuery(name = "LocaleText.findByElectionEventLocale", query = "select lt from LocaleText lt inner join lt.textId ti"
				+ " where ti.electionEvent.pk = :electionEventPk and lt.locale.pk = :localePk"),
		@NamedQuery(name = "LocaleText.findGlobalByLocale", query = "select lt from LocaleText lt inner join lt.textId ti"
				+ " where ti.electionEvent is null and lt.locale.pk = :localePk"),

		@NamedQuery(name = "LocaleText.findByLocaleAndElectionEventNull", query = "select lt from LocaleText as lt inner join lt.textId as ti"
				+ " where ti.electionEvent.pk is null and lt.locale.pk = :locale"),
		@NamedQuery(name = "LocaleText.findByElectionEventAndLocale", query = "select lt from LocaleText as lt inner join lt.textId as ti"
				+ " where ti.electionEvent.pk = :electionEvent and lt.locale.pk = :locale"),
		@NamedQuery(name = "LocaleText.findByLocaleIds", query = "SELECT  ti.textId, ltFrom.localeText, ltTo.localeText, ltFrom.pk, ltTo.pk"
				+ " from LocaleText ltFrom, LocaleText ltTo, TextId ti, Locale lTo, Locale lFrom, ElectionEvent ee" + " where ltTo.textId = ti.pk"
				+ " and ltFrom.textId = ti.pk" + " and lFrom.pk = ltFrom.locale" + " and lTo.pk = ltTo.locale" + " and ti.electionEvent = ee.pk"
				+ " and lTo.id = :toLocaleId" + " and lFrom.id = :fromLocaleId" + " and ee.name = :electionEventName" + " order by ti.textId"),
		@NamedQuery(name = "LocaleText.findByLocalePk", query = "select l from LocaleText l WHERE l.locale.pk = :localePk"),
		@NamedQuery(name = "LocaleText.findByTextId", query = "select l from LocaleText l WHERE l.textId.pk = :textIdPk"),
		@NamedQuery(name = "LocaleText.lastUpdatedTimestamp", query = "select max(auditTimestamp) from LocaleText") })
public class LocaleText extends VersionedEntity implements java.io.Serializable {

	private TextId textId;
	private Locale locale;

	@AntiSamy
	private String localeText;
	
	public LocaleText() {
	}

	public LocaleText(Locale locale, TextId textId, String localeText) {
		this.locale = locale;
		this.textId = textId;
		this.localeText = localeText;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "text_id_pk", nullable = false, updatable = false)
	public TextId getTextId() {
		return this.textId;
	}

	public void setTextId(final TextId textId) {
		this.textId = textId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "locale_pk", nullable = false, updatable = false)
	public Locale getLocale() {
		return this.locale;
	}

	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

	@Column(name = "locale_text", nullable = false, length = 32768)
	@StringNotNullOrEmpty
	@Size(max = 32768)
	public String getLocaleText() {
		return this.localeText;
	}

	public void setLocaleText(final String localeText) {
		this.localeText = localeText;
	}

}
