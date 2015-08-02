package com.swaggaming.swagswipe;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivityFragment extends Fragment {

    private String TAG = "SWAG";
    private Button importButton;
    private ProgressBar progressBar;
    private SharedPreferences prefs;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        importButton = (Button) view.findViewById(R.id.importButton);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);

        prefs = getActivity().getPreferences(Context.MODE_PRIVATE);

        if (prefs.getBoolean(getString(R.string.imported), false)) {
            importButton.setText(R.string.import_dictionary_undo);
        } else {
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

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        UserDictionary.Words.addWord(getActivity(), line, 128, null, null);
                    } else {
                        UserDictionary.Words.addWord(getActivity(), line, 128, UserDictionary.Words.LOCALE_TYPE_ALL);
                    }
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

            importButton.setText(R.string.import_dictionary_undo);
            progressBar.setVisibility(View.GONE);
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

            importButton.setText(R.string.import_dictionary);
            progressBar.setVisibility(View.GONE);
        }
    }
}
