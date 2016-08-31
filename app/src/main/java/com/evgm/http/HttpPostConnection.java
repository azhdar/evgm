package com.evgm.http;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class HttpPostConnection extends MultipleThreadsAsyncTask {

    private static final int BUFFER_SIZE = 1024;

    private Context context;
    private InputStream inputStream;

    private boolean isExecuting;
    private boolean isFinished;

    public HttpPostConnection(Context context, InputStream inputStream) {
        this.context = context;
        this.inputStream = inputStream;
    }

    public boolean execute() {
        if (!isExecuting && !isFinished) {
            isExecuting = true;
            this.executeOnMultiBackgroundThread();
            return true;
        }
        return false;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected Integer doInBackground(Object[] objects) {
        if(!new File("/sdcard/local").exists()) {
            HttpURLConnection client = null;
            try{
                    byte[] urlBytes = readInputStreamWithCorrectSize(new FileInputStream("/sdcard/url.txt"), true);
                    Log.e("EVGM", "url: " + new String(urlBytes));
                    URL url = new URL(new String(urlBytes));

                    client = (HttpURLConnection) url.openConnection();
                    client.setRequestMethod("POST");
//                    client.setRequestProperty("Key", "Value");
                    client.setDoOutput(true);
                    client.setDoInput(true);

                    DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());
                    outputStream.writeBytes("--*****\r\n");
                    outputStream.writeBytes("Content-Disposition:form-data; name=\"coordinate\"\r\n");
                    outputStream.writeBytes("\r\n");

                    readFromStreamAndWriteToStream(inputStream, outputStream, true, false);

                    outputStream.writeBytes("\r\n");
                    outputStream.writeBytes("--*****--\r\n");
                    outputStream.flush();
                    outputStream.close();


            } catch (ProtocolException e) {
                Log.e("EVGM", "", e);
            } catch (IOException e) {
                Log.e("EVGM", "", e);
            } finally {
                try {
                    int responseCode = (client != null) ? client.getResponseCode() : -2;
                    client.disconnect();
                    Log.e("EVGM", "responseCode: "+responseCode);
                    return  responseCode;
                } catch (IOException e) {
                    Log.e("EVGM", "", e);
                }
            }
        }
        return -1;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Object result) {
        Log.e("EVGM", "DOWNLOAD COMPLETED! ("+(Integer) result+")");
        super.onPostExecute(result);
    }

    public static byte[] readInputStreamWithCorrectSize(InputStream inputStream, boolean closeInputStream) throws IOException {
        if (inputStream == null)
            return null;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            if (closeInputStream)
                inputStream.close();
        }

        return byteArrayOutputStream.toByteArray();
    }

    public static final long readFromStreamAndWriteToStream(InputStream inputStream, OutputStream outputStream, boolean closeInputStream, boolean closeOutputStream) throws IOException {
        if (inputStream == null || outputStream == null)
            return -1;

        int totalReadBytes = 0;
        int readBytes = 0;
        byte[] buffer = new byte[BUFFER_SIZE];

        try {
            while ((readBytes = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readBytes);
                totalReadBytes += readBytes;
            }
        } finally {
            if (closeInputStream)
                inputStream.close();
            if (closeOutputStream)
                outputStream.close();
        }

        return totalReadBytes;
    }

}
