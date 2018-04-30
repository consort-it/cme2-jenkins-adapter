package com.consort.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {

    private StreamUtils(){
        // not used
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[1024];
        int len = in.read(buffer);
        while (len != -1) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }

    }
}
