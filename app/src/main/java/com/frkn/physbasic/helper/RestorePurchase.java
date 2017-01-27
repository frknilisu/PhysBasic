package com.frkn.physbasic.helper;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.frkn.physbasic.util.IabHelper;
import com.frkn.physbasic.util.IabResult;
import com.frkn.physbasic.util.Inventory;
import com.frkn.physbasic.util.Purchase;

/**
 * Created by frkn on 26.01.2017.
 */

public class RestorePurchase {

    private static final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApKM1MWMy/g4oAAV6WZ+ZIei+TNZSHKBgm9IxquuhTgI5LD8ZYtMFpJJvLn1j8ulO9eRbOIAZoKx/Q21mrclRsBvmwBjpphlP6FxJaN2rOvUMbhWKoak535ehaq7ISmlmzZUT0u1WMM/dETmhVMLwZ43DxcZsnlP/ERsiCOahFUOXYACLdaOtrgvlKlngpNihu4ig2rnPolXIFAuh8DrGKLBSUStpfbq0VndLC2mGe83VKRNxH4rn6eU6u8iW2slIZWtDA0Rn780uSR0Lk1zuut+qgemHwZXZCtJBZKh43bW/xG1n3W5BcNIkaDvOSr+DYOblsn4aV/ppbF3Kwpq/cQIDAQAB";
    private static final String TAG = "RestorePurchase";

    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    private String SKU_ITEM = "";
    private static final String SKU_STANDART = "standart";
    private static final String SKU_PREMIUM = "premium";
    private static final String SKU_VIP = "vip";
    private static final String SKU_TEST = "android.test.purchased";

    // (arbitrary) request code for the purchase flow
    private static final int RC_REQUEST = 10002;

    // The helper object
    IabHelper mHelper;

    RestorePurchase.RestorePurchaseListener restorePurchaseListener;
    Activity activity;

    int accountType = 0, type = -1, id = -1;
    String title, price;

    public RestorePurchase(Activity _activity, RestorePurchase.RestorePurchaseListener _listener) {
        this.activity = _activity;
        this.restorePurchaseListener = _listener;
    }

    public void restore() {
        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(activity.getBaseContext(), base64EncodedPublicKey);

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

            // Do we have any purchase?
            Purchase testItem = inventory.getPurchase(SKU_TEST);
            Purchase standart = inventory.getPurchase(SKU_STANDART);
            Purchase premium = inventory.getPurchase(SKU_PREMIUM);
            Purchase vip = inventory.getPurchase(SKU_VIP);
            if (standart != null) {
                Toast.makeText(activity.getBaseContext(), "You have Standart account already", Toast.LENGTH_SHORT).show();
                accountType = 1;
                restorePurchaseListener.onRestoreCompleted(accountType, type, id);
            }

            if (premium != null) {
                Toast.makeText(activity.getBaseContext(), "You have Premium account already", Toast.LENGTH_SHORT).show();
                accountType = 2;
                restorePurchaseListener.onRestoreCompleted(accountType, type, id);
            }

            if (vip != null) {
                Toast.makeText(activity.getBaseContext(), "You have Vip account already", Toast.LENGTH_SHORT).show();
                accountType = 3;
                restorePurchaseListener.onRestoreCompleted(accountType, type, id);
            }

            if (testItem != null) {
                Toast.makeText(activity.getBaseContext(), "You have TEST ITEM", Toast.LENGTH_SHORT).show();
                mHelper.consumeAsync(inventory.getPurchase(SKU_TEST), mConsumeFinishedListener);
            } else {
                Toast.makeText(activity.getBaseContext(), "You have not TEST ITEM", Toast.LENGTH_SHORT).show();
            }
            //mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            //Toast.makeText(getContext(), "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"), Toast.LENGTH_LONG).show();
            //Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

            //updateUi();
            //setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
            restorePurchaseListener.onRestoreCompleted(accountType, type, id);
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
        AlertDialog.Builder bld = new AlertDialog.Builder(activity.getBaseContext());
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }

    public interface RestorePurchaseListener {
        void onRestoreCompleted(int _accountType, int _type, int _id);
    }
}
