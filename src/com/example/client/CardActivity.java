package com.example.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

/**
 * Created by kbushko on 1/14/14.
 */
public class CardActivity extends Activity {

    private int MY_SCAN_REQUEST_CODE = 100;

    Button scanButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_io_layout);

        scanButton = (Button)findViewById(R.id.button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScan();
            }
        });
    }

    private void onScan() {
        Intent scanIntent = new Intent(this, CardIOActivity.class);
        scanIntent.putExtra(CardIOActivity.EXTRA_APP_TOKEN,"999551db82b04e36b0664ab8217e0624");

        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY,true);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV,false);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE,false);
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false);

        startActivityForResult(scanIntent,MY_SCAN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_SCAN_REQUEST_CODE) {
            String resultDisplayString;
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                ((EditText)findViewById(R.id.textView)).setText(scanResult.getRedactedCardNumber());
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (CardIOActivity.canReadCardWithCamera(this)) {
            scanButton.setText("Scan a credit card with card.io");
        }
        else {
            scanButton.setText("Enter credit card information");
        }
    }
}
