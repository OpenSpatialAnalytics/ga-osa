package org.knime.geoutils;

public interface Transform<TSrc, TDst> {
	public TDst transform(TSrc src);
}