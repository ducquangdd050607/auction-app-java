package com.auctionapp.auctionappjava.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressionUtils {

  // Hàm Nén (Compress)
  public static byte[] compress(byte[] data) throws IOException {
    if (data == null || data.length == 0) return data;

    ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
    try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
      gzip.write(data);
    }
    return bos.toByteArray();
  }

  // Hàm Giải nén (Decompress)
  public static byte[] decompress(byte[] compressedData) throws IOException {
    if (compressedData == null || compressedData.length == 0) return compressedData;

    ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
    try (GZIPInputStream gzip = new GZIPInputStream(bis);
        ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

      byte[] buffer = new byte[1024];
      int len;
      while ((len = gzip.read(buffer)) != -1) {
        bos.write(buffer, 0, len);
      }
      return bos.toByteArray();
    }
  }
}
