/*
 * Tigase XMPP/Jabber XML Tools
 * Copyright (C) 2004-2010 "Artur Hefczyc" <artur.hefczyc@tigase.org>
 *
 * This program is licensed to HP, under Licensor's Intellectual Property Rights,
 * a paid-up, worldwide, non-exclusive, non-transferable, perpetual license in the
 * Source Code to use, make, reproduce (for archival and back-up purposes only),
 * and prepare derivative works.
 *
 * The right to prepare derivative works includes translations, localizations,
 * adaptations, and compilations. HP shall own all right, title, and interest in
 * any derivative works prepared by HP.
 *
 * HP shall use its standard forms in distributing Object Code.
 *
 * Licensor retains all right, title, and interest in the Licensed Software and
 * Documentation.
 *
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */

package tigase.xml;

import tigase.annotations.TODO;

/**
 * <code>SimpleHandler</code> - parser handler interface for event driven
 *  parser. It is very simplified version of
 *  <code>org.xml.sax.ContentHandler</code> interface created for
 *  <code>SimpleParser</code> needs. It allows to receive events like start
 *  element (with element attributes), end element, element cdata, other XML
 *  content and error event if XML error found.
 *
 * <p>
 * Created: Sat Oct  2 00:00:08 2004
 * </p>
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 * @see SimpleParser
 */

public interface SimpleHandler {

  void error(String errorMessage);

  void startElement(StringBuilder name,
    StringBuilder[] attr_names, StringBuilder[] attr_values);

  void elementCData(StringBuilder cdata);

  void endElement(StringBuilder name);

  void otherXML(StringBuilder other);

  @TODO(note="Use generic types to store parser data.")
  void saveParserState(Object state);

  @TODO(note="Use generic types to store parser data.")
  Object restoreParserState();

}// SimpleHandler