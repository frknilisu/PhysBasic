package com.frkn.physbasic.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.frkn.physbasic.R;
import com.frkn.physbasic.util.IabHelper;
import com.frkn.physbasic.util.IabResult;
import com.frkn.physbasic.util.Inventory;
import com.frkn.physbasic.util.Purchase;

/**
 * Created by frkn on 12.01.2017.
 */

public class BuyPremiumDialog extends DialogFragment implements View.OnClickListener {

    private static final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApKM1MWMy/g4oAAV6WZ+ZIei+TNZSHKBgm9IxquuhTgI5LD8ZYtMFpJJvLn1j8ulO9eRbOIAZoKx/Q21mrclRsBvmwBjpphlP6FxJaN2rOvUMbhWKoak535ehaq7ISmlmzZUT0u1WMM/dETmhVMLwZ43DxcZsnlP/ERsiCOahFUOXYACLdaOtrgvlKlngpNihu4ig2rnPolXIFAuh8DrGKLBSUStpfbq0VndLC2mGe83VKRNxH4rn6eU6u8iW2slIZWtDA0Rn780uSR0Lk1zuut+qgemHwZXZCtJBZKh43bW/xG1n3W5BcNIkaDvOSr+DYOblsn4aV/ppbF3Kwpq/cQIDAQAB";
    private static final String TAG = "BuyPremium";

    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    private String SKU_ITEM = "";
    private static final String SKU_STANDART = "standart";
    private static final String SKU_PREMIUM = "premium";
    private static final String SKU_VIP = "vip";
    private static final String SKU_TEST = "android.test.purchased";

    // (arbitrary) request code for the purchase flow
    private static final int RC_REQUEST = 10001;

    // The helper object
    IabHelper mHelper;

    BuyPremiumListener listener;
    MainActivity mainActivity;

    int accountType = 0, buyingThingType = -1, id = -1;
    String title, price;

    @Override
    public void onAttach(Activity activity) {
        listener = (BuyPremiumListener) activity;
        mainActivity = (MainActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountType = getArguments().getInt("accountType");
        buyingThingType = getArguments().getInt("buyingThingType");
        id = getArguments().getInt("id");
        title = getArguments().getString("title");
        price = getArguments().getString("price");

        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(getContext(), base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_buy_upgrade, container, false);

        TextView txt = (TextView) rootView.findViewById(R.id.this_item_nameText);
        txt.setText("Buy " + title);

        Button btn1 = (Button) rootView.findViewById(R.id.buy_this_item_button);
        btn1.setText(price);
        Button btn2 = (Button) rootView.findViewById(R.id.buy_standart_button);
        Button btn3 = (Button) rootView.findViewById(R.id.buy_premium_button);
        Button btn4 = (Button) rootView.findViewById(R.id.buy_vip_button);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);

        if (accountType == 3) {
            btn1.setEnabled(false);
            btn2.setEnabled(false);
            btn3.setEnabled(false);
            btn4.setEnabled(false);
        } else if (accountType == 2) {
            btn1.setEnabled(true);
            btn2.setEnabled(false);
            btn3.setEnabled(false);
            btn4.setEnabled(true);
        } else if (accountType == 1) {
            btn1.setEnabled(true);
            btn2.setEnabled(false);
            btn3.setEnabled(true);
            btn4.setEnabled(true);
        } else if (accountType == 0) {
            btn1.setEnabled(true);
            btn2.setEnabled(true);
            btn3.setEnabled(true);
            btn4.setEnabled(true);
        }

        getDialog().setTitle("Upgrade Account");
        return rootView;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buy_this_item_button:
                onBuyItemClick();
                break;
            case R.id.buy_standart_button:
                onBuyStandartClick();
                break;
            case R.id.buy_premium_button:
                onBuyPremiumClick();
                break;
            case R.id.buy_vip_button:
                onBuyVipClick();
                break;
            default:
                break;
        }
    }

    public void onBuyItemClick() {
        Log.d(TAG, "onBuyItemClicked: launching purchase flow for upgrade => " + buyingThingType + " - " + id);
        SKU_ITEM = buyingThingType + ".item." + id;
        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        mHelper.launchPurchaseFlow(mainActivity, SKU_ITEM, RC_REQUEST,
                mPurchaseFinishedListener, payload);
    }

    public void onBuyStandartClick() {
        Log.d(TAG, "onBuyBronzeClicked: launching purchase flow for upgrade");
        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        mHelper.launchPurchaseFlow(mainActivity, SKU_STANDART, RC_REQUEST,
                mPurchaseFinishedListener, payload);
    }

    public void onBuyPremiumClick() {
        Log.d(TAG, "onBuySilverClicked: launching purchase flow for upgrade");
        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        mHelper.launchPurchaseFlow(mainActivity, SKU_TEST, RC_REQUEST,
                mPurchaseFinishedListener, payload);
    }

    public void onBuyVipClick() {
        Log.d(TAG, "onBuyGoldClicked: launching purchase flow for upgrade");
        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        mHelper.launchPurchaseFlow(mainActivity, SKU_VIP, RC_REQUEST,
                mPurchaseFinishedListener, payload);
    }

    /***********************************************************************************
     * IabHelper Listeners
     **********************************************************************************/

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            Purchase testItem = inventory.getPurchase(SKU_TEST);
            //Purchase gold = inventory.getPurchase(SKU_GOLD);
            if (testItem != null) {
                Toast.makeText(getContext(), "You have TEST ITEM", Toast.LENGTH_SHORT).show();
                mHelper.consumeAsync(inventory.getPurchase(SKU_TEST), mConsumeFinishedListener);
            }
            //mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            //Toast.makeText(getContext(), "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"), Toast.LENGTH_LONG).show();
            //Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

            //updateUi();
            //setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_TEST)) {
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                Toast.makeText(getContext(), "Purchase is premium upgrade. Congratulating user.", Toast.LENGTH_SHORT).show();
                //mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                accountType = 2;
                return;
            }
        }
    };


    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");
            } else {
                complain("Error while consuming: " + result);
            }
            Log.d(TAG, "End consumption flow.");
        }
    };


    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    private void complain(String message) {
        Log.e(TAG, "**** Buy Premium Error: " + message);
        alert("Error: " + message);
    }

    private void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(getContext());
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }

    @Override
    public void onDetach() {
        Log.d("onDetach", listener.getClass().getName().toString());
        listener = null;
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
            Log.d(TAG, "Destroying helper.");
            if (mHelper != null) {
                try {
                    mHelper.dispose();
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                }
            }
            mHelper = null;
            Log.d("onDismiss", "accountType: " + accountType);
            listener.onFinishBuying(accountType, buyingThingType, id);
            return;
        }
    }

    public interface BuyPremiumListener {
        void onFinishBuying(int _accountType, int _type, int _id);
    }

}
