package no.valg.eva.admin.frontend.kontekstvelger.oppsett;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Parameter.FILTER;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Parameter.NIVAER;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Parameter.TJENESTE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Parameter.VARIANT;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.HIERARKI;

import java.util.List;
import java.util.function.Function;

import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Parameter;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiFilter;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiTjeneste;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiVariant;
import no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValghierarkiTjeneste;

public final class KontekstvelgerElementHjelp {
	private KontekstvelgerElementHjelp() {
	}

	public static List<ValghierarkiNivaa> electionHierarchyLevels(KontekstvelgerElement element) {
		assertType(element, HIERARKI);
		return nivaer(element, ValghierarkiNivaa::fra);
	}

    public static List<ValggeografiNivaa> electionGeoLevels(KontekstvelgerElement element) {
		assertType(element, KontekstvelgerElement.Type.GEOGRAFI);
		return nivaer(element, ValggeografiNivaa::fra);
	}

	private static void assertType(KontekstvelgerElement element, KontekstvelgerElement.Type type) {
		if (element.getType() != type) {
			throw new IllegalArgumentException(type + " assert failed");
		}
	}

	public static List<ValggeografiNivaa> valgbareValggeografiNivaaerFra(KontekstvelgerOppsett setup) {
        KontekstvelgerElement element = pickerElementFromType(setup, KontekstvelgerElement.Type.GEOGRAFI);
		if (element == null) {
			return null;
		}
		return nivaer(element, ValggeografiNivaa::fra);
	}

	public static boolean inkluderOpptellingskategori(KontekstvelgerOppsett oppsett) {
        return pickerElementFromType(oppsett, KontekstvelgerElement.Type.OPPTELLINGSKATEGORI) != null;
	}

	public static ValggeografiFilter valggeografiFilter(KontekstvelgerOppsett setup) {
		return elementVerdiFraTypeOgParameter(setup, KontekstvelgerElement.Type.GEOGRAFI, FILTER, ValggeografiFilter::valueOf, ValggeografiFilter.DEFAULT);
	}

	public static ValggeografiVariant valggeografiVariant(KontekstvelgerOppsett setup) {
		return elementVerdiFraTypeOgParameter(setup, KontekstvelgerElement.Type.GEOGRAFI, VARIANT, ValggeografiVariant::valueOf, ValggeografiVariant.STANDARD);
	}

    public static ValggeografiTjeneste electionGeographyAction(KontekstvelgerOppsett setup) {
		return elementVerdiFraTypeOgParameter(setup, KontekstvelgerElement.Type.GEOGRAFI, TJENESTE, ValggeografiTjeneste::valueOf, ValggeografiTjeneste.DEFAULT);
	}

	public static List<ValghierarkiNivaa> valgbareValghierarkiNivaaerFra(KontekstvelgerOppsett setup) {
        KontekstvelgerElement element = pickerElementFromType(setup, HIERARKI);
		if (element == null) {
			return null;
		}
		return nivaer(element, ValghierarkiNivaa::fra);
	}

	public static ValghierarkiTjeneste valghierarkiTjeneste(KontekstvelgerOppsett setup) {
		return elementVerdiFraTypeOgParameter(setup, HIERARKI, TJENESTE, ValghierarkiTjeneste::valueOf, ValghierarkiTjeneste.DEFAULT);
	}

	private static <T> List<T> nivaer(KontekstvelgerElement element, Function<Integer, T> mapper) {
		return stream(element.get(NIVAER).split(","))
				.map(Integer::parseInt)
				.map(mapper)
				.sorted()
				.collect(toList());
	}

	private static <T> T elementVerdiFraTypeOgParameter(
			KontekstvelgerOppsett setup, KontekstvelgerElement.Type type, Parameter parameter, Function<String, T> mapper, T standardVerdi) {
        KontekstvelgerElement element = pickerElementFromType(setup, type);
		if (element == null || element.get(parameter) == null) {
			return standardVerdi;
		}
		return mapper.apply(element.get(parameter));
	}

    public static KontekstvelgerElement pickerElementFromType(KontekstvelgerOppsett setup, KontekstvelgerElement.Type type) {
		return setup.getElementer().stream()
				.filter(element -> element.getType() == type)
				.findFirst()
				.orElse(null);
	}
}
