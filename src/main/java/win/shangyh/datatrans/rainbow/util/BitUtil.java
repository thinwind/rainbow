/*
 * Copyright 2021 Shang Yehua
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

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import java.nio.charset.Charset;

import com.zdsyh.blueland.bravet8583.impl.BytesRange;

/**
 *
 * bit工具类
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2021-02-03  17:52
 *
 */
public final class BitUtil {

    private BitUtil() {
    }

    public static final Charset ASCII_CHARSET = Charset.forName("ASCII");

    public static final Charset GBK_CHARSET = Charset.forName("GB18030");

    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    public static final Charset EBCDIC_CHARSET = Charset.forName("IBM500");//替代EBCDIC
    /**
     * 把一个数值使用ASCII码的字节数组表示
     * 
     * @param value 要拆分的数值
     * @param size 要拆分的字节个数
     * @return 拆分后的数组
     */
    public static byte[] splitIntInAscii(int value, int size) {
        String valStr = String.valueOf(value);
        int delta = size - valStr.length();
        if (delta < 0) {
            throw new RuntimeException(String.format(
                    "The length([%s]) of value is larger than the expected size([%d]).", valStr,
                    size));
        }
        for (int i = 0; i < delta; i++) {
            valStr = "0" + valStr;
        }
        return valStr.getBytes(ASCII_CHARSET);
    }

    /**
     * 将字节转为ASCII字符串
     * @param data 字节数组
     * @return ASCII编码的字符串
     */
    public static String toAsciiString(byte[] data) {
        return new String(data, ASCII_CHARSET);
    }

    /**
     * 将字节转为GBK字符串
     * @param data 字节数组
     * @return GBK编码的字符串
     */
    public static String toGBKString(byte[] data) {
        return new String(data, GBK_CHARSET);
    }

    /**
     * 将字节转为ASCII字符串
     * @param data 字节数组
     * @param start 要开始编码的字节起始位置
     * @param length 要进行编码的长度
     * @return ASCII编码的字符串
     */
    public static String toAsciiString(byte[] data, int start, int length) {
        return new String(data, start, length, ASCII_CHARSET);
    }

    public static String toAsciiString(BytesRange bytes) {
        return new String(bytes.data, bytes.offset, bytes.length, ASCII_CHARSET);
    }

    /**
     * 将字节转为GBK字符串
     * @param data 字节数组
     * @param start 要开始编码的字节起始位置
     * @param length 要进行编码的长度
     * @return GBK编码的字符串
     */
    public static String toGBKString(byte[] data, int start, int length) {
        return new String(data, start, length, GBK_CHARSET);
    }

    public static String toGBKString(BytesRange bytes) {
        return new String(bytes.data, bytes.offset, bytes.length, GBK_CHARSET);
    }

    /**
     * 将字节转为UTF-8字符串
     * @param data 字节数组
     * @param start 要开始编码的字节起始位置
     * @param length 要进行编码的长度
     * @return UTF-8编码的字符串
     */
    public static String toUtf8String(byte[] data, int start, int length) {
        return new String(data, start, length, UTF8_CHARSET);
    }

    public static String toUtf8String(BytesRange bytes) {
        return new String(bytes.data, bytes.offset, bytes.length, UTF8_CHARSET);
    }

    /**
     * 将字符串编码成GBK对应的字节数组
     * 
     * @param data GBK字符串
     * @return GBK编码对应的字节数组
     */
    public static byte[] toGBKBytes(String data) {
        return data.getBytes(GBK_CHARSET);
    }

    /**
     * 将字符串编码成UTF8对应的字节数组
     * @param data UTF8编码的字符串
     * @return UTF8编码对应的字节数组
     */
    public static byte[] toUtf8Bytes(String data) {
        return data.getBytes(UTF8_CHARSET);
    }

    /**
     * 将字符数组编码成UTF8字符串
     * @param data 字节数组
     * @return UTF8编码对应的字符串
     */
    public static String toUtf8String(byte[] data) {
        return new String(data, UTF8_CHARSET);
    }

    public static String toHexString(byte[] data) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(data[i] & 0xff);
            builder.append(hex.length() == 2 ? hex : "0" + hex);
        }
        return builder.toString();
    }

    public static String toHexString(BytesRange bytes) {
        return toHexString(bytes.data, bytes.offset, bytes.length);
    }

    public static String toHexString(byte[] data, int start, int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < start + length; i++) {
            String hex = Integer.toHexString(data[i] & 0xff);
            builder.append(hex.length() == 2 ? hex : "0" + hex);
        }
        return builder.toString();
    }

    /**
     * 将字符串转成ASCII编码的字节数组
     * @param asciiStr ASCII编码的字符串
     * @return asciiStr对应的ASCII码字节数组
     */
    public static byte[] toByteArray(String asciiStr) {
        return asciiStr.getBytes(ASCII_CHARSET);
    }

    /**
     * 将一个数字拆分成byte数组
     * 效果最终相当于byte[]展开成一个大的数字
     * <p>
     * 计算过程:
     * 将最低位，赋值给数组的最末尾
     * 将最高位，赋值给数组的最开头
     * 
     * @param val 要拆分的数值
     * @param cnt 要拆分成字节数组的长度
     * @return 拆分后的数组
     */
    public static byte[] splitIntInBytes(int val, int cnt) {
        byte[] r = new byte[cnt];
        for (int i = cnt - 1; i >= 0; i--) {
            r[i] = (byte) ((val >>> (cnt - 1 - i) * 8) & 0xff);
        }
        return r;
    }

    /**
     * 将bytes合并成一个int数据
     * 效果相当于在内存中一个连续的字节段，表示的一个实际的整型值
     * 将开头赋值给高位
     * 将末尾赋值给低位
     * 以4个字节为例
     * bytes[0] bytes[1] bytes[2] bytes[3]
     * 相当于
     * bytes[0] 00000000 00000000 00000000
     * 00000000 bytes[1] 00000000 00000000
     * 00000000 00000000 bytes[2] 00000000
     * 00000000 00000000 00000000 bytes[3]
     * 
     * @param bytes
     * @return
     */
    public static int joinBytesToUnsignedInt(byte[] bytes) {
        int r = 0;
        for (int i = 0, cnt = bytes.length; i < cnt; i++) {
            int mask = 0xff << ((cnt - i - 1) * 8);
            r = r | ((bytes[i] << ((cnt - i - 1) * 8)) & mask);
        }
        return r;
    }

    public static int joinBytesToUnsignedInt(BytesRange bytesRange) {
        int r = 0;
        for (int i = bytesRange.offset, cnt = bytesRange.length + bytesRange.offset; i < cnt; i++) {
            int mask = 0xff << ((cnt - i - 1) * 8);
            r = r | ((bytesRange.data[i] << ((cnt - i - 1) * 8)) & mask);
        }
        return r;
    }

    /**
     * 将一个byte转为hex表示的字符串
     * 
     * 与Integer#toHexString不同，此方法不会省去前缀的0
     * @param b 要转换的byte
     * @return 2位的16进制全小写字符串
     */
    public static String byte2Hex(byte b) {
        String hex = Integer.toHexString(b & 0xff);
        if (hex.length() < 2) {
            return "0" + hex;
        } else {
            return hex;
        }
    }

    /**
     * 将 hex表示的字符串转为byte数组
     * 
     * 从第一个字符开始，每两个字符，解释为一个字节
     * 
     * 相比 Integer.parseInt(s, 16),此方法性能提升5倍左右
     * 
     * @param hex 要转换的字符串
     * @return 字符串表示的数组
     */
    public static byte[] hex2Bytes(String hex) {
        if (hex == null) {
            throw new NumberFormatException("null");
        }
        int hexLen = hex.length();
        if (hexLen % 2 != 0) {
            throw new IllegalArgumentException("The length of " + hex + " is not an even number.");
        }
        char[] hexChars = hex.toCharArray();
        byte[] bytes = new byte[hexLen / 2];
        for (int i = 0; i < hexLen; i += 2) {
            bytes[i / 2] = (byte) (Character.digit(hexChars[i], 16) * 16
                    + Character.digit(hexChars[i + 1], 16));
        }
        return bytes;
    }

    /**
     * 求两个int值的平均值
     * 
     * 此方法可以避免两数之和超过int最大值之后造成的溢出
     * 
     * 此方法会向下取整
     * 注意：
     * avg(Integer.MAX_VALUE,Integer.MIN_VALUE)->-1,并不是0
     * 
     * 如果采取round up可以得到0
     */
    public static int avg(int a, int b) {
        // round up
        // return (a | b) - ((a ^ b) >> 1);

        // round down
        return (a >> 1) + (b >> 1) + ((a & b) & 1);
    }

    /**
     * 把int转成对于长度的字符串，如果长度不够则前补0
     * 
     * @param val 要表示的数值
     * @param size 长度
     * @return size长度的val字符串
     */
    public static String intToString(int val, int size) {
        String lenStr = Integer.toString(val);
        while (lenStr.length() < size) {
            lenStr = "0" + lenStr;
        }
        return lenStr;
    }

    private final static Encoder base64Encoder = Base64.getEncoder();

    private final static Decoder base64Decoder = Base64.getDecoder();

    /**
     * 将字符串转成base64编码的字符串
     * 
     * @param data 要编码的数据
     * @return 使用ASCII编码的base64字符串
     */
    public static String toBase64String(byte[] data) {
        return toAsciiString(base64Encoder.encode(data));
    }

    public static byte[] base64Decode(String data) {
        return base64Decoder.decode(BitUtil.toByteArray(data));
    }
}