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

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Class contains helper functions to easily create <code>Predicate&lt;Element&gt;</code>
 */
public class ElementFilters {

	/**
	 * Method returns predicate testing <code>Element</code> XMLNS
	 */
	public static @NotNull Predicate<Element> xmlns(@NotNull String xmlns) {
		return el -> xmlns.equals(el.getXMLNS());
	}

	/**
	 * Method returns predicate testing <code>Element</code> name
	 */
	public static @NotNull Predicate<Element> name(@NotNull String name) {
		return el -> name.equals(el.getName());
	}

	/**
	 * Method returns predicate testing <code>Element</code> attribute name and value
	 */
	public static @NotNull Predicate<Element> attribute(@NotNull String name, @NotNull String value) {
		return el -> name.equals(el.getAttribute(value));
	}

	/**
	 * Method returns predicate testing <code>Element</code> name, attribute name and value
	 */
	public static @NotNull Predicate<Element> nameAndAttribute(@NotNull String name, @NotNull String attributeName,
															   @NotNull String attributeValue) {
		return name(name).and(attribute(attributeName, attributeValue));
	}

	/**
	 * Method returns predicate testing <code>Element</code> name and xmlns
	 */
	public static @NotNull Predicate<Element> nameAndXMLNS(@NotNull String name, @NotNull String xmlns) {
		return name(name).and(xmlns(xmlns));
	}

}
