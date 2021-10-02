package utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.toEpochSecond(ZoneOffset.UTC));
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        if (in == null) {
            return null;
        }

        return LocalDateTime.ofEpochSecond(in.nextLong(), 0, ZoneOffset.UTC);
    }
}