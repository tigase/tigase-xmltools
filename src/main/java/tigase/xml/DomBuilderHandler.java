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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>DomBuilderHandler</code> - implementation of <code>SimpleHandler</code> building <em>DOM</em> strctures during
 * parsing time. It also supports creation multiple, sperate document trees if parsed buffer contains a few <em>XML</em>
 * documents. As a result of work it returns always <code>Queue</code> containing all found <em>XML</em> trees in the
 * same order as they were found in network data.<br> Document trees created by this <em>DOM</em> builder consist of
 * instances of <code>Element</code> class or instances of class extending <code>Element</code> class. To receive trees
 * built with instances of proper class user must provide <code>ElementFactory</code> implementation creating instances
 * of required <code>ELement</code> extension. <p> Created: Sat Oct  2 22:01:34 2004 </p>
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */

public class DomBuilderHandler
		implements SimpleHandler {

	private static ElementFactory defaultFactory = new DefaultElementFactory();
	private static Logger log = Logger.getLogger("tigase.xml.DomBuilderHandler");
	private LinkedList<Element> all_roots = new LinkedList<>();
	private ElementFactory customFactory = null;
	private Stack<Element> el_stack = new Stack<Element>();
	private Map<String, String> namespaces = new TreeMap<>();
	private Object parserState = null;
	private String top_xmlns = null;

	public DomBuilderHandler(ElementFactory factory) {
		customFactory = factory;
	}

	public DomBuilderHandler() {
		customFactory = defaultFactory;
	}

	public Queue<Element> getParsedElements() {
		return all_roots;
	}

	public void error(String errorMessage) {
		log.log(Level.WARNING, "XML content parse error: {0}\n\n===", errorMessage);
	}

	public void startElement(StringBuilder name, StringBuilder[] attr_names, StringBuilder[] attr_values) {
		if (log.isLoggable(Level.FINEST)) {
			log.log(Level.FINEST, "Start element name: " + name + ", Element attributes names: " + Arrays.toString(attr_names) + ", Element attributes values: " + Arrays.toString(attr_values));
		}
		if (attr_names != null) {
			for (int i = 0; i < attr_names.length; ++i) {
				// Exit the loop as soon as we reach end of attributes set
				if (attr_names[i] == null) {
					break;
				}
				if (attr_names[i].toString().startsWith("xmlns:")) {
					namespaces.put(attr_names[i].substring("xmlns:".length(), attr_names[i].length()),
								   attr_values[i].toString());
				}
			}
		}

		String tmp_name = name.toString();
		String new_xmlns = null;
		String prefix = null;
		String tmp_name_prefix = null;
		int idx = tmp_name.indexOf(':');
		if (idx > 0) {
			tmp_name_prefix = tmp_name.substring(0, idx);
		}
		if (tmp_name_prefix != null) {
			for (String pref : namespaces.keySet()) {
				if (tmp_name_prefix.equals(pref)) {
					new_xmlns = namespaces.get(pref);
					tmp_name = tmp_name.substring(pref.length() + 1, tmp_name.length());
					prefix = pref;
				}
			}
		}
		Element elem = newElement(tmp_name, null, attr_names, attr_values);
		String ns = elem.getXMLNS();
		if (new_xmlns != null) {
			elem.setXMLNS(new_xmlns);
			elem.removeAttribute("xmlns:" + prefix);
		}
		el_stack.push(elem);
	}

	public void elementCData(StringBuilder cdata) {
		if (log.isLoggable(Level.FINEST)) {
			log.finest("Element CDATA: " + cdata);
		}
		try {
			el_stack.peek().addCData(cdata.toString());
		} catch (EmptyStackException e) {
			// Do nothing here, it happens sometimes that client sends
			// some white characters after sending open stream data....
		}
	}

	public boolean endElement(StringBuilder name) {
		if (log.isLoggable(Level.FINEST)) {
			log.finest("End element name: " + name);
		}
		String tmp_name = name.toString();
		String tmp_name_prefix = null;
		int idx = tmp_name.indexOf(':');
		if (idx > 0) {
			tmp_name_prefix = tmp_name.substring(0, idx);
		}
		if (tmp_name_prefix != null) {
			for (String pref : namespaces.keySet()) {
				if (tmp_name_prefix.equals(pref)) {
					tmp_name = tmp_name.substring(pref.length() + 1, tmp_name.length());
				}
			}
		}

		if (el_stack.isEmpty()) {
			el_stack.push(newElement(tmp_name, null, null, null));
		}

		Element elem = el_stack.pop();
		if (!elem.matches(ElementFilters.name(tmp_name))) {
			return false;
		}
		if (el_stack.isEmpty()) {
			all_roots.offer(elem);
			if (log.isLoggable(Level.FINEST)) {
				log.finest("Adding new request: " + elem.toString());
			}
		}
		else {
			el_stack.peek().addChild(elem);
		}
		return true;
	}

	public void otherXML(StringBuilder other) {
		if (log.isLoggable(Level.FINEST)) {
			log.finest("Other XML content: " + other);
		}
	}

	public void saveParserState(Object state) {
		parserState = state;
	}

	public Object restoreParserState() {
		return parserState;
	}

	private Element newElement(String name, String cdata, StringBuilder[] attnames, StringBuilder[] attvals) {
		return customFactory.elementInstance(name, cdata, attnames, attvals);
	}
}
