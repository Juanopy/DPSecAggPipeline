/**
 * Highly inspired by
 * https://github.com/mh-/corona-warn-companion-android
 **/

package com.example.database_test.gmsreadout;


import android.content.Context;
import android.util.Log;

import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
/*
 *                                             Key Data - 18 byte
 *                                            +--------+---------+
 *                                            |  Type  |  Value  |
 *                                            +--------+---------+
 *                                            | short  |unix days| // Days since 1970
 *                                            +--------+---------+
 *                                            |[16]byte|Device ID| // ID of the seen Device
 *                                            +--------+---------+
 *
 *
 *
 *                                                        Protobuf Data - Variable Length - Encoded in one Byte Buffer
 *                                           +------------------------------------------------------------------------------------+
 *                                           |                                                                                    |
 *                                           |                                   +--------+---------+                             |
 *                                           | ContactRecords -> []ScanRecord -> |  Type  |  Value  |                             |
 *                                           |                                   +--------+---------+                             |
 *                                           |                                   | uint32 |         | //  timestamp               |
 *                                           |                                   +--------+---------+                             |
 *                           +--------+      |                                   | bytes  |         | //  rssi_multiple_values    |
 *                           |CONTENT |      |                                   +--------+---------+                             |
 *                    +----> +--+-----+      |                                   | int32  |         | //  rssi                    |
 *                    |         |            |                                   +--------+---------+                             |
 *                    |         +----------> |                                   | bytes  |         | //  aem_multiple_values     |
 *                Data|                      |                                   +--------+---------+                             |
 *  Keys         +----+---+                  |                                   | bytes  |         | //  aem                     |
 * +----+        |    |   |                  |                                   +--------+---------+                             |
 * |    |        | +--+-+ |                  |                                   | uint32 |         | //  previous_scan_timestamp |
 * |ABCD+--------+>+----+ |                  |                                   +--------+---------+                             |
 * |    |        |        |                  |                                   | double |         | //  latitude                |
 * |EFGH+----+   | +----+ |                  |                                   +--------+---------+                             |
 * |    |    +---+>+----+ |                  |                                   | double |         | //  longitude               |
 * |IJKL+--+     |        |                  |                                   +--------+---------+                             |
 * |    |  |     | +----+ |                  |                                                                                    |
 * +----+  +-----+>+----+ |                  +------------------------------------------------------------------------------------+
 *               |        |
 *               +--------+
 */

public class ContactDB {
    private static final String TAG = "ContactDB";

    private final File path;
    private final boolean createDB;
    private DB db;

    public ContactDB(File path, boolean createDB) {
        this.path = path;
        this.createDB = createDB;
    }

    public ContactDB(File path) {
        this(path, true);
    }

    protected static File getCacheDir(Context context) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        return cacheDir;
    }

    public void open() throws IOException {
        // Now open our locally cached copy
        Options options = new Options();
        options.createIfMissing(this.createDB);
        options.compressionType(CompressionType.NONE);
        DBFactory factory = new Iq80DBFactory();

        db = factory.open(this.path, options);

        if (db != null) {
            Log.d(TAG, "Opened LevelDB.");
        } else {
            throw new IOException("LevelDB not found.");
        }
    }

    public void close() throws IOException {
        db.close();
        Log.d(TAG, "Closed LevelDB.");
        db = null;
    }

    public DB getDb() {
        return db;
    }
}
