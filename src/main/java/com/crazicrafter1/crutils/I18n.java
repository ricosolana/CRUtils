package com.crazicrafter1.crutils;

import java.util.ResourceBundle;

// Internalization
@Deprecated
public class I18n {

    // language.properties do get loaded from classpath
    // https://www.tutorialspoint.com/java/util/resourcebundle_getbundle.htm
    // https://github.com/EssentialsX/Essentials/blob/2.x/Essentials/src/main/java/com/earth2me/essentials/I18n.java
    private ResourceBundle bundle;

    public I18n(ResourceBundle bundle) {
        this.bundle = bundle;
        this.bundle = ResourceBundle.getBundle("");
    }
}
