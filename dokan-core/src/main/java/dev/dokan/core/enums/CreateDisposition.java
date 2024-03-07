package dev.dokan.core.enums;

import static dev.dokan.core.constants.CreateDispositions.*;

public enum CreateDisposition {

    SUPERSEDE(FILE_SUPERSEDE),
    OPEN(FILE_OPEN),
    CREATE(FILE_CREATE),
    OPEN_IF(FILE_OPEN_IF),
    OVERWRITE(FILE_OVERWRITE),
    OVERWRITE_IF(FILE_OVERWRITE_IF);

    private final int value;

    CreateDisposition(int value) {
        this.value = value;
    }

    public static CreateDisposition of(int value) {
        for(var createDisposition : CreateDisposition.values()) {
            if (createDisposition.value == value) {
                return createDisposition;
            }
        }
        throw new IllegalArgumentException("Unknown CreateDisposition value");
    }

    public int getValue() {
        return value;
    }
}
