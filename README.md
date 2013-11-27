Open Secret Santa is an Android app to help you manage "Secret Santa" or "Kris Kringle" style gift exchanges.

I'm in the process of completely rewriting including a redesign to simplify the screens.

Maven
-----

Set up a settings.xml file for build releases.

Android SDK
-----------

Download as per usual. You only need platforms ICS+ and the v13 support library.

> more /etc/profile.d/android.sh

> export ANDROID_HOME= ~/Development/android/android-sdk-linux/

> export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/build-tools/17.0.0/:$ANDROID_HOME/platform-tools

Java 7
---------------------------

For release builds only

> more /etc/profile.d/java7.sh

> export JAVA_HOME=/usr/lib/jvm/jdk1.7.0

> export PATH=$PATH:$JAVA_HOME/bin

Android Maven Artifacts
-----------------------

Use the maven-android-sdk-deployer: https://github.com/mosabua/maven-android-sdk-deployer

> cd maven-android-sdk-deployer

> mvn install -P 4.3

> mvn install -Dextras.compatibility.v13.groupid=com.google.android -Dextras.compatibility.v13.artifactid=support-v13 -Dextras.compatibility.v13.version.prefix=r -P 4.0

The -P switch is required because the maven-android-sdk-deployer will otherwise try
all platform (many of which are unnecessary) - even though it's only a single
platform-less support library.

Draw Engine
-----------

https://github.com/peter-tackage/draw-engine

Install the Draw Engine artifacts:

> cd draw-engine

> mvn clean install

TODO I should make this a submodule or host them as a repo.

JavaMail for Android
--------------------

http://code.google.com/p/javamail-android/

Install the artifacts using the nominal version of v1.0.0:

> mvn install:install-file -Dfile=mail.jar -DgroupId=javamail-android -DartifactId=mail -Dversion=1.0.0 -Dpackaging=jar

> mvn install:install-file -Dfile=additionnal.jar -DgroupId=javamail-android -DartifactId=additionnal -Dversion=1.0.0 -Dpackaging=jar

> mvn install:install-file -Dfile=activation.jar -DgroupId=javamail-android -DartifactId=activation -Dversion=1.0.0 -Dpackaging=jar



