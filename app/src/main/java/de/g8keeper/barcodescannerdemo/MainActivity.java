package de.g8keeper.barcodescannerdemo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();


    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void handleClick(View view) {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        String mode;

        switch (view.getId()) {
            case R.id.bt_qr_code:
                mode = "QR_CODE_MODE";
                break;
            case R.id.bt_product:
                mode = "PRODUCT_MODE";
                break;
            case R.id.bt_other:
                mode = "CODE_39,CODE_93,CODE_128,DATA_MATRIX,CODABAR";
                break;
            default:
                return;
        }

        intent.putExtra("SCAN_MODE", mode);
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (RuntimeException e) {
            Toast.makeText(this, "Scanner nicht gefunden", Toast.LENGTH_SHORT).show();
        }


//        IntentIntegrator integrator = new IntentIntegrator(this);
//
//        integrator.initiateScan();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_CODE) {
            TextView tvStatus = findViewById(R.id.tv_status);
            TextView tvResult = findViewById(R.id.tv_result);
            if (resultCode == RESULT_OK) {
                tvStatus.setText(data.getStringExtra("SCAN_RESULT_FORMAT"));
                tvResult.setText(getProductName(data.getStringExtra("SCAN_RESULT")));
            } else if (resultCode == RESULT_CANCELED) {
                tvStatus.setText("Scan wurde abgebrochen");
                tvResult.setText("Nocheinmal versuchen");
            }
        }

    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        TextView tvStatus = findViewById(R.id.tv_status);
//        TextView tvResult = findViewById(R.id.tv_result);
//
//        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
//        if (scanResult != null) {
//
//            // handle scan result
////            tvStatus.setText(data.getStringExtra("SCAN_RESULT_FORMAT"));
////            tvResult.setText(getProductName(data.getStringExtra("SCAN_RESULT")));
//            tvStatus.setText(scanResult.getFormatName());
//            tvResult.setText(scanResult.getContents());
//        }
//        // else continue with any other code you need in the method
//
//    }

    private String getProductName(String scanResult) {
        GetProductDataTask task = new GetProductDataTask();
        String result = null;

        try {
            result = task.execute(scanResult).get();
            JSONObject rootObject = new JSONObject(result);
            Log.d(TAG, "JSON:");
            Log.d(TAG, rootObject.toString(2));
            if(rootObject.has("product")){
                JSONObject productObject = rootObject.getJSONObject("product");
                if (productObject.has("product_name")){
                    return productObject.getString("product_name");
                }
            }

        } catch (ExecutionException e) {
            Log.e(TAG, "ExecutionException", e);
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException", e);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException", e);
        }

        return "Artikel nicht gefunden";

    }


    public class GetProductDataTask extends AsyncTask<String, Void, String> {

//        private final String TAG = GetProductDataTask.class.getSimpleName();


        @Override
        protected String doInBackground(String... strings) {
            final String baseUrl = "https://world.openfoodfacts.org/api/v0/product/"; //[barcode].json"
            final String requestUrl = baseUrl + strings[0] + ".json";

            StringBuilder result = new StringBuilder();
            URL url = null;

            Log.d(TAG, "URL: " + requestUrl);

            try {
                url = new URL(requestUrl);
            } catch (MalformedURLException e) {
                Log.e(TAG, "", e);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (IOException e) {
                Log.e(TAG, "", e);
            }


            return result.toString();
        }
    }
}
