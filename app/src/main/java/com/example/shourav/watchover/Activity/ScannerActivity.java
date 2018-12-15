package com.example.shourav.watchover.activity;

import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.button.MaterialButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shourav.watchover.R;
import com.google.zxing.client.result.WifiParsedResult;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScannerActivity extends AppCompatActivity {

    @BindView(R.id.scanner_result)
    TextView mScannerResult;
    @BindView(R.id.scanner_button)
    MaterialButton mScanButton;
    @BindView(R.id.scanner_connect_button) MaterialButton mConnectButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        ButterKnife.bind(this);
        getSupportActionBar().setTitle("Receive files");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mScanButton.setOnClickListener((view) -> {
            new IntentIntegrator(this).initiateScan();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                processScanResult(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void processScanResult(String contents) {
        WifiParsedResult parsedResult = parse(contents);
        String address = matchSinglePrefixedField("IP:", contents, ';', false);
        mScannerResult.setText(getString(R.string.scanner_result_text, parsedResult.getSsid(), parsedResult.getPassword(), address));
        if (parsedResult.getSsid() != null) {
            mConnectButton.setVisibility(View.VISIBLE);
            mConnectButton.setOnClickListener(view -> {
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = String.format("\"%s\"", parsedResult.getSsid());
                wifiConfig.preSharedKey = String.format("\"%s\"", parsedResult.getPassword());

                WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
                //remember id
                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();
                if (address != null) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder()
                            .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(this, Uri.parse(address));
                }
            });
        } else {
            mConnectButton.setVisibility(View.INVISIBLE);
        }
    }

    public WifiParsedResult parse(String rawText) {
        if (!rawText.startsWith("WIFI:")) {
            return null;
        }
        rawText = rawText.substring("WIFI:".length());
        String ssid = matchSinglePrefixedField("S:", rawText, ';', false);
        if (ssid == null || ssid.isEmpty()) {
            return null;
        }
        String pass = matchSinglePrefixedField("P:", rawText, ';', false);
        String type = matchSinglePrefixedField("T:", rawText, ';', false);
        if (type == null) {
            type = "nopass";
        }
        boolean hidden = Boolean.parseBoolean(matchSinglePrefixedField("H:", rawText, ';', false));
        String identity = matchSinglePrefixedField("I:", rawText, ';', false);
        String anonymousIdentity = matchSinglePrefixedField("A:", rawText, ';', false);
        String eapMethod = matchSinglePrefixedField("E:", rawText, ';', false);
        String phase2Method = matchSinglePrefixedField("H:", rawText, ';', false);
        return new WifiParsedResult(type, ssid, pass, hidden, identity, anonymousIdentity, eapMethod, phase2Method);
    }

    static String[] matchPrefixedField(String prefix, String rawText, char endChar, boolean trim) {
        List<String> matches = null;
        int i = 0;
        int max = rawText.length();
        while (i < max) {
            i = rawText.indexOf(prefix, i);
            if (i < 0) {
                break;
            }
            i += prefix.length(); // Skip past this prefix we found to start
            int start = i; // Found the start of a match here
            boolean more = true;
            while (more) {
                i = rawText.indexOf(endChar, i);
                if (i < 0) {
                    // No terminating end character? uh, done. Set i such that loop terminates and break
                    i = rawText.length();
                    more = false;
                } else if (countPrecedingBackslashes(rawText, i) % 2 != 0) {
                    // semicolon was escaped (odd count of preceding backslashes) so continue
                    i++;
                } else {
                    // found a match
                    if (matches == null) {
                        matches = new ArrayList<>(3); // lazy init
                    }
                    String element = unescapeBackslash(rawText.substring(start, i));
                    if (trim) {
                        element = element.trim();
                    }
                    if (!element.isEmpty()) {
                        matches.add(element);
                    }
                    i++;
                    more = false;
                }
            }
        }
        if (matches == null || matches.isEmpty()) {
            return null;
        }
        return matches.toArray(new String[matches.size()]);
    }

    static String matchSinglePrefixedField(String prefix, String rawText, char endChar, boolean trim) {
        String[] matches = matchPrefixedField(prefix, rawText, endChar, trim);
        return matches == null ? null : matches[0];
    }

    private static int countPrecedingBackslashes(CharSequence s, int pos) {
        int count = 0;
        for (int i = pos - 1; i >= 0; i--) {
            if (s.charAt(i) == '\\') {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    protected static String unescapeBackslash(String escaped) {
        int backslash = escaped.indexOf('\\');
        if (backslash < 0) {
            return escaped;
        }
        int max = escaped.length();
        StringBuilder unescaped = new StringBuilder(max - 1);
        unescaped.append(escaped.toCharArray(), 0, backslash);
        boolean nextIsEscaped = false;
        for (int i = backslash; i < max; i++) {
            char c = escaped.charAt(i);
            if (nextIsEscaped || c != '\\') {
                unescaped.append(c);
                nextIsEscaped = false;
            } else {
                nextIsEscaped = true;
            }
        }
        return unescaped.toString();
    }
}
