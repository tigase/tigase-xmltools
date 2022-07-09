/*
 * Tigase XML Tools - Tigase XML Tools
 * Copyright (C) 2004 Tigase, Inc. (office@tigase.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.xml;

/**
 * Created: Feb 9, 2009 12:21:43 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 */
public class CData
		implements XMLNodeIfc<CData> {

	private String cdata = null;

	public CData(String cdata) {
		this.cdata = cdata;
	}

	@Override
	public CData clone() {
		CData result = null;

		try {
			result = (CData) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}    // end of try-catch

		return result;
	}

	public String getCData() {
		return cdata;
	}

	public void setCdata(String cdata) {
		this.cdata = cdata;
	}

	@Override
	public String toString() {
		return cdata;
	}

	@Override
	public String toStringPretty() {
		return cdata;
	}

	@Override
	public String toStringSecure() {
		return (((cdata != null) && (cdata.length() > 2)) ? "CData size: " + cdata.length() : cdata);
	}
	
}
