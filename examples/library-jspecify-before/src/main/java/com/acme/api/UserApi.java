package com.acme.api;

import org.jetbrains.annotations.Nullable;
import org.springframework.lang.NonNull;

public class UserApi {

    @Nullable
    public String nickname() {
        return null;
    }

    @NonNull
    public String name() {
        return "";
    }

    public String greet(@Nullable String prefix) {
        return prefix == null ? "" : prefix;
    }
}
