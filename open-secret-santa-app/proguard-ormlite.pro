# ORMLite uses reflection  http://sourceforge.net/p/proguard/discussion/182456/thread/6765bb69
# Also http://stackoverflow.com/questions/12729375/proguard-with-orrmlite-parameterized-collection
-keep class com.j256.** {
   *;
}
-keep enum com.j256.** {
   *;
}
-keep interface com.j256.** {
   *;
}
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-keepclassmembers class * {
  public <init>(android.content.​Context);
}

# Specific rule for Open Secret Santa to keep the model classes
-keep class com.moac.android.opensecretsanta.model.** {
   *;
}