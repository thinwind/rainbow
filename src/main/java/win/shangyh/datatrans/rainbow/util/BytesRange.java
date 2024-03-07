/*
 * Copyright 2022 Shang Yehua <niceshang@outlook.com>
 */
package win.shangyh.datatrans.rainbow.util;

import java.util.Arrays;

import java.nio.charset.Charset;

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
        NONE.bytes = new byte[0];
        NONE.hash = -1;
    }

    public final int length;

    private String hex;

    private byte[] bytes;

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
        return new BytesRange(data, false);
    }

    public static BytesRange join(BytesRange... ranges) {
        if (ranges == null || ranges.length == 0) {
            return NONE;
        }

        int totalLen = 0;
        for (BytesRange range : ranges) {
            totalLen += range.length;
        }
        byte[] compdBytes = new byte[totalLen];
        int offset = 0;
        for (BytesRange range : ranges) {
            System.arraycopy(range.bytes, 0, compdBytes, offset, range.length);
            offset += range.length;
        }
        return new BytesRange(compdBytes, true);
    }

    private BytesRange(byte[] data, int offset, int length) {
        this.length = length;
        this.bytes = new byte[length];
        System.arraycopy(data, offset, bytes, 0, length);
    }

    private BytesRange(byte[] data, boolean innerBuild) {
        if (innerBuild) {
            this.bytes = data;
            this.length = data.length;
        } else {
            this.bytes = Arrays.copyOf(data, data.length);
            this.length = bytes.length;
        }
    }

    public String toStringVal(Charset charset) {
        return new String(bytes, charset);
    }

    public String toHexString() {
        if (hex == null) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                int v = bytes[i] & 0xff;
                if (v < 16) {
                    builder.append("0");
                }
                builder.append(Integer.toHexString(v));
            }
            hex = builder.toString();
        }
        return hex;
    }

    public byte byteAt(int i) {
        return bytes[i];
    }

    public byte[] getBytes() {
        byte[] copy = new byte[bytes.length];
        System.arraycopy(bytes, 0, copy, 0, bytes.length);
        return copy;
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
        return new BytesRange(bytes, offset, newLen);
    }

    public BytesRange jump(int jump) {
        if (jump < 0 || jump > length) {
            throw new IllegalArgumentException("Jump out of range(" + jump + ").");
        }
        if (jump == 0) {
            return this;
        }
        if (jump == length) {
            return NONE;
        }
        return new BytesRange(bytes, jump, length - jump);
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
        if (offset < 0 || offset > this.length) {
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

        byte[] compdBytes = new byte[length + other.length];
        System.arraycopy(this.bytes, 0, compdBytes, 0, this.length);
        System.arraycopy(other.bytes, 0, compdBytes, this.length, other.length);
        return new BytesRange(compdBytes, true);
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            for (int i = 0; i < length; i++) {
                h = 31 * h + bytes[i];
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
        if (this.bytes == other.bytes) {
            return true;
        }

        if (other.length != length) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            if (bytes[i] != other.bytes[i]) {
                return false;
            }
        }
        return true;
    }

}
