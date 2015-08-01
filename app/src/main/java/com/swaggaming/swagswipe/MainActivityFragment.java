package com.swaggaming.swagswipe;

import android.os.AsyncTask;
import android.os.Build;
import android.provider.UserDictionary;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private Button importButton;
    private ProgressBar progressBar;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        importButton = (Button) view.findViewById(R.id.importButton);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddWordDictionaryAsyncTask().execute();
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
                reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("dotadict.txt")));

                String line;
                while ((line = reader.readLine()) != null) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        UserDictionary.Words.addWord(getActivity(), line, 128, null, null);
                    } else {
                        UserDictionary.Words.addWord(getActivity(), line, 128, UserDictionary.Words.LOCALE_TYPE_ALL);
                    }
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
            progressBar.setVisibility(View.GONE);
        }
    }
}
