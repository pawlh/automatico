package edu.byu.cs.server;

import edu.byu.cs.util.Serializer;
import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

/**
 * A Serializer class adapter which implements Javalin's JsonMapper interface.
 * Allows Javalin's built-in serialization to use Serializer.
 */
public class SerializerAdapter implements JsonMapper {
    @NotNull
    @Override
    public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
        return Serializer.deserialize(json, targetType);
    }

    @NotNull
    @Override
    public String toJsonString(@NotNull Object obj, @NotNull Type type) {
        return Serializer.serialize(obj, type);
    }
}
