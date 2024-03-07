/*
 * Copyright 2022 Shang Yehua <niceshang@outlook.com>
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

import java.util.Arrays;
import java.util.Random;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * BytesRange 测试类
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2022-02-22  15:29
 *
 */
public class BytesRangeTest {

    @Test
    public void nullDataTest() {
        BytesRange range = BytesRange.of((byte[]) null);
        assertTrue(range == BytesRange.NONE);
    }

    @Test
    public void nullDataTest2() {
        BytesRange range = BytesRange.of((byte[]) null, 0, 0);
        assertTrue(range == BytesRange.NONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullDataTest3() {
        BytesRange.of(new byte[10], -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullDataTest4() {
        BytesRange.of(new byte[10], 0, -3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullDataTest5() {
        BytesRange.of(new byte[10], 0, 11);
    }

    @Test
    public void testGetBytes() {
        byte[] data1 = { 1, 2, 3, 4, 5 };
        BytesRange range1 = BytesRange.of(data1, 0, 4);

        byte[] data2 = { 1, 2, 3, 4 };
        BytesRange range2 = BytesRange.of(data2);

        assertArrayEquals(data2, range1.getBytes());
        assertArrayEquals(data2, range2.getBytes());
    }

    @Test
    public void testGetStrValue() {
        byte[] data1 = "zhuzhu".getBytes(BitUtil.ASCII_CHARSET);
        BytesRange range1 = BytesRange.of(data1);

        assertArrayEquals(data1, range1.getBytes());
        assertEquals("zhuzhu", range1.toStringVal(BitUtil.ASCII_CHARSET));
        assertEquals(range1.toStringVal(BitUtil.ASCII_CHARSET),
                range1.toStringVal(BitUtil.ASCII_CHARSET));
    }

    @Test
    public void testHashCode() {
        byte[] data1 = { 1, 2, 3, 4, 5 };
        BytesRange range1 = BytesRange.of(data1, 0, 4);

        byte[] data2 = { 1, 2, 3, 4 };
        BytesRange range2 = BytesRange.of(data2);

        BytesRange range3 = BytesRange.of(data1);
        Assert.assertEquals(range1.hashCode(), range2.hashCode());
        assertNotEquals(range1.hashCode(), range3.hashCode());
    }

    @Test
    public void testEquals() {
        byte[] data1 = { 1, 2, 3, 4, 5 };
        BytesRange range1 = BytesRange.of(data1, 0, 4);

        byte[] data2 = { 1, 2, 3, 4 };
        BytesRange range2 = BytesRange.of(data2);

        BytesRange range3 = BytesRange.of(data1);
        assertTrue(range1.equals(range2));
        assertFalse(range1.equals(range3));
    }

    @Test
    public void testJoin() {
        byte[] bytes = new byte[512];
        new Random().nextBytes(bytes);

        BytesRange range1 = BytesRange.of(bytes, 0, 100);
        BytesRange range2 = BytesRange.of(bytes, 100, 50);
        BytesRange range3 = BytesRange.of(bytes, 150, 100);
        // BytesRange range4 = BytesRange.of(bytes,250,50);
        BytesRange range5 = BytesRange.of(bytes, 300, 40);
        BytesRange range6 = BytesRange.of(bytes, 340, 60);
        BytesRange range7 = BytesRange.of(bytes, 400, 100);
        BytesRange range8 = BytesRange.of(bytes, 500, 12);

        byte[] some = new byte[50];
        BytesRange range4 = BytesRange.of(some);

        BytesRange rangeTotal1 = range1.join(range2).join(range3).join(range4).join(range5).join(range6).join(range7)
                .join(range8);
        BytesRange rangeTotal2 = BytesRange.join(range1, range2, range3, range4, range5, range6, range7, range8);
        assertEquals(rangeTotal1, rangeTotal2);
    }

    @Test
    public void testSubrange() {
        byte[] bytes = new byte[512];
        new Random().nextBytes(bytes);
        BytesRange range = BytesRange.of(bytes);
        BytesRange range1 = BytesRange.of(bytes, 0, 200);
        BytesRange range2 = BytesRange.of(bytes, 0, 100);
        BytesRange range3 = BytesRange.of(bytes, 150, 50);
        BytesRange range5 = BytesRange.of(bytes, 300, 40);

        BytesRange range6 = range1.subrange(100);
        BytesRange range7 = range1.subrange(150, 50);
        BytesRange range8 = range.subrange(300, 40);

        assertEquals(range2, range6);
        assertEquals(range3, range7);
        assertEquals(range5, range8);
    }

    @Test
    public void testReplace() {
        byte[] bytes = new byte[512];
        new Random().nextBytes(bytes);

        byte[] copy = Arrays.copyOf(bytes, 512);
        byte[] replace = new byte[100];
        new Random().nextBytes(replace);

        for (int i = 100; i < 200; i++) {
            copy[i] = replace[i - 100];
        }

        BytesRange range0 = BytesRange.of(bytes);
        BytesRange replacement = BytesRange.of(replace);
        BytesRange range1 = range0.replace(100, replacement);

        assertEquals(range1, BytesRange.of(copy));

        BytesRange range2 = BytesRange.of(bytes, 10, 20);
        BytesRange range3 = range2.replace(4, replacement);
        BytesRange range4 = BytesRange.of(copy, 10, 4).join(replacement);
        assertEquals(range3, range4);

        BytesRange range5 = BytesRange.of(bytes, 220, 200);
        BytesRange range6 = range5.replace(4, replacement);
        BytesRange range7 = BytesRange.of(copy, 220, 4).join(replacement).join(BytesRange.of(copy, 324, 96));
        assertEquals(range6, range7);

    }

    @Test
    public void jumpTest() {
        byte[] bytes = new byte[512];
        new Random().nextBytes(bytes);

        byte[] copy = Arrays.copyOf(bytes, 512);

        BytesRange range0 = BytesRange.of(bytes, 100, 100);
        BytesRange range01 = range0.jump(50);
        BytesRange range1 = BytesRange.of(copy, 150, 50);
        BytesRange range2 = range0.jump(0);
        BytesRange range3= range0.jump(100);
        assertTrue(range2==range0);
        assertTrue(range3 == BytesRange.NONE);
        assertEquals(range1, range01);

    }

}
