package com.ecommerce.api.utilities;

import java.util.List;

public class CheckData {

    public static boolean checkIsNotNull(Object o) {
        return switch (o) {
            case null -> false;
            case Long l -> l > 0;
            case String castStringType -> !castStringType.isBlank();
            case List<?> listData -> !listData.isEmpty();
            default -> true;
        };
    }
}
