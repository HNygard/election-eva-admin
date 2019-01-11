package no.valg.eva.admin.counting.port.adapter.service.valgnatt.ssb2013;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Set for grouping elements.  Children are zero or more Set or zero or more Tuple elements.
 */
@XmlType
public class Set {

	private String setId;
	private List<Set> setList = new ArrayList<>();
	private List<Tuple> tupleList = new ArrayList<>();

	public Set() {
	}

	public Set(final String setId) {
		this.setId = setId;
	}

	public void setSetList(final List<Set> setList) {
		this.setList = setList;
	}

	public void setTupleList(final List<Tuple> tupleList) {
		this.tupleList = tupleList;
	}

	public void addTuple(final Tuple tuple) {
		tupleList.add(tuple);
	}

	public void addSet(final Set set) {
		setList.add(set);
	}

	@XmlAttribute
	public String getSetId() {
		return setId;
	}

	@XmlElement(name = "Set")
	public List<Set> getSetList() {
		return setList;
	}

	@XmlElement(name = "Tuple")
	public List<Tuple> getTupleList() {
		return tupleList;
	}
}
