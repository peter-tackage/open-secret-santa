Open Secret Santa is an Android app to help you manage "Secret Santa" or "Kris Kringle" style gift exchanges.

It is built using Gradle with a few dependencies that can't be accessed from Maven Central.

Follow these steps to get hold of them -

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



