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

import java.util.function.Predicate;

public class ElementFilters {

	public static Predicate<Element> xmlns(String xmlns) {
		return el -> xmlns.equals(el.getXMLNS());
	}

	public static Predicate<Element> name(String name) {
		return el -> name.equals(el.getName());
	}

	public static Predicate<Element> attribute(String attrName, String attrValue) {
		return el -> attrValue.equals(el.getAttribute(attrName));
	}

	public static Predicate<Element> nameAndAttribute(String name, String attrName, String attrValue) {
		return name(name).and(attribute(attrName, attrValue));
	}

	public static Predicate<Element> nameAndXMLNS(String name, String xmlns) {
		return name(name).and(xmlns(xmlns));
	}

}
