package vn.duonghai.client.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImgbbUploader {

    private static final String API_KEY = "a63d84d131be3fab4525bfe92fbd6c98";
    private static final String URL = "https://api.imgbb.com/1/upload";

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        new Thread(() -> {
            try {
                // Đọc ảnh khi up lên
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                byte[] fileBytes = byteBuffer.toByteArray();
                inputStream.close();

                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("key", API_KEY)
                        .addFormDataPart("image", "product_img.jpg",
                                RequestBody.create(fileBytes, MediaType.parse("image/*")))
                        .build();

                Request request = new Request.Builder()
                        .url(URL)
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, java.io.IOException e) {
                        runOnUiThread(() -> callback.onError("Mạng lỗi: " + e.getMessage()));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws java.io.IOException {
                        if (response.isSuccessful()) {
                            try {
                                String resStr = response.body().string();
                                JSONObject jsonObject = new JSONObject(resStr);
                                String imageUrl = jsonObject.getJSONObject("data").getString("url");
                                runOnUiThread(() -> callback.onSuccess(imageUrl));
                            } catch (Exception e) {
                                runOnUiThread(() -> callback.onError("Lỗi phân tích JSON: " + e.getMessage()));
                            }
                        } else {
                            runOnUiThread(() -> callback.onError("Upload thất bại: " + response.message()));
                        }
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> callback.onError("Lỗi đọc File: " + e.getMessage()));
            }
        }).start();
    }

    private static void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
