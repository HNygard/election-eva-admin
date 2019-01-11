package no.valg.eva.admin.backend.reporting.jasperserver.api;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class DataType {

	public enum Type {
		@XmlEnumValue("text")
		TEXT("text"),
		@XmlEnumValue("number")
		NUMBER("number"),
		@XmlEnumValue("date")
		DATE("date"),
		@XmlEnumValue("dateTime")
		DATE_TIME("dateTime"),
		@XmlEnumValue("time")
		TIME("time");
		private final String name;

		Type(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	private String pattern;
	private String maxValue;
	private boolean strictMax;
	private String minValue;
	private boolean strictMin;
	private Integer maxLength;
	private Type baseType = Type.TEXT;

	public DataType() {
	}

	public DataType(final Type type) {
		this.baseType = type;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(final String pattern) {
		this.pattern = pattern;
	}

	public String getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(final String maxValue) {
		this.maxValue = maxValue;
	}

	public boolean isStrictMax() {
		return strictMax;
	}

	public void setStrictMax(final boolean strictMax) {
		this.strictMax = strictMax;
	}

	public String getMinValue() {
		return minValue;
	}

	public void setMinValue(final String minValue) {
		this.minValue = minValue;
	}

	public boolean isStrictMin() {
		return strictMin;
	}

	public void setStrictMin(final boolean strictMin) {
		this.strictMin = strictMin;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(final Integer maxLength) {
		this.maxLength = maxLength;
	}

	@XmlElement(name = "type")
	public Type getBaseType() {
		return baseType;
	}

	public void setBaseType(final Type baseType) {
		this.baseType = baseType;
	}
}
