package com.example.multiTaskingApp.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.BuildConfig;

import org.jetbrains.annotations.Contract;

import java.io.File;

public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();

    /**
     * <p>This is the main Folder Path at which the Frames are saved:
     * / + DCIM + ROOT_DIR + SPLIT_CHARACTER + FILE_PATH_FRAMES</p>
     *
     * <b>Important:</b>
     * <p>Just make sure on changing this path,
     * also update the path_providers.xml, as this path is hardcoded there</p>
     */
    public static final String FILE_PATH_FRAMES = "Frames";
    public static final String ROOT_DIR = "Paint";
    public static final String DCIM = "/DCIM";
    public static final String SPLIT_CHARACTER = "/";

    /**
     * Frame File extension
     */
    public static final String FRAME_FILE_EXTENSION = ".jpg";

    /**
     * <h3>Scan the given filePath so that it appears in gallery</h3>
     *
     * <p>Media Scanning for showing the file instantly in gallery without "restart"
     * For Media Scan, "getPath" is important, not "getAbsolutePath"
     * Mime type considered - png, jpeg, jpg, gif</p>
     *
     * @param filePath File path that needs to be scanned
     */
    public static void runMediaScan(Context context, String filePath) {
        MediaScannerConnection.scanFile(context, new String[]{filePath}, new String[]{"image/png", "image/jpeg", "image/jpg", "image/gif"}, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "MediaScanComplete : " + path);
                }
            }
        });
    }

    /**
     * Delete the file present at the filepath
     *
     * @param filePath File path that needs to be deleted
     * @return "True", if file has been properly deleted or else "False"
     */
    @NonNull
    public static Boolean deleteFrameFile(Context context, String filePath) {
        File file = new File(filePath);

        // Deleting file
        Boolean deleteStatus = file.delete();

        // Re-scanning media files
        runMediaScan(context, file.getPath());

        return deleteStatus;
    }

    /**
     * Returns the Frame Folder path
     *
     * @return Example: DCIM/Paint/Frames
     */
    @NonNull
    public static String getFrameFolderPath() {
        return Environment.getExternalStorageDirectory()
                .toString() + DCIM + SPLIT_CHARACTER + ROOT_DIR + SPLIT_CHARACTER +
                FILE_PATH_FRAMES;
    }

    /**
     * Removes the frame from internal storage (if exists)
     *
     * @param frameId Frame Id that needs to be removed
     * @return "True", if successfully removed, else "False"
     */
    public static Boolean removeFrame(Context context, String frameId) {
        boolean fileRemoved = true;
        String frameFolderPath = getFrameFolderPath();
        File directory = new File(frameFolderPath);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.contains(frameId)) {
                    // Deleting the frame file
                    fileRemoved = deleteFrameFile(context, frameFolderPath + SPLIT_CHARACTER + fileName);
                    break;
                }
            }
        }

        return fileRemoved;
    }

    /**
     * Checks if file path exists or not
     *
     * @param filePath Current file path
     * @return "True", if file exists in the given file path or else "False"
     */
    @NonNull
    public static Boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * Returns the filename with extension
     *
     * @param fileNameWithoutExtension Current filename as saved in DB without extension, example "hello"
     * @return Current filename with extension, example "hello.jpg"
     */
    @NonNull
    @Contract(pure = true)
    public static String getFrameFileName(String fileNameWithoutExtension) {
        return fileNameWithoutExtension + FRAME_FILE_EXTENSION;
    }

    /**
     * Returns the entire path of the file in External Storage
     *
     * @param fileName The current filename with extension example "hello.jpg"
     * @return The current filename physical path in external storage
     */
    @NonNull
    public static String getFrameFilePath(String fileName) {
        return Environment.getExternalStorageDirectory()
                .toString() + DCIM + SPLIT_CHARACTER + ROOT_DIR + SPLIT_CHARACTER +
                FILE_PATH_FRAMES + SPLIT_CHARACTER + fileName;
    }

    /**
     * Create any folder sub-directory structure
     * <p>
     * Example: filePath = "Media/Downloads/Images", then the function will first create the ROOT_DIR,
     * then inside ROOT_DIR it will create folders as per filePath, so the final Directory structure will look like:
     * ROOT_DIR -> Media -> Downloads -> Images
     *
     * @param filePath The filePath that needs to be created
     * @return The innermost file after creating the above filePath
     */
    @Nullable
    public static File createFilePath(String filePath) {
        // Creating root directory
        File rootDir = new File(Environment.getExternalStorageDirectory() + DCIM, ROOT_DIR);
        if (!rootDir.exists()) {
            if (!rootDir.mkdir()) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Folder creation error, Root Folder path : " + rootDir.getAbsolutePath());
                }
                return null;
            }
        }

        // Creating sub-directory structure as per filePath provided
        File currDir = null;
        String[] folderArray = filePath.split(SPLIT_CHARACTER);
        StringBuilder currPath = new StringBuilder();
        currPath.append(DCIM);
        currPath.append(SPLIT_CHARACTER);
        currPath.append(ROOT_DIR);
        for (String folderName : folderArray) {
            currDir = new File(Environment.getExternalStorageDirectory() + currPath.toString(), folderName);
            if (!currDir.exists()) {
                if (!currDir.mkdir()) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "Folder Creation Error, Folder path : " + currDir.getAbsolutePath());
                    }
                    return null;
                }
            }
            currPath.append(SPLIT_CHARACTER);
            currPath.append(folderName);
        }
        return currDir;
    }
}
