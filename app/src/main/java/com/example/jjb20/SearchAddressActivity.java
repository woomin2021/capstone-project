package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class SearchAddressActivity extends AppCompatActivity {
    //도로명 주소 검색을 위한 WebView
    //Firebase hosting 사용
    //TODO. js 파일 넣어두기
    WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        webView = findViewById(R.id.webView);
        //자바 스크립트 허용
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new BridgeInterface(), "Android");
        webView.setWebViewClient(new WebViewClient() {
            /**
             * Notify the host application that a page has finished loading. This method
             * is called only for main frame. Receiving an {@code onPageFinished()} callback does not
             * guarantee that the next frame drawn by WebView will reflect the state of the DOM at this
             * point. In order to be notified that the current DOM state is ready to be rendered, request a
             * visual state callback with {@link WebView#postVisualStateCallback} and wait for the supplied
             * callback to be triggered.
             *
             * @param view The WebView that is initiating the callback.
             * @param url  The url of the page.
             */
            @Override
            public void onPageFinished(WebView view, String url) {
                // 안드로이드에서 javascript 함수 호출
                webView.loadUrl("javascript:sample2_execDaumPostcode();");
            }
        });

        // 최초 웹뷰 로드
        webView.loadUrl("https://jjb-ver2.web.app");
    }

    private class BridgeInterface {
        @JavascriptInterface
        public void processDATA(String address) {
            //https://www.youtube.com/watch?v=I6sjUmcfCuU
            // 카카오 주소 검색 API 결과 값을 전달받음. (from javascript)
            // 브릿지 통로를 통해 전달 받는다.
            // js 파일에 processDATA가 있음
            Intent intent = new Intent();
            intent.putExtra("address",address);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
