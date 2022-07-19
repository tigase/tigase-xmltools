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
import tigase.xml.annotations.TODO;

import java.io.FileReader;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static tigase.xml.ElementFilters.name;
import static tigase.xml.ElementFilters.xmlns;

/**
 * <code>Element</code> - basic document tree node implementation. Supports Java 5.0 generic feature to make it easier
 * to extend this class and still preserve some useful functionality. Sufficient for simple cases but probably in the
 * more advanced cases should be extended with additional features. Look in API documentation for more details and
 * information about existing extensions.
 * <p> Created: Mon Oct 4 17:55:16 2004 </p>
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 */
@TODO(note = "Make it a bit lighter.")
public class Element
		implements XMLNodeIfc<Element> {

	static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("You must give file name as parameter.");
			System.exit(1);
		}    // end of if (args.length < 1)

		FileReader file = new FileReader(args[0]);
		char[] buff = new char[1];
		SimpleParser parser = new SimpleParser();
		DomBuilderHandler dom = new DomBuilderHandler();
		int result = -1;

		while ((result = file.read(buff)) != -1) {
			parser.parse(dom, buff, 0, result);
		}
		file.close();

		Queue<Element> elems = dom.getParsedElements();

		for (Element elem : elems) {
			elem.clone();
			System.out.println(elem.toString());
		}
	}
	// static reference to make things faster
	private static final String ATTR_XMLNS_KEY = "xmlns";

	// Function used for deduplication of element names to reduce memory usage when a lot of elements may contain
	// the same name
	protected static BiFunction<String, String, String> elementNameDeduplicationFn = List.of("message", "iq",
																							 "presence", "query",
																							 "pubsub", "body",
																							 "stanza-id")
			.stream()
			.collect(Collectors.toMap(Function.identity(), Function.identity()))::getOrDefault;

	// Function used for deduplication of element attribute names to reduce memory usage when a lot of attribute names
	// may be the same
	protected static BiFunction<String, String, String> attributesDeduplicationFn = List.of("id", "name",
																							ATTR_XMLNS_KEY, "from",
																							"to")
			.stream()
			.collect(Collectors.toMap(Function.identity(), Function.identity()))::getOrDefault;
	// Map of attributes
	private Map<String, String> attributes = null;

	// List of nodes (Element or CData)
	private List<XMLNodeIfc> nodes = null;

	// Element name
	private final String name;

	// XMLNS of the element
	private String xmlns = null;

	/**
	 * Shallow cloning constructor.
	 * Will create a copy of the element (name, attributes and list of subnodes) but it will not create copy of children
	 * - the same instances will be added to the new copy).
	 */
	public Element(@NotNull Element src) {
		if (src.attributes != null) {
			this.attributes = new HashMap<>(src.attributes);
		}
		this.name = src.name;

		this.xmlns = src.xmlns;
		if (src.nodes != null) {
			this.nodes = new ArrayList<>(src.nodes);
		}
	}

	/**
	 * Constructor creating element with a name
	 */
	public Element(@NotNull String name) {
		this.name = elementNameDeduplicationFn.apply(name, name);
	}

	/**
	 * Add CData node with passed value
	 *
	 * @return this element
	 */
	public @NotNull Element addCData(@NotNull String cdata) {
		addNode(new CData(cdata));
		return this;
	}

	/**
	 * Add element as a child.
	 *
	 * @return this element
	 */
	public @NotNull Element addChild(@NotNull Element child) {
		Objects.requireNonNull(child, "Element child can not be null.");
		addNode(child);
		return this;
	}

	/**
	 * Add children
	 *
	 * @return this element
	 */
	public @NotNull Element addChildren(@NotNull List<Element> children) {
		Objects.requireNonNull(children, "List of children cannot be null.");
		if (this.nodes == null) {
			this.nodes = new ArrayList<>(children.size());
		}
		for (XMLNodeIfc child : children) {
			Objects.requireNonNull(child, "Element child can not be null.");
			this.nodes.add(child);
		}
		return this;
	}

	private void addNode(@NotNull XMLNodeIfc node) {
		if (nodes == null) {
			this.nodes = new ArrayList<>();
		}    // end of if (children == null)
		nodes.add(node);
	}

	/**
	 * Serialize subnodes
	 */
	public String nodesToString() {
		StringBuilder result = new StringBuilder();
		nodesToString(result);

		return (result.length() > 0) ? result.toString() : null;
	}

	/**
	 * Serialize subnodes to passed builder
	 */
	public void nodesToString(@NotNull StringBuilder result) {
		if (nodes != null) {
			for (XMLNodeIfc node : nodes) {
				if (node instanceof Element) {
					((Element) node).toString(result);
				} else {
					result.append(node.toString());
				}
			}
		}
	}

	/**
	 * Serialize subnodes as a formatted string
	 */
	public String nodesToStringPretty() {
		StringBuilder result = new StringBuilder();

		if (nodes != null) {
			for (XMLNodeIfc node : nodes) {
				result.append(node.toStringPretty());
			}
		}

		return (result.length() > 0) ? result.toString() : null;
	}

	/**
	 * Serialize subnodes as a secure string
	 */
	public String nodesToStringSecure() {
		StringBuilder result = new StringBuilder();
		nodesToStringSecure(result);

		return (result.length() > 0) ? result.toString() : null;
	}

	/**
	 * Serialize subnodes to passed builder as a secure string
	 */
	public void nodesToStringSecure(@NotNull StringBuilder result) {
		if (nodes != null) {
			for (XMLNodeIfc node : nodes) {
				if (node instanceof Element) {
					((Element) node).toStringSecure(result);
				} else {
					result.append(node.toStringSecure());
				}
			}
		}
	}

	@Override
	public Element clone() {
		return new Element(this);
	}

	/**
	 * Methods checks equality of instances of `Element` excluding list of children
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Element)) {
			return false;
		}
		Element elem = (Element) obj;
		if (Objects.equals(getName(), elem.getName()) && Objects.equals(getXMLNS(), elem.getXMLNS())) {
			if ((attributes == null || attributes.isEmpty()) == (elem.attributes == null || attributes.isEmpty())) {
				if (attributes != null && !attributes.isEmpty()) {
					for (Map.Entry<String, String> entry : attributes.entrySet()) {
						if (!Objects.equals(entry.getValue(), elem.getAttribute(entry.getKey()))) {
							return false;
						}
					}
					return true;
				} else {
					return true;
				}
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * Method returns first child which matches predicate
	 */
	public @Nullable Element findChild(@NotNull Predicate<Element> predicate) {
		if (nodes != null) {
			for (XMLNodeIfc node : nodes) {
				if (!(node instanceof Element)) {
					continue;
				}

				Element el = (Element) node;
				if (predicate.test(el)) {
					return el;
				}
			}
		}

		return null;
	}

	/**
	 * Method returns first child which name matches
	 */
	public @Nullable Element findChild(@NotNull String name) {
		return findChild(name(name));
	}

	/**
	 * Method returns first child which matches name and xmlns
	 */
	public @Nullable Element findChild(@NotNull String name, @NotNull String xmlns) {
		return findChild(name(name).and(xmlns(xmlns)));
	}

	/**
	 * Method returns first element which matches path
	 */
	public @Nullable Element findChildAt(@NotNull Path path) {
		return path.evaluate(this);
	}

	/**
	 * Method returns list of children matching predicate
	 */
	public @NotNull List<Element> findChildren(@NotNull Predicate<Element> predicate) {
		if (nodes != null) {
			LinkedList<Element> result = new LinkedList<Element>();
			forEachChild(el -> {
				if (predicate.test(el)) {
					result.add(el);
				}
			});
			return result;
		}

		return Collections.emptyList();
	}

	/**
	 * Method returns list of children matching path
	 */
	public @NotNull List<Element> findChildrenAt(@NotNull Path path) {
		return path.evaluateAll(this);
	}

	/**
	 * Method returns value of the attribute
	 */
	public @Nullable String getAttribute(String name) {
		if (attributes != null) {
			return attributes.get(name);
		}

		return null;
	}

	/**
	 * Method returns value of the attribute of the first element matching path
	 */
	public @Nullable String getAttributeAt(Path path, String name) {
		Element subChild = findChildAt(path);
		if (subChild == null) {
			return null;
		}
		return subChild.getAttribute(name);
	}

	/**
	 * Method returns copy of all attributes
	 */
	public @NotNull Map<String, String> getAttributes() {
		return ((attributes != null) ? Collections.unmodifiableMap(attributes) : Collections.emptyMap());
	}

	/**
	 * Method replaces all attributes with name-value pairs provided in the map
	 *
	 * @return this element
	 */
	public @NotNull Element setAttributes(@NotNull Map<String, String> attributes) {
		this.attributes = new HashMap<>(attributes.size());
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			setAttribute(entry.getKey(), entry.getValue());
		}
		return this;
	}

	/**
	 * Method replaces all attributes with provided name-value pair.
	 * Length of both arrays must be equal!
	 */
	public @NotNull Element setAttributes(@NotNull String[] names, @NotNull String[] values) {
		attributes = new HashMap<>(names.length);
		for (int i = 0; i < names.length; i++) {
			setAttribute(names[i], values[i]);
		}
		return this;
	}

	/**
	 * Method returns CData value of the element
	 */
	public @Nullable String getCData() {
		return cdataToString();
	}

	/**
	 * Method returns CData value of the first element matching path
	 */
	public @Nullable String getCDataAt(@NotNull Path path) {
		Element subChild = findChildAt(path);
		if (subChild == null) {
			return null;
		}
		return subChild.getCData();
	}

	/**
	 * Method sets CData of the element.
	 * <strong>WARNING: This method replaces existing CData and removes children of the element!</strong>
	 */
	public @NotNull Element setCData(@NotNull String cdata) {
		nodes = new ArrayList<>();
		addNode(new CData(cdata));
		return this;
	}

	/**
	 * Method returns list of all children
	 */
	public List<Element> getChildren() {
		if (nodes != null) {
			ArrayList<Element> result = new ArrayList<>();
			forEachChild(result::add);
			return result;
		}

		return Collections.emptyList();
	}

	/**
	 * Method replaces all nodes (children and cdata) with passed list of children
	 *
	 * @return this element
	 */
	public @NotNull Element setChildren(@NotNull List<XMLNodeIfc> nodes) {
		this.nodes = new ArrayList<>(nodes.size());
		for (XMLNodeIfc node : nodes) {
			Objects.requireNonNull(node, "Child cannot be null!");
			this.nodes.add(node.clone());
		}    // end of for (Element child: children)
		return this;
	}

	/**
	 * Method returns name of the element
	 */
	public @NotNull String getName() {
		return this.name;
	}

	/**
	 * Method returns XMLNS of the element
	 */
	public @Nullable String getXMLNS() {
		if (xmlns == null) {
			xmlns = getAttribute(ATTR_XMLNS_KEY);
		}

		return xmlns;
	}

	/**
	 * Method sets XMLNS of the element
	 *
	 * @return this element
	 */
	public @NotNull Element setXMLNS(@Nullable String ns) {
		if (ns == null) {
			removeAttribute(ATTR_XMLNS_KEY);
		} else {
			setAttribute(ATTR_XMLNS_KEY, ns);
		}
		return this;
	}

	@Override
	public int hashCode() {
		return toStringNoChildren().hashCode();
	}

	/**
	 * Method executes function passing self as a parameter and returns its result
	 */
	public @Nullable <R> R map(@NotNull Function<Element, ? extends R> mapper) {
		return mapper.apply(this);
	}

	/**
	 * Method executes consumer passing self as a parameter
	 * Useful for conditional building of element in the single line
	 *
	 * @return this element
	 */
	public @NotNull Element modify(@NotNull Consumer<Element> modifier) {
		modifier.accept(this);
		return this;
	}

	/**
	 * Method applies function against each child of the element and returns list of non-null return values
	 */
	public <R> @NotNull List<R> compactMapChildren(@NotNull Function<Element, ? extends R> mapper) {
		if (nodes != null) {
			LinkedList<R> result = new LinkedList<R>();
			forEachChild(el -> {
				R val = mapper.apply(el);
				if (val != null) {
					result.add(val);
				}
			});
			return result;
		}

		return Collections.emptyList();
	}

	/**
	 * Method executes function passing each child and then combining returned lists of results in a single list
	 */
	public <R> @NotNull List<R> flatMapChildren(@NotNull Function<Element, Collection<? extends R>> mapper) {
		if (nodes != null) {
			LinkedList<R> result = new LinkedList<R>();
			forEachChild(el -> result.addAll(mapper.apply(el)));
			return result;
		}

		return Collections.emptyList();
	}

	/**
	 * Method applies function against each child of the element and returns list results
	 */
	public <R> @NotNull List<R> mapChildren(@NotNull Function<Element, ? extends R> mapper) {
		if (nodes != null) {
			LinkedList<R> result = new LinkedList<R>();
			forEachChild(el -> result.add(mapper.apply(el)));
			return result;
		}

		return Collections.emptyList();
	}

	private void forEachChild(Consumer<Element> consumer) {
		for (XMLNodeIfc node : nodes) {
			if (node instanceof Element) {
				consumer.accept((Element) node);
			}
		}
	}

	/**
	 * Method test if this element matches predicate
	 */
	public boolean matches(@NotNull Predicate<Element> predicate) {
		return predicate.test(this);
	}

	/**
	 * Method removes attribute
	 */
	public void removeAttribute(@NotNull String name) {
		if (attributes != null) {
			if (ATTR_XMLNS_KEY.equals(name)) {
				xmlns = null;
			}
			attributes.remove(name);
		}
	}

	/**
	 * Method removes child instance
	 *
	 * @return true - if child was removed
	 */
	public boolean removeChild(@NotNull Element child) {
		boolean res = false;

		if (nodes != null) {
			res = nodes.remove(child);
		}    // end of if (children == null)

		return res;
	}

	/**
	 * Method removes first child with matching name and xmlns
	 *
	 * @return true - if child was removed
	 */
	public boolean removeChild(@NotNull String name, @NotNull String xmlns) {
		Element child = findChild(name, xmlns);
		if (child == null) {
			return false;
		}

		return removeChild(child);
	}

	/**
	 * Method removes first child matching predicate
	 *
	 * @return true - if child was removed
	 */
	public boolean removeChild(@NotNull Predicate<Element> predicate) {
		Element child = findChild(predicate);
		if (child != null) {
			nodes.remove(child);
		}
		return child != null;
	}

	/**
	 * Method check if attribute is set
	 */
	public boolean hasAttribute(@NotNull String name) {
		return getAttribute(name) != null;
	}

	/**
	 * Method check if attribute is set to the value
	 */
	public boolean hasAttribute(@NotNull String name, @NotNull String value) {
		return Objects.equals(value, getAttribute(name));
	}

	/**
	 * Method sets attribute value
	 *
	 * @return this element
	 */
	public @NotNull Element setAttribute(@NotNull String name, @NotNull String value) {
		Objects.requireNonNull(name, "Attribute name cannot be null.");
		Objects.requireNonNull(value, "Attribute value cannot be null.");
		if (attributes == null) {
			attributes = new HashMap<>(5);
		}
		String n = attributesDeduplicationFn.apply(name, name);
		String v = value;

		if (ATTR_XMLNS_KEY.equals(n)) {
			xmlns = v;
		}
		attributes.put(n, v);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		toString(result);

		return result.toString();
	}

	/**
	 * Method serializes element to passed build
	 */
	public void toString(StringBuilder result) {
		result.append("<").append(name);
		if (attributes != null) {
			for (String key : attributes.keySet()) {
				result.append(" ").append(key).append("=\"").append(attributes.get(key)).append("\"");
			}
		}

		if (nodes != null && !nodes.isEmpty()) {
			result.append(">");
			nodesToString(result);
			result.append("</").append(name).append(">");
		} else {
			result.append("/>");
		}
	}

	/**
	 * Method serializes element to formatted string
	 */
	@Override
	public String toStringPretty() {
		StringBuilder result = new StringBuilder();

		result.append("<").append(name);
		if (attributes != null) {
			for (String key : attributes.keySet()) {
				result.append(" ").append(key).append("=\"").append(attributes.get(key)).append("\"");
			}
		}

		String nodesStr = nodesToStringPretty();

		if ((nodesStr != null) && (nodesStr.length() > 0)) {
			result.append(">");
			result.append("\n");
			result.append(nodesStr);
			result.append("</").append(name).append(">");
			result.append("\n");
		} else {
			result.append("/>");
			result.append("\n");
		}

		return result.toString();
	}

	/**
	 * Method serializes element without children
	 */
	public String toStringNoChildren() {
		StringBuilder result = new StringBuilder();

		result.append("<").append(name);
		if (attributes != null) {
			for (String key : attributes.keySet()) {
				result.append(" ").append(key).append("=\"").append(attributes.get(key)).append("\"");
			}
		}

		String cdata = cdataToString();

		if (cdata != null) {
			result.append(">");
			if (cdata != null) {
				result.append(cdata);
			}
			result.append("</").append(name).append(">");
		} else {
			result.append("/>");
		}

		return result.toString();
	}

	/**
	 * Method serializes element to a secure string
	 */
	@Override
	public String toStringSecure() {
		StringBuilder result = new StringBuilder();
		toStringSecure(result);

		return result.toString();
	}

	/**
	 * Method serializes element as a secure string to passed builder
	 */
	public void toStringSecure(StringBuilder result) {
		result.append("<").append(name);
		if (attributes != null) {
			for (String key : attributes.keySet()) {
				result.append(" ").append(key).append("=\"").append(attributes.get(key)).append("\"");
			}
		}

		if (nodes != null && !nodes.isEmpty()) {
			result.append(">");
			nodesToStringSecure(result);
			result.append("</").append(name).append(">");
		} else {
			result.append("/>");
		}
	}

	/**
	 * Method returns joined CData values
	 */
	public String cdataToString() {
		StringBuilder result = new StringBuilder();

		if (nodes != null) {
			for (XMLNodeIfc node : nodes) {
				if (node instanceof CData) {
					result.append(node.toString());
				}
			}
		}

		return (result.length() > 0) ? result.toString() : null;
	}

}

