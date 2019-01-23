package com.cloudrtc.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.cloudrtc.R;
import com.cloudrtc.sdk.SipIncomingListener;
import com.cloudrtc.sdk.SipOutgoingListener;
import com.cloudrtc.sdk.SipRegisterListener;
import com.cloudrtc.service.PhoneService;
import com.cloudrtc.util.Contacts;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CloudrtcDemo extends BaseActivity implements OnClickListener,
        SipRegisterListener, SipIncomingListener, SipOutgoingListener {

    private Button mBtnRegister, mBtnCall, mVideoCall;
    private EditText mEditPwd, mEditUser, mEditCall;
    private String mPwd, mUser, mPeerNumber;
    private  boolean ProfileValueChange;
    private SharedPreferences sp;

    @SuppressLint("HandlerLeak")

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x111) {
                mBtnRegister.setText("注销");
            } else if(msg.what == 0x222) {
                mBtnRegister.setText("注册");
            } else if(msg.what == 0x333) {
                mBtnCall.setText("挂断");
            } else if(msg.what == 0x444) {
                mBtnCall.setText("对讲");
            }
        }
    };

    @Override
    public void OnRegistrationProgress(int acc_id) {

    }

    @Override
    public void OnRegistrationSuccess(int acc_id) {
        Message msg = new Message();
        msg.what = 0x111;
        handler.sendMessage(msg);
    }

    @Override
    public void OnRegisterationFailed(int acc_id, int code, String reason) {
        Message msg = new Message();
        msg.what = 0x222;
        handler.sendMessage(msg);
    }

    @Override
    public void OnRegistrationCleared(int acc_id) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloudrtc_demo);
        mBtnRegister = (Button) this.findViewById(R.id.btnRegister);
        mBtnRegister.setOnClickListener(this);
        mBtnCall = (Button) this.findViewById(R.id.btncall);
        mBtnCall.setOnClickListener(this);
        mEditPwd = (EditText) this.findViewById(R.id.editPwd);
        mEditUser = (EditText) this.findViewById(R.id.editUser);
        mEditCall = (EditText) this.findViewById(R.id.editCall);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        // mVideoCall = (Button) this.findViewById(R.id.videocall);
       // mVideoCall.setOnClickListener(this);

        if(!PhoneService.isready()) {
            PhoneService.startService(this);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .build();

        GitHubService repo = retrofit.create(GitHubService.class);


        Call<ResponseBody> call = repo.contributorsBySimpleGetCall("square", "retrofit");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Gson gson = new Gson();
                    ArrayList<Contributor> contributorsList = gson.fromJson(response.body().string(), new TypeToken<List<Contributor>>(){}.getType());
                    for (Contributor contributor : contributorsList){
                        Log.d("login",contributor.getLogin());
                        Log.d("contributions",contributor.getContributions()+"");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            // Handle presses on the action bar items
            switch (item.getItemId()) {
                case R.id.action_settings:
                    forwardToSettings();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void initUIandEvent() {

    }

    @Override
    protected void deInitUIandEvent() {

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        this.mPwd = this.mEditPwd.getText().toString();
        this.mUser = this.mEditUser.getText().toString();
        this.mPeerNumber = this.mEditCall.getText().toString();
        PhoneService.instance().setSipRegisterListener(this);
        PhoneService.instance().setSipIncomingListener(this);
        PhoneService.instance().setSipOutgoingListener(this);
        if(v.getId() == R.id.btnRegister) {
            //boolean ret = PhoneService.instance().IsRegistered();
           // if(!ret) {
            //if(!TextUtils.isEmpty(mUser))
            String phoneNumber = sp.getString("sip_account_username", "");
            String password = sp.getString("sip_account_password", "");
            String sip_server = sp.getString("sip_account_domain", "");
            String transport_type = sp.getString("sip_account_transport", "tls");

            if (!TextUtils.isEmpty(phoneNumber) && !TextUtils.isEmpty(password)
                    && !TextUtils.isEmpty(sip_server)) {
                PhoneService.instance().RegisterSipAccount(phoneNumber, password, sip_server, "tcp");
            }
            //} else {

            //    PhoneService.instance().UnRegisterSipAccount();
           // }
        }

        else if(v.getId() == R.id.btncall) {
            if(!PhoneService.instance().InCalling()) {

              if(!TextUtils.isEmpty(mPeerNumber))
                PhoneService.instance().MakeCall(mPeerNumber, true);
            }
        }
    }

    @Override
    public void onCallIncoming(int call_id) {
        Intent intent = new Intent(this, VideoScreenActivity.class);
        intent.putExtra(Contacts.PHONESTATE, Contacts.RECEIVE_VIDEO_REQUEST);
        intent.putExtra(Contacts.ACTION_FROM_SERVICE,
                Contacts.ACTION_FROM_PHONE_SERVICE);
        intent.putExtra(Contacts.PHONNUMBER, "13798210325");
        startActivity(intent);
    }

    @Override
    public void onCallOutgoing(int call_id) {
        Intent intent = new Intent(this, VideoScreenActivity.class);
        intent.putExtra(Contacts.PHONESTATE, Contacts.INVITE_VIDEO_REQUEST);
        intent.putExtra(Contacts.ACTION_FROM_SERVICE, 100);
        intent.putExtra(Contacts.ACTION_FROM_SERVICE,
                Contacts.ACTION_FROM_PHONE_SERVICE);
        startActivity(intent);
    }

   /* @Override
    public void onCallIncoming(SipApCallPeer peer) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, VideoScreenActivity.class);
        intent.putExtra(Contacts.PHONESTATE, Contacts.RECEIVE_VIDEO_REQUEST);
        intent.putExtra(Contacts.ACTION_FROM_SERVICE,
                Contacts.ACTION_FROM_PHONE_SERVICE);
        intent.putExtra(Contacts.PHONNUMBER, peer.callUri);
        startActivity(intent);
    }*/

   /* @Override
    public void onRegisterStatus(int stat) {
        // TODO Auto-generated method stub
        if(stat == RegistrationState.Sucess.IntgerValue()) {
            Message msg = new Message();
            msg.what = 0x111;
            handler.sendMessage(msg);
        } else if(stat == RegistrationState.None.IntgerValue()) {
            Message msg = new Message();
            msg.what = 0x222;
            handler.sendMessage(msg);
        }
    }*/


   /* @Override
    public void onCallOutgoing(SipApCallPeer peer) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, VideoScreenActivity.class);
        intent.putExtra(Contacts.PHONESTATE, Contacts.INVITE_VIDEO_REQUEST);
        intent.putExtra(Contacts.ACTION_FROM_SERVICE, peer.status);
        intent.putExtra(Contacts.ACTION_FROM_SERVICE,
                Contacts.ACTION_FROM_PHONE_SERVICE);
        startActivity(intent);
    }*/

    public void forwardToSettings() {
        Intent intent = new Intent(this, ReferencesSettings.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("ProfileValueChange", false);
        intent.putExtras(bundle);
        startActivityForResult(intent,  0);

        //getFragmentManager().beginTransaction().
          //      replace(android.R.id.content, new ReferencesSettings()).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        switch (resultCode)
        {
            case RESULT_OK:
                Bundle bundle = data.getExtras();
                ProfileValueChange =  bundle.getBoolean("ProfileValueChange");
                System.out.println("=============ProfileValueChange=======:" +ProfileValueChange);
                break;
            default:
                break;
        }
    }
 }
