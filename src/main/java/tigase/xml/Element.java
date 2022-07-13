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
 * most more advanced cases should be extended with additional features. Look in API documentation for more details and
 * information about existing extensions. The most important features apart from abvious tree implementation are: <ul>
 * <li><code>toString()</code> implementation so it can generate valid <em>XML</em> content from this element and all
 * children.</li> <li><code>addChild(...)</code>, <code>getChild(childName)</code> supporting generic types.</li>
 * <li><code>findChild(childPath)</code> finding child in subtree by given path to element.</li>
 * <li><code>getChildCData(childPath)</code>, <code>getAttribute(childPath, attName)</code> returning element CData from
 * child in subtree by given path to element.</li> </ul> <p> Created: Mon Oct 4 17:55:16 2004 </p>
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
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

	// Function used for deduplication of element names to reduce memory usage when a lot of elements may contains
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
	private Map<String,String> attributes = null;

	// List of nodes (Element or CData)
	private List<XMLNodeIfc> children = null;

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
		if (src.children != null) {
			this.children = new ArrayList<>(src.children);
		}
	}

	/**
	 * Constructor creating element with a name
	 * @param name
	 */
	public Element(@NotNull String name) {
		this.name = elementNameDeduplicationFn.apply(name, name);
	}

	/**
	 * Add attribute
	 * @param name - name of the attribute
	 * @param value - value of the attribute
	 * @return this element
	 */
	public @NotNull Element addAttribute(@NotNull String name, @NotNull String value) {
		return setAttribute(name, value);
	}

	/**
	 * Add attributes
	 * @param attributes
	 * @return this element
	 */
	public @NotNull Element addAttributes(@NotNull Map<String, String> attributes) {
		if (this.attributes == null) {
			this.attributes = new HashMap<>(attributes.size());
		}
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			setAttribute(entry.getKey(), entry.getValue());
		}
		return this;
	}

	/**
	 * Add CData node with passed value
	 * @param cdata
	 * @return this element
	 */
	public @NotNull Element addCData(@NotNull String cdata) {
		return addChild(new CData(cdata));
	}

	/**
	 * Add node as a child.
	 * @param child
	 * @return this element
	 */
	public @NotNull Element addChild(@NotNull XMLNodeIfc child) {
		if (child == null) {
			throw new NullPointerException("Element child can not be null.");
		}
		if (children == null) {
			this.children = new ArrayList<>();
		}    // end of if (children == null)
		children.add(child);
		return this;
	}

	/**
	 * Add children
	 * @param children
	 * @return this element
	 */
	public @NotNull Element addChildren(@NotNull List<Element> children) {
		if (children == null) {
			throw new NullPointerException("List of children cannot be null.");
		}    // end of if (children == null)
		if (this.children == null) {
			this.children = new ArrayList<>(children.size());
		}    // end of if (children == null)
		for (XMLNodeIfc child : children) {
			if (child == null) {
				throw new NullPointerException("Child can not be null.");
			}
			this.children.add(child);
		}    // end of for (Element child: children)

		// this.children.addAll(children);
		// Collections.sort(children);
		return this;
	}

	/**
	 * Serialize subnodes
	 * @return
	 */
	public String childrenToString() {
		StringBuilder result = new StringBuilder();
		childrenToString(result);

		return (result.length() > 0) ? result.toString() : null;
	}

	/**
	 * Serialize subnodes to passed builder
	 * @param result
	 */
	public void childrenToString(@NotNull StringBuilder result) {
		if (children != null) {
			for (XMLNodeIfc child : children) {

				// This is weird but if there is a bug in some other component
				// it may add null children to the element, let's be save here.
				// FIXME: there are protections against adding null as a child
				//if (child != null) {
					if (child instanceof Element) {
						((Element) child).toString(result);
					} else {
						result.append(child.toString());
					}
				//}
			}    // end of for ()
		}        // end of if (child != null)
	}

	/**
	 * Serialize subnodes as a formatted string
	 * @return
	 */
	public String childrenToStringPretty() {
		StringBuilder result = new StringBuilder();

		if (children != null) {
			for (XMLNodeIfc child : children) {

				// This is weird but if there is a bug in some other component
				// it may add null children to the element, let's be save here.
				// FIXME: there are protections against adding null as a child
				// if (child != null) {
					result.append(child.toStringPretty());
				//}
			}    // end of for ()
		}        // end of if (child != null)

		return (result.length() > 0) ? result.toString() : null;
	}

	/**
	 * Serialize subnodes as a secure string
	 * @return
	 */
	public String childrenToStringSecure() {
		StringBuilder result = new StringBuilder();
		childrenToStringSecure(result);

		return (result.length() > 0) ? result.toString() : null;
	}

	/**
	 * Serialize subnodes to passed builder as a secure string
	 * @return
	 */
	public void childrenToStringSecure(@NotNull StringBuilder result) {
		if (children != null) {
			for (XMLNodeIfc child : children) {

				// This is weird but if there is a bug in some other component
				// it may add null children to the element, let's be save here.
				// FIXME: there are protections against adding null as a child
				// if (child != null) {
					if (child instanceof Element) {
						((Element) child).toStringSecure(result);
					} else {
						result.append(child.toStringSecure());
					}
				// }
			}    // end of for ()
		}        // end of if (child != null)
	}

	@Override
	public Element clone() {
		return new Element(this);
	}

	/**
	 * Methods checks equality of instances of `Element` excluding list of children
	 * @param obj
	 * @return
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Element)) {
			return false;
		}
		Element elem = (Element) obj;
		if (Objects.equals(getName(), elem.getName()) && Objects.equals(getXMLNS(), elem.getXMLNS())) {
			for (Map.Entry<String,String> entry : attributes.entrySet()) {
				if (!Objects.equals(entry.getValue(), elem.getAttribute(entry.getKey()))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Method returns first child which matches predicate
	 * @param predicate
	 * @return
	 */
	public @Nullable Element findChild(@NotNull Predicate<Element> predicate) {
		if (children != null) {
			for (XMLNodeIfc node : children) {
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
	 * @param name
	 * @return
	 */
	public @Nullable Element findChild(@NotNull String name) {
		return findChild(name(name));
	}

	/**
	 * Method returns first child which matches name and xmlns
	 * @param name
	 * @param xmlns
	 * @return
	 */
	public @Nullable Element findChild(@NotNull String name, @NotNull String xmlns) {
		return findChild(name(name).and(xmlns(xmlns)));
	}

	/**
	 * Method returns first element which matches path
	 * @param path
	 * @return
	 */
	public @Nullable Element findChildAt(@NotNull Path path) {
		return path.evaluate(this);
	}

	/**
	 * Method returns list of children matching predicate
	 * @param predicate
	 * @return
	 */
	public @NotNull List<Element> findChildren(@NotNull Predicate<Element> predicate) {
		if (children != null) {
			LinkedList<Element> result = new LinkedList<Element>();

			for (XMLNodeIfc node : children) {
				if (!(node instanceof Element)) {
					continue;
				}

				Element el = (Element) node;
				if (predicate.test(el)) {
					result.add(el);
				}
			}

			return result;
		}

		return Collections.emptyList();
	}

	/**
	 * Method returns list of children matching path
	 * @param path
	 * @return
	 */
	public @NotNull List<Element> findChildrenAt(@NotNull Path path) {
		return path.evaluateAll(this);
	}

	/**
	 * Method returns value of the attribute
	 * @param name
	 * @return
	 */
	public @Nullable String getAttribute(String name) {
		if (attributes != null) {
			return attributes.get(name);
		}    // end of if (attributes != null)

		return null;
	}

	/**
	 * Method returns value of the attribute of the first element matching path
	 * @param path
	 * @param name
	 * @return
	 */
	public @Nullable String getAttributeAt(Path path, String name) {
		Element subchild = findChildAt(path);
		if (subchild == null) {
			return null;
		}
		return subchild.getAttribute(name);
	}

	/**
	 * Method returns copy of all attributes
	 * @return
	 */
	public @NotNull Map<String, String> getAttributes() {
		return ((attributes != null) ? Collections.unmodifiableMap(attributes) : Collections.emptyMap());
	}

	/**
	 * Method replaces all attributes with name-value pairs provided in the map
	 * @param attributes
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
	 * @param names
	 * @param values
	 * @return
	 */
	// FIXME: Should we remove this method as well? or it is better to keep if for easy way to set attributes in bulk?
	public @NotNull Element setAttributes(@NotNull String[] names, @NotNull String[] values) {
		attributes = new HashMap<>(names.length);
		for (int i=0; i<names.length; i++) {
			setAttribute(names[i], values[i]);
		}
		return this;
	}

	/**
	 * Method returns CData value of the element
	 * @return
	 */
	public @Nullable String getCData() {
		return cdataToString();
	}

	/**
	 * Method returns CData value of the first element matching path
	 * @param path
	 * @return
	 */
	public @Nullable String getCDataAt(@NotNull Path path) {
		Element subchild = findChildAt(path);
		if (subchild == null) {
			return null;
		}
		return subchild.getCData();
	}

	/**
	 * Method sets CData of the element.
	 * <strong>WARNING: This method replaces existing CData and removes children of the element!</strong>
	 * @param cdata
	 * @return
	 */
	public @NotNull Element setCData(@NotNull String cdata) {
		children = new ArrayList<>();
		return addChild(new CData(cdata));
	}

	/**
	 * Method returns list of all children
	 * @return
	 */
	public List<Element> getChildren() {
		if (children != null) {
			LinkedList<Element> result = new LinkedList<Element>();

			for (XMLNodeIfc node : children) {
				if (node instanceof Element) {
					result.add((Element) node);
				}
			}

			return result;
		}

		return Collections.emptyList();
	}

	/**
	 * Method replaces all nodes (children and cdata) with passed list of children
	 * @param children
	 * @return this element
	 */
	public @NotNull Element setChildren(@NotNull List<XMLNodeIfc> children) {
		this.children = new ArrayList<>(children.size());
		for (XMLNodeIfc child : children) {
			this.children.add(child.clone());
		}    // end of for (Element child: children)
		return this;
	}

	/**
	 * Method returns name of the element
	 * @return
	 */
	public @NotNull String getName() {
		return this.name;
	}

	/**
	 * Method returns XMLNS of the element
	 * @return
	 */
	public @Nullable String getXMLNS() {
		if (xmlns == null) {
			xmlns = getAttribute(ATTR_XMLNS_KEY);
		}

		return xmlns;
	}

	/**
	 * Method sets XMLNS of the element
	 * @param ns
	 * @return this element
	 */
	public @NotNull Element setXMLNS(@Nullable String ns) {
		if (ns == null) {
			// FIXME: leaving setting `xmlns` to removeAttribute()/setAttribute() methods
			//xmlns = null;
			removeAttribute(ATTR_XMLNS_KEY);
		} else {
			// FIXME: leaving setting `xmlns` to removeAttribute()/setAttribute() methods
			//xmlns = ns;
			setAttribute(ATTR_XMLNS_KEY, xmlns);
		}
		return this;
	}

	@Override
	public int hashCode() {
		return toStringNoChildren().hashCode();
	}

	/**
	 * Method executes function passing self as a parameter and returns its result
	 * @param mapper
	 * @return
	 * @param <R>
	 */
	public @Nullable <R> R map(@NotNull Function<Element, ? extends R> mapper) {
		return mapper.apply(this);
	}

	/**
	 * Method executes consumer passing self as a parameter
	 * Useful for conditional building of element in the single line
	 * @param modifier
	 * @return this element
	 */
	public @NotNull Element modify(@NotNull Consumer<Element> modifier) {
		modifier.accept(this);
		return this;
	}

	/**
	 * Method applies function against each child of the element and returns list of non-null return values
	 * @param mapper
	 * @return
	 * @param <R>
	 */
	public <R> @NotNull List<R> compactMapChildren(@NotNull Function<Element, ? extends R> mapper) {
		if (children != null) {
			LinkedList<R> result = new LinkedList<R>();

			for (XMLNodeIfc node : children) {
				if (!(node instanceof Element)) {
					continue;
				}

				Element el = (Element) node;
				R val = mapper.apply(el);
				if (val != null) {
					result.add(val);
				}
			}

			return result;
		}

		return Collections.emptyList();
	}

	/**
	 * Method executes function passing each child and then combining returned lists of results in a single list
	 * @param mapper
	 * @return
	 * @param <R>
	 */
	// FIXME: I'm not sure if this should still be here
	public <R> @NotNull List<R> flatMapChildren(@NotNull Function<Element, Collection<? extends R>> mapper) {
		if (children != null) {
			LinkedList<R> result = new LinkedList<R>();

			for (XMLNodeIfc node : children) {
				if (!(node instanceof Element)) {
					continue;
				}

				Element el = (Element) node;
				result.addAll(mapper.apply(el));
			}

			return result;
		}

		return Collections.emptyList();
	}

	/**
	 * Method applies function against each child of the element and returns list results
	 * @param mapper
	 * @return
	 * @param <R>
	 */
	public <R> @NotNull List<R> mapChildren(@NotNull Function<Element, ? extends R> mapper) {
		if (children != null) {
			LinkedList<R> result = new LinkedList<R>();

			for (XMLNodeIfc node : children) {
				if (!(node instanceof Element)) {
					continue;
				}

				Element el = (Element) node;
				result.add(mapper.apply(el));
			}

			return result;
		}

		return Collections.emptyList();
	}

	/**
	 * Method test if this element matches predicate
	 * @param predicate
	 * @return
	 */
	public boolean matches(@NotNull Predicate<Element> predicate) {
		return predicate.test(this);
	}

	/**
	 * Method removes attribute
	 * @param name
	 */
	public void removeAttribute(@NotNull String name) {
		if (attributes != null) {
			if (ATTR_XMLNS_KEY.equals(name)) {
				xmlns = null;
			}
			attributes.remove(name);
		}    // end of if (attributes == null)
	}

	/**
	 * Method removes child instance
	 * @param child
	 * @return true - if child was removed
	 */
	public boolean removeChild(@NotNull Element child) {
		boolean res = false;

		if (children != null) {
			res = children.remove(child);
		}    // end of if (children == null)

		return res;
	}

	/**
	 * Method removes first child with matching name and xmlns
	 * @param name
	 * @param xmlns
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
	 * @param predicate
	 * @return true - if child was removed
	 */
	public boolean removeChild(@NotNull Predicate<Element> predicate) {
		Element child = findChild(predicate);
		if (child != null) {
			children.remove(child);
		}
		return child != null;
	}

	/**
	 * Method check if attribute is set
	 * @param name
	 * @return
	 */
	public boolean hasAttribute(@NotNull String name) {
		return getAttribute(name) != null;
	}

	/**
	 * Method check if attribute is set to the value
	 * @param name
	 * @return
	 */
	public boolean hasAttribute(@NotNull String name, @NotNull String value) {
		return Objects.equals(value, getAttribute(name));
	}

	/**
	 * Method sets attribute value
	 * @param name
	 * @param value
	 * @return this element
	 */
	public @NotNull Element setAttribute(@NotNull String name, @NotNull String value) {
		if (name == null) {
			throw new NullPointerException("Attribute name cannot be null.");
		}
		if (value == null) {
			throw new NullPointerException("Attribute value cannot be null.");
		}
		if (attributes == null) {
			attributes = new HashMap<>(5);
		}    // end of if (attributes == null)
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
	 * @param result
	 */
	public void toString(StringBuilder result) {
		result.append("<").append(name);
		if (attributes != null) {
			for (String key : attributes.keySet()) {
				result.append(" ").append(key).append("=\"").append(attributes.get(key)).append("\"");
			}    // end of for ()
		}      // end of if (attributes != null)

		if (children != null && !children.isEmpty()) {
			result.append(">");
			childrenToString(result);
			result.append("</").append(name).append(">");
		} else {
			result.append("/>");
		}
	}

	/**
	 * Method serializes element to formatted string
	 * @return
	 */
	@Override
	public String toStringPretty() {
		StringBuilder result = new StringBuilder();

		result.append("<").append(name);
		if (attributes != null) {
			for (String key : attributes.keySet()) {
				result.append(" ").append(key).append("=\"").append(attributes.get(key)).append("\"");
			}    // end of for ()
		}      // end of if (attributes != null)

		String childrenStr = childrenToStringPretty();

		if ((childrenStr != null) && (childrenStr.length() > 0)) {
			result.append(">");
			result.append("\n");
			result.append(childrenStr);
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
	 * @return
	 */
	public String toStringNoChildren() {
		StringBuilder result = new StringBuilder();

		result.append("<").append(name);
		if (attributes != null) {
			for (String key : attributes.keySet()) {
				result.append(" ").append(key).append("=\"").append(attributes.get(key)).append("\"");
			}    // end of for ()
		}      // end of if (attributes != null)

		String cdata = cdataToString();

		if (cdata != null) {
			result.append(">");
			if (cdata != null) {
				result.append(cdata);
			}    // end of if (cdata != null)
			result.append("</").append(name).append(">");
		} else {
			result.append("/>");
		}

		return result.toString();
	}

	/**
	 * Method serializes element to a secure string
	 * @return
	 */
	@Override
	public String toStringSecure() {
		StringBuilder result = new StringBuilder();
		toStringSecure(result);

		return result.toString();
	}

	/**
	 * Method serializes element as a secure string to passed builder
	 * @param result
	 */
	public void toStringSecure(StringBuilder result) {
		result.append("<").append(name);
		if (attributes != null) {
			for (String key : attributes.keySet()) {
				result.append(" ").append(key).append("=\"").append(attributes.get(key)).append("\"");
			}    // end of for ()
		}      // end of if (attributes != null)

		if (children != null && !children.isEmpty()) {
			result.append(">");
			childrenToStringSecure(result);
			result.append("</").append(name).append(">");
		} else {
			result.append("/>");
		}
	}

	/**
	 * Method returns joined CData values
	 * @return
	 */
	public String cdataToString() {
		StringBuilder result = new StringBuilder();

		if (children != null) {
			for (XMLNodeIfc child : children) {

				// This is weird but if there is a bug in some other component
				// it may add null children to the element, let's be save here.
				if ((child != null) && (child instanceof CData)) {
					result.append(child.toString());
				}
			}    // end of for ()
		}        // end of if (child != null)

		return (result.length() > 0) ? result.toString() : null;
	}

}    // Element

