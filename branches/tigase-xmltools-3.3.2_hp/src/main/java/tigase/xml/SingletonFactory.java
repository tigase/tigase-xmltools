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
 * <code>SingletonFactory</code> provides a way to use only one instance of
 *  <code>SimpleParser</code> in all your code.
 *  Since <code>SimpleParser</code> if fully thread safe implementation there is
 *  no sense to use multiple instances of this class. This in particular useful
 *  when processing a lot of network connections sending <em>XML</em> streams
 *  and using one instance for all connections can save some resources.<br/>
 *  Of course it is still possible to create as many instances of
 *  <code>SimpleParser</code> you like in normal way using public constructor.
 *
 * <p>
 * Created: Sat Oct  2 22:12:21 2004
 * </p>
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */

public class SingletonFactory {

  private static SimpleParser parser = null;

  public static SimpleParser getParserInstance() {
    if (parser == null) {
      parser = new SimpleParser();
    } // end of if (parser == null;)
    return parser;
  }

}// SingletonFactory