package no.valg.eva.admin.common.rbac;

import java.io.Serializable;
import no.valg.eva.admin.common.PersonId;

public class BuypassOperator implements Serializable{
	private PersonId fnr;
	private String buypassKeySerialNumber;
	
	public void setFnr(PersonId fnr) {
		this.fnr = fnr;
	}

	public void setBuypassKeySerialNumber(String buypassKeySerialNumber) {
		this.buypassKeySerialNumber = buypassKeySerialNumber;
	}

	public String getFnr() {
		return fnr.getId();
	}

	public String getBuypassKeySerialNumber() {
		return buypassKeySerialNumber;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BuypassOperator)) {
			return false;
		}

		BuypassOperator operator = (BuypassOperator) o;

		if (!buypassKeySerialNumber.equals(operator.buypassKeySerialNumber)) {
			return false;
		}
		if (!fnr.equals(operator.fnr)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = fnr.hashCode();
		result = 31 * result + buypassKeySerialNumber.hashCode();
		return result;
	}
}
