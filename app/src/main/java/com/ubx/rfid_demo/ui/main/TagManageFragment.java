package com.ubx.rfid_demo.ui.main;

import android.os.Bundle;


import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.ubx.rfid_demo.BaseApplication;
import com.ubx.rfid_demo.MainActivity;
import com.ubx.rfid_demo.R;
import com.ubx.rfid_demo.pojo.TagManage;
import com.ubx.rfid_demo.pojo.TagScan;
import com.ubx.rfid_demo.utils.ToastUtil;
import com.ubx.usdk.util.SoundTool;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TagManageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TagManageFragment extends Fragment {

    public static final String TAG =  TagManageFragment.class.getSimpleName();

    /**
     * (0x00:RESERVED, 0x01:EPC, 0x02:TID, 0x03:USER)
     */
    private int btMemBank;
    private ManageListAdapterRv manageListAdapterRv;
    private static MainActivity mActivity;
    private ArrayAdapter epcArrayAdapter;
    private List<TagManage> data;
    private HashMap<String, TagManage> map = new HashMap<>();

    private RecyclerView manageListRv;
    private Spinner manageBankSpinner,manageEpcDatasSpinner;
    public TextView tvChoiceEpcTid;
    public EditText manageWriteEdit,manageCntEdit,manageAddressEdit,managePasswordEdit;
    public Button manageReadBtn,manageWriteBtn;

    private int writeTagStatus = 0;

    // 提供公共方法
    public int getWriteTagStatus() {
        return writeTagStatus;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TagManageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TagManageFragment newInstance(MainActivity activity) {
        mActivity = activity;
        TagManageFragment fragment = new TagManageFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tag_manage, container, false);
    }

    @Override
    public void onViewCreated(  View view,  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        manageListRv = view.findViewById(R.id.manage_list_rv);
        manageBankSpinner = view.findViewById(R.id.manage_bank_spinner);
        manageEpcDatasSpinner = view.findViewById(R.id.manage_epc_datas_spinner );

        manageCntEdit = view.findViewById(R.id.manage_cnt_edit);
        manageAddressEdit= view.findViewById(R.id.manage_address_edit);
        managePasswordEdit= view.findViewById(R.id.manage_password_edit);



        tvChoiceEpcTid = view.findViewById(R.id.tv_choice_epc_tid);
        manageWriteEdit = view.findViewById(R.id.manage_write_edit);

        manageReadBtn  = view.findViewById(R.id.manage_read_btn);
        manageWriteBtn= view.findViewById(R.id.manage_write_btn);

        manageListRv.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        manageListRv.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        manageListAdapterRv = new ManageListAdapterRv(null, getActivity());
        manageListRv.setAdapter(manageListAdapterRv);

        initEvents();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void initEvents() {
        manageBankSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        btMemBank = 0x00;
                        break;
                    case 1:
                        btMemBank = 0x01;
                        break;
                    case 2:
                        btMemBank = 0x02;
                        break;
                    case 3:
                        btMemBank = 0x03;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        manageBankSpinner.setSelection(1, true);


        epcArrayAdapter =new ArrayAdapter(mActivity,android.R.layout.simple_spinner_dropdown_item,mActivity.tagScanSpinner){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                convertView = View.inflate(getActivity(),R.layout.spinner_epc_tid_item,null);//获得Spinner布局View
                if(convertView!=null)
                {
                    TextView tvEpc =  convertView.findViewById(R.id.sp_epc_item);
                    TextView tvTid =  convertView.findViewById(R.id.sp_tid_item);
                    try
                    {
                        String epc = mActivity.tagScanSpinner.get(position).getEpc();
//                        String tid = mActivity.tagScanSpinner.get(position).getTid();
                        tvEpc.setText("EPC:"+epc);
//                        tvTid.setText("TID:"+tid);
                        tvTid.setVisibility(View.INVISIBLE);
                    }catch (Exception e){}

                }
                return convertView;
            }
        };
        //给Spinner set适配器
        manageEpcDatasSpinner.setAdapter(epcArrayAdapter);
        manageEpcDatasSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override//重写Item被选择的事件
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                List<TagScan> datas = mActivity.tagScanSpinner;
                if (datas!=null && datas.size()>0){
                    TagScan tagScan = datas.get(position);
                    String tid =  tagScan.getTid().replace(" ", "");
                    if (!TextUtils.isEmpty(tid)){

                    }else {

                    }

                    String epc =  tagScan.getEpc().replace(" ", "");
                    tvChoiceEpcTid.setText(epc);
                }


            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        tvChoiceEpcTid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageEpcDatasSpinner.performClick();
            }
        });

        manageReadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mActivity.mRfidManager==null) {
                    toast(getString(R.string.rfid_not_init));
                    return;
                }


                String s_epc = tvChoiceEpcTid.getText().toString().trim();
                String strPwd = managePasswordEdit.getText().toString();
                String s_address = manageAddressEdit.getText().toString();
                String s_length = manageCntEdit.getText().toString();
                if (s_epc.equals("") || strPwd.equals("") || s_address.equals("") || s_length.equals("")) {
                    toast("Please fill in the parameters first.");
                    return;
                }

                map.clear();
                byte cnt = Integer.valueOf(s_length).byteValue();
                byte address = Integer.valueOf(s_address).byteValue();
                byte[] pwd = hexStringToBytes(strPwd);



                int mem = manageBankSpinner.getSelectedItemPosition();
                Log.d(TAG, "initEvents: epc:"+s_epc +"  cnt =  "+ cnt + ", address = " + address + ", strPwd = " + Arrays.toString(hexStringToBytes(strPwd)) + ", pwd" + Arrays.toString(pwd));

                String dataRead = mActivity.mRfidManager.readTag(s_epc, (byte) mem, address, cnt, pwd);
                Log.e(TAG, "onClick: ......");
                if (TextUtils.isEmpty(dataRead)) {
                    Log.e(TAG, "onClick: ......");
                    toast("read tag fail");
                } else {
                    toast("read tag success");
                    readTag(s_epc, dataRead);
                }
                //1fb1f280951t
            }
        });
        manageWriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "initEvents: read tag");
                if(mActivity.mRfidManager==null) {
                    toast(getString(R.string.rfid_not_init));
                    return;
                }
                //map.clear();
                String s_epc = tvChoiceEpcTid.getText().toString().trim();
                String s_pwd = managePasswordEdit.getText().toString();
                String s_address = manageAddressEdit.getText().toString();
                String s_length = manageCntEdit.getText().toString();

                if (s_epc.equals("") || s_pwd.equals("") || s_address.equals("") || s_length.equals("")) {
                    toast("Please fill in the parameters first.");
                    return;
                }

                byte[] pwd = hexStringToBytes(managePasswordEdit.getText().toString());
                int add = Integer.parseInt(manageAddressEdit.getText().toString());
                int cnt = Integer.parseInt(manageCntEdit.getText().toString());
                String dataEd = manageWriteEdit.getText().toString();

                if (TextUtils.isEmpty(dataEd)) {
                    toast(getString(R.string.write_epc_first));
                    return;
                }
                String data = dataEd.replaceAll(" ", "");

                if (data.length() % 4 != 0) {//TODO data 长度不够4的倍数，后面自动补0
                    int less = data.length() % 4;
                    for (int i = 0; i < 4 - less; i++) {
                        data = data + "0";
                    }
                }
                byte[] d = hexStringToBytes(data);
                int mem = manageBankSpinner.getSelectedItemPosition();
                Log.e(TAG, "onClick: epc:" + s_epc + " mem:" + mem + " address:" + add + " data:" + data + "len:" + d.length / 2);
                int i = mActivity.mRfidManager.writeTag(s_epc, pwd, (byte) mem, (byte) add, (byte) (d.length / 2), d);
                writeTagStatus = i;
                if (i == 0) {
                    toast("Data was written successfully");
                    SoundTool.getInstance(BaseApplication.getContext()).playBeep(1);
                } else {
                    toast("Data write failed " + i);
                }
            }
        });

    }
    private void readTag(final String epc, final String tid){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                {
                    if (!map.containsKey(epc)) {
                        TagManage tagManage = new TagManage(epc, "", tid, "", false);
                        map.put(epc, tagManage);
                    } else {
                        TagManage tagManage = map.get(epc);
                        tagManage.setData(tid);
                        map.put(epc, tagManage);
                    }
                    data = new ArrayList<>(map.values());
                    Log.d(TAG, "onOperationTag: data = " + Arrays.toString(data.toArray()));
                    manageListAdapterRv.setData(data);
                    SoundTool.getInstance(BaseApplication.getContext()).playBeep(1);

                }
            }
        });
    }

    private void write(final String epc, final String tid){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                {
                    if (!map.containsKey(epc)) {
                        TagManage tagManage = new TagManage(epc, "", tid, "", false);
                        map.put(epc, tagManage);
                    } else {
                        TagManage tagManage = map.get(epc);
                        tagManage.setData(tid);
                        map.put(epc, tagManage);
                    }
                    data = new ArrayList<>(map.values());
                    Log.d(TAG, "onOperationTag: data = " + Arrays.toString(data.toArray()));
                    manageListAdapterRv.setData(data);

                }
            }
        });
    }

    /**
     * 获取 PC值
     * @param epc
     * @return
     */
    private String getPC(String epc){
        String pc ="0000";
        int len = epc.length()/4;
        int b = len << 11;
        String aHex = Integer.toHexString(b);
        if (aHex.length() == 3){
            pc = "0"+aHex;
        } else {
            pc = aHex;
        }
        return pc;
    }


    /**
     * 将Hex String转换为Byte数组
     *
     * @param hexString the hex string
     * @return the byte [ ]
     */
    public static byte[] hexStringToBytes(String hexString) {
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() >> 1];
        int index = 0;
        for (int i = 0; i < hexString.length(); i++) {
            if (index > hexString.length() - 1) {
                return byteArray;
            }
            byte highDit = (byte) (Character.digit(hexString.charAt(index), 16) & 0xFF);
            byte lowDit = (byte) (Character.digit(hexString.charAt(index + 1), 16) & 0xFF);
            byteArray[i] = (byte) (highDit << 4 | lowDit);
            index += 2;
        }
        return byteArray;
    }


    private void toast(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.toast(message);
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();

        if(mActivity.tagScanSpinner.size()!=0){
            manageEpcDatasSpinner.setSelection(0);
        }
    }

    @Override
    public void onPause() {

        super.onPause();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            manageBankSpinner.setSelection(1);
            if(mActivity.tagScanSpinner.size()>0){
                manageEpcDatasSpinner.setSelection(0);
                tvChoiceEpcTid.setText(mActivity.tagScanSpinner.get(0).getEpc());
            }
            epcArrayAdapter.notifyDataSetChanged();
        }else{
        }
    }


}