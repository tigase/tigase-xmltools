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
package tigase.xml.db;

import tigase.xml.Element;
import tigase.xml.ElementFactory;

/**
 * <code>DBElementFactory</code> is implementation of factory design pattern
 * required by <em>XML</em> <em>DOM</em> builder to create proper
 * <code>Element</code> instances for tree nodes.
 *
 * <p>
 * Created: Tue Oct 26 22:41:57 2004
 * </p>
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public class DBElementFactory implements ElementFactory {

  private static DBElementFactory factory =
    new DBElementFactory();

  /**
   * Creates a new <code>DBElementFactory</code> instance.
   *
   */
  private DBElementFactory() { }

  // Implementation of tigase.xml.ElementFactory

  /**
   * Describe <code>elementInstance</code> method here.
   *
   * @param name a <code>String</code> value
   * @param cdata a <code>String</code> value
   * @param attnames a <code>StringBuilder[]</code> value
   * @param attvalues a <code>StringBuilder[]</code> value
   * @return an <code>DBElement</code> value
   */
  public Element elementInstance(final String name, final String cdata,
    final StringBuilder[] attnames, final StringBuilder[] attvalues) {
    return new DBElement(name, cdata, attnames, attvalues);
  }

  public static DBElementFactory getFactory() {
    return factory;
  }

} // DBElementFactory