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

/**
 * This is parrent exception for all data base related exceptions. It is not
 * directly thrown. They are a few descendants implementations which are thrown
 * in some particular cases.
 * <p>
 * Created: Thu Nov 11 20:49:08 2004
 * </p>
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public class XMLDBException extends Exception {

  private static final long serialVersionUID = 1L;

  public XMLDBException() { super(); }
  public XMLDBException(String message) { super(message); }
  public XMLDBException(String message, Throwable cause) {
    super(message, cause);
  }
  public XMLDBException(Throwable cause) { super(cause); }

} // XMLDBException