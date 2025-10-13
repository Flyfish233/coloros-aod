-keep class me.weishu.reflection.** {*;}
-dontwarn java.lang.reflect.AnnotatedType

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static *** throwUninitializedProperty(...);
    public static *** throwUninitializedPropertyAccessException(...);
}