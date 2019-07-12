package com.back4app.androidstripepaymentgateway;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final String PUBLISHABLE_KEY = "pk_test_4vNOmTdMglgn7DZlY2dA4MEZ";
    private Card card;
    private Button purchase;
    private Stripe stripe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String number = String.valueOf("4242424242424242");

        card = Card.create(number, 12, 2030, "123");
        purchase = (Button) findViewById(R.id.purchase);
        purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buy();
            }
        });
    }

    private void buy() {
        boolean validation = card.validateCard();
        if(validation) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle("Validating Credit Card");
            alertDialogBuilder.setMessage("Please Wait").setCancelable(false);
            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            new Stripe(this).createToken(
                    card,
                    PUBLISHABLE_KEY,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            Log.d("DEBUG",token.toString());
                            Toast.makeText(getApplicationContext(), "Token created: " + token.getId(), Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                            charge(token);
                        }

                        public void onError(Exception error) {
                            Toast.makeText(MainActivity.this,
                                    "Stripe -" + error.toString(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } else if (!card.validateNumber()) {
            Toast.makeText(MainActivity.this,
                    "Stripe - The card number that you entered is invalid",
                    Toast.LENGTH_LONG).show();
        } else if (!card.validateExpiryDate()) {
            Toast.makeText(MainActivity.this,
                    "Stripe - The expiration date that you entered is invalid",
                    Toast.LENGTH_LONG).show();
        } else if (!card.validateCVC()) {
            Toast.makeText(MainActivity.this,
                    "Stripe - The CVC code that you entered is invalid",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this,
                    "Stripe - The card details that you entered are invalid",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void charge(Token cardToken){
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("ItemName", "test");
        params.put("cardToken", cardToken.getId());
        params.put("name","Dominic Wong");
        params.put("email","dominwong4@gmail.com");
        params.put("address","HIHI");
        params.put("zip","99999");
        params.put("city_state","CA");
        ParseCloud.callFunctionInBackground("purchaseItem", params, new FunctionCallback<Object>() {
            public void done(Object response, ParseException
                    e) {
                if (e == null) {
                    Log.d("Cloud Response", "There were no exceptions! " + response.toString());
                    Toast.makeText(getApplicationContext(),
                            "Item Purchased Successfully ",
                            Toast.LENGTH_LONG).show();
                } else {
                    Log.d("Cloud Response", "Exception: " + e);
                    Toast.makeText(getApplicationContext(),
                            e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
