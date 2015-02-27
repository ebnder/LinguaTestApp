package com.bender.linguatestapp.models;

import com.orm.SugarRecord;

public class Word extends SugarRecord<Word> {

    private String inputWord;
    private String translatedWord;

    public Word() {
    }

    public Word(String input, String translation) {
        this.inputWord = input;
        this.translatedWord = translation;
    }

    public String getInputWord() {
        return inputWord;
    }

    public String getTranslatedWord() {
        return translatedWord;
    }
}
