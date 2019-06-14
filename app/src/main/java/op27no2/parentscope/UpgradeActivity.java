package op27no2.parentscope;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.bumptech.glide.util.Util;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.PurchaseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by CristMac on 11/4/17.
 */

public class UpgradeActivity extends android.support.v4.app.Fragment implements PurchasesUpdatedListener {

    private Util mUtil;
    private ArrayList<String> mTitles = new ArrayList<String>();
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;

    private TextView mTextTitle1;
    private TextView mTextTitle2;
    private Button purchaseButton;

    private ArrayList<ArrayList<String>> mGenericData = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> mFitnessData = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> mMusicData = new ArrayList<ArrayList<String>>();

    private BillingClient mBillingClient;
    private List<String> skuList = new ArrayList<>();
    private List<String> priceList = new ArrayList<>();
    private Activity mActivity;

    private RecyclerView mPurchaseRecyclerView;
    private LinearLayoutManager mPurchaseLayoutManager;
    private MyPurchaseAdapter mPurchaseAdapter;
    private ArrayList<String> mData = new ArrayList<String>();
    private ArrayList<String> mData2 = new ArrayList<String>();
    private ArrayList<String> mDataSku = new ArrayList<String>();
    private HashMap<String, String> mPriceMap = new HashMap<String, String>();

    private Boolean billingReady = false;

    //TODO NEED TO CHECK SUBSCRIPTINO STATUS TO CANCEL FEATURES IF NOT SUBBED. EVENTUALLY MIGRATE BACK TO BILLING 2.0 and confirm purchasesss.

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.upgrade_activity, container, false);

    //    ImageView mExcel = getActivity().findViewById(R.id.excel_button);
    //    mExcel.setVisibility(View.GONE);

        prefs = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();

        //premium_generic(1-4)
        //fitness_followback_(1-4)
        //music_(1-7)
        mDataSku.add("standard1");
        mDataSku.add("yearsub1");


        mData.add("Standard");
        mData.add("Year Sub ");

        mData2.add("Standard Subscription, $4.99 per month");
        mData2.add("Year Sub - discounted for a year subscription");

    //    mUtil = new Util(getActivity());
        //TODO DELETE THIS
        TextView mtitle = view.findViewById(R.id.text1);
        mtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("clicK");
                edt.putString("type","");
                edt.commit();
            }
        });



        mPurchaseRecyclerView =  view.findViewById(R.id.purchase_recycler);
        mPurchaseRecyclerView.setHasFixedSize(true);
        mPurchaseLayoutManager = new LinearLayoutManager(getActivity());
        mPurchaseLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mPurchaseRecyclerView.setLayoutManager(mPurchaseLayoutManager);

        //ADD DATA
        mPurchaseAdapter = new MyPurchaseAdapter(mData, mData2);

        mPurchaseRecyclerView.setAdapter(mPurchaseAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mPurchaseRecyclerView.getContext(),
                mPurchaseLayoutManager.getOrientation());
        mPurchaseRecyclerView.addItemDecoration(dividerItemDecoration);
        mPurchaseRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPurchaseRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mPurchaseRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        System.out.println("list item clicked: "+position);

                        if(billingReady) {
                            if(position == 0){
                                purchaseDialog(position);
                            }
                            else {
                                purchaseDialog(position);
                            }
                        }
                        else{
                            Toast.makeText(getActivity(), "Billing Connection Still Loading or Not Able to Connect", Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        System.out.println("list item long clicked: "+position);

                    }
                })
        );

        //sub names from google console
        skuList.add("standard1");
        skuList.add("yearsub1");


        mBillingClient = BillingClient.newBuilder(getActivity()).setListener(this).build();
        //billing 2.0
        //mBillingClient = BillingClient.newBuilder(getActivity()).setListener(this).enablePendingPurchases().build();

        mBillingClient.startConnection(new BillingClientStateListener() {

            //billing 2.0
          /*  @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    System.out.println("billing client ready");
                    billingReady = true;
                     if(mBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).getResponseCode()== BillingClient.BillingResponseCode.OK){
                         System.out.println("subs supported");
                     }else{
                         System.out.println("subs not supported? ");
                     }
                    priceList = getPrices(skuList);

                }
            }*/

            @Override
            public void onBillingSetupFinished(int responseCode) {
                if (responseCode == BillingClient.BillingResponse.OK) {
                    System.out.println("billing client ready");
                    billingReady = true;

                    priceList = getPrices(skuList);


                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                System.out.println("billing services disconnected ");
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Fragment Premium Viewed")
                .putContentType("Premium Views")
                .putContentId("premium"));




        return view;
    }



    private ArrayList<String> getPrices(List<String> productList) {
        final ArrayList<String> prices = new ArrayList<String>();

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        mBillingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                        if (responseCode == BillingClient.BillingResponse.OK
                                && skuDetailsList != null) {

                            System.out.println("Price results: " + skuDetailsList);
                            for (SkuDetails skuDetails : skuDetailsList) {
                                String price = skuDetails.getPrice();
                                String name = skuDetails.getSku();
                                mPriceMap.put(name,price);
                            }
                            System.out.println("pricemap: "+mPriceMap);



                        }else{
                            System.out.println("billing response code not ok: "+responseCode);
                            Toast.makeText(getActivity(), "Billing unavaialble, please check your internet connection", Toast.LENGTH_LONG).show();
                        }
                    }

                    //Billing 2.0
                   /* @Override
                    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                                && skuDetailsList != null) {

                            System.out.println("Price results: " + skuDetailsList);
                            for (SkuDetails skuDetails : skuDetailsList) {
                                String price = skuDetails.getPrice();
                                String name = skuDetails.getSku();
                                String duration = skuDetails.getSubscriptionPeriod();
                                System.out.println("duration: "+duration);

                                mPriceMap.put(name,price);
                            }
                            System.out.println("pricemap: "+mPriceMap);



                        }else{
                            System.out.println("billing response code not ok: "+billingResult.getResponseCode());
                            Toast.makeText(getActivity(), "Billing unavaialble, please check your internet connection", Toast.LENGTH_LONG).show();
                        }
                    }*/


                });

        return prices;
    }

//TODO acknowledge purchase within 3 days
    private void queryForProducts(final String productSku){
        System.out.println("query product method called");

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        mBillingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                        if (responseCode == BillingClient.BillingResponse.OK
                                && skuDetailsList != null) {

                            System.out.println("IAP results: " + skuDetailsList);
                            for (SkuDetails skuDetails : skuDetailsList) {
                                String sku = skuDetails.getSku();
                                String name = skuDetails.getTitle();
                                String price = skuDetails.getPrice();
                                if (productSku.equals(sku)) {
                                    String mPremiumUpgradePrice = price;
                                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                            .setSku(sku)
                                            .setType(BillingClient.SkuType.INAPP)
                                            .build();
                                    int mresponseCode = mBillingClient.launchBillingFlow(mActivity, flowParams);
                                    System.out.println("billing response code: " + mresponseCode);

                                }
                            }
                        }
                    }

                    //billing 2.0
          /*          @Override
                    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                                && skuDetailsList != null) {

                            System.out.println("IAP results: " + skuDetailsList);
                            for (SkuDetails skuDetails : skuDetailsList) {
                                String sku = skuDetails.getSku();
                                String name = skuDetails.getTitle();
                                String price = skuDetails.getPrice();
                                if (productSku.equals(sku)) {
                                    String mPremiumUpgradePrice = price;
                                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                            .setSkuDetails(skuDetails)
                                            .build();
                                    BillingResult result = mBillingClient.launchBillingFlow(getActivity(), flowParams);

                                  //  int mresponseCode = mBillingClient.launchBillingFlow(mActivity, flowParams);
                                    System.out.println("billing response code: " + result.getResponseCode());

                                }
                            }
                        }
                        else{
                            System.out.println("no IAP returned: "+ billingResult);
                        }
                    }
*/

                });



    }

    //2.0 billing
/*    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        System.out.println("purchase response: "+billingResult.getResponseCode());

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            System.out.println("purchase OK");
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            System.out.println("purchase CANCELED");

        }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ERROR) {
            System.out.println("purchase ERROR "+billingResult.getResponseCode());
        }
    }*/



    private void handlePurchase(Purchase mPurchase){
        if(mPurchase.getSku().equals("yearsub1")){
            edt.putBoolean("subscribed",true);
            edt.commit();
        }
        if(mPurchase.getSku().equals("standard1")){
            edt.putBoolean("subscribed",true);
            edt.commit();
        }

        Answers.getInstance().logPurchase(new PurchaseEvent()
                .putItemName("Dialog purchase "+mPurchase.getSku())
                .putItemType("Apparel")
                .putSuccess(true));

    }

    private void purchaseDialog(final int position){
        final Dialog dialog = new Dialog(getActivity());


        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(mDataSku.get(position))
                .putContentType("Purchase Views")
                .putContentId(mDataSku.get(position)));


        System.out.println("price map: "+mPriceMap);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_purchase);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;

        dialog.getWindow().setLayout((8 * width) / 9, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView mText = dialog.findViewById(R.id.title_text);
        TextView mText2 = dialog.findViewById(R.id.detail_text);
        switch(position){
            case 0:
            mText.setText(mData.get(position)+" - "+mPriceMap.get(mDataSku.get(position)));
            mText2.setText("Standard Subscription - provides full options for high quality recording to be used with unlimited monitored devices");

                break;

            case 1:
            mText.setText(mData.get(position)+" - "+mPriceMap.get(mDataSku.get(position)));
            mText2.setText("Yearly Discount Subscription - provides full options for high quality recording to be used with unlimited monitored devices at a discounted rate for a yearly subscription");

                break;
        }


        Button myButton1 = dialog.findViewById(R.id.cancel);
        myButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button myButton = dialog.findViewById(R.id.confirm);

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryForProducts(mDataSku.get(position));
                dialog.dismiss();
            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            this.mActivity = (Activity) context;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mActivity = null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mActivity = null;
    }


    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        System.out.println("purchase response: "+responseCode);
        System.out.println("purchase updated");
        if (responseCode == BillingClient.BillingResponse.OK
                && purchases != null) {
            System.out.println("purchase OK");
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            System.out.println("purchase CANCELED");
            // Handle an error caused by a user cancelling the purchase flow.
        } else if (responseCode == 0) {
            System.out.println("purchase OK");
            edt.putBoolean("subscribed",true);
            edt.commit();
            Answers.getInstance().logPurchase(new PurchaseEvent()
                .putItemName("Dialog purchase ")
                .putItemType("Apparel")
                .putSuccess(true));
        }
        else {
            // Handle any other error codes.
            System.out.println("purchase ERROR "+responseCode);
        }
    }

}