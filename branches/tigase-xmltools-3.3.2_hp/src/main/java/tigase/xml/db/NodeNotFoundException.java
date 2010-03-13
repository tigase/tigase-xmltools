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
 * Exception is thrown when user tries to access non-existen node on 1st level.
 * All subnodes on lower higher levels are automatically created when required
 * apart from nodes on 1st level. Nodes on 1st level have special maining. They
 * act in similar way as tables in relational data bases.
 *
 * <p>
 * Created: Thu Nov 11 20:51:20 2004
 * </p>
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public class NodeNotFoundException extends XMLDBException {

  private static final long serialVersionUID = 1L;

  public NodeNotFoundException() { super(); }
  public NodeNotFoundException(String message) { super(message); }
  public NodeNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
  public NodeNotFoundException(Throwable cause) { super(cause); }

} // NodeNotFoundException