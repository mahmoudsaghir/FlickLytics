package models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {
    public static String processStream(InputStream stream, BufferedReaderProcessor p) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = p.process(br)) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
    }
}
