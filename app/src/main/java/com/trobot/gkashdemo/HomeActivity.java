package com.trobot.gkashdemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.trobot.gkashdemo.model.PaymentMethod;
import com.trobot.gkashdemo.model.Terminal;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {

    private Button postButton, voidButton, testPurchaseButton, testPurchaseButton1, testPurchaseButton2;
    private TextView displayText, paymentStatusText;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private String auth = "";
    private String companyName = "";
    private String userID = "";
    private String tokenForPayment = "";
    private String posId = "";
    private ArrayList<PaymentMethod> paymentMethods = new ArrayList<>();
    private ArrayList<Terminal> terminals = new ArrayList<>();

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        postButton = findViewById(R.id.postButton);
        voidButton = findViewById(R.id.voidButton);
        testPurchaseButton = findViewById(R.id.testPurchaseButton);
        testPurchaseButton1= findViewById(R.id.testPurchaseButton1);
        testPurchaseButton2 = findViewById(R.id.testPurchaseButton2);
        displayText = findViewById(R.id.displayText);
        paymentStatusText = findViewById(R.id.paymentStatusText);

        voidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, VoidTransactionActivity.class);
                intent.putExtra("posId", posId);
                startActivity(intent);
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestForGkashPayment("1.00");
            }

        });

        Toast.makeText(this, "Created", Toast.LENGTH_SHORT).show();

        testPurchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(HomeActivity.this)
                        .setMessage("Select Payment Method")
                        .setCancelable(true)
                        .setPositiveButton("Credit / Debit Card", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(HomeActivity.this, "Card", Toast.LENGTH_SHORT).show();
                                requestForGkashPayment("1.00");
                            }
                        })
                        .setNegativeButton("Cash / Coin", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(HomeActivity.this, "Cash / Coin", Toast.LENGTH_SHORT).show();
                                new SocketSendMessage().execute("server cash with 1.00");
                            }
                        })
                        .show();
            }
        });

        testPurchaseButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(HomeActivity.this)
                        .setMessage("Select Payment Method")
                        .setCancelable(true)
                        .setPositiveButton("Credit / Debit Card", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(HomeActivity.this, "Card", Toast.LENGTH_SHORT).show();
                                requestForGkashPayment("1.50");
                            }
                        })
                        .setNegativeButton("Cash / Coin", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(HomeActivity.this, "Cash / Coin", Toast.LENGTH_SHORT).show();
                                new SocketSendMessage().execute("server cash with 1.50");
                            }
                        })
                        .show();
            }
        });

        testPurchaseButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(HomeActivity.this)
                        .setMessage("Select Payment Method")
                        .setCancelable(true)
                        .setPositiveButton("Credit / Debit Card", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(HomeActivity.this, "Card", Toast.LENGTH_SHORT).show();
                                requestForGkashPayment("2.50");
                            }
                        })
                        .setNegativeButton("Cash / Coin", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(HomeActivity.this, "Cash / Coin", Toast.LENGTH_SHORT).show();
                                new SocketSendMessage().execute("server cash with 2.50");
                            }
                        })
                        .show();
            }
        });

        // Create intent to wait for the Gkash feedback
        Intent intent = getIntent();

        displayText.append("status: " + intent.getStringExtra("Status") + '\n');
        displayText.append("TransferAmount: " + intent.getStringExtra("TransferAmount") + '\n');
        displayText.append("TransferCurrency: " + intent.getStringExtra("TransferCurrency") + '\n');
        displayText.append("TransferDate: " + intent.getStringExtra("TransferDate") + '\n');
        displayText.append("PORemID: " + intent.getStringExtra("PORemID") + '\n');
        displayText.append("CartID: " + intent.getStringExtra("CartID") + '\n');
        displayText.append("paymentType: " + intent.getStringExtra("paymentType") + '\n');
//        displayText.append("MID: " + intent.getStringExtra("MID") + '\n');
//        displayText.append("TID: " + intent.getStringExtra("TID") + '\n');
//        displayText.append("AuthIDResponse: " + intent.getStringExtra("AuthIDResponse") + '\n');
//        displayText.append("TraceNo: " + intent.getStringExtra("TraceNo") + '\n');
//        displayText.append("ResponseOrderNumber: " + intent.getStringExtra("ResponseOrderNumber") + '\n');
//        displayText.append("ApplicationId: " + intent.getStringExtra("ApplicationId") + '\n');
//        displayText.append("Message: " + intent.getStringExtra("Message") + '\n');

        posId = intent.getStringExtra("PORemID");

        if (intent.getStringExtra("Status") != null) {
            if (intent.getStringExtra("Status").equals("88 - Transferred")) {
                new SocketSendMessage().execute("server card");
            }
        }
    }

    private void requestForGkashPayment(String amount) {
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

        post(url, json, amount);
    }

    private void post(String url, String json, final String amount) {
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

                        HomeActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displayText.setText(auth);
                            }
                        });

                        goToGkashPayment(amount);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private void goToGkashPayment(String amount) {
        String packageName = "com.gkash.business.gkashunifiedterminal.prod";
        String className = "gkash.com.gkashmpos.SplashActivity";

        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);

        intent.setAction(Intent.ACTION_SEND);
        intent.setClassName(packageName, className);
        intent.setType("text/plain");

        intent.putExtra("amount", amount);
        intent.putExtra("DeviceName", "NuBox");
        intent.putExtra("token", tokenForPayment);
        intent.putExtra("otherApp", true);
        intent.putExtra("TerminalID", terminals.get(0).getTerminalID());
        intent.putExtra("paymentType", "Visa/MasterCard");
        intent.putExtra("ClassName", "com.trobot.gkashdemo.HomeActivity");
        intent.putExtra("PackageName", "com.trobot.gkashdemo");
        startActivity(intent);
    }

    class SocketSendMessage extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                try {
                    Socket socket = new Socket("172.20.10.14", 8888);
                    PrintWriter outToServer = new PrintWriter(new OutputStreamWriter(
                            socket.getOutputStream()
                    ));
                    outToServer.print(params[0]);
                    outToServer.flush();
                    Log.d("RESP", params[0]);
                    if (params[0].split(" ")[0].equals("server")) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));

                        int str;
                        StringBuffer buffer = new StringBuffer();;

                        while ((str = in.read()) != '\n') {
                            buffer.append((char)str);
                        }
                        Log.d("resp", buffer.toString());

                        if (buffer.toString().equals("DONE")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    paymentStatusText.setText("Payment Done! Item Dispensed!");
                                }
                            });
                        }
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
