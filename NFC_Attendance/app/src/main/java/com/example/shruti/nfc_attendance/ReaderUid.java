package com.example.shruti.nfc_attendance;

/**
 * Created by shruti on 6/11/17.
 */
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class ReaderUid extends DialogFragment {

    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    public static final String TAG = ReaderUid.class.getSimpleName();

    public static ReaderUid newInstance() {

        return new ReaderUid();
    }

    private TextView mTvMessage;
    private Listener mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout3,container,false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {

        mTvMessage = (TextView) view.findViewById(R.id.tv_message);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (Listener) context;
        mListener.onDialogDisplayed();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onDialogDismissed();
    }

    public void onNfcDetected(Ndef ndef){

        readFromNFC(ndef);
    }

    private void readFromNFC(Ndef ndef) {
        try {
            ndef.connect();
            Tag myTag = ndef.getTag();
            String str = bytesToHex(myTag.getId());
            NdefMessage ndefMessage = ndef.getNdefMessage();
            String message = new String(ndefMessage.getRecords()[0].getPayload());
            Thread thread = new Thread(new Runnable(){
                public void run() {
                    try  {
                        makeRequest(message, str);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

            Log.i("tag ID", str);
            Log.d(TAG, "readFromNFC: " + message);
            mTvMessage.setText(message);
            ndef.close();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void makeRequest(String msg, String uid)
    {
        try {
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
            RequestBody body = RequestBody.create(mediaType, "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"username\"\r\n\r\n"+msg+"\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"uid\"\r\n\r\n"+uid+"\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--");
            Request request = new Request.Builder()
                    .url("http://10.42.0.1:8000/saveuid")
                    .post(body)
                    .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "b0bff1bd-4d71-e548-72a1-0aa14decc6e7")
                    .build();
            //Log.d(TAG, "readFromNFC: " + body);
            Response response = client.newCall(request).execute();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
