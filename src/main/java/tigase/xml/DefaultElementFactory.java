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
 * <code>DefaultElementFactory</code> is an <code>ElementFactory</code> implementation creating instances of basic
 * <code>Element</code> class. This implementation exists to offer complementary implementation of <em>DOM</em>. It can
 * be used when basic <code>Element</code> class is sufficient for particular needs. <p> Created: Mon Oct 25 22:08:37
 * 2004 </p>
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public class DefaultElementFactory
		implements ElementFactory {

	/**
	 * Creates a new <code>DefaultElementFactory</code> instance.
	 */
	public DefaultElementFactory() {
	}

	// Implementation of tigase.xml.ElementFactory

	public final Element elementInstance(final String name, final String cdata, final StringBuilder[] attnames,
										 final StringBuilder[] attvals) {
		Element result = new Element(name);
		if (cdata != null) {
			result.addCData(cdata);
		}
		if (attnames != null && attvals != null) {
			for (int i=0; i<attnames.length; i++) {
				StringBuilder attnameBuilder = attnames[i];
				StringBuilder attvalueBuilder = attvals[i];
				if (attnameBuilder == null || attvalueBuilder == null) {
					continue;
				}
				String attname = attnameBuilder.toString();
				if (attname.isEmpty()) {
					continue;
				}
				result.addAttribute(attname, attvalueBuilder.toString());
			}
		}
		return result;
	}

} // DefaultElementFactory