/**
 * Highly inspired by
 * https://github.com/mh-/corona-warn-companion-android
 **/

package com.example.database_test.gmsreadout;


import static com.example.database_test.MainActivity.lastlabel;
import static com.example.database_test.MainActivity.lastlabelprob;
import static com.example.database_test.MainActivity.vectorlist;
import static com.example.database_test.MainActivity.vectors;
import static com.example.database_test.gmsreadout.ContactDB.getCacheDir;

import android.content.Context;

import com.example.database_test.MainActivity;
import com.example.database_test.audioclassify.AcousticSceneClassification;
import com.google.protobuf.InvalidProtocolBufferException;

import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.ReadOptions;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ActivityContactDB {
    private static final String TAG = "ActivityContactDB";

    public final Context context;
    private final ContactDB db;
    private final GMSContactDB gmsDB;
    private int lastActivity;


    public ActivityContactDB(Context context) {
        this.context = context;

        File dbPath = new File(getCacheDir(context).getPath() + "/" + "extended");
        this.db = new ContactDB(dbPath, true);
        this.gmsDB = new GMSContactDB(context);
    }

    public void open() throws IOException {
        this.db.open();
    }

    public void close() throws IOException {
        this.db.close();
    }

    public Map<RpiKey, ContactRecordsProtos.ContactRecords> readToMap(ContactDB db) throws InvalidProtocolBufferException {
        //Map DB entries to work with them
        Map<RpiKey, ContactRecordsProtos.ContactRecords> data = new HashMap<>();
        ReadOptions readOptions = new ReadOptions();
        readOptions.verifyChecksums(true);
        readOptions.fillCache(true);
        DBIterator iterator = db.getDb().iterator(readOptions);
        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            ByteBuffer buffer = ByteBuffer.wrap(iterator.peekNext().getKey());
            ContactRecordsProtos.ContactRecords records = ContactRecordsProtos.ContactRecords.parseFrom(iterator.peekNext().getValue());
            data.put(new RpiKey(buffer), records);
        }
        return data;
    }

    public void writeActivityData(int activity) throws IOException {

        this.lastActivity = activity;
        //When Activity is set do DB update
        try {
            this.fetchMissingData();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void exportVectors() throws InterruptedException, IOException {
        this.gmsDB.open();
        vectorlist.clear();
        Map<RpiKey, ContactRecordsProtos.ContactRecords> contactDBData = readToMap(db);
        for (Map.Entry<RpiKey, ContactRecordsProtos.ContactRecords> entry : contactDBData.entrySet()) {
            RpiKey k = entry.getKey();
            ContactRecordsProtos.ContactRecords cdbV = contactDBData.get(k);
            for (int a = 0; a < cdbV.getRecordList().size(); a++) {
                String tmpvector = cdbV.getRecord(a).getVectors();
                String decvector = new String(java.util.Base64.getDecoder().decode(tmpvector.trim()));
                vectorlist.add(decvector);
            }
        }
    }

    public void fetchMissingData() throws IOException, InterruptedException {
        com.example.database_test.audioclassify.Utils.executeLater(60 * 1000L,() -> {
            try {
                fetchMissingData();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        });
        this.gmsDB.open();

        System.out.println("db check" + lastActivity + lastlabel + lastlabelprob);

        // Convert DB to Map for better use
        int missing = 0;
        Map<RpiKey, ContactRecordsProtos.ContactRecords> gmsData = readToMap(gmsDB);
        Map<RpiKey, ContactRecordsProtos.ContactRecords> contactDBData = readToMap(db);

        //Iterate over every Entry
        for (Map.Entry<RpiKey, ContactRecordsProtos.ContactRecords> entry : gmsData.entrySet()) {
            RpiKey k = entry.getKey();
            ContactRecordsProtos.ContactRecords gmsV = entry.getValue();
            ContactRecordsProtos.ContactRecords cdbV = contactDBData.get(k);

            //Check if Key exist in new DB
            if (cdbV == null) {
                missing++;
                System.out.println("missing key");
                ContactRecordsProtos.ContactRecords.Builder contactRecordsBuilder = ContactRecordsProtos.ContactRecords.newBuilder();

                //Check if any Key-Scan is missing
                for (int a = 0; a < gmsV.getRecordList().size(); a++) {
                    //Checks if Activity is set if not add original with no Activity and Label
                    ContactRecordsProtos.ScanRecord record = null;
                    if (this.lastActivity != 99 && lastlabel != null) {
                        System.out.println("wrote activity1" + lastActivity + lastlabel + lastlabelprob);
                        record = ContactRecordsProtos.ScanRecord.newBuilder(gmsV.getRecord(a))
                                .setActivity(this.lastActivity)
                                .setLabel(lastlabel)
                                .setProb(lastlabelprob)
                                .setVectors(vectors)
                                .build();
                    } else {
                        record = ContactRecordsProtos.ScanRecord.newBuilder(gmsV.getRecord(a))
                                .build();
                    }
                    contactRecordsBuilder.addRecord(record);
                }

                //Write to New DB the new Key

                db.getDb().put(k.toByteArray(), contactRecordsBuilder.build().toByteArray());
                continue;

            }

            if (gmsV.getRecordCount() == cdbV.getRecordCount()) {
                //Record is Complete in new DB then skip Key
                continue;
            }

            //Key exist but is not Complete
            //Take all old Enters from New DB
            ContactRecordsProtos.ContactRecords.Builder contactRecordsBuilder = ContactRecordsProtos.ContactRecords.newBuilder(cdbV);
            // For entries in Gms-DB - New DB so we only add new and let old entries untouched
            for (int i = cdbV.getRecordList().size(); i < gmsV.getRecordList().size(); i++) {
                ContactRecordsProtos.ScanRecord gmsRecord = gmsV.getRecord(i);
                ContactRecordsProtos.ScanRecord.Builder builder = ContactRecordsProtos.ScanRecord.newBuilder(gmsRecord);
                System.out.println("new");
                //Checks if Activity is set if not add original with no Activity and Label
                if (this.lastActivity != 99 && lastlabel != null) {
                    System.out.println("wrote activity2" + lastActivity + lastlabel + lastlabelprob);
                    builder.setActivity(this.lastActivity)
                            .setLabel(lastlabel)
                            .setProb(lastlabelprob)
                            .setVectors(vectors);
                }
                contactRecordsBuilder.addRecord(builder.build());
                missing++;
            }

            //Write to New DB the new Key
            db.getDb().put(k.toByteArray(), contactRecordsBuilder.build().toByteArray());
        }

        // Complete
        if (missing == 0) {
            System.out.println("all keys already there");
        } else {
            AcousticSceneClassification TestAccousticClassification = new AcousticSceneClassification();
            TestAccousticClassification.start();
            System.out.println("Database Editor" + "Missing:" + missing);
        }

        //Close Google DB after Work
        this.gmsDB.close();

        //Copy for easy access
        Utils.copyDir(getCacheDir(context), MainActivity.getInstance().getCacheDir());


    }
}
