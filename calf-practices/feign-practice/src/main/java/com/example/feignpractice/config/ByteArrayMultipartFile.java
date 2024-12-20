package com.example.feignpractice.config;

import lombok.NonNull;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;

/**
 * @author xw
 * @date 2023/3/8
 */
@Value
public class ByteArrayMultipartFile implements MultipartFile {
    String name;

    String originalFilename;

    String contentType;

    @NonNull
    byte[] bytes;

    @Override
    public boolean isEmpty () {
        return bytes.length == 0;
    }

    @Override
    public long getSize () {
        return bytes.length;
    }

    @Override
    public InputStream getInputStream () {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void transferTo (File destination) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(destination.toPath())) {
            outputStream.write(bytes);
        }
    }
}
