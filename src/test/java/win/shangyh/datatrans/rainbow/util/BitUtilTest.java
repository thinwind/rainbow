/*
 * Copyright 2021 Shang Yehua <niceshang@outlook.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package win.shangyh.datatrans.rainbow.util;

import java.util.Random;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 *
 * TODO BitUtilTest说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2021-04-02  10:38
 *
 */
public class BitUtilTest {

    @Test
    public void testToByteArray() {
        String mti = "0302";
        byte[] mtiInBytes = new byte[4];
        mtiInBytes[0] = '0';
        mtiInBytes[1] = '3';
        mtiInBytes[2] = '0';
        mtiInBytes[3] = '2';
        assertArrayEquals(mtiInBytes, BitUtil.toAsciiByteArray(mti));
    }

    @Test(expected = RuntimeException.class)
    public void whenSizeLargerThanExpectedThenExcept() {
        BitUtil.splitIntInAscii(100, 2);
    }

    @Test
    public void whenSizeEqualsToExpectThenReturn() {
        byte[] r = BitUtil.splitIntInAscii(1234, 4);
        assertEquals(r.length, 4);
        assertEquals(new String(r, 0, 1), "1");
        assertEquals(new String(r, 1, 1), "2");
        assertEquals(new String(r, 2, 1), "3");
        assertEquals(new String(r, 3, 1), "4");
    }

    @Test
    public void whenSizeLessThanExpectedThenPaddingLeftWithZeros() {
        byte[] r = BitUtil.splitIntInAscii(1234, 8);
        assertEquals(8, r.length);
        assertEquals(new String(r, 0, 1), "0");
        assertEquals(new String(r, 1, 1), "0");
        assertEquals(new String(r, 2, 1), "0");
        assertEquals(new String(r, 3, 1), "0");
        assertEquals(new String(r, 4, 1), "1");
        assertEquals(new String(r, 5, 1), "2");
        assertEquals(new String(r, 6, 1), "3");
        assertEquals(new String(r, 7, 1), "4");
    }

    @Test
    public void testToAscii() {
        String asciiInHex = "20212324303132334041425e";
        String expected = " !#$0123@AB^";
        byte[] bytes = new byte[asciiInHex.length() / 2];
        for (int i = 0; i < asciiInHex.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(asciiInHex.substring(i, i + 2), 16);
        }
        assertEquals(expected, BitUtil.toAsciiString(bytes));
    }

    @Test
    public void testAsciiToHexString() {
        String asciiInHex = "20212324303132334041425e";
        String str = " !#$0123@AB^";
        byte[] bytes = BitUtil.toAsciiByteArray(str);
        assertEquals(asciiInHex, BitUtil.toHexString(bytes));
    }

    @Test
    public void testToHexStringWithZeros() {
        byte[] cus = new byte[5];
        cus[0] = 0x00;
        cus[1] = 0x01;
        cus[2] = 0x03;
        cus[3] = (byte) 0xCA;
        cus[4] = (byte) 0xFE;
        String expected = "000103cafe";
        assertEquals(expected, BitUtil.toHexString(cus));
    }

    @Test
    public void splitIntInBytesTest() {
        int s = new Random().nextInt(65535);
        String b = Integer.toBinaryString(s);
        byte[] target = new byte[2];
        target[0] = (byte) Integer.parseInt(b.substring(0, b.length() - 8), 2);
        target[1] = (byte) Integer.parseInt(b.substring(b.length() - 8, b.length()), 2);
        int t = Integer.parseInt(BitUtil.toHexString(BitUtil.splitIntInBytes(s, 2)), 16);
        assertArrayEquals(target, BitUtil.splitIntInBytes(s, 2));
        assertEquals(s, t);
    }

    @Test
    public void splitIntInBytesTest4() {
        int s = new Random().nextInt(Integer.MAX_VALUE / 2) + Short.MAX_VALUE;
        String b = Integer.toBinaryString(s);
        byte[] target = new byte[4];
        target[0] = (byte) Integer.parseInt(b.substring(0, b.length() - 24), 2);
        target[1] = (byte) Integer.parseInt(b.substring(b.length() - 24, b.length() - 16), 2);
        target[2] = (byte) Integer.parseInt(b.substring(b.length() - 16, b.length() - 8), 2);
        target[3] = (byte) Integer.parseInt(b.substring(b.length() - 8, b.length()), 2);
        int t = Integer.parseInt(BitUtil.toHexString(BitUtil.splitIntInBytes(s, 4)), 16);
        assertArrayEquals(target, BitUtil.splitIntInBytes(s, 4));
        assertEquals(s, t);
    }

    @Test
    public void joinBytesToIntTest() {
        for (int i = 0; i < 10000; i++) {
            int s = new Random().nextInt(65535);
            byte[] splited = BitUtil.splitIntInBytes(s, 2);
            int t = BitUtil.joinBytesToUnsignedInt(splited);
            assertEquals(s, t);
        }
    }
    
    @Test
    public void joinSingleBytesToIntTest() {
        byte[] data = new byte[]{(byte)255};
        System.out.println(data[0]);
        int t = BitUtil.joinBytesToUnsignedInt(data);
        assertEquals(255, t);
    }

    @Test
    public void joinBytesToIntTest2() {
        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            int s = random.nextInt(65535);
            byte[] splited = BitUtil.splitIntInBytes(s, 2);
            byte[] ranBytes = new byte[100];
            random.nextBytes(ranBytes);
            int offset = random.nextInt(98);
            ranBytes[offset] = splited[0];
            ranBytes[offset + 1] = splited[1];
            BytesRange brange = BytesRange.of(ranBytes, offset, 2);
            int t = BitUtil.joinBytesToUnsignedInt(brange);
            assertEquals(s, t);
        }
    }

    @Test
    public void testGbkString() throws UnsupportedEncodingException {
        String ch = "中文";
        assertEquals("中文", BitUtil.toGBKString(ch.getBytes("GBK")));
    }

    @Test
    public void testGbkBytes() throws UnsupportedEncodingException {
        String ch = "中文";
        assertArrayEquals("中文".getBytes("GBK"), BitUtil.toGBKBytes(ch));
    }

    @Test
    public void testUtf8Encode() throws UnsupportedEncodingException {
        String ch = "中文";
        assertArrayEquals("中文".getBytes("UTF-8"), BitUtil.toUtf8Bytes(ch));
    }

    @Test
    public void testUtf8Decode() throws UnsupportedEncodingException {
        String ch = "中文";
        assertEquals("中文", BitUtil.toUtf8String(ch.getBytes("UTF-8")));
    }

    @Test
    public void testUtf8Decode2() throws UnsupportedEncodingException {
        String ch = "中文";
        byte[] array = new byte[10];
        System.arraycopy(ch.getBytes("UTF-8"), 0, array, 3, 6);
        assertEquals("中文", BitUtil.toUtf8String(array, 3, 6));
    }

    @Test
    public void testByte2Hex() {
        byte b = 1;
        assertEquals("01", BitUtil.byte2Hex(b));
        b = 15;
        assertEquals("0f", BitUtil.byte2Hex(b));
        b = 0x1f;
        assertEquals("1f", BitUtil.byte2Hex(b));
        b = (byte) 0xff;
        assertEquals("ff", BitUtil.byte2Hex(b));
    }

    @Test
    public void testHex2Bytes() {
        String hex = "010f1fff";
        byte[] bytes1 = BitUtil.hex2Bytes(hex);
        byte[] bytes2 = { 1, 15, 0x1f, (byte) 0xff };
        assertArrayEquals(bytes2, bytes1);
    }

    @Test
    public void testAvg() {
        int a = 100;
        int b = 200;
        assertEquals(150, BitUtil.avg(a, b));

        a = 101;
        b = 10;
        assertEquals(55, BitUtil.avg(a, b));

        a = 11;
        b = 21;
        assertEquals(16, BitUtil.avg(a, b));

        a = -100;
        b = 102;
        assertEquals(1, BitUtil.avg(a, b));

        a = -200;
        b = 102;
        assertEquals(-49, BitUtil.avg(a, b));

        a = Integer.MAX_VALUE;
        b = Integer.MAX_VALUE - 2;
        assertEquals(Integer.MAX_VALUE - 1, BitUtil.avg(a, b));

        a = Integer.MAX_VALUE;
        b = Integer.MIN_VALUE;
        assertEquals(-1, BitUtil.avg(a, b));
    }

    private String fill0To8(String binaryString) {
        for (int i = 0, delta = 8 - binaryString.length(); i < delta; i++) {
            binaryString = "0" + binaryString;
        }
        return binaryString;
    }

    @Test
    public void testBase64Encoding() {
        byte[] data = new byte[12345];
        new Random().nextBytes(data);
        String base64 = BitUtil.toBase64String(data);
        byte[] decodedData = BitUtil.base64Decode(base64);
        assertArrayEquals(data, decodedData);
    }

    @Test
    public void testPrintStr() {
        String s = "3034364e503b9f5bf570daaec8897f85f2fd59195130303030303030303030303030303030303030303030303030303030";
        System.out.println(BitUtil.toAsciiString(BitUtil.hex2Bytes(s)));
    }
}
