package com.wenganse.android.photogallery;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by Plus on 05.06.2017.
 */

public class PhotoPageFragment extends VisibleFragment {
    private static final String ARG_URL ="photo_page_url";

    private Uri mUri;
    private WebView mWebView;
    private ProgressBar mProgressBar;

    public static PhotoPageFragment newInstance(Uri uri){
        Bundle args = new Bundle();
        args.putParcelable( ARG_URL, uri );
        PhotoPageFragment fragment = new PhotoPageFragment();
        fragment.setArguments( args );
        return fragment;
    }

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        mUri = getArguments().getParcelable( ARG_URL );
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate( R.layout.fragment_photo_page, container, false );
        mWebView = (WebView) v.findViewById( R.id.fragment_photo_page_web_view );
        //объявление прогерсс бара.
        mProgressBar = (ProgressBar) v.findViewById( R.id.fragment_photo_page_progress_bar );
        mProgressBar.setMax( 100 ); //начения в диапозоне 0-100
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient( new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress == 100){
                    mProgressBar.setVisibility(View.GONE );
                }else {
                    mProgressBar.setVisibility( View.VISIBLE );
                    mProgressBar.setProgress( newProgress );
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                activity.getSupportActionBar().setSubtitle(title);
            }
        } );
        mWebView.setWebViewClient( new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                return false;
            }
        });

        mWebView.loadUrl( mUri.toString() );

        return v;
    }
}
