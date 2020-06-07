package com.example.OMPM;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExpenditurePC#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExpenditurePC extends Fragment {
    WebView mWebView;

    public ExpenditurePC() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ExpenditurePC newInstance(String param1, String param2) {
        ExpenditurePC fragment = new ExpenditurePC();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_expenditure_p_c, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
    mWebView = getView().findViewById(R.id.wvPieChart);
    WebSettings websettings = mWebView.getSettings();
    websettings.setJavaScriptEnabled(true);
    }
}