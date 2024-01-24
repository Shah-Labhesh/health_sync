package com.fyp.health_sync.utils;


import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ImageUtils {


    public static String convertToBase64(byte[] profilePicture) {
        return Base64.getEncoder().encodeToString(profilePicture);
    }
    public static byte[] compressImage(byte[] image){
        Deflater deflater = new Deflater();
        deflater.setLevel(Deflater.BEST_COMPRESSION);
        deflater.setInput(image);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(image.length);

        byte[] buffer = new byte[4*1024];
        while (!deflater.finished()){
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }

        try {
            outputStream.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return outputStream.toByteArray();

    }

    public static byte[] decompressImage(byte[] image) throws  DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(image);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(image.length);
        byte[] buffer = new byte[4*1024];
        while (!inflater.finished()){
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        try {
            outputStream.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return outputStream.toByteArray();
    }


}
