/*
 * Copyright 2022 Shang Yehua <niceshang@outlook.com>
 */
package com.zdsyh.blueland.bravet8583.impl;

import java.nio.charset.Charset;

import com.zdsyh.blueland.bravet8583.utils.BitUtil;
import com.zdsyh.blueland.bravet8583.utils.EbcdicTranslator;

/**
 *
 * 一个字节数组的片段
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2022-02-08  10:27
 *
 */
public class BytesRange {

    public static final BytesRange NONE = new BytesRange(new byte[0], 0, 0);

    static {
        NONE.strValue = "";
        NONE.bytes = NONE.data;
        NONE.hash = 1;
    }

    public final byte[] data;

    public final int offset;

    public final int length;

    private byte[] bytes;

    private String strValue;

    private int hash;

    public static BytesRange of(byte[] data, int offset, int length) {
        if (data == null || data.length == 0) {
            return NONE;
        }

        //合法性校验
        if (offset < 0 || length < 0) {
            throw new IllegalArgumentException("offset or length is negative.");
        }
        if (data.length < offset || data.length < (offset + length)) {
            throw new IllegalArgumentException("The length of data is not long enough.");
        }

        return new BytesRange(data, offset, length);
    }

    public static BytesRange of(byte[] data) {
        if (data == null || data.length == 0) {
            return NONE;
        }
        return new BytesRange(data, 0, data.length);
    }

    public static BytesRange join(BytesRange... ranges) {
        return join(0, ranges);
    }

    private static BytesRange join(int start, BytesRange... ranges) {
        if (ranges == null || ranges.length == 0 || start >= ranges.length) {
            return NONE;
        }
        if (ranges.length - start == 1) {
            return ranges[start];
        }
        BytesRange acc = ranges[start];
        for (int i = start + 1; i < ranges.length; i++) {
            if (acc.data == ranges[i].data) {
                acc = acc.join(ranges[i]);
            } else {
                return acc.join(join(i, ranges));
            }
        }
        return acc;
    }

    private BytesRange(byte[] data, int offset, int length) {
        this.data = data;
        this.offset = offset;
        this.length = length;
    }

    private BytesRange(byte[] data) {
        this.data = data;
        this.offset = 0;
        this.length = data.length;
    }
    //TODO w18404 添加E转码
    public String getStrValue(Charset charset) {
        if (strValue == null) {
            if (charset.equals(BitUtil.EBCDIC_CHARSET)){
                strValue = EbcdicTranslator.fromEbcdicBytes(this.getBytes());
            }else{
                strValue = new String(data, offset, length, charset);
            }
        }
        return strValue;
    }

    public String toStringVal(Charset charset) {
        return new String(data, offset, length, charset);
    }

    public byte[] bytesCopy() {
        byte[] bytes = new byte[length];
        System.arraycopy(data, offset, bytes, 0, length);
        return bytes;
    }

    public byte byteAt(int i) {
        return data[offset + i];
    }

    public byte[] getBytes() {
        if (bytes == null) {
            bytes = new byte[length];
            System.arraycopy(data, offset, bytes, 0, length);
        }
        return bytes;
    }
    /**
     * w18404
     * @param length
     * @return
     */
    public byte[] getBytes(int length) {
        if (bytes == null) {
            bytes = new byte[length];
            System.arraycopy(data, offset, bytes, 0, length);
        }
        return bytes;
    }

    public BytesRange subrange(int offset, int newLen) {
        if (offset + newLen > length) {
            throw new IllegalArgumentException("The length of data is not long enough.");
        }
        if (newLen == 0) {
            return NONE;
        }
        if (offset == 0 && newLen == length) {
            return this;
        }
        return new BytesRange(data, this.offset + offset, newLen);
    }

    public BytesRange jump(int jump) {
        if (offset + jump < 0 || jump > length) {
            throw new IllegalArgumentException("Jump out of range(" + offset + jump + ").");
        }
        if (jump == 0) {
            return this;
        }
        if (jump == length) {
            return NONE;
        }
        return new BytesRange(data, this.offset + jump, length - jump);
    }

    /**
     * 子序列
     * @param newLen 子序列长度
     * @return 新的子序列
     *         与本序列offset相同
     */
    public BytesRange subrange(int newLen) {
        return subrange(0, newLen);
    }

    public BytesRange replace(final int offset, final BytesRange replacement) {
        if (offset < 0 || offset > this.offset + this.length) {
            throw new IllegalArgumentException("offset is negative or length is not enough.");
        }
        BytesRange prefix = this.subrange(offset);
        BytesRange postfix = NONE;
        int repSize = offset + replacement.length;
        if (repSize < this.length) {
            postfix = this.subrange(repSize, this.length - repSize);
        }
        return join(prefix, replacement, postfix);
    }

    public BytesRange join(BytesRange other) {
        if (this.length == 0) {
            return other;
        }
        if (other.length == 0) {
            return this;
        }
        //同底层数据优化
        if (this.data == other.data && (this.offset + this.length == other.offset)) {
            return new BytesRange(data, offset, this.length + other.length);
        }
        byte[] bytes = new byte[length + other.length];
        System.arraycopy(this.data, this.offset, bytes, 0, this.length);
        System.arraycopy(other.data, other.offset, bytes, this.length, other.length);
        return new BytesRange(bytes);
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            for (int i = offset; i < offset + length; i++) {
                h = 31 * h + data[i];
            }
            hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BytesRange)) {
            return false;
        }
        BytesRange other = (BytesRange) obj;
        if (other.length != length) {
            return false;
        }
        if (this.data == other.data && this.offset == other.offset) {
            return true;
        }
        for (int i = 0; i < length; i++) {
            if (data[offset + i] != other.data[other.offset + i]) {
                return false;
            }
        }
        return true;
    }

}
