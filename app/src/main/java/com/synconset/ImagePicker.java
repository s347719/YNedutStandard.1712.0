/**
 * An Image Picker Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import android.app.Activity;
import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ImagePicker extends CordovaPlugin {
	public static String TAG = "ImagePicker";
    private static final int REQUEST_CODE = 1;
	 
	private CallbackContext callbackContext;
	private JSONObject params;
	 
	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
		 this.callbackContext = callbackContext;
		 this.params = args.getJSONObject(0);
		if (action.equals("getPictures")) {
			Intent intent = new Intent(cordova.getActivity(), MultiImageChooserActivity.class);
			int max = 20;
			int desiredWidth = 0;
			int desiredHeight = 0;
			int quality = 100;
			if (this.params.has("maximumImagesCount")) {
				max = this.params.getInt("maximumImagesCount");
			}
			if (this.params.has("width")) {
				desiredWidth = this.params.getInt("width");
			}
			if (this.params.has("height")) {
				desiredHeight = this.params.getInt("height");
			}
			if (this.params.has("quality")) {
				quality = this.params.getInt("quality");
			}
			intent.putExtra("MAX_IMAGES", max);
			intent.putExtra("WIDTH", desiredWidth);
			intent.putExtra("HEIGHT", desiredHeight);
			intent.putExtra("QUALITY", quality);
			if (this.cordova != null) {
				this.cordova.startActivityForResult((CordovaPlugin) this, intent, REQUEST_CODE);
			}
		}
		return true;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE)
        {
            if (resultCode == Activity.RESULT_OK && data != null)
            {
                ArrayList<String> fileNames = data.getStringArrayListExtra("MULTIPLEFILENAMES");
                JSONArray res = new JSONArray(fileNames);
                this.callbackContext.success(res);
            } else if (resultCode == Activity.RESULT_CANCELED && data != null)
            {
                String error = data.getStringExtra("ERRORMESSAGE");
                this.callbackContext.error(error);
            } else if (resultCode == Activity.RESULT_CANCELED)
            {
                JSONArray res = new JSONArray();
                this.callbackContext.success(res);
            } else
            {
                this.callbackContext.error("No images selected");
            }
        }
	}
}
