package com.atharva.questionsocr;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.atharva.questionsocr.SingleProcessRequest;
import com.atharva.questionsocr.DetectionResult;
import com.google.gson.Gson;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class UploadImageTask extends AsyncTask<UploadImageTask.UploadParams, Void, UploadImageTask.Result> {

    private final ResultListener listener;

    UploadImageTask(ResultListener listener) {
        this.listener = listener;
    }

    @Override
    protected Result doInBackground(UploadParams... arr) {
        UploadParams params = arr[0];
        Result result;
        try {
            OkHttpClient client = new OkHttpClient();
            SingleProcessRequest singleProcessRequest = new SingleProcessRequest(params.image);
            MediaType JSON = MediaType.parse("application/json");
            RequestBody requestBody = RequestBody.create(JSON, new Gson().toJson(singleProcessRequest));

            Request request = new Request.Builder()
                    .url("https://api.mathpix.com/v3/text")
                    .addHeader("content-type", "application/json")
                    .addHeader("app_id", "tagalpallewaratharva_gmail_com")
                    .addHeader("app_key", "a2afbffa96489c23b815")
                    .post(requestBody)
                    .build();
            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            DetectionResult detectionResult = new Gson().fromJson(responseString, DetectionResult.class);
            if (detectionResult != null && detectionResult.text != null) {
                result = new ResultSuccessful(detectionResult.text);
            } else if (detectionResult != null && detectionResult.error != null) {
                result = new ResultFailed(detectionResult.error);
            } else {
                result = new ResultFailed("Math not found");
            }
        } catch (Exception e) {
            result = new ResultFailed("Failed to send to server. Check your connection and try again");
        }
        return result;
    }

    @Override
    protected void onPostExecute(Result result) {
        if (result instanceof ResultSuccessful) {
            ResultSuccessful successful = (ResultSuccessful) result;
            listener.onSuccess(successful.text);
        } else if (result instanceof ResultFailed) {
            ResultFailed failed = (ResultFailed) result;
            listener.onError(failed.message);
        }
    }

    interface ResultListener {
        void onError(String message);

        void onSuccess(String url);
    }

    static class UploadParams {
        private Bitmap image;

        UploadParams(Bitmap image) {
            this.image = image;
        }
    }

    static class Result {
    }

    private static class ResultSuccessful extends Result {
        String text;

        ResultSuccessful(String text) {
            this.text = text;
        }
    }

    private static class ResultFailed extends Result {
        String message;

        ResultFailed(String message) {
            this.message = message;
        }
    }
}

