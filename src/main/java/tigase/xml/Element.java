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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
		}

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

	private static final Map<String,String> ATTRIBUTES_EMPTY = Collections.emptyMap();
	private static final List<XMLNodeIfc> NODES_EMPTY = Collections.emptyList();

	// Function used for deduplication of element names to reduce memory usage when a lot of elements may contain
	// the same name
	protected static BiFunction<String, String, String> elementNameDeduplicationFn = List.of("message", "iq",
																							 "presence", "query",
																							 "pubsub", "body",
																							 "stanza-id", "event")
			.stream()
			.collect(Collectors.toMap(Function.identity(), Function.identity()))::getOrDefault;

	// Function used for deduplication of element attribute names to reduce memory usage when a lot of attribute names
	// may be the same
	protected static BiFunction<String, String, String> attributesDeduplicationFn = List.of("id", "name",
																							ATTR_XMLNS_KEY, "from",
																							"to", "hash")
			.stream()
			.collect(Collectors.toMap(Function.identity(), Function.identity()))::getOrDefault;
	// Map of attributes
	private @NonNull Map<String, String> attributes = ATTRIBUTES_EMPTY;

	// List of nodes (Element or CData)
	private @NonNull List<XMLNodeIfc> nodes = NODES_EMPTY;

	// Element name
	private final String name;

	// XMLNS of the element
	private String xmlns = null;

	/**
	 * Shallow cloning constructor.
	 * Will create a copy of the element (name, attributes and list of subnodes) but it will not create copy of children
	 * - the same instances will be added to the new copy).
	 */
	public Element(@NonNull Element src) {
		if (!src.attributes.isEmpty()) {
			this.attributes = new HashMap<>(src.attributes);
		}
		this.name = src.name;

		this.xmlns = src.xmlns;
		if (!src.nodes.isEmpty()) {
			this.nodes = new ArrayList<>(src.nodes);
		}
	}

	/**
	 * Constructor creating element with a name
	 */
	public Element(@NonNull String name) {
		this.name = elementNameDeduplicationFn.apply(name, name);
	}

	/**
	 * Add CData node with passed value
	 *
	 * @return this element
	 */
	public @NonNull Element addCData(@NonNull String cdata) {
		addNode(new CData(cdata));
		return this;
	}

	/**
	 * Add element as a child.
	 *
	 * @return this element
	 */
	public @NonNull Element addChild(@NonNull Element child) {
		Objects.requireNonNull(child, "Element child can not be null.");
		addNode(child);
		return this;
	}

	/**
	 * Add children
	 *
	 * @return this element
	 */
	public @NonNull Element addChildren(@NonNull List<Element> children) {
		Objects.requireNonNull(children, "List of children cannot be null.");
		if (this.nodes == NODES_EMPTY) {
			this.nodes = new ArrayList<>(children.size());
		}
		for (XMLNodeIfc child : children) {
			Objects.requireNonNull(child, "Element child can not be null.");
			this.nodes.add(child);
		}
		return this;
	}

	private void addNode(@NonNull XMLNodeIfc node) {
		if (nodes == NODES_EMPTY) {
			this.nodes = new ArrayList<>();
		}
		nodes.add(node);
	}

	/**
	 * Serialize subnodes to passed builder
	 */
	public void nodesToString(@NonNull StringBuilder result) {
		for (XMLNodeIfc node : nodes) {
			if (node instanceof Element) {
				((Element) node).toString(result);
			} else {
				result.append(node.toString());
			}
		}
	}

	/**
	 * Serialize subnodes as a formatted string
	 */
	public void nodesToStringPretty(@NonNull StringBuilder result) {
		for (XMLNodeIfc node : nodes) {
			result.append(node.toStringPretty());
		}
	}
	
	/**
	 * Serialize subnodes to passed builder as a secure string
	 */
	public void nodesToStringSecure(@NonNull StringBuilder result) {
		for (XMLNodeIfc node : nodes) {
			if (node instanceof Element) {
				((Element) node).toStringSecure(result);
			} else {
				result.append(node.toStringSecure());
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
		if (!(obj instanceof Element elem)) {
			return false;
		}

		if (Objects.equals(getName(), elem.getName()) && Objects.equals(getXMLNS(), elem.getXMLNS())) {
			return Objects.equals(attributes, elem.attributes);
		}
		return false;
	}

	/**
	 * Method returns first child which matches predicate
	 */
	public @Nullable Element findChild(@NonNull Predicate<Element> predicate) {
		for (XMLNodeIfc node : nodes) {
			if (!(node instanceof Element el)) {
				continue;
			}

			if (predicate.test(el)) {
				return el;
			}
		}

		return null;
	}

	/**
	 * Method returns first child which name matches
	 */
	public @Nullable Element findChild(@NonNull String name) {
		return findChild(name(name));
	}

	/**
	 * Method returns first child which matches name and xmlns
	 */
	public @Nullable Element findChild(@NonNull String name, @NonNull String xmlns) {
		return findChild(name(name).and(xmlns(xmlns)));
	}

	/**
	 * Method returns first element which matches path
	 */
	public @Nullable Element findChildAt(@NonNull Path path) {
		return path.evaluate(this);
	}

	/**
	 * Method returns first element which matches path
	 */
	public @Nullable Element findChildAt(@NonNull Predicate<Element> predicate, @NonNull Predicate<Element>... path) {
		Element child = findChild(predicate);
		if (child == null) {
			return null;
		}
		for (Predicate<Element> pathPredicate : path) {
			child = child.findChild(pathPredicate);
			if (child == null) {
				return null;
			}
		}
		return child;
	}

	/**
	 * Method returns list of children matching predicate
	 */
	public @NonNull List<Element> findChildren(@NonNull Predicate<Element> predicate) {
		if (!nodes.isEmpty()) {
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
	public @NonNull List<Element> findChildrenAt(@NonNull Path path) {
		return path.evaluateAll(this);
	}

	/**
	 * Method returns value of the attribute
	 */
	public @Nullable String getAttribute(String name) {
		return attributes.get(name);
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
	public @NonNull Map<String, String> getAttributes() {
		return (attributes != ATTRIBUTES_EMPTY) ? Collections.unmodifiableMap(attributes) : ATTRIBUTES_EMPTY;
	}

	/**
	 * Method replaces all attributes with name-value pairs provided in the map
	 *
	 * @return this element
	 */
	public @NonNull Element setAttributes(@NonNull Map<String, String> attributes) {
		this.attributes = new HashMap<>(attributes.size());
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			setAttribute(entry.getKey(), entry.getValue());
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
	public @Nullable String getCDataAt(@NonNull Path path) {
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
	public @NonNull Element setCData(@NonNull String cdata) {
		nodes = new ArrayList<>();
		addNode(new CData(cdata));
		return this;
	}

	/**
	 * Method returns list of all children
	 */
	public @NonNull List<Element> getChildren() {
		if (nodes != NODES_EMPTY) {
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
	public @NonNull Element setChildren(@NonNull List<XMLNodeIfc> nodes) {
		this.nodes = new ArrayList<>(nodes.size());
		for (XMLNodeIfc node : nodes) {
			Objects.requireNonNull(node, "Child cannot be null!");
			this.nodes.add(node.clone());
		}
		return this;
	}

	/**
	 * Method returns name of the element
	 */
	public @NonNull String getName() {
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
	public @NonNull Element setXMLNS(@Nullable String ns) {
		if (ns == null) {
			removeAttribute(ATTR_XMLNS_KEY);
		} else {
			setAttribute(ATTR_XMLNS_KEY, ns);
		}
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name) + Objects.hashCode(xmlns) + Objects.hashCode(attributes);
	}

	/**
	 * Method executes function passing self as a parameter and returns its result
	 */
	public @Nullable <R> R map(@NonNull Function<Element, ? extends R> mapper) {
		return mapper.apply(this);
	}

	/**
	 * Method executes consumer passing self as a parameter
	 * Useful for conditional building of element in the single line
	 *
	 * @return this element
	 */
	public @NonNull Element modify(@NonNull Consumer<Element> modifier) {
		modifier.accept(this);
		return this;
	}
	

	private static class ElementMapperHelper {
		
		protected static <X> List<X> toList(Consumer<Consumer<X>> consumer) {
			ArrayList<X> result = new ArrayList<X>();
			consumer.accept(result::add);
			return result;
		}

		protected static <X,Y> Consumer<Y> map(@NonNull Function<Y, ? extends X> mapper, @NonNull Consumer<X> consumer) {
			return el -> consumer.accept(mapper.apply(el));
		}

		protected static <X> Consumer<X> filter(@NonNull Predicate<X> predicate, @NonNull Consumer<X> consumer) {
			return it -> {
				if (predicate.test(it)) {
					consumer.accept(it);
				}
			};
		}
		
	}

	/**
	 * Method transforms first child element matching predicate
	 */
	public <R> R mapChild(@NonNull Predicate<Element> predicate, @NonNull Function<Element, R> mapper) {
		Element child = findChild(predicate);
		if (child == null) {
			return null;
		}
		return mapper.apply(child);
	}

	/**
	 * Method applies function against each child of the element and returns list results
	 */
	public <R> @NonNull List<R> mapChildren(@NonNull Function<Element, ? extends R> mapper) {
		return mapChildren(null, mapper, null);
	}

	/**
	 * Method applies function against each child of the element and returns list results
	 */
	public <R> @NonNull List<R> mapChildren(@NonNull Predicate<Element> predicate, @NonNull Function<Element, ? extends R> mapper) {
		return mapChildren(predicate, mapper, null);
	}
	
	/**
	 * Method applies function against each child of the element and returns list of non-null return values
	 */
	public <R> @NotNull List<R> compactMapChildren(@NonNull Function<Element, ? extends R> mapper) {
		return mapChildren(null, mapper, Objects::nonNull);
	}

	/**
	 * Method applies function against each child of the element and returns list of non-null return values
	 */
	public <R> @NotNull List<R> compactMapChildren(@Nullable Predicate<Element> predicate, @NonNull Function<Element, ? extends R> mapper) {
		return mapChildren(predicate, mapper, Objects::nonNull);
	}

	/**
	 * Method applied filtering and transformation for child elements (used internally)
	 */
	private <R> @NotNull List<R> mapChildren(@Nullable Predicate<Element> predicate, @NonNull Function<Element, ? extends R> mapper, @Nullable Predicate<R> elementPredicate) {
		return ElementMapperHelper.toList(consumer -> {
			var mappingConsumer = ElementMapperHelper.map(mapper, elementPredicate == null ? consumer : ElementMapperHelper.filter(elementPredicate, consumer));
			forEachChild(predicate == null ? mappingConsumer : ElementMapperHelper.filter(predicate, mappingConsumer));
		});
	}

	public void forEachChild(Consumer<Element> consumer) {
		for (XMLNodeIfc node : nodes) {
			if (node instanceof Element) {
				consumer.accept((Element) node);
			}
		}
	}

	public void forEachChild(@NonNull Predicate<Element> predicate, Consumer<Element> consumer) {
		forEachChild(child -> {
			if (predicate.test(child)) {
				consumer.accept(child);
			}
		});
	}

	/**
	 * Method test if this element matches predicate
	 */
	public boolean matches(@NonNull Predicate<Element> predicate) {
		return predicate.test(this);
	}

	/**
	 * Method removes attribute
	 */
	public void removeAttribute(@NonNull String name) {
		if (attributes != ATTRIBUTES_EMPTY) {
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
	public boolean removeChild(@NonNull Element child) {
		boolean res = false;

		if (nodes != NODES_EMPTY) {
			res = nodes.remove(child);
		}

		return res;
	}

	/**
	 * Method removes first child with matching name and xmlns
	 *
	 * @return true - if child was removed
	 */
	public boolean removeChild(@NonNull String name, @NonNull String xmlns) {
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
	public boolean removeChild(@NonNull Predicate<Element> predicate) {
		Element child = findChild(predicate);
		if (child != null) {
			nodes.remove(child);
		}
		return child != null;
	}

	/**
	 * Method check if attribute is set
	 */
	public boolean hasAttribute(@NonNull String name) {
		return getAttribute(name) != null;
	}

	/**
	 * Method check if attribute is set to the value
	 */
	public boolean hasAttribute(@NonNull String name, @NonNull String value) {
		return Objects.equals(value, getAttribute(name));
	}

	/**
	 * Method sets attribute value
	 *
	 * @return this element
	 */
	public @NonNull Element setAttribute(@NonNull String name, @NonNull String value) {
		Objects.requireNonNull(name, "Attribute name cannot be null.");
		Objects.requireNonNull(value, "Attribute value cannot be null.");
		if (attributes == ATTRIBUTES_EMPTY) {
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
		if (!attributes.isEmpty()) {
			for (String key : attributes.keySet()) {
				result.append(" ").append(key).append("=\"").append(attributes.get(key)).append("\"");
			}
		}

		if (!nodes.isEmpty()) {
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
		if (!attributes.isEmpty()) {
			for (String key : attributes.keySet()) {
				result.append(" ").append(key).append("=\"").append(attributes.get(key)).append("\"");
			}
		}

		if (!nodes.isEmpty()) {
			result.append(">");
			result.append("\n");
			nodesToString(result);
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
		if (!attributes.isEmpty()) {
			for (String key : attributes.keySet()) {
				result.append(" ").append(key).append("=\"").append(attributes.get(key)).append("\"");
			}
		}

		String cdata = cdataToString();

		if (cdata != null) {
			result.append(">");
			result.append(cdata);
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
		if (!attributes.isEmpty()) {
			for (String key : attributes.keySet()) {
				result.append(" ").append(key).append("=\"").append(attributes.get(key)).append("\"");
			}
		}

		if (!nodes.isEmpty()) {
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

		for (XMLNodeIfc node : nodes) {
			if (node instanceof CData) {
				result.append(node.toString());
			}
		}

		return (!result.isEmpty()) ? result.toString() : null;
	}

}

