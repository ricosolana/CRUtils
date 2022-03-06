package com.crazicrafter1.crutils;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Tainted;
import java.util.Arrays;

public class MutableString implements Appendable, CharSequence {
    private char[] value;
    private int count;
    private int offset;
    //private int testOffset;

    public MutableString() {
        this(new char[15], 0, 0);
    }

    public MutableString(@Nonnull String str) {
        this(str.toCharArray(), str.length(), 0);
    }

    private MutableString(char[] buf) {
        this(buf, buf.length, 0);
    }

    private MutableString(char[] buf, int count, int offset) {
        if (count > buf.length)
            throw new IllegalArgumentException("Length cannot exceed buffer");

        this.value = buf;
        this.count = count;
        this.offset = offset;
        //this.testOffset = offset;
    }

    @Nonnull
    @CheckReturnValue
    public static MutableString copy(CharSequence seq) {
        return copy(seq, 0);
    }

    @Nonnull
    @CheckReturnValue
    public static MutableString copy(CharSequence seq, int beginIndex) {
        return copy(seq, beginIndex, seq.length());
    }

    @Nonnull
    @CheckReturnValue
    public static MutableString copy(@Nonnull CharSequence seq, int beginIndex, int endIndex) {
        if (beginIndex >= endIndex)
            throw new IllegalArgumentException("Start cannot overlap or exceed end");

        if (endIndex > seq.length())
            endIndex = seq.length();

        if (beginIndex < 0)
            beginIndex = 0;

        char[] buf = new char[endIndex - beginIndex];

        for (int i=beginIndex; i < endIndex; i++) {
            buf[i-beginIndex] = seq.charAt(i);
        }
        return new MutableString(buf);
    }



    @Nonnull
    @CheckReturnValue
    public static MutableString mutable(@Nonnull MutableString matcher) {
        return mutable(matcher, 0);
    }

    @Nonnull
    @CheckReturnValue
    public static MutableString mutable(@Nonnull MutableString matcher, int beginIndex) {
        return mutable(matcher, beginIndex, matcher.count);
    }

    @Nonnull
    @CheckReturnValue
    public static MutableString mutable(@Nonnull MutableString matcher, int beginIndex, int endIndex) {
        return new MutableString(matcher.value, endIndex - beginIndex, matcher.offset + beginIndex);
    }



    @Nonnull
    @CheckReturnValue
    public static MutableString mutable(@Nonnull char[] buf) {
        return mutable(buf, 0);
    }

    @Nonnull
    @CheckReturnValue
    public static MutableString mutable(@Nonnull char[] buf, int beginIndex) {
        return mutable(buf, beginIndex, buf.length);
    }

    @Nonnull
    @CheckReturnValue
    public static MutableString mutable(@Nonnull char[] buf, int beginIndex, int endIndex) {
        return new MutableString(buf, endIndex - beginIndex, beginIndex);
    }



    public void ensureExtraCapacity(int extra) {
        if (extra > 0) {
            value = Arrays.copyOf(value,
                    newCapacity(extra + value.length));
        }
    }

    public void ensureCapacity(int minimumCapacity) {
        if (minimumCapacity - value.length > 0) {
            value = Arrays.copyOf(value,
                    newCapacity(minimumCapacity));
        }
    }

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private int newCapacity(int minCapacity) {
        // overflow-conscious code
        int newCapacity = (value.length << 1) + 2;
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;

        return (newCapacity <= 0 || MAX_ARRAY_SIZE - newCapacity < 0)
                ? hugeCapacity(minCapacity)
                : newCapacity;
    }

    private int hugeCapacity(int minCapacity) {
        if (Integer.MAX_VALUE - minCapacity < 0) { // overflow
            throw new OutOfMemoryError();
        }
        return Math.max(minCapacity, MAX_ARRAY_SIZE);
    }



    @Nonnull
    public MutableString append(CharSequence seq, int beginIndex) {
        return append(seq, beginIndex, seq.length());
    }

    @Nonnull
    @Override
    public MutableString append(CharSequence seq) {
        return append(seq, 0, seq.length());
    }

    @Nonnull
    @Override
    public MutableString append(CharSequence seq, int beginIndex, int endIndex) {
        if (beginIndex == endIndex)
            return this;

        if (beginIndex > endIndex)
            throw new IllegalArgumentException("Start cannot overlap or exceed end");

        if (beginIndex < 0)
            beginIndex = 0;

        if (endIndex > seq.length())
            endIndex = seq.length();

        int len = endIndex - beginIndex;
        ensureExtraCapacity(len);

        // add to this
        for (; beginIndex < endIndex;
             beginIndex++, count++)
            value[count + offset] = seq.charAt(beginIndex);

        return this;
    }

    @Nonnull
    @Override
    public MutableString append(char c) {
        ensureExtraCapacity(1);
        value[count++] = c;
        return this;
    }



    @Override
    public int length() {
        return count;
    }

    @CheckReturnValue
    public int capacity() {
        return value.length - offset;
    }

    @CheckReturnValue
    public int totalCapacity() {
        return value.length;
    }



    public char[] getChars() {
        return value;
    }

    public void getChars(char[] out) {
        if (count >= 0)
            System.arraycopy(value, offset, out, 0, count);
    }

    public char[] getCharsCopy() {
        char[] copy = new char[count];
        System.arraycopy(value, offset, copy, 0, count);
        return copy;
    }


    public boolean startsWith(char ch) {
        if (offset == value.length)
            return false;
        return value[offset] == ch;
    }

    //public boolean is(char ch) {
    //    return value[testOffset] == ch;
    //}
    //
    //public boolean nextIs(char ch) {
    //    if (value)
    //    return testOffset
    //}




    @CheckReturnValue
    @Override
    public char charAt(int index) {
        return value[offset + index];
    }

    @CheckReturnValue
    @Override
    public CharSequence subSequence(int start, int end) {
        return copy(this, start, end);
    }

    public MutableString sub(int startIndex) {
        return sub(startIndex, count);
    }

    public MutableString sub(int beginIndex, int endIndex) {
        this.count = endIndex - beginIndex;
        this.offset += beginIndex;
        return this;
    }

    @CheckReturnValue
    public String substring(int startIndex) {
        return copy(this, startIndex).toString();
    }

    @CheckReturnValue
    public String substring(int startIndex, int endIndex) {
        return copy(this, startIndex, endIndex).toString();
    }

    /*
     * Index of specialty
     */

    public int indexOf(char ch) {
        return indexOfByOccurrence(ch, 1, 0);
    }

    public int indexOf(char ch, int beginIndex) {
        return indexOfByOccurrence(ch, 1, beginIndex);
    }

    public int indexOfByOccurrence(char ch, int occurrence) {
        return indexOfByOccurrence(ch, occurrence, 0);
    }

    public int indexOfByOccurrence(char ch, int occurrence, int beginIndex) {
        if (beginIndex < 0)
            beginIndex = 0;

        int max = count;
        if (beginIndex >= max)
            return -1;

        for (int i = beginIndex; i < count; i++)
            if (value[offset + i] == ch && --occurrence == 0)
                return i;

        return -1;
    }




    public int lastIndexOf(char ch) {
        return lastIndexOfByOccurrence(ch, 1);
    }

    public int lastIndexOf(char ch, int beginIndex) {
        return lastIndexOfByOccurrence(ch, 1, beginIndex);
    }

    public int lastIndexOfByOccurrence(char ch, int occurrence) {
        return lastIndexOfByOccurrence(ch, occurrence, count-1);
    }

    public int lastIndexOfByOccurrence(char ch, int occurrence, int beginIndex) {
        if (beginIndex < 0)
            return -1;
        //beginIndex = 0;

        int max = count - 1;
        if (beginIndex >= max)
            //return -1;
            beginIndex = max;

        for (int i = beginIndex; i >= 0; i--)
            if (value[offset + i] == ch && --occurrence == 0)
                return i;

        return -1;
    }




    /**
     * Keep the string on the right of the specified char, optionally keeping the char
     * @param startCh the char to look for
     * @return this
     */
    @Nonnull
    public MutableString subRight(char startCh) {
        return subRight(startCh, 0);
    }

    @Nonnull
    public MutableString subRight(char startCh, int startIndex) {
        return subRight(startCh, startIndex, 1);
    }

    @Nonnull
    public MutableString subRight(char startCh, int startIndex, int occurrence) {
        return subRight(startCh, startIndex, occurrence, false);
    }

    @Nonnull
    public MutableString subRight(char startCh, int startIndex, int occurrence, boolean include) {
        int index = indexOfByOccurrence(startCh, occurrence, startIndex);
        if (index != -1)
            return sub(index + (include ? 0 : 1));
        return this;
    }



    /**
     * Keep the string on the left of the specified char, optionally keeping the char
     * @param startCh the char to look for
     * @return this
     */
    @Nonnull
    public MutableString subLeft(char startCh) {
        return subLeft(startCh, 0);
    }

    @Nonnull
    public MutableString subLeft(char startCh, int startIndex) {
        return subLeft(startCh, startIndex, 1);
    }

    @Nonnull
    public MutableString subLeft(char startCh, int startIndex, int occurrence) {
        return subLeft(startCh, startIndex, occurrence, false);
    }

    @Nonnull
    public MutableString subLeft(char startCh, int startIndex, int occurrence, boolean include) {
        int index = indexOfByOccurrence(startCh, occurrence, startIndex);
        if (index != -1)
            return sub(0, index + (include ? 1 : 0));
        return this;
    }


    /**
     * Remove chars to the left of the string to a final length of toLength
     * @param toLength the new length
     * @return this
     */
    public MutableString limitLeft(int toLength) {
        if (count >= toLength)
            return sub(count - toLength);
        return this;
    }

    /**
     * Remove chars to the right of the string to a final length of toLength
     * @param toLength the new length
     * @return this
     */
    public MutableString limitRight(int toLength) {
        if (count >= toLength)
            return sub(0, toLength);
        return this;
    }



    /**
     * Remove clipCount chars from the left of the string
     * @param clipCount how much to remove
     * @return this
     */
    public MutableString clipLeft(int clipCount) {
        if (count >= clipCount)
            return sub(clipCount);
        return this;
    }

    /**
     * Remove clipCount chars from the right of the string
     * @param clipCount how much to remove
     * @return this
     */
    public MutableString clipRight(int clipCount) {
        if (count - clipCount >= 0)
            return sub(0, count - clipCount);
        return this;
    }

    @Override
    public String toString() {
        return new String(value, offset, count);
    }
}
