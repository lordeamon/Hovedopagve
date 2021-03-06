package dk.dampbiksen.community.models;

import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import dk.dampbiksen.community.R;
import dk.dampbiksen.community.util.FirebaseCallback;

/**
 * A poll entry in the list of products.
 */
public class PollEntry {
    private static final String TAG = PollEntry.class.getSimpleName();

    public final String title;
    public final Uri dynamicUrl;
    public final String url;
    public final String id;
    public final String description;
    public final String pollid;


    public PollEntry(
            String title, String dynamicUrl, String url, String id, String description, String pollid) {
        this.title = title;
        this.dynamicUrl = Uri.parse(dynamicUrl);
        this.url = url;
        this.id = id;
        this.description = description;
        this.pollid = pollid;
    }

    public static List<PollEntry> initPollList(final FirebaseCallback firebaseCallback) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Source");
        DatabaseReference pollsListRef = rootRef.child("Polls");
        Query query = pollsListRef.orderByChild("id");
        final ArrayList<PollEntry> pollEntriesList = new ArrayList<>();

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pollEntriesList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    pollEntriesList.add(new PollEntry(
                            ds.child("title").getValue(String.class),
                            "",
                            ds.child("url").getValue(String.class),
                            ds.child("id").getValue(String.class),
                            ds.child("description").getValue(String.class),
                            ds.child("pollid").getValue(String.class)));
                }
                firebaseCallback.onCallbackPoll(pollEntriesList);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        query.addValueEventListener(eventListener);
        return pollEntriesList;
    }
    /**
     * Loads a raw JSON at R.raw.pollcontender and converts it into a list of PollEntry objects
     * Used before Firebase intergration
     */
    public static List<PollEntry> initPollEntryList(Resources resources) {

        InputStream inputStream = resources.openRawResource(R.raw.pollcontender);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            int pointer;
            while ((pointer = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, pointer);
            }
        } catch (IOException exception) {
            Log.e(TAG, "Error writing/reading from the JSON file.", exception);
        } finally {
            try {
                inputStream.close();
            } catch (IOException exception) {
                Log.e(TAG, "Error closing the input stream.", exception);
            }
        }
        String jsonProductsString = writer.toString();
        Gson gson = new Gson();
        Type productListType = new TypeToken<ArrayList<PollEntry>>() {
        }.getType();
        return gson.fromJson(jsonProductsString, productListType);
    }


}