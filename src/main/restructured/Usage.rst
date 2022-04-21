Usage
======

Parsing XML
--------------

.. code:: java

   import tigase.xml.*;

   DomBuilderHandler domHandler = new DomBuilderHandler();
   SimpleParser parser = SingletonFactory.getParserInstance();

   // array of chars to parse
   char[] data = "<test/>".toCharArray();

   // parsing data using parser and handler
   parser.parse(handler, data, 0, data.length);

   // check if there was no pasing errors
   if (domHandler.parseError()) {
       // do something if XML parsing fails, ie. due to invalid characters in the input array..
   }

   // retrieve queue of parsed elements (root elements)
   Queue<Element> elems = domHandler.getParsedElements();

   // for each parsed element print it
   Element elem = null;
   while ((elem = elems.poll()) != null) {
       System.out.println("parsed element = " + elem);
   }

Creating elements tree
-------------------------

Creating ``message`` element with ``body`` inner element. Body element will contain a value ``Test``.

**Example.**

.. code:: java

   import tigase.xml.*;

   Element messageElem = new Element("message");
   Element bodyElem = new Element("body");
   bodyElem.setCData("Test");
   messageElem.addChild(bodyElem);

   System.out.println(messageElem.toString());

**Result.**

.. code:: xml

   <message><body>Test</body></message>

Modifying elements
--------------------

In ``messageElem`` variable we have a message element created in a previous example. Now we will set ``message`` attribute ``id`` to ``1``, remove ``body`` inner element and add new element ``test``.

**Example.**

.. code:: java

   import tigase.xml.*;

   messageElem.setAttributeStaticStr("id", "1");

   Element bodyElem = messageElem.getChildStaticStr("body");
   if (bodyElem != null) {
       messageElem.removeChild(bodyElem);
   }

   Element testElem = new Element("test");
   messageElem.addChild(testElem);

   System.out.println(messageElem.toString());

**Result.**

.. code:: xml

   <message id="1"><test/></message>

Serializing to XML
-----------------------

To serialize an element and its subelements to ``String`` you need to call its ``toString()`` method which will return serialized element.
