package fr.ignishky.fma.generator.helper;

import com.vividsolutions.jts.geom.Point;

import static com.github.davidmoten.geo.GeoHash.encodeHash;
import static com.google.common.base.Preconditions.checkState;

public class Geohash {

    private static final String SYMBOLS = "psc8b9fzejv0uhx1nm5rgt4yk3d627qw";
    private static final int[] lookup = indexByLetter();

    private Geohash() {
    }

    // GeoHashUtils.stringEncode() returns 11 letters.
    // decode() encode letters to long with 5 bit per letter.
    // A geohash takes 55 bits, we use 3 bits to encode the layer
    public static long encodeGeohash(int layer, double x, double y) {
        String stringEncode = encodeHash(y, x, 11);
        long decode = encodeString(stringEncode);
        checkState(layer >= 0 && layer < 8);
        long mask = (long) layer << 55;
        return decode | mask;
    }

    public static long encodeGeohash(int layer, Point point) {
        return encodeGeohash(layer, point.getX(), point.getY());
    }

    private static long encodeString(String s) {
        long num = 0;
        for (char ch : s.toCharArray()) {
            num *= SYMBOLS.length();
            num += lookup[ch];
        }
        return num;
    }

    private static int[] indexByLetter() {
        int[] lookup = new int[128];
        int i = 0;
        for (char ch : Geohash.SYMBOLS.toCharArray()) {
            lookup[ch] = i++;
        }
        return lookup;
    }
}
