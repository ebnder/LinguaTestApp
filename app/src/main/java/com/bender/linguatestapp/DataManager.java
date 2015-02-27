package com.bender.linguatestapp;

import com.bender.linguatestapp.models.Word;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;

public class DataManager {

    private static DataManager manager = new DataManager();
    public static DataManager getInstance() {
        return manager;
    }

    public void addWord(Word word) {
        word.save();
    }

    public void removeWord(Word word) {
        word.delete();
    }

    public ArrayList<Word> getWords() {
        return (ArrayList<Word>)Word.listAll(Word.class);
    }

    public ArrayList<Word> getWords(String filter) {
        if (filter.isEmpty()) return getWords();
        filter = "%"+filter+"%";
        return (ArrayList<Word>)Select.from(Word.class).where(Condition.prop("input_word").like(filter))
                .or(Condition.prop("translated_word").like(filter)).list();
    }

    public ArrayList<Word> getWord(String request) {
        return (ArrayList<Word>)Select.from(Word.class).where(Condition.prop("input_word").eq(request))
                .or(Condition.prop("translated_word").eq(request)).list();
    }

}
