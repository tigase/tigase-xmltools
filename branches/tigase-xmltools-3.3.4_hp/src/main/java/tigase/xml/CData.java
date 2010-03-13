/*
 * Tigase XMPP/Jabber XML Tools
 * Copyright (C) 2004-2010 "Artur Hefczyc" <artur.hefczyc@tigase.org>
 *
 * This program is licensed to HP, under Licensor's Intellectual Property Rights,
 * a paid-up, worldwide, non-exclusive, non-transferable, perpetual license in the
 * Source Code to use, make, reproduce (for archival and back-up purposes only),
 * and prepare derivative works.
 *
 * The right to prepare derivative works includes translations, localizations,
 * adaptations, and compilations. HP shall own all right, title, and interest in
 * any derivative works prepared by HP.
 *
 * HP shall use its standard forms in distributing Object Code.
 *
 * Licensor retains all right, title, and interest in the Licensed Software and
 * Documentation.
 *
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */

package tigase.xml;

/**
 * Created: Feb 9, 2009 12:21:43 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public class CData implements XMLNodeIfc<CData> {

	private String cdata = null;

	@Override
	public CData clone() {
		CData result = null;
		try {
			result = (CData)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		} // end of try-catch
		return result;
	}

	public CData(String cdata) {
		this.cdata = cdata;
	}

	public String getCData() {
		return cdata;
	}

	@Override
	public String toString() {
		return cdata;
	}

	@Override
	public String toStringSecure() {
		return (cdata != null && cdata.length() > 2 ? "CData size: " + cdata.length() : cdata);
	}

	public int compareTo(CData o) {
		return cdata.compareTo(o.cdata);
	}

}