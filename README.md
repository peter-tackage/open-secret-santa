This was the first Android app that I wrote.

I'm in the process of completely rewriting it now, along with a redesign to simplify the screens.

Instructions
------------

You will need a few things to get going.

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

Draw Engine
-----------

https://github.com/peter-tackage/draw-engine

TODO I should make this a submodule or host them as a repo.

JavaMail for Android
--------------------

http://code.google.com/p/javamail-android/

I've manually installed these JARs and nominally called them v1.0.0

Android Support Library
-----------------------

This install uses the maven-android-sdk-deployer: https://github.com/mosabua/maven-android-sdk-deployer

> mvn install -Dextras.compatibility.v13.groupid=com.google.android \
>           -Dextras.compatibility.v13.artifactid=support-v13 \
>           -Dextras.compatibility.v13.version.prefix=r -P 4.0

The -P switch is required because the maven-android-sdk-deployer will otherwise try
all platform (many of which are unnecessary) - even though it's only a single
platform-less support library.

