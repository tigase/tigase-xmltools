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

import org.jetbrains.annotations.Nullable;
import tigase.xml.annotations.TODO;

import java.io.FileReader;
import java.util.*;
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

	protected static final Map<String, String> elementNameDeduplicationMap = List.of("message", "iq", "presence",
																					 "query", "pubsub", "body",
																					 "stanza-id")
			.stream()
			.collect(Collectors.toMap(Function.identity(), Function.identity()));
	protected static Map<String, String> attributesDeduplicationMap = List.of("id", "name", ATTR_XMLNS_KEY, "from", "to")
			.stream()
			.collect(Collectors.toMap(Function.identity(), Function.identity()));
	private Map<String,String> attributes = null;

	private List<XMLNodeIfc> children = null;

	protected String name = null;

	protected String xmlns = null;
	
	public Element(Element src) {
		if (src.attributes != null) {
			this.attributes = new HashMap<>(src.attributes);
		}
		this.name = src.name;

		this.xmlns = src.xmlns;
		if (src.children != null) {
			this.children = new ArrayList<>(src.children);
		}
	}

	public Element(String argName) {
		setName(argName);
	}

	public Element(String argName, String argCData) {
		setName(argName);
		if (argCData != null) {
			setCData(argCData);
		}
	}

	public Element(String name, String xmlns, String cdata) {
		setName(name);
		if (xmlns != null) {
			setXMLNS(xmlns);
		}
		if (cdata != null) {
			setCData(cdata);
		}
	}

	public Element(String argName, Map<String,String> attributes) {
		setName(argName);
		if (attributes != null) {
			setAttributes(attributes);
		}
	}

	public Element(String argName, Map<String,String> attributes, String cdata) {
		this(argName, attributes);
		if (cdata != null) {
			setCData(cdata);
		}
	}

	public Element(String argName, Map<String,String> attributes, Element[] children) {
		this(argName, attributes);
		if (children != null) {
			addChildren(Arrays.asList(children));
		}
	}

	public Element(String argName, Map<String,String> attributes, List<Element> children) {
		this(argName, attributes);
		if (children != null) {
			addChildren(children);
		}
	}
	
	public Element(String argName, String[] att_names, String[] att_values) {
		setName(argName);
		if (att_names != null) {
			setAttributes(att_names, att_values);
		}    // end of if (att_names != null)
	}

	public Element(String argName, Element[] children, String[] att_names, String[] att_values) {
		setName(argName);
		if (att_names != null) {
			setAttributes(att_names, att_values);
		}    // end of if (att_names != null)
		addChildren(Arrays.asList(children));
	}

	public Element(String argName, String argCData, String[] att_names, String[] att_values) {
		setName(argName);
		if (argCData != null) {
			setCData(argCData);
		}
		if (att_names != null) {
			setAttributes(att_names, att_values);
		}    // end of if (att_names != null)
	}

	public Element(String argName, String argCData, StringBuilder[] att_names, StringBuilder[] att_values) {
		setName(argName);
		if (argCData != null) {
			setCData(argCData);
		}
		if (att_names != null) {
			setAttributes(att_names, att_values);
		}    // end of if (att_names != null)
	}

	public Element(String name, List<Element> children) {
		this(name);
		if (children != null) {
			addChildren(children);
		}
	}

	public Element(String name, String xmlns, List<Element> children) {
		this(name, children);
		setXMLNS(xmlns);
	}

	public Element(String name, Consumer<Element> builder) {
		setName(name);
		builder.accept(this);
	}

	public Element(String name, String xmlns, Consumer<Element> builder) {
		setName(name);
		setXMLNS(xmlns);
		builder.accept(this);
	}

	public Element addAttribute(String name, String value) {
		return setAttribute(name, value);
	}

	public Element addAttributes(Map<String, String> attributes) {
		if (this.attributes == null) {
			this.attributes = new HashMap<>(attributes.size());
		}
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			setAttribute(entry.getKey(), entry.getValue());
		}
		return this;
	}

	public Element addCData(String cdata) {
		return addChild(new CData(cdata));
	}

	public Element addChild(XMLNodeIfc child) {
		if (child == null) {
			throw new NullPointerException("Element child can not be null.");
		}
		if (children == null) {
			this.children = new ArrayList<>();
		}    // end of if (children == null)
		children.add(child);
		return this;
	}

	public Element addChildren(List<Element> children) {
		if (children == null) {
			return this;
		}    // end of if (children == null)
		if (this.children == null) {
			this.children = new ArrayList<>(children.size());
		}    // end of if (children == null)
		for (XMLNodeIfc child : children) {
			this.children.add(child);
		}    // end of for (Element child: children)

		// this.children.addAll(children);
		// Collections.sort(children);
		return this;
	}

	public String childrenToString() {
		StringBuilder result = new StringBuilder();
		childrenToString(result);

		return (result.length() > 0) ? result.toString() : null;
	}

	public void childrenToString(StringBuilder result) {
		if (children != null) {
			for (XMLNodeIfc child : children) {

				// This is weird but if there is a bug in some other component
				// it may add null children to the element, let's be save here.
				if (child != null) {
					if (child instanceof Element) {
						((Element) child).toString(result);
					} else {
						result.append(child.toString());
					}
				}
			}    // end of for ()
		}        // end of if (child != null)
	}

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

	public String childrenToStringSecure() {
		StringBuilder result = new StringBuilder();
		childrenToStringSecure(result);

		return (result.length() > 0) ? result.toString() : null;
	}

	public void childrenToStringSecure(StringBuilder result) {
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

	public @Nullable Element findChild(Predicate<Element> matcher) {
		if (children != null) {
			for (XMLNodeIfc node : children) {
				if (!(node instanceof Element)) {
					continue;
				}

				Element el = (Element) node;
				if (matcher.test(el)) {
					return el;
				}
			}
		}

		return null;
	}

	public @Nullable Element findChild(String name) {
		return findChild(name(name));
	}

	public @Nullable Element findChild(String name, String xmlns) {
		return findChild(name(name).and(xmlns(xmlns)));
	}

	public @Nullable Element findChildAt(Path path) {
		return path.evaluate(this);
	}

	public List<Element> findChildren(Predicate<Element> matcher) {
		if (children != null) {
			LinkedList<Element> result = new LinkedList<Element>();

			for (XMLNodeIfc node : children) {
				if (!(node instanceof Element)) {
					continue;
				}

				Element el = (Element) node;
				if (matcher.test(el)) {
					result.add(el);
				}
			}

			return result;
		}

		return Collections.emptyList();
	}
	
	public @Nullable List<Element> findChildrenAt(Path path) {
		return path.evaluateAll(this);
	}

	public String getAttribute(String name) {
		if (attributes != null) {
			return attributes.get(name);
		}    // end of if (attributes != null)

		return null;
	}
	
	public @Nullable String getAttributeAt(Path path, String name) {
		Element subchild = findChildAt(path);
		if (subchild == null) {
			return null;
		}
		return subchild.getAttribute(name);
	}

	public Map<String, String> getAttributes() {
		return ((attributes != null) ? Collections.unmodifiableMap(attributes) : Collections.emptyMap());
	}


	public Element setAttributes(Map<String, String> attributes) {
		this.attributes = new HashMap<>(attributes.size());
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			setAttribute(entry.getKey(), entry.getValue());
		}
		return this;
	}

	public Element setAttributes(String[] names, String[] values) {
		attributes = new HashMap<>(names.length);
		for (int i=0; i<names.length; i++) {
			setAttribute(names[i], values[i]);
		}
		return this;
	}

	private void setAttributes(StringBuilder[] names, StringBuilder[] values) {
		attributes = new HashMap<>();
		for (int i=0; i<names.length; i++) {
			if (names[i] != null) {
				setAttribute(names[i].toString(), values[i].toString());
			}
		}
	}

	public String getCData() {
		return cdataToString();
	}
	
	public String getCDataAt(Path path) {
		Element subchild = findChildAt(path);
		if (subchild == null) {
			return null;
		}
		return subchild.getCData();
	}

	public Element setCData(String cdata) {
		children = new ArrayList<>();
		return addChild(new CData(cdata));
	}


	public Element getChild(String name) {
		if (children != null) {
			for (XMLNodeIfc el : children) {
				if (el instanceof Element) {
					Element elem = (Element) el;

					if (elem.getName().equals(name)) {
						return elem;
					}
				}
			}
		}    // end of if (children != null)

		return null;
	}

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


	public Element setChildren(List<XMLNodeIfc> children) {
		this.children = new ArrayList<>(children.size());
		for (XMLNodeIfc child : children) {
			this.children.add(child.clone());
		}    // end of for (Element child: children)
		return this;
	}
	
	public String getName() {
		return this.name;
	}

	private void setName(String argName) {
		this.name = elementNameDeduplicationMap.getOrDefault(argName, argName);
	}

	public String getXMLNS() {
		if (xmlns == null) {
			xmlns = getAttribute(ATTR_XMLNS_KEY);
		}

		return xmlns;
	}

	public void setXMLNS(String ns) {
		if (ns == null) {
			// FIXME: leaving setting `xmlns` to removeAttribute()/setAttribute() methods
			//xmlns = null;
			removeAttribute(ATTR_XMLNS_KEY);
		} else {
			// FIXME: leaving setting `xmlns` to removeAttribute()/setAttribute() methods
			//xmlns = ns;
			setAttribute(ATTR_XMLNS_KEY, xmlns);
		}
	}

	@Override
	public int hashCode() {
		return toStringNoChildren().hashCode();
	}
	
	public <R> R map(Function<Element, ? extends R> mapper) {
		return mapper.apply(this);
	}

	public <R> List<R> compactMapChildren(Function<Element, ? extends R> mapper) {
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

	// FIXME: I'm not sure if this should still be here
	public <R> List<R> flatMapChildren(Function<Element, Collection<? extends R>> mapper) {
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

	public <R> List<R> mapChildren(Function<Element, ? extends R> mapper) {
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

	public boolean matches(Predicate<Element> matcher) {
		return matcher.test(this);
	}

	public void removeAttribute(String key) {
		if (attributes != null) {
			if (ATTR_XMLNS_KEY.equals(key)) {
				xmlns = null;
			}
			attributes.remove(key);
		}    // end of if (attributes == null)
	}

	public boolean removeChild(Element child) {
		boolean res = false;

		if (children != null) {
			res = children.remove(child);
		}    // end of if (children == null)

		return res;
	}

	public boolean removeChild(String name, String xmlns) {
		Element child = findChild(name, xmlns);
		if (child == null) {
			return false;
		}

		return removeChild(child);
	}
	
	public boolean removeChild(Predicate<Element> predicate) {
		Element child = findChild(predicate);
		if (child != null) {
			children.remove(child);
		}
		return child != null;
	}
	
	public boolean hasAttribute(String key) {
		return getAttribute(key) != null;
	}

	public boolean hasAttribute(String key, String value) {
		return Objects.equals(value, getAttribute(key));
	}

	public Element setAttribute(String name, String value) {
		assert value != null;
		if (attributes == null) {
			attributes = new HashMap<>(5);
		}    // end of if (attributes == null)
		String n = attributesDeduplicationMap.getOrDefault(name, name);
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

	@Override
	public String toStringSecure() {
		StringBuilder result = new StringBuilder();
		toStringSecure(result);

		return result.toString();
	}

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

