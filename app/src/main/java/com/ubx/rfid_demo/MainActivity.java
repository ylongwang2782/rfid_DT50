package com.ubx.rfid_demo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ubx.rfid_demo.pojo.TagScan;
import com.ubx.rfid_demo.ui.main.Activity6BTag;
import com.ubx.rfid_demo.ui.main.SectionsPagerAdapter;

import com.ubx.rfid_demo.ui.main.TagManageFragment;
import com.ubx.rfid_demo.ui.main.TagScanFragment;
import com.ubx.rfid_demo.ui.main.SettingFragment;

import com.ubx.usdk.USDKManager;
import com.ubx.usdk.rfid.RfidManager;
import com.ubx.usdk.rfid.aidl.RfidDate;
import com.ubx.usdk.util.QueryMode;
import com.ubx.usdk.util.SoundTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "usdk";

    public boolean RFID_INIT_STATUS = false;
    public RfidManager mRfidManager;
    public List<String> mDataParents;
    public List<TagScan> tagScanSpinner;
    private List<Fragment> fragments;
    private ViewPager viewPager;
    private TabLayout tabs;
    public int readerType = 0;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SoundTool.getInstance(BaseApplication.getContext());
        mDataParents = new ArrayList<>();
        tagScanSpinner = new ArrayList<>();
        initRfid();

        // init 3 fragment in a tabs
        fragments = Arrays.asList(TagScanFragment.newInstance(MainActivity.this)
                , TagManageFragment.newInstance(MainActivity.this)
                , SettingFragment.newInstance(MainActivity.this));
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), fragments);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        // Jump to tab 1 when init done
        viewPager.post(() -> viewPager.setCurrentItem(0));

        // Initialize WebView
        webView = findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient()); // Prevent opening links in external browser
        webView.getSettings().setDomStorageEnabled(true); // Enable localStorage
        webView.getSettings().setJavaScriptEnabled(true); // Enable JavaScript
        webView.addJavascriptInterface(new WebAppInterface(this), "AndroidEpc");

        SharedPreferences sharedPreferences = getSharedPreferences("WebViewPrefs", MODE_PRIVATE);
        String defaultUrl = sharedPreferences.getString("defaultUrl", "http://192.168.0.101:8082");

        webView.loadUrl(defaultUrl); // Load your desired URL
        webView.setVisibility(View.VISIBLE); // Make WebView visible

        String userAgent = webView.getSettings().getUserAgentString();
        Log.d("WebViewUserAgent", userAgent);

    }

    private List<TagScan> tagData; // Make sure this list is accessibl
    private boolean scanStatus; // Make sure this list is accessibl

    public class WebAppInterface {
        MainActivity mActivity;

        WebAppInterface(MainActivity activity) {
            mActivity = activity;
        }

        // Get epc json
        @JavascriptInterface
        public String startSpinnerInventory() {
            TagScanFragment fragment = (TagScanFragment) fragments.get(0);
            if (fragment.scanStartBtn != null && fragment.scanStartBtn.isEnabled()) {
                fragment.setCallback();
                fragment.setScanStatus(true);
            }
            if (tagData != null && !tagData.isEmpty()) {
                // 获取第一个对象
                TagScan firstTag = tagData.get(0);

                // 再次强制更新列表tvChoiceEpcTid
                TagManageFragment TagManagementFragment = (TagManageFragment) fragments.get(1);
                String epc = firstTag.getEpc().replace(" ", "");
                TagManagementFragment.tvChoiceEpcTid.setText(epc);

                // 返回EPC字符串
                Gson gson = new Gson();
                Log.d(TAG, "tag get ok json" + gson.toJson(firstTag.epc));
                return gson.toJson(firstTag.epc);
            } else {
                Log.d(TAG, "tag is null");
                return null;
            }
        }

        @JavascriptInterface
        public int startContinuousInventory() {
            TagScanFragment fragment = (TagScanFragment) fragments.get(0);
            if (fragment.scanStartBtn != null && fragment.scanStartBtn.isEnabled()) {
                fragment.setCallback();
                fragment.setContinuousScanStatus(true);
            }
            return 0;
        }

        @JavascriptInterface
        public int stopContinuousInventory() {
            TagScanFragment fragment = (TagScanFragment) fragments.get(0);
            if (fragment.scanStartBtn != null && fragment.scanStartBtn.isEnabled()) {
                fragment.setContinuousScanStatus(false);
            }
            return 0;
        }

        @JavascriptInterface
        public String getInventory() {
            TagScanFragment fragment = (TagScanFragment) fragments.get(0);
            if (tagData != null && !tagData.isEmpty()) {
                // 更新管理界面的EPC显示（保持原有逻辑）
                TagScan firstTag = tagData.get(0);
                TagManageFragment TagManagementFragment = (TagManageFragment) fragments.get(1);
                String epc = firstTag.getEpc().replace(" ", "");
                TagManagementFragment.tvChoiceEpcTid.setText(epc);

                // 打印整个tagData列表
                for (TagScan tag : tagData) {
                    Log.d(TAG, "tagData: " + tag.getEpc());
                }

                // 返回整个tagData列表的JSON，包含所有字段
                Gson gson = new Gson();
                Log.d(TAG, "All tags data: " + gson.toJson(tagData));
                return gson.toJson(tagData);
            } else {
                Log.d(TAG, "tag is null");
                return null;
            }
        }

        @JavascriptInterface
        public int htmlAccessInventory(int epcsPos, int memTypePos, int wordStartPos, int wordCount, String accessPWB,
                                       String accessData) {
            TagManageFragment fragment = (TagManageFragment) fragments.get(1);
            // fill word start position
            if (fragment.manageAddressEdit != null) {
                fragment.manageAddressEdit.setText(String.valueOf(wordStartPos));
            }
            if (fragment.manageCntEdit != null) {
                fragment.manageCntEdit.setText(String.valueOf(wordCount));
            }
            fragment.manageWriteEdit.setText(accessData);
            fragment.manageWriteBtn.callOnClick();

            int ret = fragment.getWriteTagStatus();

            return ret;
        }

        @JavascriptInterface
        public void clearFocus() {
            webView.clearFocus();
        }

        // 获取功率JS接口
        @JavascriptInterface
        public int getPower() {
            return mRfidManager.getOutputPower();
        }

        // 设置功率JS接口
        @JavascriptInterface
        public int setPower(int power) {
            if (power < 0 || power > 33) {
                Log.d(TAG, getString(R.string.power_value_not_allow));
                return -1;
            }
            mRfidManager.setOutputPower((byte) power);
            return 0;
        }

    }

    public void onKeyDownHandler() {
        webView.loadUrl("javascript:onKeyDownHandler()");
    }

    public void onKeyUpHandler() {
        webView.loadUrl("javascript:onKeyUpHandler()");
    }

    /*get tagData continuous*/
    public void updateData(List<TagScan> newData) {
        tagData = newData;
    }

    public void updateScanStatus(boolean newScanStatus) {
        scanStatus = newScanStatus;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void initRfid() {
        // 在异步回调中拿到RFID实例
        USDKManager.getInstance().init(BaseApplication.getContext(), new USDKManager.InitListener() {
            @Override
            public void onStatus(USDKManager.STATUS status) {
                if (status == USDKManager.STATUS.SUCCESS) {
                    Log.d(TAG, "initRfid()  success.");
                    RFID_INIT_STATUS = true;
                    mRfidManager = USDKManager.getInstance().getRfidManager();
                    readerType = mRfidManager.getReaderType();//80为短距，其他为长距
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String firmware = mRfidManager.getFirmwareVersion();
                            try {
                                ((TagScanFragment) fragments.get(0)).textFirmware.setText(getString(R.string.firmware) + firmware);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    Log.d(TAG, "initRfid: GetReaderType() = " + readerType);
                } else {
                    Log.d(TAG, "initRfid  fail.");
                }
            }
        });

    }

    /**
     * 设置查询模式
     *
     * @param mode
     */
    private void setQueryMode(int mode) {
        mRfidManager.setQueryMode(QueryMode.EPC_TID);
    }

    /**
     * 通过TID写标签
     */
    private void writeTagByTid() {
        //写入方法（不需要先选中）
        String tid = "24 length TID";
        String writeData = "need write EPC datas ";
        mRfidManager.writeTagByTid(tid, (byte) 0, (byte) 2, "00000000".getBytes(), writeData);
    }

    @Override
    protected void onDestroy() {

        RFID_INIT_STATUS = false;
        if (mRfidManager != null) {
            mRfidManager.disConnect();
            mRfidManager.release();
            Log.d(TAG, "onDestroyView: rfid close");
        }
        SoundTool.getInstance(BaseApplication.getContext()).release();
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        SoundTool.getInstance(BaseApplication.getContext()).release();
        super.onPause();
    }

    /**
     * 设置盘存时间
     *
     * @param interal 0-200 ms
     */
    private void setScanInteral(int interal) {
        int setScanInterval = mRfidManager.setScanInterval(interal);
        Log.v(TAG, "--- setScanInterval()   ----" + setScanInterval);
    }

    /**
     * 获取盘存时间
     */
    private void getScanInteral() {
        int getScanInterval = mRfidManager.getScanInterval();
        Log.v(TAG, "--- getScanInterval()   ----" + getScanInterval);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);//弹出Menu前调用的方法
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_rate_main, menu);
        return true;
    }

    // Creat a reques code admin
    private static final int REQUEST_CODE_ADMIN = 1;

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
//            case R.id.action_invisible:
//                webView.setVisibility(View.INVISIBLE);
//                return true;
//            case R.id.action_visible:
//                webView.setVisibility(View.VISIBLE);
//                return true;
            case R.id.action_refresh:
                if (webView != null) {
                    webView.reload(); // 刷新 WebView
                }
                return true;
            case R.id.action_web_setting:
                Intent intent = new Intent(this, AdminActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADMIN);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // rewrite onActivityResult to recv
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Log.d("BackOndestory","onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADMIN && resultCode == RESULT_OK && data != null) {
            String newUrl = data.getStringExtra("newUrl");
            if (newUrl != null) {
                webView.loadUrl(newUrl);
            }
        }
    }

    @Override
    public void finish() {
        Log.e(TAG, " finish: .......");
        SoundTool.getInstance(BaseApplication.getContext()).release();
        USDKManager.getInstance().release();
        super.finish();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == 523 && event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            onKeyDownHandler();

            if (viewPager.getCurrentItem() == 0) {
//                TagScanFragment fragment = (TagScanFragment) fragments.get(0);
//                fragment.scanStartBtn.callOnClick();
            }
            return true;
        } else if (event.getKeyCode() == 523 && event.getAction() == KeyEvent.ACTION_UP && event.getRepeatCount() == 0) {
            onKeyUpHandler();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

}