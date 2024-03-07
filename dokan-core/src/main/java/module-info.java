module dokan.java.core {
    requires com.sun.jna;
    requires transitive com.sun.jna.platform;

    exports dev.dokan.core;
    exports dev.dokan.core.constants;
    exports dev.dokan.core.enums;
    exports dev.dokan.core.nativeannotations;
    exports dev.dokan.core.structures;
}
