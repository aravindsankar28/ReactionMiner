package grank.graph;

/**
 * An array of bytes as an object.
 * It is used for fingerprint and unique code
 *
 * @author Huahai He
 * @version 1.0
 */
public class ByteArray implements Comparable {
  byte[] bytes;
  public ByteArray(byte[] _bytes) {
    bytes = _bytes;
  }

  public ByteArray(short[] _shorts) {
    bytes = new byte[_shorts.length*2];
    for(int i=0;i<_shorts.length;i++) {
      bytes[i*2]=(byte)(_shorts[i]&0xff);
      bytes[i*2+1]=(byte)((_shorts[i]>>8) & 0xff);
    }
  }
  public boolean equals(Object o) {
    ByteArray bs = (ByteArray) o;
    if (bytes.length != bs.bytes.length) {
      return false;
    }
    for (int i = 0; i < bytes.length; i++) {
      if (bytes[i] != bs.bytes[i]) {
        return false;
      }
    }
    return true;
  }

  public int compareTo(Object o) {
    ByteArray bs = (ByteArray) o;
    int len = Math.min(bytes.length, bs.bytes.length);
    for (int i = 0; i < len; i++) {
      if (bytes[i] != bs.bytes[i]) {
        return bytes[i] - bs.bytes[i];
      }
    }
    if (bytes.length < bs.bytes.length) {
      return -1;
    }
    else if (bytes.length == bs.bytes.length) {
      return 0;
    }
    else {
      return 1;
    }
  }

  private int hash = 0;
  public int hashCode() {
    if (hash == 0) {
      for (int i = 0; i < bytes.length; i++) {
        hash = 31 * hash + bytes[i];
      }
    }
    return hash;
  }

  public String toString() {
    String s = "(";
    for (int i = 0; i < bytes.length; i++) {
      s += bytes[i] + " ";
    }
    s += ")";
    return s;
  }
}
