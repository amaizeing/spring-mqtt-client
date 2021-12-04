package io.github.amaizeing.mqtt.core.util;

import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayUtil {

  private ArrayUtil() {
  }


  public static List<byte[]> splitArray(byte[] bytes, int splitSize) {
    val results = new ArrayList<byte[]>();
    int start = 0;
    while (start < bytes.length) {
      val end = Math.min(bytes.length, start + splitSize);
      results.add(Arrays.copyOfRange(bytes, start, end));
      start += splitSize;
    }
    return results;
  }

}
