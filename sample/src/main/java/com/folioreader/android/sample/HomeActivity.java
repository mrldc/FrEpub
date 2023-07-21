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

import android.app.Activity;
import android.content.Intent;
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
import com.folioreader.ui.activity.FolioActivity;
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
         {

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private FolioReader folioReader;

    private AppCompatActivity compatActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        compatActivity = this;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        findViewById(R.id.btn_001).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(),  FolioActivity.class);
                startIntent.putExtra("BOOK_FILE_URL", getPath("10001.epub") );
                setResult(Activity.RESULT_OK, startIntent);
                startActivity(startIntent);
            }
        });
        findViewById(R.id.btn_002).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(),  FolioActivity.class);
                startIntent.putExtra("BOOK_FILE_URL", getPath("10002.epub") );
                setResult(Activity.RESULT_OK, startIntent);
                startActivity(startIntent);
            }
        });
        findViewById(R.id.btn_003).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(),  FolioActivity.class);
                startIntent.putExtra("BOOK_FILE_URL", getPath("10003.epub") );
                setResult(Activity.RESULT_OK, startIntent);
                startActivity(startIntent);
            }
        });
        findViewById(R.id.btn_004).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(),  FolioActivity.class);
                startIntent.putExtra("BOOK_FILE_URL", getPath("10004.epub") );
                setResult(Activity.RESULT_OK, startIntent);
                startActivity(startIntent);
            }
        });

    }



    private String getPath(String book){
        return this.getApplication().getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS
        ).toString() + "/"+book;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        FolioReader.clear();
    }

}