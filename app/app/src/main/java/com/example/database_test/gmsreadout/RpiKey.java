/**
 * Highly inspired by
 * https://github.com/mh-/corona-warn-companion-android
 **/

package com.example.database_test.gmsreadout;

import com.google.android.gms.common.util.Hex;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class RpiKey {
    byte[] rpiBytes = new byte[16];
    int daysSinceEpochUTC;

    RpiKey(ByteBuffer buffer) {
        daysSinceEpochUTC = buffer.getShort();  // get first 2 bytes: date
        buffer.get(rpiBytes); // get the next 16 bytes:
    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(18);
        buffer.putShort((short) daysSinceEpochUTC);
        buffer.put(rpiBytes);
        return buffer.array();
    }

    @Override
    public String toString() {
        return "RpiKey{" +
                "daysSinceEpochUTC=" + daysSinceEpochUTC +
                "rpiBytes=" + Hex.bytesToStringUppercase(rpiBytes) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpiKey rpiKey = (RpiKey) o;
        return daysSinceEpochUTC == rpiKey.daysSinceEpochUTC && Arrays.equals(rpiBytes, rpiKey.rpiBytes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(daysSinceEpochUTC);
        result = 31 * result + Arrays.hashCode(rpiBytes);
        return result;
    }
}
