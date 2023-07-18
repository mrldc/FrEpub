package com.folioreader.ui.base;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.folioreader.model.db.PageProgress;
import com.folioreader.model.sqlite.PageProgressTable;
import com.folioreader.util.AppUtil;

import org.readium.r2.shared.Link;
import org.readium.r2.shared.LinkKt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * 页面进度条计算
 *
 *
 * @author by gautam on 12/6/17.
 */

public class PageProgressTask extends AsyncTask<Link, Void, List<PageProgress>> {

    private static final String TAG = "PageProgressTask";

    private PageProgressTaskCallback callback;
    private String host;

    private Context context;

    public PageProgressTask(Context context,PageProgressTaskCallback callback, String host) {
        this.context = context;
        this.callback = callback;
        this.host = host;
    }

    @Override
    protected List<PageProgress> doInBackground(Link... links) {

        List<PageProgress> pageProgressList = new ArrayList<>();
        float totalSize = 0;
        for (int i = 0; i < links.length; i++) {
            Link link = links[0];
            PageProgress pageProgress = new PageProgress();
            pageProgress.pageNumber = i;
            pageProgress.href = link.getHref();
            try {
                String strUrl = host+link.getHref();
                URL url = new URL(strUrl);
                URLConnection urlConnection = url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, AppUtil.charsetNameForURLConnection(urlConnection)));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append('\n');
                }
                if (stringBuilder.length() > 0)
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                pageProgress.contentSize=stringBuilder.length();
                totalSize += pageProgress.contentSize;
                pageProgressList.add(pageProgress);
            } catch (IOException e) {
                Log.e(TAG, "PageProgressTask 获取页面内容出错", e);
            }
        }
        //计算各页面百分比
        float start = 0;
        for (int i = 0; i < pageProgressList.size(); i++) {
            PageProgress pageProgress = pageProgressList.get(i);
            pageProgress.start = start;
            if(pageProgressList.size() - 1 == i){
                //最后一页用总百分比去减
                pageProgress.end = 100 - start;
            }else{
                //保留两位小数
                pageProgress.end = Math.round(pageProgress.contentSize / totalSize * 10000)*0.01f +start;
            }
            start =  pageProgress.end;
        }
        //保存到数据库
        for (int i = 0; i < pageProgressList.size(); i++) {
            new PageProgressTable(context).insertBook(pageProgressList.get(i));
        }
        return null;
    }




    @Override
    protected void onPostExecute(List<PageProgress> pageProgressList) {
        if (pageProgressList != null) {
            callback.onCompletePageCalculate(pageProgressList);
        } else {
            callback.onError();
        }
        cancel(true);
    }
}
