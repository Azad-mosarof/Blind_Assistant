package com.example.multiTaskingApp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;


import com.example.multiTaskingApp.R;
import com.example.multiTaskingApp.StaticsKt;


import java.io.File;

/**
 * This class is used for sharing data between current and other app.
 * Just create a ShareDataToOthers object by passing data to constructor, then call exportIntent() from that object.
 */
public class ShareDataToOthers {
    File file;
    Context context;
    String preText;

    /**
     * Export any file to any other app
     *
     * @param file    The file object that needs to be shared
     * @param context Application context from where its called
     * @param preText The text that will be used in creating EXTRA_TEXT and EXTRA_SUBJECT
     */
    public ShareDataToOthers(File file, Context context, String preText) {
        this.file = file;
        this.context = context;
        this.preText = preText;
    }

    /**
     * Export the share intent of any extension
     */
    public void exportIntent() {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        Uri uri = Uri.fromFile(file);
        String ext = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        String type = mimeTypeMap.getExtensionFromMimeType(ext);

        if (type == null) type = "*/*";

        try {
            Intent intent = new Intent(Intent.ACTION_SEND);

            intent.putExtra(Intent.EXTRA_TEXT, preText + " " + context.getString(R.string.share_intent_text));
            intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_intent_subject));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri path = FileProvider.getUriForFile(context, StaticsKt.APP_PROVIDER_AUTHORITY, file);
                intent.putExtra(Intent.EXTRA_STREAM, path);
            } else {
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }

            // Set mime type
            intent.setType(type);

            // Start activity
            context.startActivity(intent);
//            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_via)));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, context.getString(R.string.no_app_to_share), Toast.LENGTH_SHORT).show();
        }
    }
}
