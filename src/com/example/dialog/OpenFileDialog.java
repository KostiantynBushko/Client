package com.example.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.client.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kbushko on 1/3/14.
 */
public class OpenFileDialog extends DialogFragment{

    public static interface onButtonClickListener {
        void onPositiveButtonClick(String filePath);
    }

    private int mNum;
    Button positiveButton;
    String path = "";


    String root;
    ArrayList<HashMap<String, Object>> listObject = new ArrayList<HashMap<String, Object>>();
    List<String> folderTree = new ArrayList<String>();
    private static final String NAME = "name";
    private static final String DESCRIBE = "describe";
    private static final String ICON = "icon";
    private static final String SIZE = "size";
    private static final String OBJECT = "object";

    SimpleAdapter adapter;
    TextView textView;
    ListView listView;

    public static OpenFileDialog newInstance(int num) {
        OpenFileDialog dialog = new OpenFileDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("num", num);
        dialog.setArguments(bundle);
        return dialog;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNum = getArguments().getInt("num");

        int style = DialogFragment.STYLE_NORMAL;
        int theme = 0;
        switch ((mNum-1)%6) {
            case 1: style = DialogFragment.STYLE_NO_TITLE; break;
            case 2: style = DialogFragment.STYLE_NO_FRAME; break;
            case 3: style = DialogFragment.STYLE_NO_INPUT; break;
            case 4: style = DialogFragment.STYLE_NORMAL; break;
            case 5: style = DialogFragment.STYLE_NORMAL; break;
            case 6: style = DialogFragment.STYLE_NO_TITLE; break;
            case 7: style = DialogFragment.STYLE_NO_FRAME; break;
            case 8: style = DialogFragment.STYLE_NORMAL; break;
        }
        switch ((mNum-1)%6) {
            case 4: theme = android.R.style.Theme_Holo; break;
            case 5: theme = android.R.style.Theme_Holo_Light_Dialog; break;
            case 6: theme = android.R.style.Theme_Holo_Light; break;
            case 7: theme = android.R.style.Theme_Holo_Light_Panel; break;
            case 8: theme = android.R.style.Theme_Holo_Light; break;
        }
        setStyle(style,theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.open_file_dialog, container, false);
        ((Button)view.findViewById(R.id.negative)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        positiveButton = (Button)view.findViewById(R.id.positive);
        positiveButton.setEnabled(false);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() instanceof onButtonClickListener){
                    ((onButtonClickListener) getActivity()).onPositiveButtonClick(path);
                    dismiss();
                }
            }
        });

        textView = (TextView)view.findViewById(R.id.textView2);
        /* List view */
        listView = (ListView)view.findViewById(R.id.listView);
        root = Environment.getRootDirectory().getPath();
        File file = new File("/");
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            HashMap<String, Object> res = new HashMap<String, Object>();
            res.put(NAME, f.getName());
            if (f.isDirectory()) {
                res.put(DESCRIBE, f.getAbsolutePath().toString());
                res.put(ICON, R.drawable.folder);
                res.put(SIZE,null);
            } else {
                res.put(DESCRIBE, f.getAbsolutePath().toString());
                res.put(ICON, R.drawable.file);
                if (f.length() > 1024) {
                    res.put(SIZE,new String(Long.toString(f.length()/1024)) + " Kb");
                }else{
                    res.put(SIZE,new String(Long.toString(f.length())) + " B");
                }
            }
            res.put(OBJECT,f);
            listObject.add(res);
        }
        adapter = new SimpleAdapter(getActivity(), listObject, R.layout.file_item,
                new String[]{NAME,DESCRIBE,ICON,SIZE},
                new int[]{R.id.text1,R.id.text2,R.id.icon,R.id.text3});
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                HashMap<String,Object> object = listObject.get(position);
                File f = (File)(object.get(OBJECT));
                if (f.isDirectory()){
                    File[] files = f.listFiles();
                    listObject.clear();
                    textView.setText(f.getAbsolutePath().toString());
                    HashMap<String, Object> res = new HashMap<String, Object>();
                    if (f.getParentFile() != null){
                        res.put(NAME,f.getParentFile().getPath().toString());
                        res.put(DESCRIBE,null);
                        res.put(ICON,R.drawable.folder);
                        res.put(OBJECT,f.getParentFile());
                        res.put(SIZE,null);
                        listObject.add(res);
                    }
                    if (files != null) {
                        for (int i = 0; i < files.length; i++) {
                            File _f = files[i];
                            res = new HashMap<String, Object>();
                            res.put(NAME, _f.getName());
                            if (_f.isDirectory()) {
                                res.put(DESCRIBE, _f.getAbsolutePath().toString());
                                res.put(ICON, R.drawable.folder);
                                res.put(SIZE,null);
                            } else {
                                res.put(DESCRIBE, _f.getAbsolutePath().toString());
                                res.put(ICON, getFileImage(res.get(NAME).toString()));
                                if (_f.length() > 1024) {
                                    res.put(SIZE,new String(Long.toString(_f.length()/1024)) + " Kb");
                                }else{
                                    res.put(SIZE,new String(Long.toString(_f.length())) + " B");
                                }
                            }
                            res.put(OBJECT,_f);
                            listObject.add(res);
                            positiveButton.setEnabled(false);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    path="";
                }else {
                    String extension = "";
                    //extension = object.get(NAME).toString().substring(object.get(NAME).toString().lastIndexOf("."));
                    try{
                        extension = object.get(NAME).toString().substring(object.get(NAME).toString().lastIndexOf("."));
                    }catch(Exception e){
                        extension = "";
                    }
                    if (extension.equals(".apk")){
                        Toast.makeText(getActivity(), extension, Toast.LENGTH_SHORT).show();
                        positiveButton.setEnabled(true);
                        textView.setText(object.get(DESCRIBE).toString() /*object.get(NAME).toString()*/);
                        path = object.get(DESCRIBE).toString();
                    }else {
                        Toast.makeText(getActivity(), "Only *.apk files...", Toast.LENGTH_SHORT).show();
                        positiveButton.setEnabled(false);
                        textView.setText("...");
                        path = "";
                    }
                }
            }
        });

        return view;
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
}
