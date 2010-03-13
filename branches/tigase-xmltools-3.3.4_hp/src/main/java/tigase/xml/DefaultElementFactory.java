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


/**
 * <code>DefaultElementFactory</code> is an <code>ElementFactory</code>
 *  implementation creating instances of basic <code>Element</code> class. This
 *  implementation exists to offer complementary implementation of
 *  <em>DOM</em>. It can be used when basic <code>Element</code> class is
 *  sufficient for particular needs.
 * <p>
 * Created: Mon Oct 25 22:08:37 2004
 * </p>
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public class DefaultElementFactory implements ElementFactory {

  /**
   * Creates a new <code>DefaultElementFactory</code> instance.
   *
   */
  public DefaultElementFactory() { }

  // Implementation of tigase.xml.ElementFactory

  public final Element elementInstance(final String name,
		final String cdata,
    final StringBuilder[] attnames, final StringBuilder[] attvals) {
    return new Element(name, cdata, attnames, attvals);
  }

} // DefaultElementFactory