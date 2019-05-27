package com.flosebastian.app.biblepicker;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class BibleManager {
    private static final BibleManager ourInstance = new BibleManager();

    private boolean         m_isInitialized     = false;

    private Context         m_context           = null;
    private JSONArray       m_bibles            = null;
    private JSONObject      m_settings          = null;
    private JSONObject      m_currentBible      = null;

    public static BibleManager getInstance() {
        return ourInstance;
    }

    private BibleManager() {
    }

    public void init(Context context){
        m_context = context;
        try{
            JSONObject jBible = new JSONObject(loadJSONFromAsset("bibles/bibles.json"));
            m_bibles    = jBible.getJSONArray("bibles");
            m_settings  = jBible.getJSONObject("settings");

            String defaultBible = m_settings.getString("default-bible");

            m_isInitialized = loadBible(defaultBible);

        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public boolean loadBible(String code){
        boolean isSuccess = false;
        try{
            for(int i = 0; i < m_bibles.length(); i++) {
                m_currentBible = m_bibles.getJSONObject(i);
                String bibleId = m_currentBible.getString("id");
                if(code.equalsIgnoreCase(bibleId)) {
                    String bibleFilename = "bibles/" + m_currentBible.getString("filename");
                    JSONArray bible = new JSONArray( loadJSONFromAsset(bibleFilename) );
                    m_currentBible.put("data", bible);
                    isSuccess = true;
                    break;
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return isSuccess;
    }

    public boolean loadBible(int idx){
        boolean isSuccess = false;
        try{
            m_currentBible = m_bibles.getJSONObject(idx);
            String bibleFilename = "bibles/" + m_currentBible.getString("filename");
            JSONArray bible = new JSONArray( loadJSONFromAsset(bibleFilename) );
            m_currentBible.put("data", bible);
            isSuccess = true;
        }catch(JSONException e){
            e.printStackTrace();
        }
        return isSuccess;
    }

    public List<String> getBiblesList(){
        List<String> bibles = new ArrayList<String>();
        try{
            for(int i = 0; i < m_bibles.length(); i++){
                JSONObject bible = m_bibles.getJSONObject(i);
                bibles.add(bible.getString("shortname"));
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return bibles;
    }

    public List<String> getBooksList(){
        List<String> books = new ArrayList<String>();
        try{
            JSONArray booksJArr = m_currentBible.getJSONArray("data");
            for(int i = 0; i < booksJArr.length(); i++){
                JSONObject book = booksJArr.getJSONObject(i);
                books.add(book.getString("name"));
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return books;
    }

    public JSONObject getBook(String bookName){
        int bookId = this.getBookId(bookName);
        return getBook(bookId);
    }

    public JSONObject getBook(int bookId){
        JSONObject book = null;
        try{
            JSONArray booksJArr = m_currentBible.getJSONArray("data");
            book = booksJArr.getJSONObject(bookId);
        }catch(JSONException e){
            e.printStackTrace();
        }
        return book;
    }

    public int getBookId(String bookName){
        int bookId = -1;
        try{
            JSONArray booksJArr = m_currentBible.getJSONArray("data");
            for(int i = 0; i < booksJArr.length(); i++){
                JSONObject book = booksJArr.getJSONObject(i);
                if(bookName.equalsIgnoreCase(book.getString("name"))
                        || bookName.equalsIgnoreCase(book.getString("abbrev"))
                ){
                    bookId = i;
                    break;
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return bookId;
    }

    public int getChaptersCount(String bookName){
        int chapterCount = 0;
        try{
            JSONObject book = getBook(bookName);
            if(book != null){
                JSONArray chapters = book.getJSONArray("chapters");
                chapterCount = chapters.length();
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return chapterCount;
    }

    public int getChaptersCount(int bookId){
        int chapterCount = 0;
        try{
            JSONArray booksJArr = m_currentBible.getJSONArray("data");
            JSONObject book = booksJArr.getJSONObject(bookId);
            JSONArray chapters = book.getJSONArray("chapters");
            chapterCount = chapters.length();
        }catch(JSONException e){
            e.printStackTrace();
        }
        return chapterCount;
    }

    public JSONArray getChapter(String bookName, int chapterIdx){
        JSONArray  chapter = null;
        JSONObject book = getBook(bookName);
        try{
            JSONArray chapters = book.getJSONArray("chapters");
            chapter = chapters.getJSONArray(chapterIdx);
        }catch(JSONException e){
            e.printStackTrace();
        }
        return chapter;
    }

    public JSONArray getChapter(int bookIdx, int chapterIdx){
        JSONArray  chapter = null;
        JSONObject book = getBook(bookIdx);
        try{
            JSONArray chapters = book.getJSONArray("chapters");
            chapter = chapters.getJSONArray(chapterIdx);
        }catch(JSONException e){
            e.printStackTrace();
        }
        return chapter;
    }

    public int getVersesCount(String bookName, int chapterIdx){
        int verseCount = 0;
        JSONArray chapters = getChapter(bookName, chapterIdx);
        verseCount = chapters.length();
        return verseCount;
    }

    public int getVersesCount(int bookIdx, int chapterIdx){
        int verseCount = 0;
        JSONArray chapters = getChapter(bookIdx, chapterIdx);
        verseCount = chapters.length();
        return verseCount;
    }

    public String getVerses(String bookName, int chapterIdx, int verseStart, int verseEnd ){
        String result = null;
        try {
            JSONArray chapter = this.getChapter(bookName, chapterIdx);
            result = "";
            for(int i = verseStart; i <= verseEnd; i++){
                result += chapter.getString(i);
                if(i != verseEnd){
                    result += "\n";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getVerses(int bookIdx, int chapterIdx, int verseStart, int verseEnd ){
        String result = null;
        try {
            JSONArray chapter = this.getChapter(bookIdx, chapterIdx);
            result = "";
            for(int i = verseStart; i <= verseEnd; i++){
                result += Utilities.superscriptNum(String.valueOf(i + 1));
                result += chapter.getString(i);
                if(i != verseEnd){
                    result += "\n";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getVerses(int bookIdx, int chapterIdx, int verses[] ){
        String result = null;
        try {
            JSONArray chapter = this.getChapter(bookIdx, chapterIdx);
            result = "";
            for(int i = 0; i < verses.length; i++){
                result += Utilities.superscriptNum(String.valueOf(i + 1));
                result += chapter.getString(verses[i]);
                if(i != verses.length - 1){
                    result += "\n";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getSingleVerse(String bookName, int chapterIdx, int verseIdx){
        return getVerses(bookName, chapterIdx, verseIdx, verseIdx);
    }

    public String getSingleVerse(int bookIdx, int chapterIdx, int verseIdx){
        return getVerses(bookIdx, chapterIdx, verseIdx, verseIdx);
    }

    public List<String> getVersesInChapter(int bookIdx, int chapterIdx){
        List<String> verses = new ArrayList<String>();

        try {
            JSONArray chapter = this.getChapter(bookIdx, chapterIdx);
            for(int i = 0; i < chapter.length(); i++){
                String line = "";
                line += Utilities.superscriptNum(String.valueOf(i + 1));
                line += chapter.getString(i);
                verses.add(line);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return verses;
    }

    public String loadJSONFromAsset(String filename) {
        String json = null;

        try {
            InputStream is = m_context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }
}
