package edu.stanford.pepe.jdt;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Formatter;

/**
 * Simple wrapper for a SHA-1 fingerprint
 * @author jtamayo
 */
public class Fingerprint implements Serializable {

    private final byte[] fingerprint;
    
    public Fingerprint(byte[] fingerprint) {
        if (fingerprint == null) {
            throw new NullPointerException("fingerprint cannot be null");
        }
        if (fingerprint.length != 20) {
            throw new IllegalArgumentException("SHA-1 should have 160 bits");
        }
        this.fingerprint = new byte[fingerprint.length];
        System.arraycopy(fingerprint, 0, this.fingerprint, 0, fingerprint.length);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(fingerprint);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Fingerprint))
            return false;
        Fingerprint other = (Fingerprint) obj;
        if (!Arrays.equals(fingerprint, other.fingerprint))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter();
        for (byte b : fingerprint) {
            sb.append(f.format("%02X", b));
        }
        return sb.toString();
    }
    
}
