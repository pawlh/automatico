package edu.byu.cs.util;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import edu.byu.cs.canvas.model.CanvasRubricAssessment;
import edu.byu.cs.canvas.model.CanvasRubricItem;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class Serializer {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
            .registerTypeAdapter(CanvasRubricAssessment.class, new RubricAssessmentAdapter())
            .create();

    public static String serialize(Object obj) {
        try {
            return GSON.toJson(obj);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    public static String serialize(Object obj, Type type) {
        try {
            return GSON.toJson(obj, type);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    public static <T> T deserialize(JsonElement jsonElement, Class<T> classOfT) {
        return deserialize(jsonElement.toString(), classOfT);
    }
    public static <T> T deserialize(String jsonStr, Class<T> classOfT) {
        try {
            return GSON.fromJson(jsonStr, classOfT);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    public static <T> T deserialize(String jsonStr, Type targetType) {
        try {
            return GSON.fromJson(jsonStr, targetType);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    public static <T> T deserialize(Reader reader, Class<T> classOfT) {
        try {
            return GSON.fromJson(reader, classOfT);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    public static class SerializationException extends RuntimeException {
        public SerializationException(Throwable cause) {
            super(cause);
        }
    }

    private static class ZonedDateTimeAdapter extends TypeAdapter<ZonedDateTime> {
        @Override
        public void write(JsonWriter jsonWriter, ZonedDateTime zonedDateTime) {
        }

        @Override
        public ZonedDateTime read(JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            }
            ZonedDateTime utc = ZonedDateTime.parse(jsonReader.nextString());
            // TODO: Read timezone from dynamic location
            return utc.withZoneSameInstant(ZoneId.of("America/Denver"));
        }
    }

    private static class RubricAssessmentAdapter implements JsonDeserializer<CanvasRubricAssessment> {
        @Override
        public CanvasRubricAssessment deserialize(JsonElement jsonElement, Type type,
                                                  JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {
            Map<String, Map<String, Object>> map = jsonDeserializationContext.deserialize(jsonElement, Map.class);
            Map<String, CanvasRubricItem> items = new HashMap<>();
            for (var entry : map.entrySet()) {
                String key = entry.getKey();
                Map<String, Object> value = entry.getValue();

                String comments = (String) value.get("comments");

                Double points = ((Double) value.get("points"));

                float score = (points == null) ? 0 : points.floatValue();

                items.put(key, new CanvasRubricItem(comments, score));
            }
            return new CanvasRubricAssessment(items);
        }
    }

    private static class InstantAdapter extends TypeAdapter<Instant> {

        @Override
        public void write(JsonWriter jsonWriter, Instant instant) throws IOException {
            jsonWriter.value(instant.toString());
        }

        @Override
        public Instant read(JsonReader jsonReader) throws IOException {
            return Instant.parse(jsonReader.nextString());
        }
    }
}
