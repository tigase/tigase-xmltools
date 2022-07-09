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

import java.util.*;
import java.util.function.Predicate;

public class ElementMatcher implements Predicate<Element> {

	protected static ElementMatcher parse(Path.State state) throws Path.PathFormatException {
		int slashIdx = state.text.indexOf('/');
		int squareIdx = state.text.indexOf('[');
		if (slashIdx == -1) {
			slashIdx = Integer.MAX_VALUE;
		}
		int nameEndIdx = Math.min(slashIdx, squareIdx);
		String name = state.text.substring(0, nameEndIdx);
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

	private final String name;
	private final String xmlns;

	private final Attribute[] attributes;

	public ElementMatcher(String name, String xmlns, List<Attribute> attributes) {
		this.name = name;
		this.xmlns = xmlns;
		this.attributes = attributes.toArray(Attribute[]::new);
	}

	@Override
	public boolean test(Element element) {
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

	public record Attribute(@NotNull String name, String value) {

		protected static Attribute parse(Path.State state) throws Path.PathFormatException {
			if (!state.text.startsWith("[@") || state.text.length() < 2) {
				throw new Path.PathFormatException();
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
						throw new Path.PathFormatException();
					}
					String value = state.text.substring(0, endIdx);
					state.text = state.text.substring(endIdx+2);
					return new ElementMatcher.Attribute(name, value);
				} else {
					throw new Path.PathFormatException();
				}
			} else {
				if (squareIdx <= 1) {
					throw new Path.PathFormatException();
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

