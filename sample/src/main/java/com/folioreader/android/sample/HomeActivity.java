/*
 * Copyright (C) 2016 Pedro Paulo de Amorim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.folioreader.android.sample;

import static com.folioreader.Constants.DATE_FORMAT;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.folioreader.Config;
import com.folioreader.FolioReader;
import com.folioreader.model.HighLight;
import com.folioreader.model.locators.ReadLocator;
import com.folioreader.model.sqlite.BookmarkTable;
import com.folioreader.ui.base.OnSaveHighlight;
import com.folioreader.util.AppUtil;
import com.folioreader.util.OnHighlightListener;
import com.folioreader.util.ReadLocatorListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity
        implements OnHighlightListener,ReadLocatorListener, FolioReader.OnClosedListener {

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private FolioReader folioReader;

    private AppCompatActivity compatActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        compatActivity = this;
        folioReader = FolioReader.get()
                .setOnHighlightListener(this)
                .setReadLocatorListener(this)
                .setOnClosedListener(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getHighlightsAndSave();

        findViewById(R.id.btn_raw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Config config = AppUtil.getSavedConfig(getApplicationContext());
                if (config == null)
                    config = new Config();
                config.setAllowedDirection(Config.AllowedDirection.VERTICAL_AND_HORIZONTAL);
                config.setShowTextSelection(true);
                /*folioReader.setConfig(config, true)
                        .openBook(R.raw.four);*/
                String path  =  compatActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)+"/10005.epub";
                File file = new File(path);
                try {
                    int permission =ActivityCompat.checkSelfPermission(compatActivity,"android.permission.READ_EXTERNAL_STORAGE");
                    if(permission != PackageManager.PERMISSION_GRANTED){
                        Log.v("无权限","");
                    }else
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if(Environment.isExternalStorageLegacy()){
                            FileInputStream fis = new FileInputStream(file);
                            byte[] bytes = new byte[fis.available()];
                            fis.read(bytes);
                            String ss = new String(bytes);
                            fis.close();
                        }
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                folioReader.setConfig(config, true)
//                .openBook(R.raw.accessible_epub_3);
                        .openBook(path);
            }
        });

        findViewById(R.id.btn_assest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ReadLocator readLocator = getLastReadLocator();

                Config config = AppUtil.getSavedConfig(getApplicationContext());
                if (config == null)
                    config = new Config();
                config.setAllowedDirection(Config.AllowedDirection.VERTICAL_AND_HORIZONTAL);
                config.setNightThemeColorInt(Color.parseColor("#FFFFFF"));
                config.setShowRemainingIndicator(true);
                config.setShowTextSelection(false);

                folioReader.setReadLocator(readLocator);
                folioReader.setConfig(config, true)
                        .openBook("file:///android_asset/john.epub");
            }
        });

        Config config = AppUtil.getSavedConfig(getApplicationContext());
        if (config == null)
            config = new Config();
        config.setAllowedDirection(Config.AllowedDirection.VERTICAL_AND_HORIZONTAL);

        folioReader.setConfig(config, true)
                .openBook(R.raw.test);
//                .openBook("file://Documents/folioreader/accessible_epub_3/accessible_epub_3.epub");
    }

    private ReadLocator getLastReadLocator() {

        String jsonString = loadAssetTextAsString("Locators/LastReadLocators/last_read_locator_1.json");
        return ReadLocator.fromJson(jsonString);
    }

    //@Override
    public void saveReadLocator(ReadLocator readLocator, String mBookId, String markType) {
      /*  Log.i(LOG_TAG, "-> saveReadLocator -> " + readLocator.toJson()+" markType:"+markType);
        //收到获取阅读位置信息
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        String cfi = readLocator.getHref()+readLocator.getLocations().getCfi();
        if(FolioReader.EXTRA_BOOKMARK_ADD.equals(markType)){//添加标签
            boolean insertResult =new BookmarkTable(this).insertBookmark(
                    mBookId,
                    simpleDateFormat.format(new Date()),
                    readLocator.getTitle(),
                    readLocator.toJson().toString(),
                    cfi,BookmarkTable.MARK_TYPE
            );
            if(insertResult){
                Toast.makeText(
                        this, "已添加到书签", Toast.LENGTH_SHORT
                ).show();
            }

        }else if(FolioReader.EXTRA_BOOKMARK_DELETE.equals(markType)){//删除标签
            int bookmarkId =  BookmarkTable.getBookmarkIdByCfi(cfi,mBookId,this);
            if(bookmarkId != -1){
                boolean deleteResult = BookmarkTable.deleteBookmarkById(bookmarkId,this);
                if(deleteResult){
                    Toast.makeText(
                            this, "已删除书签", Toast.LENGTH_SHORT
                    ).show();
                }
            }
        }*/

    }

    /*
     * For testing purpose, we are getting dummy highlights from asset. But you can get highlights from your server
     * On success, you can save highlights to FolioReader DB.
     */
    private void getHighlightsAndSave() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<HighLight> highlightList = null;
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    highlightList = objectMapper.readValue(
                            loadAssetTextAsString("highlights/highlights_data.json"),
                            new TypeReference<List<HighlightData>>() {
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (highlightList == null) {
                    folioReader.saveReceivedHighLights(highlightList, new OnSaveHighlight() {
                        @Override
                        public void onFinished() {
                            //You can do anything on successful saving highlight list
                        }
                    });
                }
            }
        }).start();
    }

    private String loadAssetTextAsString(String name) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ((str = in.readLine()) != null) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Log.e("HomeActivity", "Error opening asset " + name);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e("HomeActivity", "Error closing asset " + name);
                }
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FolioReader.clear();
    }

    @Override
    public void onHighlight(HighLight highlight, HighLight.HighLightAction type) {
        Toast.makeText(this,
                "highlight id = " + highlight.getUUID() + " type = " + type,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFolioReaderClosed() {
        Log.v(LOG_TAG, "-> onFolioReaderClosed");
    }
}