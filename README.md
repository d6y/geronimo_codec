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

Note: the WAR does deploy and run under a plain Tomcat 7 server.



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
	
(See the end of this file for a full stack trace)

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
	
Additional findings
====================

Tomcat 7
---------

The Scala 2.10 project does deploy and run under Tomcat 7:

	$ tar zxf ~/Downloads/apache-tomcat-7.0.35.tar.gz 
	$ cp geronimo_codec/target/scala-2.10/simple_2.10-0.0.1.war apache-tomcat-7.0.35/webapps/
	$ cd apache-tomcat-7.0.35/bin
	bin$ chmod a+rx *
	bin$ ./catalina.sh run
	Using CATALINA_BASE:   /Users/richard/tmp/weird/apache-tomcat-7.0.35
	Using CATALINA_HOME:   /Users/richard/tmp/weird/apache-tomcat-7.0.35
	Using CATALINA_TMPDIR: /Users/richard/tmp/weird/apache-tomcat-7.0.35/temp
	Using JRE_HOME:        /Library/Java/JavaVirtualMachines/jdk1.7.0_13.jdk/Contents/Home
	Using CLASSPATH:       /Users/richard/tmp/weird/apache-tomcat-7.0.35/bin/bootstrap.jar:/Users/richard/tmp/weird/apache-tomcat-7.0.35/bin/tomcat-juli.jar
	Feb 12, 2013 9:34:29 AM org.apache.catalina.core.AprLifecycleListener init
	INFO: The APR based Apache Tomcat Native library which allows optimal performance in production environments was not found on the java.library.path: /Users/richard/Library/Java/Extensions:/Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java:.
	Feb 12, 2013 9:34:29 AM org.apache.coyote.AbstractProtocol init
	INFO: Initializing ProtocolHandler ["http-bio-8080"]
	Feb 12, 2013 9:34:29 AM org.apache.coyote.AbstractProtocol init
	INFO: Initializing ProtocolHandler ["ajp-bio-8009"]
	Feb 12, 2013 9:34:29 AM org.apache.catalina.startup.Catalina load
	INFO: Initialization processed in 485 ms
	Feb 12, 2013 9:34:29 AM org.apache.catalina.core.StandardService startInternal
	INFO: Starting service Catalina
	Feb 12, 2013 9:34:29 AM org.apache.catalina.core.StandardEngine startInternal
	INFO: Starting Servlet Engine: Apache Tomcat/7.0.35
	Feb 12, 2013 9:34:29 AM org.apache.catalina.startup.HostConfig deployWAR
	INFO: Deploying web application archive /Users/richard/tmp/weird/apache-tomcat-7.0.35/webapps/lift-2.5-starter-template_2.10-0.0.1.war
	Feb 12, 2013 9:34:32 AM org.apache.catalina.startup.HostConfig deployWAR
	INFO: Deploying web application archive /Users/richard/tmp/weird/apache-tomcat-7.0.35/webapps/simple_2.10-0.0.1.war
	Feb 12, 2013 9:34:33 AM org.apache.catalina.startup.HostConfig deployDirectory
	INFO: Deploying web application directory /Users/richard/tmp/weird/apache-tomcat-7.0.35/webapps/docs
	Feb 12, 2013 9:34:33 AM org.apache.catalina.startup.HostConfig deployDirectory
	INFO: Deploying web application directory /Users/richard/tmp/weird/apache-tomcat-7.0.35/webapps/examples
	Feb 12, 2013 9:34:33 AM org.apache.catalina.startup.HostConfig deployDirectory
	INFO: Deploying web application directory /Users/richard/tmp/weird/apache-tomcat-7.0.35/webapps/host-manager
	Feb 12, 2013 9:34:33 AM org.apache.catalina.startup.HostConfig deployDirectory
	INFO: Deploying web application directory /Users/richard/tmp/weird/apache-tomcat-7.0.35/webapps/manager
	Feb 12, 2013 9:34:33 AM org.apache.catalina.startup.HostConfig deployDirectory
	INFO: Deploying web application directory /Users/richard/tmp/weird/apache-tomcat-7.0.35/webapps/ROOT
	Feb 12, 2013 9:34:33 AM org.apache.coyote.AbstractProtocol start
	INFO: Starting ProtocolHandler ["http-bio-8080"]
	Feb 12, 2013 9:34:33 AM org.apache.coyote.AbstractProtocol start
	INFO: Starting ProtocolHandler ["ajp-bio-8009"]
	Feb 12, 2013 9:34:33 AM org.apache.catalina.startup.Catalina start
	INFO: Server startup in 4277 ms


In a separate terminal:

	~$ curl -v http://127.0.0.1:8080/simple_2.10-0.0.1/
	* About to connect() to 127.0.0.1 port 8080 (#0)
	*   Trying 127.0.0.1...
	* connected
	* Connected to 127.0.0.1 (127.0.0.1) port 8080 (#0)
	> GET /simple_2.10-0.0.1/ HTTP/1.1
	> User-Agent: curl/7.24.0 (x86_64-apple-darwin12.0) libcurl/7.24.0 OpenSSL/0.9.8r zlib/1.2.5
	> Host: 127.0.0.1:8080
	> Accept: */*
	> 
	< HTTP/1.1 200 OK
	< Server: Apache-Coyote/1.1
	< X-Seen: 1
	< Accept-Ranges: bytes
	< ETag: W/"89-1360661598000"
	< Last-Modified: Tue, 12 Feb 2013 09:33:18 GMT
	< Content-Type: text/html
	< Content-Length: 89
	< Date: Tue, 12 Feb 2013 09:35:00 GMT
	< 
	<html>
	
	<head>
		<title>Hi!</title>
	</head>
	
	<h1>Hi! Really great to see you</h1>
	
	* Connection #0 to host 127.0.0.1 left intact
	</html>* Closing connection #0
	~$ 
	
	
WAR file contents
-----------------

	geronimo_codec (master)$ jar tf target/scala-2.10/simple_2.10-0.0.1.war
	META-INF/MANIFEST.MF
	WEB-INF/
	WEB-INF/classes/
	WEB-INF/classes/org/
	WEB-INF/classes/org/example/
	WEB-INF/lib/
	WEB-INF/web.xml
	WEB-INF/geronimo-web.xml
	WEB-INF/lib/scala-library.jar
	WEB-INF/lib/commons-codec-1.6.jar
	index.html
	WEB-INF/classes/org/example/MyFilter.class
	

Scala 2.10.1-RC1
----------------

Same error with Scala 2.10.1-RC1 as with Scala 2.10.0:

	2013-02-12 09:40:17,256 ERROR [[/hello]] Exception starting filter MyFilter
	java.lang.NoClassDefFoundError: org/apache/commons/codec/binary/BaseNCodec
		at java.lang.Class.forName0(Native Method)
		at java.lang.Class.forName(Class.java:266)
		at org.apache.xbean.recipe.RecipeHelper.loadClass(RecipeHelper.java:52)
		at org.apache.xbean.recipe.ObjectRecipe.getType(ObjectRecipe.java:353)
		at org.apache.xbean.recipe.ObjectRecipe.internalCreate(ObjectRecipe.java:266)
		at org.apache.xbean.recipe.AbstractRecipe.create(AbstractRecipe.java:96)
		at org.apache.xbean.recipe.AbstractRecipe.create(AbstractRecipe.java:61)
		at org.apache.geronimo.j2ee.annotation.Holder.newInstance(Holder.java:180)
		at org.apache.geronimo.tomcat.TomcatInstanceManager.newInstance(TomcatInstanceManager.java:64)
		at org.apache.catalina.core.ApplicationFilterConfig.getFilter(ApplicationFilterConfig.java:256)
		at org.apache.catalina.core.ApplicationFilterConfig.setFilterDef(ApplicationFilterConfig.java:382)
		at org.apache.catalina.core.ApplicationFilterConfig.<init>(ApplicationFilterConfig.java:103)
		at org.apache.catalina.core.StandardContext.filterStart(StandardContext.java:4638)
		at org.apache.catalina.core.StandardContext.startInternal(StandardContext.java:5294)
		at org.apache.geronimo.tomcat.GeronimoStandardContext.access$201(GeronimoStandardContext.java:121)
		at org.apache.geronimo.tomcat.GeronimoStandardContext$SystemMethodValve.invoke(GeronimoStandardContext.java:722)
		at org.apache.geronimo.tomcat.valve.GeronimoBeforeAfterValve.invoke(GeronimoBeforeAfterValve.java:48)
		at org.apache.geronimo.tomcat.valve.ProtectedTargetValve.invoke(ProtectedTargetValve.java:53)
		at org.apache.geronimo.tomcat.GeronimoStandardContext.startInternal(GeronimoStandardContext.java:459)
		at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:150)
		at org.apache.catalina.core.ContainerBase.addChildInternal(ContainerBase.java:895)
		at org.apache.catalina.core.ContainerBase.addChild(ContainerBase.java:871)
		at org.apache.catalina.core.StandardHost.addChild(StandardHost.java:615)
		at org.apache.geronimo.tomcat.TomcatContainer.addContext(TomcatContainer.java:310)
		at org.apache.geronimo.tomcat.TomcatWebAppContext.doStart(TomcatWebAppContext.java:567)
		at org.apache.geronimo.gbean.runtime.GBeanInstance.createInstance(GBeanInstance.java:1000)
		at org.apache.geronimo.gbean.runtime.GBeanInstanceState.attemptFullStart(GBeanInstanceState.java:271)
		at org.apache.geronimo.gbean.runtime.GBeanInstanceState.start(GBeanInstanceState.java:105)
		at org.apache.geronimo.gbean.runtime.GBeanInstance.start(GBeanInstance.java:555)
		at org.apache.geronimo.gbean.runtime.GBeanDependency.attemptFullStart(GBeanDependency.java:110)
		at org.apache.geronimo.gbean.runtime.GBeanDependency.addTarget(GBeanDependency.java:145)
		at org.apache.geronimo.gbean.runtime.GBeanDependency$1.running(GBeanDependency.java:119)
		at org.apache.geronimo.kernel.basic.BasicLifecycleMonitor.fireRunningEvent(BasicLifecycleMonitor.java:176)
		at org.apache.geronimo.kernel.basic.BasicLifecycleMonitor.access$300(BasicLifecycleMonitor.java:45)
		at org.apache.geronimo.kernel.basic.BasicLifecycleMonitor$RawLifecycleBroadcaster.fireRunningEvent(BasicLifecycleMonitor.java:254)
		at org.apache.geronimo.gbean.runtime.GBeanInstanceState.attemptFullStart(GBeanInstanceState.java:301)
		at org.apache.geronimo.gbean.runtime.GBeanInstanceState.start(GBeanInstanceState.java:105)
		at org.apache.geronimo.gbean.runtime.GBeanInstanceState.startRecursive(GBeanInstanceState.java:127)
		at org.apache.geronimo.gbean.runtime.GBeanInstance.startRecursive(GBeanInstance.java:569)
		at org.apache.geronimo.kernel.basic.BasicKernel.startRecursiveGBean(BasicKernel.java:386)
		at org.apache.geronimo.kernel.config.ConfigurationUtil.startConfigurationGBeans(ConfigurationUtil.java:466)
		at org.apache.geronimo.kernel.config.KernelConfigurationManager.start(KernelConfigurationManager.java:225)
		at org.apache.geronimo.kernel.config.SimpleConfigurationManager.startConfiguration(SimpleConfigurationManager.java:710)
		at org.apache.geronimo.kernel.config.SimpleConfigurationManager.startConfiguration(SimpleConfigurationManager.java:689)
		at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
		at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
		at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
		at java.lang.reflect.Method.invoke(Method.java:601)
		at org.apache.geronimo.gbean.runtime.ReflectionMethodInvoker.invoke(ReflectionMethodInvoker.java:34)
		at org.apache.geronimo.gbean.runtime.GBeanOperation.invoke(GBeanOperation.java:131)
		at org.apache.geronimo.gbean.runtime.GBeanInstance.invoke(GBeanInstance.java:883)
		at org.apache.geronimo.kernel.basic.BasicKernel.invoke(BasicKernel.java:245)
		at org.apache.geronimo.kernel.KernelGBean.invoke(KernelGBean.java:344)
		at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
		at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
		at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
		at java.lang.reflect.Method.invoke(Method.java:601)
		at org.apache.geronimo.gbean.runtime.ReflectionMethodInvoker.invoke(ReflectionMethodInvoker.java:34)
		at org.apache.geronimo.gbean.runtime.GBeanOperation.invoke(GBeanOperation.java:131)
		at org.apache.geronimo.gbean.runtime.GBeanInstance.invoke(GBeanInstance.java:883)
		at org.apache.geronimo.kernel.basic.BasicKernel.invoke(BasicKernel.java:245)
		at org.apache.geronimo.system.jmx.MBeanGBeanBridge.invoke(MBeanGBeanBridge.java:172)
		at com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.invoke(DefaultMBeanServerInterceptor.java:819)
		at com.sun.jmx.mbeanserver.JmxMBeanServer.invoke(JmxMBeanServer.java:791)
		at com.sun.jmx.remote.security.MBeanServerAccessController.invoke(MBeanServerAccessController.java:468)
		at javax.management.remote.rmi.RMIConnectionImpl.doOperation(RMIConnectionImpl.java:1486)
		at javax.management.remote.rmi.RMIConnectionImpl.access$300(RMIConnectionImpl.java:96)
		at javax.management.remote.rmi.RMIConnectionImpl$PrivilegedOperation.run(RMIConnectionImpl.java:1327)
		at java.security.AccessController.doPrivileged(Native Method)
		at javax.management.remote.rmi.RMIConnectionImpl.doPrivilegedOperation(RMIConnectionImpl.java:1426)
		at javax.management.remote.rmi.RMIConnectionImpl.invoke(RMIConnectionImpl.java:847)
		at sun.reflect.GeneratedMethodAccessor70.invoke(Unknown Source)
		at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
		at java.lang.reflect.Method.invoke(Method.java:601)
		at sun.rmi.server.UnicastServerRef.dispatch(UnicastServerRef.java:322)
		at sun.rmi.transport.Transport$1.run(Transport.java:177)
		at sun.rmi.transport.Transport$1.run(Transport.java:174)
		at java.security.AccessController.doPrivileged(Native Method)
		at sun.rmi.transport.Transport.serviceCall(Transport.java:173)
		at sun.rmi.transport.tcp.TCPTransport.handleMessages(TCPTransport.java:553)
		at sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.run0(TCPTransport.java:808)
		at sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.run(TCPTransport.java:667)
		at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
		at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
		at java.lang.Thread.run(Thread.java:722)
	Caused by: java.lang.ClassNotFoundException: org.apache.commons.codec.binary.BaseNCodec
		at org.eclipse.osgi.internal.loader.BundleLoader.findClassInternal(BundleLoader.java:467)
		at org.eclipse.osgi.internal.loader.BundleLoader.findClass(BundleLoader.java:429)
		at org.eclipse.osgi.internal.loader.BundleLoader.findClass(BundleLoader.java:417)
		at org.apache.geronimo.hook.equinox.GeronimoClassLoader.loadClass(GeronimoClassLoader.java:85)
		at java.lang.ClassLoader.loadClass(ClassLoader.java:356)
		... 85 more
