package fr.ignishky.fma.generator.helper;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import com.vividsolutions.jts.geom.Coordinate;

import static com.github.davidmoten.geo.GeoHash.encodeHash;
import static com.google.common.base.Preconditions.checkState;

public final class Geohash {

    private static final String SYMBOLS = "psc8b9fzejv0uhx1nm5rgt4yk3d627qw";
    private static final int[] lookup = indexByLetter();

    private Geohash() {
    }

    // GeoHashUtils.stringEncode() returns 11 letters.
    // decode() encode letters to long with 5 bit per letter.
    // A geohash takes 55 bits, we use 3 bits to encode the layer
    public static long encodeGeohash(int layer, Coordinate coordinate) {
        String stringEncode = encodeHash(coordinate.y, coordinate.x, 11);
        long decode = encodeString(stringEncode);
        checkState(layer >= 0 && layer < 8);
        long mask = (long) layer << 55;
        return decode | mask;
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
        for (char ch : SYMBOLS.toCharArray()) {
            lookup[ch] = i++;
        }
        return lookup;
    }

    public static LatLong decodeGeohash(long geohash) {
        return GeoHash.decodeHash(decodeString(withoutLayer(geohash)));
    }

    private static String decodeString(long geohash) {
        StringBuilder sb = new StringBuilder();
        while (geohash != 0) {
            sb.append(SYMBOLS.charAt((int) (geohash % SYMBOLS.length())));
            geohash /= SYMBOLS.length();
        }
        return sb.reverse().toString();
    }

    private static long withoutLayer(long geohash) {
        long mask = 0b111L << 55;
        return geohash & ~mask;
    }
}
