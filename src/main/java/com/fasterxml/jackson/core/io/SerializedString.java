package com.fasterxml.jackson.core.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.fasterxml.jackson.core.SerializableString;

/**
 * String token that can lazily serialize String contained and then reuse that
 * serialization later on. This is similar to JDBC prepared statements, for example,
 * in that instances should only be created when they are used more than use;
 * prime candidates are various serializers.
 *<p>
 * Class is final for performance reasons and since this is not designed to
 * be extensible or customizable (customizations would occur in calling code)
 */
public class SerializedString implements SerializableString
{
    protected final String _value;

    /* 13-Dec-2010, tatu: Whether use volatile or not is actually an important
     *   decision for multi-core use cases. Cost of volatility can be non-trivial
     *   for heavy use cases, and serialized-string instances are accessed often.
     *   Given that all code paths with common Jackson usage patterns go through
     *   a few memory barriers (mostly with cache/reuse pool access) it seems safe
     *   enough to omit volatiles here, given how simple lazy initialization is.
     *   This can be compared to how {@link String#intern} works; lazily and
     *   without synchronization or use of volatile keyword.
     */
    
    protected /*volatile*/ byte[] _quotedUTF8Ref;

    protected /*volatile*/ byte[] _unquotedUTF8Ref;

    protected /*volatile*/ char[] _quotedChars;

    public SerializedString(String v) { _value = v; }

    /*
    /**********************************************************
    /* API
    /**********************************************************
     */

//  @Override
    public final String getValue() { return _value; }
    
    /**
     * Returns length of the String as characters
     */
//  @Override
    public final int charLength() { return _value.length(); }
    
//  @Override
    public final char[] asQuotedChars()
    {
        char[] result = _quotedChars;
        if (result == null) {
            result = JsonStringEncoder.getInstance().quoteAsString(_value);
            _quotedChars = result;
        }
        return result;
    }

    /**
     * Accessor for accessing value that has been quoted using JSON
     * quoting rules, and encoded using UTF-8 encoding.
     */
//  @Override
    public final byte[] asUnquotedUTF8()
    {
        byte[] result = _unquotedUTF8Ref;
        if (result == null) {
            result = JsonStringEncoder.getInstance().encodeAsUTF8(_value);
            _unquotedUTF8Ref  = result;
        }
        return result;
    }

    /**
     * Accessor for accessing value as is (without JSON quoting)
     * encoded using UTF-8 encoding.
     */
//  @Override
    public final byte[] asQuotedUTF8()
    {
        byte[] result = _quotedUTF8Ref;
        if (result == null) {
            result = JsonStringEncoder.getInstance().quoteAsUTF8(_value);
            _quotedUTF8Ref = result;
        }
        return result;
    }

    /*
    /**********************************************************
    /* Additional 2.0 methods for appending/writing contents
    /**********************************************************
     */

//  @Override
    public int appendQuotedUTF8(byte[] buffer, int offset)
    {
        byte[] result = _quotedUTF8Ref;
        if (result == null) {
            result = JsonStringEncoder.getInstance().quoteAsUTF8(_value);
            _quotedUTF8Ref = result;
        }
        final int length = result.length;
        if ((offset + length) > buffer.length) {
            return -1;
        }
        System.arraycopy(result, 0, buffer, offset, length);
        return length;
    }

//  @Override
    public int appendQuoted(char[] buffer, int offset)
    {
        char[] result = _quotedChars;
        if (result == null) {
            result = JsonStringEncoder.getInstance().quoteAsString(_value);
            _quotedChars = result;
        }
        final int length = result.length;
        if ((offset + length) > buffer.length) {
            return -1;
        }
        System.arraycopy(result, 0, buffer, offset, length);
        return length;
    }

//  @Override
    public int appendUnquotedUTF8(byte[] buffer, int offset)
    {
        byte[] result = _unquotedUTF8Ref;
        if (result == null) {
            result = JsonStringEncoder.getInstance().encodeAsUTF8(_value);
            _unquotedUTF8Ref  = result;
        }
        final int length = result.length;
        if ((offset + length) > buffer.length) {
            return -1;
        }
        System.arraycopy(result, 0, buffer, offset, length);
        return length;
    }

//  @Override
    public int appendUnquoted(char[] buffer, int offset)
    {
        String str = _value;
        final int length = str.length();
        if ((offset + length) > buffer.length) {
            return -1;
        }
        str.getChars(0,  length, buffer, offset);
        return length;
    }

//  @Override
    public int writeQuotedUTF8(OutputStream out) throws IOException
    {
        byte[] result = _quotedUTF8Ref;
        if (result == null) {
            result = JsonStringEncoder.getInstance().quoteAsUTF8(_value);
            _quotedUTF8Ref = result;
        }
        final int length = result.length;
        out.write(result, 0, length);
        return length;
    }

//  @Override
    public int writeUnquotedUTF8(OutputStream out) throws IOException
    {
        byte[] result = _unquotedUTF8Ref;
        if (result == null) {
            result = JsonStringEncoder.getInstance().encodeAsUTF8(_value);
            _unquotedUTF8Ref  = result;
        }
        final int length = result.length;
        out.write(result, 0, length);
        return length;
    }

//  @Override
    public int putQuotedUTF8(ByteBuffer buffer)
    {
        byte[] result = _quotedUTF8Ref;
        if (result == null) {
            result = JsonStringEncoder.getInstance().quoteAsUTF8(_value);
            _quotedUTF8Ref = result;
        }
        final int length = result.length;
        if (length > buffer.remaining()) {
            return -1;
        }
        buffer.put(result, 0, length);
        return length;
    }

//  @Override
    public int putUnquotedUTF8(ByteBuffer buffer)
    {
        byte[] result = _unquotedUTF8Ref;
        if (result == null) {
            result = JsonStringEncoder.getInstance().encodeAsUTF8(_value);
            _unquotedUTF8Ref  = result;
        }
        final int length = result.length;
        if (length > buffer.remaining()) {
            return -1;
        }
        buffer.put(result, 0, length);
        return length;
    }

    
    /*
    /**********************************************************
    /* Standard method overrides
    /**********************************************************
     */

    @Override
    public final String toString() { return _value; }
    
    @Override
    public final int hashCode() { return _value.hashCode(); }

    @Override
    public final boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null || o.getClass() != getClass()) return false;
        SerializedString other = (SerializedString) o;
        return _value.equals(other._value);
    }
}
