package no.valg.eva.admin.counting.port.adapter.service.valgnatt.ssb2013;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Tuple element has one or more Data child elements.
 */
@XmlType
public class Tuple {

	private String tupleId;
	private List<Data> dataList = new ArrayList<>();

	public Tuple() {
	}

	public Tuple(final String tupleId) {
		this.tupleId = tupleId;
	}

	public void setTupleId(final String tupleId) {
		this.tupleId = tupleId;
	}

	public void setDataList(final List<Data> dataList) {
		this.dataList = dataList;
	}

	public void addData(final Data data) {
		dataList.add(data);
	}

	@XmlAttribute
	public String getTupleId() {
		return tupleId;
	}

	@XmlElement(name = "Data")
	public List<Data> getDataList() {
		return dataList;
	}
}
