package com.trobot.gkashdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.trobot.gkashdemo.model.PaymentMethod;
import com.trobot.gkashdemo.model.Terminal;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VoidTransactionActivity extends AppCompatActivity {

    private Button voidButton;
    private TextView posId;
    private TextView displayText;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    private String auth = "";
    private String companyName = "";
    private String userID = "";
    private String tokenForPayment = "";
    private ArrayList<PaymentMethod> paymentMethods = new ArrayList<>();
    private ArrayList<Terminal> terminals = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_void_transaction);

        voidButton = findViewById(R.id.voidButton);
        posId = findViewById(R.id.posId);
        displayText = findViewById(R.id.displayText);

        Intent intent = getIntent();

        posId.setText(intent.getStringExtra("posId"));

        displayText.append(intent.getStringExtra("Status") + '\n');
        displayText.append(intent.getStringExtra("TransferAmount") + '\n');
        displayText.append(intent.getStringExtra("PORemID") + '\n');
        displayText.append(intent.getStringExtra("TransferCurrency") + '\n');
        displayText.append(intent.getStringExtra("CartID") + '\n');
        displayText.append(intent.getStringExtra("mode") + '\n');

        voidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://api.gkash.my/apim/auth/thirdPartyLogin";
                JSONObject object = new JSONObject();
                try {
                    object.put("UserName", "nuvendingmalaysia@gmail.com");
                    object.put("Password", "Nuvending123$");
                    object.put("CompanyRemID", "ANDROID");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String json = object.toString();

                post(url, json, posId.getText().toString());
            }
        });


    }

    private void post(String url, String json, final String poremId) {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder().url(url).post(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();

                    try {
                        JSONObject obj = new JSONObject(myResponse);
                        auth = obj.getString("Auth");
                        companyName = obj.getString("CompanyName");
                        userID = obj.getString("UserID");
                        JSONArray methods = obj.getJSONArray("Methods");
                        for (int i = 0; i < methods.length(); i++) {
                            String cid = methods.getJSONObject(i).getString("Cid");
                            String method = methods.getJSONObject(i).getString("Method");
                            String remarks = methods.getJSONObject(i).getString("Remarks");
                            String token = methods.getJSONObject(i).getString("Token");
                            paymentMethods.add(new PaymentMethod(cid, method, remarks, token));
                            if (method.equals("Visa/MasterCard")) {
                                tokenForPayment = token;
                            }
                        }
                        JSONArray terminalList = obj.getJSONArray("Terminals");
                        for (int i = 0; i < terminalList.length(); i++) {
                            String terminalId = terminalList.getJSONObject(i).getString("TerminalID");
                            String branchName = terminalList.getJSONObject(i).getString("BranchName");
                            terminals.add(new Terminal(terminalId, branchName));
                        }

                        goToGkashVoidTransaction(poremId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private void goToGkashVoidTransaction(String poremId) {

        String packageName = "com.gkash.business.gkashunifiedterminal.prod";
        String className = "gkash.com.gkashmpos.TransactionActivity";

        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);

        intent.setAction(Intent.ACTION_SEND);
        intent.setClassName(packageName, className);
        intent.setType("text/plain");

        intent.putExtra("token", tokenForPayment);
        intent.putExtra("mainToken", auth);
        intent.putExtra("otherApp", true);
        intent.putExtra("ClassName", "com.trobot.gkashdemo.VoidTransactionActivity");
        intent.putExtra("PackageName", "com.trobot.gkashdemo");
        intent.putExtra("TerminalID", terminals.get(0).getTerminalID());
        intent.putExtra("PORemID", poremId);
        intent.putExtra("mode", "VISA/MASTER");

        startActivity(intent);
    }
}
