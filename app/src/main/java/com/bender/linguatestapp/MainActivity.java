package com.bender.linguatestapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bender.linguatestapp.models.Word;
import com.bender.linguatestapp.models.WordApiResponse;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    private static final String PREFS_NAME = "LeoPrefs";
    private static final String PREFS_KEY_COUNT = "fab_press_count";
    private WordsAdapter mAdapter;
    private ArrayList<Word> mWords;
    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.Toolbar_TextAppearance);
        setSupportActionBar(toolbar);

        final TextView hint = (TextView) findViewById(R.id.hint);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.button_add);
        list = (ListView) findViewById(R.id.list);

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new DialogFragment() {
                    private TextView cancel, commit;

                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_remove_word, null));
                        return builder.create();
                    }

                    @Override
                    public void onStart() {
                        super.onStart();
                        cancel = (TextView) getDialog().findViewById(R.id.cancel);
                        commit = (TextView) getDialog().findViewById(R.id.commit);

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dismiss();
                            }
                        });
                        commit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DataManager.getInstance().removeWord((Word)mAdapter.getItem(position));
                                refreshList();
                                dismiss();
                            }
                        });
                    }
                }.show(getFragmentManager(), "remove");
                return true;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pressCount = preferences.getInt(PREFS_KEY_COUNT, 0);
                if (pressCount < 3)
                    preferences.edit().putInt(PREFS_KEY_COUNT, pressCount + 1).apply();
                else hint.setVisibility(View.GONE);

                new DialogFragment() {
                    private TextView cancel, commit;
                    private EditText editText;
                    private ProgressBar progress;

                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_add_word, null));
                        return builder.create();
                    }

                    @Override
                    public void onStart() {
                        super.onStart();
                        cancel = (TextView) getDialog().findViewById(R.id.cancel);
                        commit = (TextView) getDialog().findViewById(R.id.commit);
                        editText = (EditText) getDialog().findViewById(R.id.et_word);
                        progress = (ProgressBar) getDialog().findViewById(R.id.progress);

                        editText.addTextChangedListener(new TextWatcher() {
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            public void afterTextChanged(Editable s) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                editText.setError(null);
                            }
                        });
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dismiss();
                            }
                        });
                        commit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final String word = editText.getText().toString().toLowerCase().trim();
                                if (!word.isEmpty()) {
                                    if (alreadyAdded(word)) {
                                        Toast.makeText(MainActivity.this, R.string.word_already_added, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    NetworkManager.getInstance().getTranslation(word.trim(), new JsonHttpResponseHandler(){
                                        @Override
                                        public void onStart() {
                                            progress.setVisibility(View.VISIBLE);
                                            commit.setEnabled(false);
                                        }

                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                            WordApiResponse apiResponse = new WordApiResponse(response);
                                            /* по идее тут можно сделать что то типа
                                             switch(apiResponse.getResponseCode()) :
                                             и реагировать по ситуации. но мы оптимисты,
                                             рассчитываем на безошибочную работу
                                              */
                                            if (getActivity() != null && getDialog() != null && getDialog().isShowing()) {
                                                if (word.equals(apiResponse.getTranslation().toLowerCase().trim())) {
                                                    Toast.makeText(MainActivity.this, R.string.no_translation_for_word, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    DataManager.getInstance().addWord(new Word(word, apiResponse.getTranslation()));
                                                    ((MainActivity)getActivity()).refreshList();
                                                    dismiss();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                            if (throwable!=null && !throwable.getMessage().isEmpty()) Log.e("MainActivity", throwable.getMessage());
                                            Toast.makeText(MainActivity.this, R.string.request_error, Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFinish() {
                                            progress.setVisibility(View.GONE);
                                            commit.setEnabled(true);
                                        }
                                    });
                                } else {
                                    editText.setError(getString(R.string.dialog_error));
                                }
                            }
                        });
                    }
                }.show(getFragmentManager(), "add");
            }
        });

        mWords = DataManager.getInstance().getWords();
        if (mWords.size() > 0) {
            findViewById(R.id.empty_list_text).setVisibility(View.GONE);
            mAdapter = new WordsAdapter(this, mWords);
            list.setAdapter(mAdapter);
        }

        int pressCount = preferences.getInt(PREFS_KEY_COUNT, 0);
        if (pressCount > 2) hint.setVisibility(View.GONE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                filterWords(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                filterWords(query);
                return true;
            }

        });
        return true;
    }

    private void refreshList() {
        mWords.clear();
        mWords.addAll(DataManager.getInstance().getWords());
        if (mWords.size() > 0) {
            if (mAdapter == null) {
                mAdapter = new WordsAdapter(this, mWords);
                list.setAdapter(mAdapter);
                findViewById(R.id.empty_list_text).setVisibility(View.GONE);
            } else {
                mAdapter.notifyDataSetChanged();
            }
        } else {
            findViewById(R.id.empty_list_text).setVisibility(View.VISIBLE);
        }
    }

    private boolean alreadyAdded(String query) {
        return DataManager.getInstance().getWord(query).size() > 0;
    }

    private void filterWords(String query) {
        mWords.clear();
        mWords.addAll(DataManager.getInstance().getWords(query));
        mAdapter.notifyDataSetChanged();
        findViewById(R.id.empty_list_text).setVisibility((mWords.size() > 0) ? View.GONE : View.VISIBLE);
    }
}
