# Dagger and Javawriter (http://stackoverflow.com/questions/18102084/dagger-cannot-create-object-graph-although-it-can-produce-dot-file/18177491#18177491)
-dontwarn dagger.internal.codegen.**
-dontwarn com.squareup.javawriter.**
-keepclassmembers class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}

-keep class **$$ModuleAdapter
-keep class **$$InjectAdapter
-keep class **$$StaticInjection

-keepnames !abstract class coffee.*
-keepnames class dagger.Lazy

# Guava warnings
-dontwarn javax.annotation.**
-dontwarn sun.misc.Unsafe

# Guava Warning: com.google.common.util.concurrent.ServiceManager: can't find referenced class javax.inject.Inject
-dontwarn javax.inject.Inject
-dontwarn javax.inject.Singleton