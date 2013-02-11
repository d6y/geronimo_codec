Unexpected behaviour with commons-codec, Scala 2.10 and Geronimo
================================================================

This is an SBT project that builds a WAR file which includes a filter
to sets a response header.

The project references commons-codec (1.6) base64.

Problem
=======

When built with Scala 2.10, the WAR file does not deploy into Geronimo. It gives:

	2013-02-11 21:39:20,542 ERROR [[/hello]] Exception starting filter MyFilter
	java.lang.NoClassDefFoundError: org/apache/commons/codec/binary/BaseNCodec

If you either...

* remove the base64 `val ignore` and `import` from  `src/main/scala/filter.scala`

or

* build with Scala 2.9

...the WAR file will deploy without problem.

The expectation is that the WAR file would work with either version of Scala.

Environment
===========

* Geronimo-tomcat7-javaee6-3.0.0

* Mac OS 10.8.2

* java version "1.7.0_13", Java(TM) SE Runtime Environment (build 1.7.0_13-b20), Java HotSpot(TM) 64-Bit Server VM (build 23.7-b01, mixed mode)

Steps to reproduce
==================

This project is configured to build with Scala 2.10 by default.

Failure with Scala 2.10
-----------------------

First, build the project:

    $ ./sbt clean package

This will produce the file `geronimo_codec/target/scala-2.10/simple_2.10-0.0.1.war`.

Start Geronimo:

    $ export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_13.jdk/Contents/Home
    $ export JAVA_OPTS="-XX:MaxPermSize=512m"

Configure `var/config/config-substitutions.properties` to replace 0.0.0. with 127.0.0.1 if required (I had to).

    bin$ ./geronimo run

Now `deploy` (or `redeploy`) the WAR file:

	bin$ ./deploy --user system --password manager deploy /Users/richard/tmp/weird/geronimo_codec/target/scala-2.10/simple_2.10-0.0.1.war
	Using GERONIMO_HOME:   /Users/richard/tmp/weird/geronimo-tomcat7-javaee6-3.0.0
	Using GERONIMO_SERVER: /Users/richard/tmp/weird/geronimo-tomcat7-javaee6-3.0.0
	Using GERONIMO_TMPDIR: /Users/richard/tmp/weird/geronimo-tomcat7-javaee6-3.0.0/var/temp
	Using JRE_HOME:        /Library/Java/JavaVirtualMachines/jdk1.7.0_13.jdk/Contents/Home/jre
	    No ModuleID or TargetModuleID provided.  Attempting to guess based
	    on the content of the archive.
	    Attempting to use ModuleID 'my.app/app1/1.0/war'
	    Stopped my.app/app1/1.0/war
	    Unloaded my.app/app1/1.0/war
	    Uninstalled my.app/app1/1.0/war
	    Deployed my.app/app1/1.0/war
	    Redeployed my.app/app1/1.0/war
	2013-02-11 21:39:20,881 ERROR [DeployTool] Error:
	org.apache.geronimo.common.DeploymentException: Operation failed: start of my.app/app1/1.0/war failed


In the Geronimo terminal the cause is:

	2013-02-11 21:39:20,542 ERROR [[/hello]] Exception starting filter MyFilter
	java.lang.NoClassDefFoundError: org/apache/commons/codec/binary/BaseNCodec

This user would expect the WAR to deploy without error.


Success with Scala 2.9
----------------------

Modify `geronimo_codec/build.sbt` and replace...

	scalaVersion := "2.10.0"

	//scalaVersion := "2.9.1"

...with:

	//scalaVersion := "2.10.0"

	scalaVersion := "2.9.1"

Save the file and run:

    $ ./sbt clean package

This will produce the file `geronimo_codec/target/scala-2.9.1/simple_2.9.1-0.0.1.war`.

Assuming Geronimo is still running, redeploy:

	bin$ ./deploy --user system --password manager redeploy /Users/richard/tmp/weird/geronimo_codec/target/scala-2.9.1/simple_2.9.1-0.0.1.war
	Using GERONIMO_HOME:   /Users/richard/tmp/weird/geronimo-tomcat7-javaee6-3.0.0
	Using GERONIMO_SERVER: /Users/richard/tmp/weird/geronimo-tomcat7-javaee6-3.0.0
	Using GERONIMO_TMPDIR: /Users/richard/tmp/weird/geronimo-tomcat7-javaee6-3.0.0/var/temp
	Using JRE_HOME:        /Library/Java/JavaVirtualMachines/jdk1.7.0_13.jdk/Contents/Home/jre
	    No ModuleID or TargetModuleID provided.  Attempting to guess based
	    on the content of the archive.
	    Attempting to use ModuleID 'my.app/app1/1.0/war'
	    Stopped my.app/app1/1.0/war
	    Unloaded my.app/app1/1.0/war
	    Uninstalled my.app/app1/1.0/war
	    Deployed my.app/app1/1.0/war
	    Started my.app/app1/1.0/war
	    Redeployed my.app/app1/1.0/war

Observe that the WAR file has deployed as expected. Test:

	bin$ curl -v http://127.0.0.1:8080/hello/
	* About to connect() to 127.0.0.1 port 8080 (#0)
	*   Trying 127.0.0.1...
	* connected
	* Connected to 127.0.0.1 (127.0.0.1) port 8080 (#0)
	> GET /hello/ HTTP/1.1
	> User-Agent: curl/7.24.0 (x86_64-apple-darwin12.0) libcurl/7.24.0 OpenSSL/0.9.8r zlib/1.2.5
	> Host: 127.0.0.1:8080
	> Accept: */*
	>
	< HTTP/1.1 200 OK
	< Server: Apache-Coyote/1.1
	< X-Seen: 1
	< Accept-Ranges: bytes
	< ETag: W/"89-1360619083000"
	< Last-Modified: Mon, 11 Feb 2013 21:44:43 GMT
	< Content-Type: text/html
	< Content-Length: 89
	< Date: Mon, 11 Feb 2013 21:45:14 GMT
	<
	<html>

	<head>
		<title>Hi!</title>
	</head>

	<h1>Hi! Really great to see you</h1>

	* Connection #0 to host 127.0.0.1 left intact
	</html>* Closing connection #0

