package com.example.paymentgatewayandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    PaymentSheet paymentSheet;
    String paymentIntentClientSecret;
    PaymentSheet.CustomerConfiguration configuration;

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetchApi();
        Button button = findViewById(R.id.pay_now);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("paymentIntentClientSecret", paymentIntentClientSecret.toString());
                if (paymentIntentClientSecret != null) {
                    Log.d("if paymentIntentClientSecret", paymentIntentClientSecret.toString());

                    paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret,
                        new PaymentSheet.Configuration("Payment Gateway", configuration));
            }else

            {
                Toast.makeText(getApplicationContext(), "API Loading...", Toast.LENGTH_SHORT).show();
            }
        }
    });
    paymentSheet =new

    PaymentSheet(this,this::onPaymentSheetResult);

}

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            fetchApi();
            Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
            String errorMessage = ((PaymentSheetResult.Canceled) paymentSheetResult).toString();
            Log.e("Payment Canceled", errorMessage);
        }

        if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            fetchApi();
            Toast.makeText(this, ((PaymentSheetResult.Failed) paymentSheetResult).getError().getMessage(), Toast.LENGTH_SHORT).show();
            String errorMessage = ((PaymentSheetResult.Failed) paymentSheetResult).getError().getMessage();
            Log.e("Payment Failed", errorMessage);
        }

        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Log.e("Payment Completed", "");
            fetchApi();
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
        }
    }

    public void fetchApi() {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.0.104/stripe-android-api/";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            configuration = new PaymentSheet.CustomerConfiguration(
                                    jsonObject.getString("customer"),
                                    jsonObject.getString("ephemeralKey")
                            );
                            paymentIntentClientSecret = jsonObject.getString("paymentIntent");
                            PaymentConfiguration.init(getApplicationContext(), jsonObject.getString("publishableKey"));
                        } catch (JSONException e) {
                            Log.e("catch", e.toString());
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onErrorResponse", error.toString());
                error.printStackTrace();

            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> paramV = new HashMap<>();
                paramV.put("authKey", "abc");
                return paramV;
            }
        };
        queue.add(stringRequest);

    }
}