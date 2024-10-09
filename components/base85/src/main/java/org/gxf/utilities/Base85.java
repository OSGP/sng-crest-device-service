package org.gxf.utilities;


import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Example: <br>
 * <code> String encodedString = Base85.getZ85Encoder().encodeToString( byteArray ); <br>
 * byte[] data = Base85.getZ85Decoder().decodeToBytes( encodedString );</code>
 */
public class Base85 {
    // Constants used in encoding and decoding
    private static final long Power4 = 52200625; // 85^4
    private static final long Power3 = 614125;  // 85^3
    private static final long Power2 = 7225;   // 85^2

    /**
     * This is a base class for encoding data using the Base85 encoding scheme,
     * in the same style as Base64 encoder.
     * Encoder instances can be safely shared by multiple threads.
     */
    public static abstract class Encoder {
        /**
         * Calculate byte length of encoded string.
         *
         * @param data string to be encoded in UTF-8 bytes
         * @return length of encoded data in byte
         */
        public int calcEncodedLength(final String data) {
            return calcEncodedLength(data.getBytes(UTF_8));
        }

        /**
         * Calculate byte length of encoded data.
         *
         * @param data data to be encoded
         * @return length of encoded data in byte
         */
        public int calcEncodedLength(final byte[] data) {
            return calcEncodedLength(data, 0, data.length);
        }

        /**
         * Calculate byte length of encoded data.
         *
         * @param data   data to be encoded
         * @param offset byte offset that data starts
         * @param length number of data bytes
         * @return length of encoded data in byte
         */
        public int calcEncodedLength(final byte[] data, final int offset, final int length) {
            if (offset < 0 || length < 0) throw new IllegalArgumentException("Offset and length must not be negative");
            return (int) Math.ceil(length * 1.25);
        }

        /**
         * Encode string data into Base85 string.  The input data will be converted to byte using UTF-8 encoding.
         *
         * @param data text to encode
         * @return encoded Base85 string
         */
        public final String encode(final String data) {
            return encodeToString(data.getBytes(UTF_8));
        }

        /**
         * Encode binary data into Base85 string.
         *
         * @param data data to encode
         * @return encoded Base85 string
         */
        public final String encodeToString(final byte[] data) {
            return new String(encode(data), US_ASCII);
        }

        /**
         * Encode part of binary data into Base85 string.
         *
         * @param data   data to encode
         * @param offset byte offset that data starts
         * @param length number of data bytes
         * @return encoded Base85 string
         */
        public final String encodeToString(final byte[] data, final int offset, final int length) {
            return new String(encode(data, offset, length), US_ASCII);
        }

        /**
         * Encode binary data into a new byte array.
         *
         * @param data data to encode
         * @return encoded Base85 encoded data in ASCII charset
         */
        public final byte[] encode(final byte[] data) {
            return encode(data, 0, data.length);
        }

        /**
         * Encode part of a binary data into a new byte array.
         *
         * @param data   array with data to encode
         * @param offset byte offset to start reading data
         * @param length number of byte to read
         * @return encoded Base85 encoded data in ASCII charset
         */
        public final byte[] encode(final byte[] data, final int offset, final int length) {
            byte[] out = new byte[calcEncodedLength(data, offset, length)];
            int len = _encode(data, offset, length, out, 0);
            if (out.length == len) return out;
            return Arrays.copyOf(out, len);
        }

        /**
         * Encode part of a byte array and write the output into a byte array in ASCII charset.
         *
         * @param data       array with data to encode
         * @param offset     byte offset to start reading data
         * @param length     number of byte to read
         * @param out        array to write encoded data to
         * @param out_offset byte offset to start writing encoded data to
         */
        public final void encode(final byte[] data, final int offset, final int length, final byte[] out, final int out_offset) {
            int size = calcEncodedLength(data, offset, length);
            _encode(data, offset, length, out, out_offset);
        }

        /**
         * Encode the data as one block in reverse output order.
         * This is the strict algorithm specified by RFC 1924 for IP address encoding,
         * when the data is exactly 16 bytes (128 bits) long.
         * Because the whole input data is encoded as one big block,
         * this is much less efficient than the more common encodings.
         *
         * @param data Byte data to encode
         * @return Encoded data in ascii encoding
         * @see https://tools.ietf.org/html/rfc1924
         */
        public byte[] encodeBlockReverse(byte[] data) {
            int size = Math.max(0, (int) Math.ceil(data.length * 1.25));
            byte[] result = new byte[size];
            encodeBlockReverse(data, 0, data.length, result, 0);
            return result;
        }

        /**
         * Encode part of data as one block in reverse output order into output array.
         * This is the strict algorithm specified by RFC 1924 for IP address encoding,
         * when the data part is exactly 16 bytes (128 bits) long.
         * Because the whole input data part is encoded as one big block,
         * this is much less efficient than the more common encodings.
         *
         * @param data       array to read data from
         * @param offset     byte offset to start reading data
         * @param length     number of byte to read
         * @param out        array to write encoded data to
         * @param out_offset byte offset to start writing encoded data to
         * @see https://tools.ietf.org/html/rfc1924
         */
        public void encodeBlockReverse(byte[] data, int offset, int length, byte[] out, int out_offset) {
            int size = (int) Math.ceil(length * 1.25);
            if (offset != 0 || length != data.length)
                data = Arrays.copyOfRange(data, offset, offset + length);
            BigInteger sum = new BigInteger(1, data), b85 = BigInteger.valueOf(85);
            byte[] map = getEncodeMap();
            for (int i = size + out_offset - 1; i >= out_offset; i--) {
                BigInteger[] mod = sum.divideAndRemainder(b85);
                out[i] = map[mod[1].intValue()];
                sum = mod[0];
            }
        }

        protected int _encodeDangling(final byte[] encodeMap, final byte[] out, final int wi, final ByteBuffer buffer, int leftover) {
            long sum = buffer.getInt(0) & 0x00000000ffffffffL;
            out[wi] = encodeMap[(int) (sum / Power4)];
            sum %= Power4;
            out[wi + 1] = encodeMap[(int) (sum / Power3)];
            sum %= Power3;
            if (leftover >= 2) {
                out[wi + 2] = encodeMap[(int) (sum / Power2)];
                sum %= Power2;
                if (leftover >= 3)
                    out[wi + 3] = encodeMap[(int) (sum / 85)];
            }
            return leftover + 1;
        }

        protected int _encode(byte[] in, int ri, int rlen, byte[] out, int wi) {
            final int wo = wi;
            final ByteBuffer buffer = ByteBuffer.allocate(4);
            final byte[] buf = buffer.array(), encodeMap = getEncodeMap();
            for (int loop = rlen / 4; loop > 0; loop--, ri += 4) {
                System.arraycopy(in, ri, buf, 0, 4);
                wi = _writeData(buffer.getInt(0) & 0x00000000ffffffffL, encodeMap, out, wi);
            }
            int leftover = rlen % 4;
            if (leftover == 0) return wi - wo;
            buffer.putInt(0, 0);
            System.arraycopy(in, ri, buf, 0, leftover);
            return wi - wo + _encodeDangling(encodeMap, out, wi, buffer, leftover);
        }

        protected int _writeData(long sum, byte[] map, byte[] out, int wi) {
            out[wi] = map[(int) (sum / Power4)];
            sum %= Power4;
            out[wi + 1] = map[(int) (sum / Power3)];
            sum %= Power3;
            out[wi + 2] = map[(int) (sum / Power2)];
            sum %= Power2;
            out[wi + 3] = map[(int) (sum / 85)];
            out[wi + 4] = map[(int) (sum % 85)];
            return wi + 5;
        }

        protected abstract byte[] getEncodeMap();

        public String getCharset() {
            return new String(getEncodeMap(), US_ASCII);
        }
    }

    /**
     * This class encodes data in the Base85 encoding scheme using the character set described by IETF RFC 1924,
     * but in the efficient algorithm of Ascii85 and Z85.
     * This scheme does not use quotes, comma, or slash, and can usually be used in sql, json, csv etc. without escaping.
     * <p>
     * Encoder instances can be safely shared by multiple threads.
     *
     * @see https://tools.ietf.org/html/rfc1924
     */
    public static class Rfc1924Encoder extends Encoder {
        private static final byte[] ENCODE_MAP = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!#$%&()*+-;<=>?@^_`{|}~".getBytes(US_ASCII);

        @Override
        protected byte[] getEncodeMap() {
            return ENCODE_MAP;
        }
    }

    /**
     * This is a skeleton class for decoding data in the Base85 encoding scheme.
     * in the same style as Base64 decoder.
     * Decoder instances can be safely shared by multiple threads.
     */
    public static abstract class Decoder {
        /**
         * Calculate byte length of decoded data.
         * Assumes data is correct; use test method to validate data.
         *
         * @param data Encoded data in ascii charset
         * @return number of byte of decoded data
         */
        public int calcDecodedLength(String data) {
            return calcDecodedLength(data.getBytes(US_ASCII));
        }

        /**
         * Calculate byte length of decoded data.
         * Assumes data is correct; use test method to validate data.
         *
         * @param data Encoded data in ascii charset
         * @return number of byte of decoded data
         */
        public int calcDecodedLength(final byte[] data) {
            return calcDecodedLength(data, 0, data.length);
        }

        /**
         * Calculate byte length of decoded data.
         * Assumes data is correct; use test method to validate data.
         *
         * @param data   Encoded data in ascii charset
         * @param offset byte offset that data starts
         * @param length number of data bytes
         * @return number of byte of decoded data
         */
        public int calcDecodedLength(final byte[] data, final int offset, final int length) {
            if (length % 5 == 1)
                throw new IllegalArgumentException(length + " is not a valid Base85/RFC1924 data length.");
            return (int) (length * 0.8);
        }

        /**
         * Decode Base85 string into a UTF-8 string.
         *
         * @param data text to decode
         * @return decoded UTF-8 string
         */
        public final String decode(final String data) {
            return new String(decode(data.getBytes(US_ASCII)), UTF_8);
        }

        /**
         * Decode ASCII Base85 data into a new byte array.
         *
         * @param data data to decode
         * @return decoded binary data
         */
        public final byte[] decode(final byte[] data) {
            return decode(data, 0, data.length);
        }

        /**
         * Decode Base85 string into a new byte array.
         *
         * @param data data to decode
         * @return decoded binary data
         */
        public final byte[] decodeToBytes(final String data) {
            return decode(data.getBytes(US_ASCII));
        }

        /**
         * Decode ASCII Base85 data into a new byte array.
         *
         * @param data   array with data to decode
         * @param offset byte offset to start reading data
         * @param length number of byte to read
         * @return decoded binary data
         * @throws IllegalArgumentException if offset or length is negative, or if data array is not big enough (data won't be written)
         */
        public final byte[] decode(final byte[] data, final int offset, final int length) {
            byte[] result = new byte[calcDecodedLength(data, offset, length)];
            try {
                int len = _decode(data, offset, length, result, 0);
                // Should not happen, but fitting the size makes sure tests will fail when it does happen.
                if (result.length != len) return Arrays.copyOf(result, len);
            } catch (ArrayIndexOutOfBoundsException ex) {
                throwMalformed(ex);
            }
            return result;
        }

        /**
         * Decode part of a byte array and write the output into a byte array in ASCII charset.
         *
         * @param data       array with data to encode
         * @param offset     byte offset to start reading data
         * @param length     number of byte to read
         * @param out        array to write decoded data to
         * @param out_offset byte offset to start writing decoded data to
         * @throws IllegalArgumentException if offset or length is negative, or if either array is not big enough (data won't be written)
         */
        public final void decode(final byte[] data, final int offset, final int length, final byte[] out, final int out_offset) {
            int size = calcDecodedLength(data, offset, length);
            try {
                _decode(data, offset, length, out, out_offset);
            } catch (ArrayIndexOutOfBoundsException ex) {
                throwMalformed(ex);
            }
        }

        /**
         * Decode the data as one block in reverse input order.
         * This is the strict algorithm specified by RFC 1924 for IP address decoding,
         * when the data is exactly 16 bytes (128 bits) long.
         *
         * @param data Byte data to encode
         * @return Encoded data in ascii encoding
         * @see https://tools.ietf.org/html/rfc1924
         */
        public byte[] decodeBlockReverse(byte[] data) {
            int size = Math.max(0, (int) Math.ceil(data.length * 0.8));
            byte[] result = new byte[size];
            decodeBlockReverse(data, 0, data.length, result, 0);
            return result;
        }

        /**
         * Decode part of data as one block in reverse input order into output array.
         * This is the strict algorithm specified by RFC 1924 for IP address decoding,
         * when the data part is exactly 16 bytes (128 bits) long.
         *
         * @param data       array to read data from
         * @param offset     byte offset to start reading data
         * @param length     number of byte to read
         * @param out        array to write decoded data to
         * @param out_offset byte offset to start writing decoded data to
         * @see https://tools.ietf.org/html/rfc1924
         */
        public void decodeBlockReverse(byte[] data, int offset, int length, byte[] out, int out_offset) {
            int size = (int) Math.ceil(length * 0.8);
            BigInteger sum = BigInteger.ZERO, b85 = BigInteger.valueOf(85);
            byte[] map = getDecodeMap();
            try {
                for (int i = offset, len = offset + length; i < len; i++)
                    sum = sum.multiply(b85).add(BigInteger.valueOf(map[data[i]]));
            } catch (ArrayIndexOutOfBoundsException ex) {
                throwMalformed(ex);
            }
            System.arraycopy(sum.toByteArray(), 0, out, out_offset, size);
        }

        /**
         * Test that given data can be decoded correctly.
         *
         * @param data Encoded data in ascii charset
         * @return true if data is of correct length and composed of correct characters
         */
        public boolean test(final String data) {
            return test(data.getBytes(US_ASCII));
        }

        /**
         * Test that given data can be decoded correctly.
         *
         * @param data Encoded data in ascii charset
         * @return true if data is of correct length and composed of correct characters
         */
        public boolean test(final byte[] data) {
            return test(data, 0, data.length);
        }

        /**
         * Test that part of given data can be decoded correctly.
         *
         * @param data   Encoded data in ascii charset
         * @param offset byte offset that data starts
         * @param length number of data bytes
         * @return true if data is of correct length and composed of correct characters
         */
        public boolean test(byte[] encoded_data, int offset, int length) {
            return _test(encoded_data, offset, length);
        }

        protected boolean _test(final byte[] data, final int offset, final int length) {
            byte[] valids = getDecodeMap();
            try {
                for (int i = offset, len = offset + length; i < len; i++) {
                    byte e = data[i];
                    if (valids[e] < 0)
                        return false;
                }
                calcDecodedLength(data, offset, length);
            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException ex) {
                return false;
            }
            return true;
        }

        protected void throwMalformed(Exception ex) {
            throw new IllegalArgumentException("Malformed Base85/RFC1924 data", ex);
        }

        protected int _decodeDangling(final byte[] decodeMap, final byte[] in, final int ri, final ByteBuffer buffer, int leftover) {
            if (leftover == 1) throwMalformed(null);
            long sum = decodeMap[in[ri]] * Power4 +
                    decodeMap[in[ri + 1]] * Power3 + 85;
            if (leftover >= 3) {
                sum += decodeMap[in[ri + 2]] * Power2;
                if (leftover >= 4)
                    sum += decodeMap[in[ri + 3]] * 85;
                else
                    sum += Power2;
            } else
                sum += Power3 + Power2;
            buffer.putInt(0, (int) sum);
            return leftover - 1;
        }

        protected int _decode(byte[] in, int ri, int rlen, final byte[] out, int wi) {
            final int wo = wi;
            final ByteBuffer buffer = ByteBuffer.allocate(4);
            final byte[] buf = buffer.array(), decodeMap = getDecodeMap();
            for (int loop = rlen / 5; loop > 0; loop--, wi += 4, ri += 5) {
                _putData(buffer, decodeMap, in, ri);
                System.arraycopy(buf, 0, out, wi, 4);
            }
            int leftover = rlen % 5;
            if (leftover == 0) return wi - wo;
            leftover = _decodeDangling(decodeMap, in, ri, buffer, leftover);
            System.arraycopy(buf, 0, out, wi, leftover);
            return wi - wo + leftover;
        }

        protected void _putData(ByteBuffer buffer, byte[] map, byte[] in, int ri) {
            buffer.putInt(0, (int) (map[in[ri]] * Power4 +
                    map[in[ri + 1]] * Power3 +
                    map[in[ri + 2]] * Power2 +
                    map[in[ri + 3]] * 85 +
                    map[in[ri + 4]]));
        }

        protected abstract byte[] getDecodeMap();

    }

    /**
     * This class decodes data in the Base85 encoding using the character set described by IETF RFC 1924,
     * in the efficient algorithm of Ascii85 and Z85.
     * Malformed data may or may not throws IllegalArgumentException on decode; call test(byte[]) to check data if necessary.
     * Decoder instances can be safely shared by multiple threads.
     *
     * @see https://tools.ietf.org/html/rfc1924
     */
    public static class Rfc1924Decoder extends Decoder {
        private static final byte[] DECODE_MAP = new byte[127];

        static {
            Arrays.fill(DECODE_MAP, (byte) -1);
            for (byte i = 0, len = (byte) Rfc1924Encoder.ENCODE_MAP.length; i < len; i++) {
                byte b = Rfc1924Encoder.ENCODE_MAP[i];
                DECODE_MAP[b] = i;
            }
        }

        @Override
        protected byte[] getDecodeMap() {
            return DECODE_MAP;
        }
    }

    private static Encoder RFC1924ENCODER;
    private static Decoder RFC1924DECODER;

    public static Encoder getRfc1924Encoder() {
        if (RFC1924ENCODER == null) RFC1924ENCODER = new Rfc1924Encoder();
        return RFC1924ENCODER; // No worry if multiple encoder is created in multiple threads. Same for all.
    }

    public static Decoder getRfc1924Decoder() {
        if (RFC1924DECODER == null) RFC1924DECODER = new Rfc1924Decoder();
        return RFC1924DECODER;
    }
}
