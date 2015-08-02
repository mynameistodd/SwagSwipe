package com.swaggaming.swagswipe;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivityFragment extends Fragment {

    public static GoogleAnalytics analytics;
    public static Tracker tracker;
    private String TAG = "SWAG";
    private LinearLayout tileLinearLayout;
    private Button importButton;
    private ProgressBar progressBar;
    private SharedPreferences prefs;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analytics = GoogleAnalytics.getInstance(getActivity());

        tracker = analytics.newTracker(R.xml.global_tracker);
        tracker.enableAdvertisingIdCollection(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        tileLinearLayout = (LinearLayout) view.findViewById(R.id.tileLinearLayout);
        importButton = (Button) view.findViewById(R.id.importButton);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);

        prefs = getActivity().getPreferences(Context.MODE_PRIVATE);

        if (prefs.getBoolean(getString(R.string.imported), false)) {
            tileLinearLayout.setBackground(getActivity().getResources().getDrawable(R.drawable.remove));
            importButton.setText(R.string.import_dictionary_undo);
        } else {
            tileLinearLayout.setBackground(getActivity().getResources().getDrawable(R.drawable.add));
            importButton.setText(R.string.import_dictionary);
        }

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isImported = prefs.getBoolean(getString(R.string.imported), false);
                Log.d(TAG, "Imported Pref: " + isImported);

                if (!isImported) {
                    new AddWordDictionaryAsyncTask().execute();
                } else {
                    new ResetDictionaryAsyncTask().execute();
                }
            }
        });

        return view;
    }

    private class AddWordDictionaryAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("dota_dictionary.csv")));

                String line;
                while ((line = reader.readLine()) != null) {

                    UserDictionary.Words.addWord(getActivity(), line, 128, null, null);

                    Log.d(TAG, "Adding Word: " + line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            prefs.edit().putBoolean(getString(R.string.imported), true).apply();

            tileLinearLayout.setBackground(getActivity().getResources().getDrawable(R.drawable.remove));
            importButton.setText(R.string.import_dictionary_undo);
            progressBar.setVisibility(View.GONE);

            tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("UX")
                            .setAction("ImportDictionary")
                            .setCategory("dota2")
                            .build()
            );
        }
    }

    private class ResetDictionaryAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("dota_dictionary.csv")));

                String line;
                while ((line = reader.readLine()) != null) {

                    getActivity().getContentResolver().delete(UserDictionary.Words.CONTENT_URI,
                            UserDictionary.Words.WORD + "=?", new String[]{line});
                    Log.d(TAG, "Deleting Word: " + line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            prefs.edit().putBoolean(getString(R.string.imported), false).apply();

            tileLinearLayout.setBackground(getActivity().getResources().getDrawable(R.drawable.add));
            importButton.setText(R.string.import_dictionary);
            progressBar.setVisibility(View.GONE);

            tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("UX")
                            .setAction("RevertDictionary")
                            .setCategory("dota2")
                            .build()
            );
        }
    }
}
