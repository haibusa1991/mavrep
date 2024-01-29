package com.personal.microart.api.operations.browse;

import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class Content {
    private String name;
    private String uri;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Content content = (Content) o;

        if (!name.equals(content.name)) return false;
        return uri.equals(content.uri);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + uri.hashCode();
        return result;
    }
}
