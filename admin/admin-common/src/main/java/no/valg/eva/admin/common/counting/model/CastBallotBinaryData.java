package no.valg.eva.admin.common.counting.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public final class CastBallotBinaryData implements Serializable {
	private final String fileName;
	private final String mimeType;
	private final byte[] bytes;

	public CastBallotBinaryData(String fileName, String mimeType, byte[] bytes) {
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.bytes = new byte[bytes.length];
		System.arraycopy(bytes, 0, this.bytes, 0, bytes.length);
	}

	public String fileName() {
		return fileName;
	}

	public String mimeType() {
		return mimeType;
	}

	public byte[] bytes() {
		byte[] result = new byte[this.bytes.length];
		System.arraycopy(this.bytes, 0, result, 0, this.bytes.length);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CastBallotBinaryData)) {
			return false;
		}
		CastBallotBinaryData that = (CastBallotBinaryData) o;
		return new EqualsBuilder()
				.append(fileName, that.fileName)
				.append(mimeType, that.mimeType)
				.append(bytes, that.bytes)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(fileName)
				.append(mimeType)
				.append(bytes)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("fileName", fileName)
				.append("mimeType", mimeType)
				.append("bytes.length", bytes.length)
				.toString();
	}
}
