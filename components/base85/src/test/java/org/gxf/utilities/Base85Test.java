package org.gxf.utilities;

import org.gxf.utilities.Base85.Decoder;
import org.junit.jupiter.api.Test;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.*;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

public class Base85Test {
    private final Random rng = new Random();
    private final Base85.Encoder rfcE;
    private final Decoder rfcD;

    public Base85Test() {
        rfcE = Base85.getRfc1924Encoder();
        rfcD = Base85.getRfc1924Decoder();
    }

    /////////// Common utils ///////////

    private byte[] reverseCharset(byte[] valids) {
        List<Byte> invalidList = new ArrayList<>(256);
        for (int i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++) invalidList.add((byte) i);
        for (byte e : valids) invalidList.remove(Byte.valueOf(e));
        final int len = invalidList.size();
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) result[i] = invalidList.get(i);
        return result;
    }

    private void recurTestValidate(byte[] ok, byte[] fail, byte[] buf, int offset, Decoder decoder) {
        if (offset >= buf.length) return;
        int count = offset + 1, oklen = ok.length;
        if (count % 5 == 1) {
            buf[offset] = ok[rng.nextInt(oklen)];
            testValidateFail(buf, count, decoder);
            recurTestValidate(ok, fail, buf, count, decoder);
            return;
        }
        for (byte b : fail) {
            buf[offset] = b; // Wrong data should also fails
            testValidateFail(buf, offset + 1, decoder);
        }
        buf[offset] = ok[rng.nextInt(oklen)];
        if (!decoder.test(buf, 0, count)) // Otherwise should pass
            fail(randData(buf, count) + " should not return false");
        recurTestValidate(ok, fail, buf, count, decoder);
    }

    private void testValidateFail(byte[] buf, int len, Decoder decoder) {
        if (decoder.test(buf, 0, len))
            fail("test( " + randData(buf, len) + ") should not return true");
    }

    private String randData(byte[] buf, int len) {
        return "atob(\"" + Base64.getEncoder().encodeToString(Arrays.copyOf(buf, len)) + "\")";
    }

    private void testException(Runnable action, String testName) {
        testException(action, Exception.class, testName);
    }

    private void testException(Runnable action, Class<? extends Exception> exceptionClass, String testName) {
        try {
            action.run();
            fail(testName + " does not throw any exception");
        } catch (Exception yes) {
            if (!exceptionClass.isInstance(yes))
                fail(testName + " throws " + yes.getClass() + ", expected " + exceptionClass);
        }
    }

    /////////// Generic Test Routines ///////////

    public void testStrEncode(Base85.Encoder e, String[] map) {
        for (int i = 0; i < map.length; i += 2)
            assertEquals(map[i + 1], e.encode(map[i]), "Encode " + map[i]);
    }

    public void testStrDecode(Decoder d, String[] map) {
        for (int i = 0; i < map.length; i += 2) {
            assertEquals(map[i], d.decode(map[i + 1]), "Decode " + map[i + 1]);
            assertArrayEquals(map[i].getBytes(UTF_8), d.decode(map[i + 1].getBytes(US_ASCII)), "Decode " + map[i + 1] + " to bytes");
        }
    }

    public void testByteEncode(Base85.Encoder e, String[] map) {
        String origStr = map[map.length - 2], codeStr = map[map.length - 1];
        byte[] orig = origStr.getBytes(UTF_8), code = codeStr.getBytes(US_ASCII);
        assertEquals(codeStr, e.encodeToString(orig), "encodeToString");
        assertArrayEquals(code, e.encode(orig), "Byte to byte encode");
        byte[] buf = Arrays.copyOf(orig, orig.length * 2);
        assertArrayEquals(code, e.encode(buf, 0, orig.length), "Byte to byte encode offset 0");
        System.arraycopy(buf, 0, buf, 2, orig.length);
        assertArrayEquals(code, e.encode(buf, 2, orig.length), "Byte to byte encode offset 2");
        byte[] output = new byte[code.length + 2];
        e.encode(orig, 0, orig.length, output, 0);
        assertArrayEquals(code, Arrays.copyOfRange(output, 0, code.length), "Byte to byte direct encode offset 0");
        e.encode(buf, 2, orig.length, output, 2);
        assertArrayEquals(code, Arrays.copyOfRange(output, 2, code.length + 2), "Byte to byte direct encode offset 2");
    }

    public void testByteDecode(Decoder d, String[] map) {
        String origStr = map[map.length - 2], codeStr = map[map.length - 1];
        byte[] orig = origStr.getBytes(UTF_8), code = codeStr.getBytes(US_ASCII);
        assertArrayEquals(orig, d.decode(code), "Byte to byte decode");
        byte[] buf = Arrays.copyOf(code, code.length * 2);
        assertArrayEquals(orig, d.decode(buf, 0, code.length), "Byte to byte decode offset 0");
        System.arraycopy(buf, 0, buf, 2, code.length);
        assertArrayEquals(orig, d.decode(buf, 2, code.length), "Byte to byte decode offset 2");
        byte[] output = new byte[orig.length + 2];
        d.decode(code, 0, code.length, output, 0);
        assertArrayEquals(orig, Arrays.copyOfRange(output, 0, orig.length), "Byte to byte direct decode offset 0");
        d.decode(buf, 2, code.length, output, 2);
        assertArrayEquals(orig, Arrays.copyOfRange(output, 2, orig.length + 2), "Byte to byte direct decode offset 2");
    }

    public void testRoundTrip(Base85.Encoder e, Decoder d) {
        for (int len = 1; len <= 12; len++) {
            byte[] from = new byte[len], enc, dec;
            for (int v = Byte.MIN_VALUE; v <= Byte.MAX_VALUE; v++) {
                Arrays.fill(from, (byte) v);
                String test = "byte[" + len + "]{" + v + "} ";
                try {
                    enc = e.encode(from);
                    assertTrue(d.test(enc), test + " encoded data test.");
                    assertEquals(enc.length, e.calcEncodedLength(from), test + " encoded length");
                    dec = d.decode(enc);
                    assertEquals(dec.length, d.calcDecodedLength(enc), test + " decoded length");
                    assertArrayEquals(from, dec, test + " round trip.");
                } catch (Exception ex) {
                    fail(test + " round trip throws " + ex);
                }
            }
        }
    }

    public void testInvalidLength(Base85.Encoder e, Decoder d) {
        byte[] buf = new byte[4];
        Arrays.fill(buf, e.getEncodeMap()[0]); // Fill in valid values in case an decode actually tries to read it
        testException(() -> e.encode(buf, -1, 4), "Encode in offset -1");
        testException(() -> e.encode(buf, 4, 1), "Encode in offset > size");
        testException(() -> e.encode(buf, 0, -1), "Encode in length -1");
        testException(() -> e.encode(buf, 0, 5), "Encode in length > size");
        testException(() -> e.encode(buf, 2, 4), "Encode in index out of bounds");
        testException(() -> e.encode(buf, 0, 1, buf, -1), "Encode out index -1");
        testException(() -> e.encode(buf, 0, 1, buf, 4), "Encode out index > size");
        testException(() -> e.encode(buf, 0, 1, buf, 3), "Encode out index out of bounds");
        testException(() -> d.decode(buf, -1, 4), "Decode in offset -1");
        testException(() -> d.decode(buf, 4, 1), "Decode in offset > size");
        testException(() -> d.decode(buf, 0, -1), "Decode in length -1");
        testException(() -> d.decode(buf, 0, 5), "Decode in length > size");
        testException(() -> d.decode(buf, 2, 4), "Decode in index out of bounds");
        testException(() -> d.decode(buf, 0, 1, buf, -1), "Decode out index -1");
        testException(() -> d.decode(buf, 0, 1, buf, 4), "Decode out index > size");
        testException(() -> d.decode(buf, 0, 1, buf, 3), "Decode out index out of bounds");
    }

    public void testInvalidData(Base85.Encoder e, Decoder d) {
        testException(() -> d.decode(new byte[]{127, 127}), IllegalArgumentException.class, "Decode char(127)");
        testException(() -> d.decode(new byte[]{-1, -1}), IllegalArgumentException.class, "Decode char(-1)");
        byte[] validCodes = e.getCharset().getBytes(US_ASCII);
        byte[] invalidCodes = reverseCharset(validCodes);
        recurTestValidate(validCodes, invalidCodes, new byte[11], 0, d);
    }

    /////////// RFC Tests ///////////

    private final String[] rfcTests = {
            "", "",
            "A", "K>",
            "AB", "K|%",
            "ABC", "K|(_",
            "ABCD", "K|(`B",
            "ABCDE", "K|(`BMF",
            "ABCDEF", "K|(`BMMe",
            "ABCDEFG", "K|(`BMMg&",
            "ABCDEFGH", "K|(`BMMg(R",
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!#$%&()*+-;<=>?@^_`{|}~",
            "FflSSG&MFiI5|N=LqtVJM@UIZOH55pPf$@(Q&d$}S6EqEVPa!sWoBn+X=-b1ZEkOHadLBXb#`}nd3qruBqb&&DJm;1J3Ku;KR{kzV0(Oheg",
            "測試中", "=D4irsix$(tp",
            "اختبارات", "*r(X8*s9p5*r(XB*r(X4",
    };

    @Test
    public void testRfcSpec() throws UnknownHostException {
        byte[] addr = Inet6Address.getByName("1080:0:0:0:8:800:200C:417A").getAddress();
        String encoded = "4)+k&C#VzJ4br>0wv%Yp";
        assertEquals(encoded, new String(rfcE.encodeBlockReverse(addr), US_ASCII), "Inet encode");
        assertArrayEquals(addr, rfcD.decodeBlockReverse(encoded.getBytes(US_ASCII)), "Inet decode");
    }

    @Test
    public void testRfcStrEncode() {
        testStrEncode(rfcE, rfcTests);
    }

    @Test
    public void testRfcStrDecode() {
        testStrDecode(rfcD, rfcTests);
    }

    @Test
    public void testRfcEncode() {
        testByteEncode(rfcE, rfcTests);
    }

    @Test
    public void testRfcDecode() {
        testByteDecode(rfcD, rfcTests);
    }

    @Test
    public void testRfcRoundTrip() {
        testRoundTrip(rfcE, rfcD);
    }

    @Test
    public void testRfcWrongData() {
        testInvalidData(rfcE, rfcD);
    }

    @Test
    public void testRfcWrongLength() {
        testInvalidLength(rfcE, rfcD);
    }

    @Test
    public void testRfcDecodedLenErr6() {
        assertThrows(IllegalArgumentException.class, () -> rfcD.calcDecodedLength(null, 0, 6));
    }

    @Test
    public void testRfcDecodedLenErr1() {
        assertThrows(IllegalArgumentException.class, () -> rfcD.calcDecodedLength(null, 0, 1));
    }
}
