package com.nstd.tools.test;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by maoting on 2016/7/5.
 */
public class TestMimeActivity extends AppCompatActivity {

    private static final String TAG = TestMimeActivity.class.getSimpleName();

    @BindView(R.id.tv_mime_types)
    TextView tvMimeTypes;

    String[] fileNames = {"doc1.doc", "doc2.docx", "excel1.xls", "excel2.xlsx", "pdf1.pdf", "ppt1.ppt", "ppt2.pptx", "text1.txt"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mime);
        ButterKnife.bind(this);

        try {
            String[] assets = getAssets().list("doc");
            for(int i=0; i<assets.length; i++) {
                printFileMine(assets[i]);
            }
            openFileBy3rdApp(assets[4]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String printFileMine(String fileName) {

        InputStream is = null;
        try {
            is = getAssets().open("doc/" + fileName);

            ContentHandler contentHandler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            Parser parser = new AutoDetectParser();
            ParseContext pContext = new ParseContext();

            metadata.set(Metadata.RESOURCE_NAME_KEY, fileName);
            parser.parse(is, contentHandler, metadata, pContext);

            Log.e(TAG, fileName + ":" + metadata.get(Metadata.CONTENT_TYPE));

//            MimeTypes mimeTypes = TikaConfig.getDefaultConfig().getMimeRepository();
//            String mime = mimeTypes.detect(is, metadata).toString();
//            Log.e(TAG, fileName + ":" + mime);

            return metadata.get(Metadata.CONTENT_TYPE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }


    private void openFileBy3rdApp(String fileName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        InputStream is = null;
        OutputStream os = null;

        File f = new File(getExternalCacheDir().toString() + fileName);
        try {
            is = getAssets().open("doc/" + fileName);
            os = new FileOutputStream(f);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = is.read(bytes)) != -1) {
                os.write(bytes, 0, read);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        Uri uri = Uri.fromFile(f);
        intent.setDataAndType(uri, printFileMine(fileName));

        if(isIntentAvailable(this, intent)) {
            startActivity(intent);
        } else {
            Log.e(TAG, "no app can open this file");
            Toast.makeText(this, "no app can open this file", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for(ResolveInfo resolveInfo : list) {
            Log.e(TAG, "resolveInfo:" + resolveInfo.toString());
        }

        return list.size() > 0;
    }
}
