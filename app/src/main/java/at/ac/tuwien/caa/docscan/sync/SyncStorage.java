package at.ac.tuwien.caa.docscan.sync;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import at.ac.tuwien.caa.docscan.logic.Document;
import at.ac.tuwien.caa.docscan.logic.DocumentStorage;

public class SyncStorage {

    private static final String SYNC_STORAGE_FILE_NAME = "syncstorage.json";
    private static final String CLASS_NAME = "SyncStorage";
    private static SyncStorage sInstance;

    private ArrayList<SyncFile> mFileSyncList;
    private ArrayList<SyncFile> mUploadedList;
    private ArrayList<Integer> mUnprocessedUploadIDs;
    private ArrayList<Integer> mUnfinishedUploadIDs;
    private ArrayList<String> mUploadDocumentTitles;
//    private ArrayList<File> mAwaitingUploadFiles;

    public static SyncStorage getInstance() {

        if (sInstance == null)
            sInstance = new SyncStorage();

        return sInstance;

    }

    public static boolean isInstanceNull() {

        return sInstance == null;
    }

    public static void loadJSON(Context context) {

        File path = context.getFilesDir();
        File storeFile = new File(path, SYNC_STORAGE_FILE_NAME);

        if (!storeFile.exists())
            sInstance = new SyncStorage();
        else {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(storeFile));

                Gson gson = new Gson();
                sInstance = gson.fromJson(bufferedReader, SyncStorage.class);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                sInstance = new SyncStorage();
            }
        }

    }

    public static void saveJSON(Context context) {

        File path = context.getFilesDir();
        File storeFile = new File(path, SYNC_STORAGE_FILE_NAME);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(storeFile));
            String syncStorage = new Gson().toJson(sInstance);
            writer.write(syncStorage);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public SyncStorage() {

        mFileSyncList = new ArrayList<>();
        mUploadedList = new ArrayList<>();
        mUnfinishedUploadIDs = new ArrayList<>();
        mUnprocessedUploadIDs = new ArrayList<>();

    }



    public void clearSyncList() {

        mFileSyncList = new ArrayList<>();

    }

    public void setUploadDocumentTitles(ArrayList<String> titles) {

        mUploadDocumentTitles = titles;

    }

    public void addUploadDirs(ArrayList<String> dirs) {

        if (mUploadDocumentTitles == null)
            mUploadDocumentTitles = new ArrayList<>();

//        Just add the directory if it is not already contained:
        for (String dir : dirs)
            if (!mUploadDocumentTitles.contains(dir))
                mUploadDocumentTitles.add(dir);

        createAwaitingUploadList();

    }

    public void setUnprocessedUploadIDs(ArrayList<Integer> unprocessedUploadIDs) {

        mUnprocessedUploadIDs = unprocessedUploadIDs;

    }

    public ArrayList<Integer> getUnprocessedUploadIDs() {

        return mUnprocessedUploadIDs;

    }

    public ArrayList<Integer> getUnfinishedUploadIDs() {

        return mUnfinishedUploadIDs;

    }

    public void setUnfinishedUploadIDs(ArrayList<Integer> unfinishedUploadIDs) {

        mUnfinishedUploadIDs = unfinishedUploadIDs;

    }

    public void printUnfinishedUploadIDs() {

        for (Integer i : mUnfinishedUploadIDs) {
            Log.d(CLASS_NAME, "printUnfinishedUploadIDs: unfinished ID:" + i);
        }

    }

    private void createAwaitingUploadList() {

//        mAwaitingUploadFiles = new ArrayList<>();
//
//        if (mUploadDocumentTitles == null || mUploadDocumentTitles.isEmpty())
//            return;
//
//        for (String dir : mUploadDocumentTitles) {
//
//            if (dir == null)
//                continue;
//
////            Get the files contained in the document:
//            Document document = DocumentStorage.getInstance().getDocument(dir);
//            if (document != null) {
//                ArrayList<File> files = document.getFiles();
//                if (files != null && !files.isEmpty()) {
////                    Add it to the awaiting list:
//                    for (File file : files) {
//                        mAwaitingUploadFiles.add(file);
//                    }
//                }
//            }
//
//        }

    }


    public ArrayList<String> getUploadDocumentTitles() {

        return mUploadDocumentTitles;
    }

    public void addToUploadedList(SyncFile fileSync) {

        if (mUploadedList != null)
            mUploadedList.add(fileSync);

    }

    public void addTranskribusFile(File file, int uploadId) {

        if (mFileSyncList != null) {
            mFileSyncList.add(new TranskribusSyncFile(file, uploadId));
//            startSyncJob(context);
        }

    }

    public void removeDocument(String title) {

        Document document = DocumentStorage.getInstance().getDocument(title);
        if (document != null) {
            ArrayList<File> files = document.getFiles();
            if (files != null && !files.isEmpty()) {
                for (File file : files) {
                    removeFile(file);
                }
            }
        }

    }

    public ArrayList<SyncFile> getSyncList() {

        return mFileSyncList;

    }

    public void setSyncList(ArrayList<SyncFile> fileSyncList) {

        mFileSyncList = fileSyncList;

    }


    private ArrayList<SyncFile> getUploadedList() {

        return mUploadedList;

    }

    public void setUploadedList(ArrayList<SyncFile> uploadedList) {

        mUploadedList = uploadedList;

    }

    private void removeFile(File fileRemoved) {

        Iterator<SyncFile> iter = mUploadedList.iterator();
        while (iter.hasNext()) {
            File file = iter.next().getFile();
            if (file.getAbsolutePath().compareToIgnoreCase(fileRemoved.getAbsolutePath()) == 0) {
                Log.d(CLASS_NAME, "removeDocument: " + file);
                iter.remove();
            }
        }

    }

    public boolean areFilesUploaded(ArrayList<File> files) {

        if (files == null || files.isEmpty())
            return false;

        // Check if every file contained in the folder is already uploaded:
        for (File file : files) {
            if (!isFileUploaded(file))
                return false;
        }

        return true;

    }

    public boolean isDocumentAwaitingUpload(Document document) {

        Log.d(CLASS_NAME, "isDirAwaitingUpload");

        String dir = document.getTitle();
        if (document == null)
            return false;

        ArrayList<File> files = document.getFiles();

        if (files == null || files.isEmpty())
            return false;

        // Is the dir already added to the upload list:
        if ((mUploadDocumentTitles != null) && (mUploadDocumentTitles.contains(dir))) {
            Log.d(CLASS_NAME, "isDirAwaitingUpload: mUploadDirs.size: " + mUploadDocumentTitles.size());
            return true;
        }
        else if ((mFileSyncList != null) && (mFileSyncList.size() > 0)) {
            Log.d(CLASS_NAME, "isDirAwaitingUpload: mFileSyncList.size: " + mFileSyncList.size());
            Log.d(CLASS_NAME, "isDirAwaitingUpload: files.size: " + files.size());
            for (File file : files) {
                boolean isFileOnList = false;
                for (SyncFile fileSync : mFileSyncList) {
                    if (fileSync.getFile().equals(file)) {
                        isFileOnList = true;
                        break;
                    }
                }

                if (!isFileOnList)
                    return false;

            }

            return true;

        }

        return false;


    }

    private boolean isFileUploaded(File file) {

        for (SyncFile fileSync : mUploadedList) {
            if (file.getAbsolutePath().compareTo(fileSync.getFile().getAbsolutePath()) == 0)
                return true;
        }

        return false;
    }

    public interface Callback {
        void onUploadComplete(SyncFile syncFile);
        void onError(Exception e);
    }





}