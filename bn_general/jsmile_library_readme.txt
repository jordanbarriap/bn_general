Use jsmile on local MAC
1. web application/with apache tomcat: put libjsmile.jnilib under JRE_HOME/libraray (/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Libraries), within Eclipse configure apache tomcat (7.0) to use the corresponding JRE.
	-- I included the library smile wrapper source code (it seems to be necessary). Later I could try to compile a jar for it.
2. not web application/without apache tomcat: add the directory with libjsmile.jnilib as the directory of the native library directory of JRE System library under the properties (although what I did is to create a /lib under the project and put libjsmile.jnilib under it and then add this directory)
	-- Used Java JRE library 1.6, jdk 1.6 for compatibility with older version of this jsmile library (Nov. 24, 2014)
	-- Another way which works (not recommended): put libjsmile.jnilib into the /System/Library/Java/Extensions 

	
On pawscomp2 server:
-- I put libjsmile.so (latest version Nov. 1, 2017) under /usr/share/java/tomcat which is the same as /usr/share/tomcat/lib
-- I added CATALINA_OPTS="-Djava.library.path=/usr/share/tomcat/lib" in tomcat.conf (/usr/share/tomcat/)