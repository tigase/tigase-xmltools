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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * Class implements <code>Predicate</code> for filtering <code>Element</code>s.
 */
public class ElementMatcher implements Predicate<Element> {

	/**
	 * Method parses <code>String</code> into matcher
	 * @param state
	 * @return
	 * @throws Path.PathFormatException
	 */
	protected static @NotNull ElementMatcher parse(@NotNull Path.State state) throws Path.PathFormatException {
		int slashIdx = state.text.indexOf('/');
		int squareIdx = state.text.indexOf('[');
		if (slashIdx == -1) {
			slashIdx = Integer.MAX_VALUE;
		}
		int nameEndIdx = Math.min(slashIdx, squareIdx);
		String name = state.text.substring(0, nameEndIdx);
		if (name.isEmpty()) {
			throw new Path.PathFormatException("Element name cannot be empty!");
		}
		if ("*".equals(name)) {
			name = null;
		}
		state.text = state.text.substring(nameEndIdx);
		if (slashIdx < squareIdx) {
			state.text = state.text.substring(slashIdx);
			return new ElementMatcher(name, null, Collections.emptyList());
		} else {
			String xmlns = null;
			List<Attribute> attributes = new ArrayList<>();
			while (state.text.startsWith("[")) {
				ElementMatcher.Attribute attr = ElementMatcher.Attribute.parse(state);
				if ("xmlns".equals(attr.name)) {
					xmlns = attr.value;
				} else {
					attributes.add(attr);
				}
			}
			return new ElementMatcher(name, xmlns, attributes);
		}
	}

	/**
	 * Method parses <code>String</code> into matcher
	 * @param text
	 * @return
	 * @throws Path.PathFormatException
	 */
	public static @NotNull ElementMatcher parse(@NotNull String text) throws Path.PathFormatException {
		Path.State state = new Path.State(text);
		return parse(state);
	}

	private String name;
	private String xmlns;

	private Attribute[] attributes;

	public ElementMatcher() {
		name = null;
		xmlns = null;
		attributes = new Attribute[0];
	}

	/**
	 * Constructor to create instance
	 * @param name - to match or null
	 * @param xmlns - to match or null
	 * @param attributes - to match or empty list
	 */
	public ElementMatcher(@Nullable String name, @Nullable String xmlns, @NotNull List<Attribute> attributes) {
		this(name, xmlns, attributes.toArray(Attribute[]::new));
	}

	/**
	 * Method sets matcher name to compare (or sets it to null to match any name)
	 * @param name
	 * @return
	 */
	public @NotNull ElementMatcher setName(@Nullable String name) {
		this.name = name;
		return this;
	}

	/**
	 * Method sets matcher xmlns to compare (or sets it to null to match any xmlns)
	 * @param xmlns
	 * @return
	 */
	public @NotNull ElementMatcher setXMLNS(@Nullable String xmlns) {
		this.xmlns = xmlns;
		return this;
	}

	/**
	 * Method add attribute to the matcher
	 * @param name - attribute name
	 * @param value - attribute value or null to accept any value
	 * @return
	 */
	public @NotNull ElementMatcher addAttribute(@NotNull String name, @Nullable String value) {
		Objects.requireNonNull(name, "Attribute name cannot be null!");
		attributes = Arrays.copyOf(attributes, attributes.length + 1);
		attributes[attributes.length - 1] = new Attribute(name, value);
		return this;
	}

	private ElementMatcher(@Nullable String name, @Nullable String xmlns, @NotNull Attribute[] attributes) {
		this.name = name;
		this.xmlns = xmlns;
		this.attributes = attributes;
	}

	@Override
	public boolean test(@NotNull Element element) {
		if (name != null && !name.equals(element.getName())) {
			return false;
		}
		if (xmlns != null && !xmlns.equals(element.getXMLNS())) {
			return false;
		}
		for (Attribute attr : attributes) {
			String value = element.getAttribute(attr.name);
			if (attr.value != null) {
				if (!Objects.equals(attr.value, value)) {
					return false;
				}
			} else {
				if (value == null) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof ElementMatcher) {
			ElementMatcher o = (ElementMatcher) obj;
			if (Objects.equals(name, o.name) && Objects.equals(xmlns, o.xmlns)) {
				if (attributes.length == o.attributes.length) {
					for (Attribute attr : attributes) {
						boolean found = false;
						for (Attribute attr1 : attributes) {
							if (Objects.equals(attr, attr1)) {
								found = true;
								break;
							}
						}
						if (!found) {
							return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(name, xmlns);
		result = 31 * result + Arrays.hashCode(attributes);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toStringBuilder(sb);
		return sb.toString();
	}

	public void toStringBuilder(StringBuilder sb) {
		if (name == null) {
			sb.append("*");
		} else {
			sb.append(name);
		}
		if (xmlns != null) {
			sb.append("[@xmlns='").append(xmlns).append("']");
		}
		for (Attribute attr : attributes) {
			sb.append("[@" + attr.name);
			if (attr.value != null) {
				sb.append("='").append(attr.value).append("'");
			}
			sb.append("]");
		}
	}

	/**
	 * Class holds attribute name and value to match against <code>Element</code> using <code>ElementMatcher</code>
	 * @param name
	 * @param value
	 */
	public record Attribute(@NotNull String name, @Nullable String value) {

		protected static @NotNull Attribute parse(@NotNull Path.State state) throws Path.PathFormatException {
			if (!state.text.startsWith("[@") || state.text.length() < 2) {
				throw new Path.PathFormatException("Invalid attribute format");
			}

			state.text = state.text.substring(2);
			int quoteIdx = state.text.indexOf('\'');
			int squareIdx = state.text.indexOf(']');
			if (quoteIdx < squareIdx) {
				if (quoteIdx > 1 && state.text.charAt(quoteIdx - 1) == '=') {
					String name = state.text.substring(0, quoteIdx-1);
					state.text = state.text.substring(quoteIdx+1);
					int endIdx = state.text.indexOf('\'');
					if (endIdx < 0) {
						throw new Path.PathFormatException("Invalid attribute format - missing end of value");
					}
					String value = state.text.substring(0, endIdx);
					state.text = state.text.substring(endIdx+2);
					return new ElementMatcher.Attribute(name, value);
				} else {
					throw new Path.PathFormatException("Invalid attribute format - missing value");
				}
			} else {
				if (squareIdx <= 1) {
					throw new Path.PathFormatException("Invalid attribute format - empty attribute condition");
				}
				state.text = state.text.substring(squareIdx+1);
				return new ElementMatcher.Attribute(state.text.substring(0, squareIdx), null);
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Attribute)) {
				return false;
			}
			Attribute attribute = (Attribute) o;
			return name.equals(attribute.name) && Objects.equals(value, attribute.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, value);
		}
	}
}

