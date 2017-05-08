package com.maks.farmfresh24.ccavenuepayment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.maks.farmfresh24.R;
import com.maks.farmfresh24.ccavenuepayment.utility.AvenuesParams;
import com.maks.farmfresh24.ccavenuepayment.utility.Constants;
import com.maks.farmfresh24.ccavenuepayment.utility.RSAUtility;
import com.maks.farmfresh24.ccavenuepayment.utility.ServiceHandler;
import com.maks.farmfresh24.ccavenuepayment.utility.ServiceUtility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WebViewActivity extends Activity {
	private ProgressDialog dialog;
	Intent mainIntent;
	String html, encVal;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_webview);
		mainIntent = getIntent();
		
		// Calling async task to get display content
		new RenderView().execute();
	}
	
	/**
	 * Async task class to get json by making HTTP call
	 * */
	private class RenderView extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			dialog = new ProgressDialog(WebViewActivity.this);
			dialog.setMessage("Please wait...");
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			// Creating service handler class instance
			ServiceHandler sh = new ServiceHandler();
	
			// Making a request to url and getting response
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(AvenuesParams.ACCESS_CODE, mainIntent.getStringExtra(AvenuesParams.ACCESS_CODE)));
			params.add(new BasicNameValuePair(AvenuesParams.ORDER_ID, mainIntent.getStringExtra(AvenuesParams.ORDER_ID)));
	
			String vResponse = sh.makeServiceCall(mainIntent.getStringExtra(AvenuesParams.RSA_KEY_URL), ServiceHandler.POST, params);
			if(!ServiceUtility.chkNull(vResponse).equals("") 
					&& ServiceUtility.chkNull(vResponse).toString().indexOf("ERROR")==-1){
				StringBuffer vEncVal = new StringBuffer("");
				//vEncVal.append(ServiceUtility.addToPostParams(AvenuesParams.CVV, mainIntent.getStringExtra(AvenuesParams.CVV)));
				vEncVal.append(ServiceUtility.addToPostParams(AvenuesParams.AMOUNT, mainIntent.getStringExtra(AvenuesParams.AMOUNT)));
				vEncVal.append(ServiceUtility.addToPostParams(AvenuesParams.CURRENCY, mainIntent.getStringExtra(AvenuesParams.CURRENCY)));
				//vEncVal.append(ServiceUtility.addToPostParams(AvenuesParams.CARD_NUMBER, mainIntent.getStringExtra(AvenuesParams.CARD_NUMBER)));
				vEncVal.append(ServiceUtility.addToPostParams(AvenuesParams.CUSTOMER_IDENTIFIER, mainIntent.getStringExtra(AvenuesParams.CUSTOMER_IDENTIFIER)));
				//vEncVal.append(ServiceUtility.addToPostParams(AvenuesParams.EXPIRY_YEAR, mainIntent.getStringExtra(AvenuesParams.EXPIRY_YEAR)));
				//vEncVal.append(ServiceUtility.addToPostParams(AvenuesParams.EXPIRY_MONTH, mainIntent.getStringExtra(AvenuesParams.EXPIRY_MONTH)));
				
				encVal = RSAUtility.encrypt(vEncVal.substring(0,vEncVal.length()-1), vResponse);
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// Dismiss the progress dialog
			if (dialog.isShowing())
				dialog.dismiss();
			
			@SuppressWarnings("unused")
			class MyJavaScriptInterface
			{
				@JavascriptInterface
			    public void processHTML(String html)
			    {
			        // process the html as needed by the app
			    	String status = null;
			    	if(html.indexOf("Failure")!=-1){
			    		status = "Transaction Declined!";
			    	}else if(html.indexOf("Success")!=-1){
			    		status = "Transaction Successful!";
			    	}else if(html.indexOf("Aborted")!=-1){
			    		status = "Transaction Cancelled!";
			    	}else{
			    		status = "Status Not Known!";
			    	}
			    	//Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
			    	Intent intent = new Intent(getApplicationContext(),StatusActivity.class);
					intent.putExtra("transStatus", status);
					startActivity(intent);
			    }
			}
			
			final WebView webview = (WebView) findViewById(R.id.webview);
			webview.getSettings().setJavaScriptEnabled(true);
			webview.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
			webview.setWebViewClient(new WebViewClient(){
				@Override
	    	    public void onPageFinished(WebView view, String url) {
	    	        super.onPageFinished(webview, url);
	    	        if(url.indexOf("/ccavResponseHandler.jsp")!=-1){
	    	        	webview.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
	    	        }
	    	    }  

	    	    @Override
	    	    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
	    	        Toast.makeText(getApplicationContext(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
	    	    }
			});
			
			/* An instance of this class will be registered as a JavaScript interface */
			StringBuffer params = new StringBuffer();
			params.append(ServiceUtility.addToPostParams(AvenuesParams.ACCESS_CODE,mainIntent.getStringExtra(AvenuesParams.ACCESS_CODE)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.MERCHANT_ID,mainIntent.getStringExtra(AvenuesParams.MERCHANT_ID)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.ORDER_ID,mainIntent.getStringExtra(AvenuesParams.ORDER_ID)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.REDIRECT_URL,mainIntent.getStringExtra(AvenuesParams.REDIRECT_URL)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.CANCEL_URL,mainIntent.getStringExtra(AvenuesParams.CANCEL_URL)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.LANGUAGE,"EN"));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.BILLING_NAME,mainIntent.getStringExtra(AvenuesParams.BILLING_NAME)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.BILLING_ADDRESS,mainIntent.getStringExtra(AvenuesParams.BILLING_ADDRESS)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.BILLING_CITY,mainIntent.getStringExtra(AvenuesParams.BILLING_CITY)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.BILLING_STATE,mainIntent.getStringExtra(AvenuesParams.BILLING_STATE)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.BILLING_ZIP,mainIntent.getStringExtra(AvenuesParams.BILLING_ZIP)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.BILLING_COUNTRY,mainIntent.getStringExtra(AvenuesParams.BILLING_COUNTRY)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.BILLING_TEL,mainIntent.getStringExtra(AvenuesParams.BILLING_TEL)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.BILLING_EMAIL,mainIntent.getStringExtra(AvenuesParams.BILLING_EMAIL)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.DELIVERY_NAME,mainIntent.getStringExtra(AvenuesParams.DELIVERY_NAME)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.DELIVERY_ADDRESS,mainIntent.getStringExtra(AvenuesParams.DELIVERY_ADDRESS)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.DELIVERY_CITY,mainIntent.getStringExtra(AvenuesParams.DELIVERY_CITY)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.DELIVERY_STATE,mainIntent.getStringExtra(AvenuesParams.DELIVERY_STATE)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.DELIVERY_ZIP,mainIntent.getStringExtra(AvenuesParams.DELIVERY_ZIP)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.DELIVERY_COUNTRY,mainIntent.getStringExtra(AvenuesParams.DELIVERY_COUNTRY)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.DELIVERY_TEL,mainIntent.getStringExtra(AvenuesParams.DELIVERY_TEL)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.MERCHANT_PARAM1,"additional Info."));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.MERCHANT_PARAM2,"additional Info."));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.MERCHANT_PARAM3,"additional Info."));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.MERCHANT_PARAM4,"additional Info."));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.PAYMENT_OPTION,mainIntent.getStringExtra(AvenuesParams.PAYMENT_OPTION)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.CARD_TYPE,mainIntent.getStringExtra(AvenuesParams.CARD_TYPE)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.CARD_NAME,mainIntent.getStringExtra(AvenuesParams.CARD_NAME)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.DATA_ACCEPTED_AT,mainIntent.getStringExtra(AvenuesParams.DATA_ACCEPTED_AT)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.ISSUING_BANK,mainIntent.getStringExtra(AvenuesParams.ISSUING_BANK)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.ENC_VAL, URLEncoder.encode(encVal)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.EMI_PLAN_ID,mainIntent.getStringExtra(AvenuesParams.EMI_PLAN_ID)));
			params.append(ServiceUtility.addToPostParams(AvenuesParams.EMI_TENURE_ID,mainIntent.getStringExtra(AvenuesParams.EMI_TENURE_ID)));
			if(mainIntent.getStringExtra(AvenuesParams.SAVE_CARD)!=null)
				params.append(ServiceUtility.addToPostParams(AvenuesParams.SAVE_CARD,mainIntent.getStringExtra(AvenuesParams.SAVE_CARD)));
			
			String vPostParams = params.substring(0,params.length()-1);
			try {
				webview.postUrl(Constants.TRANS_URL, EncodingUtils.getBytes(vPostParams, "UTF-8"));
			} catch (Exception e) {
				showToast("Exception occured while opening webview.");
			}
		}
	}
	
	public void showToast(String msg) {
		Toast.makeText(this, "Toast: " + msg, Toast.LENGTH_LONG).show();
	}
} 