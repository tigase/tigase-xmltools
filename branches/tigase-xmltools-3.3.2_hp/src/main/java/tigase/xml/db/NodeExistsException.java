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
 * This exception is thrown when user tries to add subnode with name which
 * already exists in data base. Data base implementation requires that all nodes
 * have unique names thus adding multiple nodes with the same name is not
 * permitted.
 *
 * <p>
 * Created: Thu Nov 11 20:52:34 2004
 * </p>
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public class NodeExistsException extends XMLDBException {

  private static final long serialVersionUID = 1L;

  public NodeExistsException() { super(); }
  public NodeExistsException(String message) { super(message); }
  public NodeExistsException(String message, Throwable cause) {
    super(message, cause);
  }
  public NodeExistsException(Throwable cause) { super(cause); }

} // NodeExistsException