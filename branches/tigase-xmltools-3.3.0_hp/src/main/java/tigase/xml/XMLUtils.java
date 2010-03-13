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

import java.util.Queue;
import java.io.FileReader;

/**
 * Describe class XMLUtil here.
 *
 *
 * Created: Tue Jan 23 20:59:30 2007
 *
 * @author <a href="mailto:artur.hefczyc@gmail.com">Artur Hefczyc</a>
 * @version $Rev$
 */
public abstract class XMLUtils {

	private static final String[] decoded = {"&", "<", ">"};
	private static final String[] encoded = {"&amp;", "&lt;", "&gt;"};

	public static String translateAll(String input,
		String[] patterns, String[] replacements) {
		String result = input;
		for (int i = 0; i < patterns.length; i++) {
			result = result.replaceAll(patterns[i], replacements[i]);
		}
		return result;
	}

	public static String escape(String input) {
		return translateAll(input, decoded, encoded);
	}

	public static String unescape(String input) {
		return translateAll(input, encoded, decoded);
	}

  public static void main(final String[] args) throws Exception {

    if (args.length < 1) {
      System.err.println("You must give file name as parameter.");
      System.exit(1);
    } // end of if (args.length < 1)

    FileReader file = new FileReader(args[0]);
    char[] buff = new char[16*1024];
    SimpleParser parser = new SimpleParser();
    DomBuilderHandler dombuilder = new DomBuilderHandler();
    int result = -1;
    while((result = file.read(buff)) != -1) {
      parser.parse(dombuilder, buff, 0, result);
    }
    file.close();
		Queue<Element> results = dombuilder.getParsedElements();
		for (Element elem: results) {
			System.out.println(elem.toString());
		}
  }

}