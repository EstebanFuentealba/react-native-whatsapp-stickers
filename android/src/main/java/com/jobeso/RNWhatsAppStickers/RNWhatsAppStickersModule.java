
package com.jobeso.RNWhatsAppStickers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import java.util.ArrayList;
import java.util.List;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.google.gson.Gson;


import org.json.JSONException;

public class RNWhatsAppStickersModule extends ReactContextBaseJavaModule {
  public static final String EXTRA_STICKER_PACK_ID = "sticker_pack_id";
  public static final String EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority";
  public static final String EXTRA_STICKER_PACK_NAME = "sticker_pack_name";

  public static final int ADD_PACK = 200;
  public static final String ERROR_ADDING_STICKER_PACK = "Could not add this sticker pack. Please install the latest version of WhatsApp before adding sticker pack";

  private final ReactApplicationContext reactContext;

  public RNWhatsAppStickersModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNWhatsAppStickers";
  }

  @ReactMethod
  public void test(Promise promise){
    promise.resolve("");
  }

  public static String getContentProviderAuthority(Context context){
    return context.getPackageName() + ".stickercontentprovider";
  }


  @ReactMethod
  public void createStickerPack(ReadableMap options, Promise promise) throws JSONException {

      String identifier = options.getString("identifier");
      String name = options.getString("name");
      String publisher = options.getString("publisher");
      String trayImageFile = options.getString("tray_image_file");
      String publisherEmail = options.getString("publisher_email");
      String publisherWebsite = options.getString("publisher_website");
      String privacyPolicyWebsite = options.getString("privacy_policy_website");
      String licenseAgreementWebsite = options.getString("license_agreement_website");
      ReadableArray readableStickers = options.getArray("stickers");


      StickerPack stickerPack = new StickerPack(identifier, name, publisher, trayImageFile, publisherEmail, publisherWebsite, privacyPolicyWebsite, licenseAgreementWebsite);
      List<Sticker> stickers = new ArrayList<Sticker>();
      for (int i = 0; i < readableStickers.size(); i++) {
          ReadableMap stickerMap = readableStickers.getMap(i);
          List<String> emojis = new ArrayList<>();
          ReadableArray readableArray = stickerMap.getArray("emojis");
          for(int j = 0; j < readableArray.size(); j++) {
              emojis.add(readableArray.getString(j));
          }
          stickers.add(new Sticker(stickerMap.getString("image_file"), emojis));
      }
      stickerPack.setStickers(stickers);
      List<StickerPack> stickerPacks = new ArrayList<StickerPack>();
      stickerPacks.add(stickerPack);
      Gson gson = new Gson();
      String JSONOptions = gson.toJson(stickerPacks);

      SharedPreferences sharedPreferences = this.reactContext.getSharedPreferences("WHATSAPP_STICKERS", Context.MODE_PRIVATE);
      SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
      prefsEditor.putString(StickerContentProvider.STICKERS, JSONOptions.toString() );
      prefsEditor.commit();
      promise.resolve(JSONOptions.toString() );
  }
  @ReactMethod
  public void send(String identifier, String stickerPackName, Promise promise) {
    Intent intent = new Intent();
    intent.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
    intent.putExtra(EXTRA_STICKER_PACK_ID, identifier);
    intent.putExtra(EXTRA_STICKER_PACK_AUTHORITY, getContentProviderAuthority(reactContext));
    intent.putExtra(EXTRA_STICKER_PACK_NAME, stickerPackName);

    try {
      Activity activity = getCurrentActivity();
      ResolveInfo should = activity.getPackageManager().resolveActivity(intent, 0);
      if (should != null) {
        activity.startActivityForResult(intent, ADD_PACK);
        promise.resolve("OK");
      } else {
        promise.resolve("OK, but not opened");
      }
    } catch (ActivityNotFoundException e) {
      promise.reject(ERROR_ADDING_STICKER_PACK, e);
    } catch  (Exception e){
      promise.reject(ERROR_ADDING_STICKER_PACK, e);
    }
  }

}
