package com.example.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.client.R;
import com.example.client.SApplication;
import com.example.client.URL;
import com.example.common.FileHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Saiber on 10.11.13.
 */
public class FileExplorerFragment extends Fragment {

    private JSONArray JSONFileList;
    ArrayList<HashMap<String, Object>> listContent;
    private String dir;
    private String currentPath = "/";
    private static String NAME = "name";
    private static String PATH = "path";
    private static String SIZE = "size";
    private static String ICON = "icon";
    private static String DIR  = "dir";
    private ListView listView = null;

    private int firstVisibleItem = 0;
    private int visibleCountItem = 0;
    private int totalCountItem = 0;
    private int selectedItem = 0;
    boolean is_runing = false;

    Bundle bundle = null;
    private final long FILE_CACHE_SIZE = 350;


    public static Fragment newInstance(Context context) {
        FileExplorerFragment fileExplorerFragment = new FileExplorerFragment();
        return fileExplorerFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("info"," FileExplorerFragment [ onCreateView ]");
        if (savedInstanceState != null){
            currentPath = savedInstanceState.getString("current_path");
            bundle = savedInstanceState;
        }
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.file_list, null);
        listView = (ListView)root.findViewById(R.id.listView);
        listContent = new ArrayList<HashMap<String, Object>>();

        // list view listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(is_runing)
                    return;
                selectedItem = i;
                HashMap<String,Object>item = listContent.get(i);
                boolean dir = (Boolean)item.get(DIR);
                if (dir){
                    String p = (String)item.get(PATH)  + (String)item.get(NAME);
                    currentPath = p;
                    new DirTask().execute(currentPath);
                }else{
                    String p = (String)item.get(PATH)  + (String)item.get(NAME);
                    String[] param = {p, (String)item.get(PATH).toString(), (String)item.get(NAME)};
                    new OpenFileTask().execute(param);
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return true;
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {}
            @Override
            public void onScroll(AbsListView absListView, int firstVisible, int visibleCount, int totalCount) {
                firstVisibleItem = firstVisible;
                visibleCountItem = visibleCount;
                totalCountItem = totalCount;
            }
        });
        dir = new String(URL.host) + "/ls/?path=";
        return root;
    }

    /* life cycle */
    @Override
    public void onResume(){
        super.onResume();
        if (getCacheSize() >= FILE_CACHE_SIZE)
            clearChache();
        new DirTask().execute(currentPath);
    }
    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onSaveInstanceState(Bundle onState) {
        super.onSaveInstanceState(onState);
        onState.putString("current_path",currentPath);
    }

    /* ls */
    class DirTask extends AsyncTask<String, Void, Boolean> {
        private String responce;

        @Override
        protected Boolean doInBackground(String... path) {
            is_runing = true;
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpConnectionParams.setSoTimeout(httpParams, 10000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpGet httpGet = new HttpGet(dir + path[0]);
            String str = dir + path[0];
            Log.i("info"," GET : " + str);

            try {
                HttpContext httpContext = new BasicHttpContext();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);
                HttpResponse httpResponse = httpClient.execute(httpGet,httpContext);
                HttpEntity httpEntity = httpResponse.getEntity();
                responce = EntityUtils.toString(httpEntity);
                Log.i("info",responce);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result){
                try {
                    JSONFileList = new JSONArray(responce);
                    listContent.clear();
                    char[] p = currentPath.toCharArray();
                    int count = p.length;
                    while(count != 0){
                        if (p[count-1] == '/'){
                            if (count < p.length)
                                break;
                        }
                        count--;
                    }
                    String back = currentPath.substring(0,count);

                    if (back.toString().length() == 0)
                        back = "/";

                    HashMap<String,Object>resurce = new HashMap<String, Object>();

                    resurce.put(NAME,back);
                    resurce.put(ICON,R.drawable.folder);
                    resurce.put(PATH,"");
                    resurce.put(DIR, Boolean.TRUE);
                    listContent.add(resurce);

                    if (JSONFileList.length() > 0){
                        for (int i = 0; i<JSONFileList.length(); i++){
                            JSONObject file = JSONFileList.getJSONObject(i);
                            file = file.getJSONObject("fields");
                            resurce = new HashMap<String, Object>();
                            resurce.put(NAME,file.optString("name"));
                            resurce.put(PATH,file.optString("path"));
                            if (file.optBoolean("is_dir")){
                                resurce.put(SIZE,"");
                                resurce.put(ICON,R.drawable.folder);
                                resurce.put(DIR,Boolean.TRUE);
                            } else {
                                float size = (float)file.optDouble("size");
                                String fileSize = String.format("%.0f",(float)size) + " B";
                                if (size > 2048){
                                    size = size / 1024;
                                    fileSize = String.format("%.1f",(float)size)+ " Kb";
                                }
                                if (size > 2048)
                                    fileSize = String.format("%.2f",(float)(size/1024)) + " Mb";

                                resurce.put(SIZE, fileSize);
                                resurce.put(ICON,getFileImage(file.optString("name")));
                                resurce.put(DIR,Boolean.FALSE);
                            }
                            listContent.add(resurce);
                        }
                    }
                } catch (JSONException e) {
                    is_runing = false;
                    e.printStackTrace();
                }

                SimpleAdapter adapter = new SimpleAdapter(getActivity(), listContent, R.layout.file_item,
                        new String[]{NAME,PATH,ICON,SIZE},
                        new int[]{R.id.text1,R.id.text2,R.id.icon,R.id.text3});
                listView.setAdapter(adapter);
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                listView.setSelection(firstVisibleItem);
                is_runing = false;
            }
        }
    }

    /* open file */
    class OpenFileTask extends AsyncTask<String, Void, Boolean> {
        ProgressDialog progressDialog = null;
        @Override
        protected void onPreExecute(){
            Context context = getActivity();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            progressDialog.cancel();
        }

        @Override
        protected Boolean doInBackground(String... file) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams,10000);
            HttpConnectionParams.setSoTimeout(httpParams,10000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpGet httpGet = null;
            try {
                httpGet = new HttpGet(URL.host + "/get_file/?file=" + URLEncoder.encode(file[0], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }

            HttpContext httpContext = new BasicHttpContext();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, SApplication.cookieStore);

            try {
                HttpResponse httpResponse = httpClient.execute(httpGet,httpContext);
                Log.i("info", httpResponse.getFirstHeader("Content-length").toString());
                byte[] _file_ = EntityUtils.toByteArray(httpResponse.getEntity());
                File f = new File(get_cache_path(), file[2]);
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(_file_, 0, _file_.length);
                fos.flush();
                fos.close();
                byte[] newByte = new byte[15];
                System.arraycopy(_file_, 0, newByte, 0, 15);
                Log.i("info"," - files data = " + new String(_file_,"UTF-8"));
                Intent intent = FileHelper.openFileIntent(f);
                if(intent != null) {
                    startActivity(FileHelper.openFileIntent(f));
                    return true;
                }else {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

    }

    private long getCacheSize() {
        long size = 0;
        File file = new File(get_cache_path());
        if(file != null && file.isDirectory()){
            File[] files = file.listFiles();
            for(File f : files)
                size += f.length() / 1024 / 1024;
        }
        Log.i("info"," Cache Size = " + Long.toString(size) + " Mb");
        return size;
    }

    private void clearChache() {
        File cacheDir;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"Client");
        else
            cacheDir=getActivity().getCacheDir();

        File[] files = cacheDir.listFiles();
        long size = 0;
        if (files != null) {
            for (File f : files)
                f.delete();
        }
        cacheDir.delete();
    }

    private String get_cache_path(){
        File cacheDir;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"Client");
        }else {
            cacheDir=getActivity().getCacheDir();
        }

        if(!cacheDir.exists())
            cacheDir.mkdirs();
        return cacheDir.getAbsolutePath();
    }

    private int getFileImage(String filename) {
        String extension = "";
        try{
            extension = filename.substring(filename.lastIndexOf("."));
        }catch(Exception e){
            return R.drawable.file;
        }

        if (extension.equals(".pdf")){
            return R.drawable.file_pdf;
        }else if(extension.equals(".h")){
            return R.drawable.file_h;
        }else if(extension.equals(".py")){
            return R.drawable.file_py;
        }else if (extension.equals(".xls")){
            return R.drawable.file_excel;
        }else if (extension.equals(".doc") || extension.equals(".docx")){
            return R.drawable.file_word;
        }else if (extension.equals(".c")){
            return R.drawable.file_c;
        }else if (extension.equals(".cpp") || extension.equals(".cp") || extension.equals(".c++")
                || extension.equals(".gcc") || extension.equals(".g++") || extension.equals(".cc")){
            return R.drawable.file_cpp;
        }else if(extension.equals(".java")){
            return R.drawable.file_java;
        }else if (extension.equals(".bmp") || extension.equals(".jpg") || extension.equals(".png")) {
            return R.drawable.file_image;
        }else if(extension.equals(".xml")){
            return R.drawable.file_xml;
        }else if(extension.equals(".apk") || extension.equals(".exe")) {
            return R.drawable.file_exe;
        }else if(extension.equals(".mp4")) {
            return R.drawable.file_film;
        }else if(extension.equals(".mp3")){
            return R.drawable.file_sound;
        }
        return R.drawable.file;
    }

    private String parseFileHeader(byte[] array) {
        return null;
    }
}
