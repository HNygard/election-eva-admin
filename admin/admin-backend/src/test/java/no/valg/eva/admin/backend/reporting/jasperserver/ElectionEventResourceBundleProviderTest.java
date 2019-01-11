package no.valg.eva.admin.backend.reporting.jasperserver;

import com.google.common.collect.ImmutableMap;
import no.evote.service.TranslationServiceBean;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.LocaleText;
import no.valg.eva.admin.configuration.domain.model.TextId;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.test.TestGroups;
import org.apache.commons.lang3.tuple.Pair;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@Test(groups = TestGroups.RESOURCES)
public class ElectionEventResourceBundleProviderTest extends MockUtilsTestCase {
	public static final long LOCALE_NB_PK = 1L;
	public static final long LOCALE_NN_PK = 2L;
	public static final String ELECTION_EVENT_ID = "950001";
	public static final String EVA_RESOURCES = "EvaResources";
	public static final String TEXT1_ID = "@text1";
	public static final String TEXT_NB1 = "text_nb";
	public static final ImmutableMap<String, String> TEXT_NB = of(TEXT1_ID, TEXT_NB1);
	public static final String TEXT_NN1 = "text_nn";
	public static final ImmutableMap<String, String> TEXT_NN = of(TEXT1_ID, TEXT_NN1);
	public static final String TEXT1_KEY = "@text1=";
	@Mock
	private ElectionEvent mockElectionEvent;

	private Locale localeNb;

	private ElectionEventResourceBundleProvider electionEventResourceBundleProvider;

	@BeforeMethod
	public void init() throws Exception {
		initMocks(this);
		localeNb = new Locale();
		localeNb.setId("nb-NO");
		localeNb.setPk(LOCALE_NB_PK);
		Locale localeNn = new Locale();
		localeNn.setId("nn-NO");
		localeNn.setPk(LOCALE_NN_PK);
		electionEventResourceBundleProvider = initializeMocks(ElectionEventResourceBundleProvider.class);
		when(mockElectionEvent.getId()).thenReturn(ELECTION_EVENT_ID);
		when(getInjectMock(ElectionEventRepository.class).findAll()).thenReturn(newArrayList(mockElectionEvent));
		when(getInjectMock(ElectionEventRepository.class).getLocalesForEvent(mockElectionEvent)).thenReturn(newArrayList(localeNb, localeNn));
		when(getInjectMock(TranslationServiceBean.class).getLocaleTexts(mockElectionEvent, LOCALE_NB_PK)).thenReturn(TEXT_NB);
		when(getInjectMock(TranslationServiceBean.class).getLocaleTexts(mockElectionEvent, LOCALE_NN_PK)).thenReturn(TEXT_NN);
		when(getInjectMock(LocaleRepository.class).findAllLocales()).thenReturn(newArrayList(localeNb, localeNn));

		LocaleText globalTextNb = new LocaleText();
		TextId textId1 = new TextId();
		textId1.setTextId(TEXT1_ID);
		globalTextNb.setTextId(textId1);
		globalTextNb.setLocaleText("text_nb");
		globalTextNb.setLocale(localeNb);

		LocaleText globalTextNn = new LocaleText();
		textId1 = new TextId();
		textId1.setTextId(TEXT1_ID);
		globalTextNn.setTextId(textId1);
		globalTextNn.setLocaleText("text_nn");
		globalTextNn.setLocale(localeNn);
	}

	@Test
	public void testCreateResourceBundlesAllElectionEvents() throws Exception {
		List<Pair<String, byte[]>> resourceBundles = electionEventResourceBundleProvider.resourceBundles(EVA_RESOURCES, localeNb.getId());
		assertThat(resourceBundles).hasSize(6);

		assertThat(resourceBundles.get(0).getKey()).isEqualTo(EVA_RESOURCES + "_nb_NO.properties");
		assertThat(resourceBundles.get(0).getValue()).isEqualTo((TEXT1_KEY + TEXT_NB1 + "\n").getBytes(ISO_8859_1));

		assertThat(resourceBundles.get(1).getKey()).isEqualTo(EVA_RESOURCES + ".properties");
		assertThat(resourceBundles.get(1).getValue()).isEqualTo((TEXT1_KEY + TEXT_NB1 + "\n").getBytes(ISO_8859_1));

		assertThat(resourceBundles.get(2).getKey()).isEqualTo(EVA_RESOURCES + "_nn_NO.properties");
		assertThat(resourceBundles.get(2).getValue()).isEqualTo((TEXT1_KEY + TEXT_NN1 + "\n").getBytes(ISO_8859_1));

		assertThat(resourceBundles.get(3).getKey()).isEqualTo(EVA_RESOURCES + "_nb_NO_" + ELECTION_EVENT_ID + ".properties");
		assertThat(resourceBundles.get(3).getValue()).isEqualTo((TEXT1_KEY + TEXT_NB1 + "\n").getBytes(ISO_8859_1));

		assertThat(resourceBundles.get(4).getKey()).isEqualTo(EVA_RESOURCES + "_nn_NO_" + ELECTION_EVENT_ID + ".properties");
		assertThat(resourceBundles.get(4).getValue()).isEqualTo((TEXT1_KEY + TEXT_NN1 + "\n").getBytes(ISO_8859_1));

		assertThat(resourceBundles.get(5).getKey()).isEqualTo("valglogo.jpg");
		assertThat(resourceBundles.get(5).getValue().length > 0).isTrue();
	}
}
