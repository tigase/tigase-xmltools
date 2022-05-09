<p align="center">
    <img src="https://github.com/tigaseinc/website-assets/blob/master/tigase/images/xml-96.png?raw=true"/>
    <br/>
    <img src="https://github.com/tigaseinc/website-assets/blob/master/tigase/images/tigase-logo.png?raw=true" width="25px"/>
    <img src="https://tc.tigase.net/app/rest/builds/buildType:(id:TigaseUtils_Build)/statusIcon" width="100"/>
</p>

(project currently available under our own, self-hosted 1dev: https://forge.tigase.net/projects/25/)

# What it is

Tigase XML Tools are used for fast and low resource XML parsing in [Tigase XMPP Server](https://tigase.net)

# Features

* Marshalling and unmarshalling of XML 
* Creating and modifying Element objects
* Handling of DOM

# Support

When looking for support, please first search for answers to your question in the available online channels:

* Our online documentation: [Tigase Docs](https://docs.tigase.net/tigase-xmltools/4.0.0/Tigase_XML_Tools_Guide/html/)
* Our online forums: [Tigase Forums](https://help.tigase.net/portal/community)
* Our online Knowledge Base [Tigase KB](https://help.tigase.net/portal/kb)

If you didn't find an answer in the resources above, feel free to open a [support ticket](https://help.tigase.net/portal/newticket).

# Downloads

Binaries can be downloaded from our [Maven repository](https://maven-repo.tigase.net/#artifact/tigase/tigase-xmltools)

You can easily add it to your project by including it as dependency:

```xml
<dependency>
  <groupId>tigase</groupId>
  <artifactId>tigase-xmltools</artifactId>
  <version>4.0.0</version>
</dependency>
```

# Using software

Please refer to [javadoc](https://docs.tigase.net/tigase-xmltools/master-snapshot/javadoc/)

# Compilation 

It's a Maven project therefore after cloning the repository you can easily build it with:

```bash
mvn -Pdist clean install
```

# License

<img alt="Tigase Tigase Logo" src="https://github.com/tigase/website-assets/blob/master/tigase/images/tigase-logo.png?raw=true" width="25"/> Official <a href="https://tigase.net/">Tigase</a> repository is available at: https://github.com/tigase/tigase-xmltools/.

Copyright (c) 2004 Tigase, Inc.

Licensed under AGPL License Version 3. Other licensing options available upon request.
