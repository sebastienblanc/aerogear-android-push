/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.aerogear.android.unifiedpush.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.http.HttpStatus;
import org.jboss.aerogear.android.pipe.util.UrlUtils;
import org.jboss.aerogear.android.unifiedpush.AbstractAeroGearPushRegistrar;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.core.Provider;
import org.jboss.aerogear.android.pipe.http.HttpException;
import org.jboss.aerogear.android.pipe.http.HttpProvider;
import org.jboss.aerogear.android.pipe.http.HttpRestProvider;

public class AeroGearGCMPushRegistrar extends AbstractAeroGearPushRegistrar implements PushRegistrar {

    private final Set<String> senderIds;

    private GoogleCloudMessaging gcm;



    private Provider<GoogleCloudMessaging> gcmProvider = new Provider<GoogleCloudMessaging>() {

        @Override
        public GoogleCloudMessaging get(Object... context) {
            return GoogleCloudMessaging.getInstance((Context) context[0]);
        }
    };

    public AeroGearGCMPushRegistrar(AeroGearGCMPushConfiguration config) {
        super(config);
        this.senderIds = config.getSenderIds();
        try {
            this.deviceRegistryURL = UrlUtils.appendToBaseURL(config.getPushServerURI().toURL(), registryDeviceEndpoint);
        } catch (MalformedURLException ex) {
            Log.e(TAG, ex.getMessage());
            throw new IllegalStateException("pushserverUrl was not a valid URL");
        }
    }

    @Override
    public void register(final Context context, final Callback<Void> callback) {
        new AsyncTask<Void, Void, Exception>() {

            @Override
            protected Exception doInBackground(Void... params) {

                try {

                    if (gcm == null) {
                        gcm = gcmProvider.get(context);
                    }
                    String regid = getRegistrationId(context);

                    if (regid.length() == 0) {
                        regid = gcm.register(senderIds
                                .toArray(new String[] {}));
                        AeroGearGCMPushRegistrar.this.setRegistrationId(context, regid);
                    }

                    return upsRegistration(regid);

                } catch (Exception ex) {
                    return ex;
                }

            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onPostExecute(Exception result) {
                if (result == null) {
                    callback.onSuccess(null);
                } else {
                    if (result instanceof HttpException) {
                        HttpException httpException = (HttpException) result;
                        switch (httpException.getStatusCode()) {
                        case HttpStatus.SC_MOVED_PERMANENTLY:
                        case HttpStatus.SC_MOVED_TEMPORARILY:
                        case HttpStatus.SC_TEMPORARY_REDIRECT:
                            Log.w(TAG, httpException.getMessage());
                            try {
                                URL redirectURL = new URL(httpException.getHeaders().get("Location"));
                                AeroGearGCMPushRegistrar.this.deviceRegistryURL = redirectURL;
                                register(context, callback);
                            } catch (MalformedURLException e) {
                                callback.onFailure(e);
                            }
                            break;
                        default:
                            callback.onFailure(result);
                        }
                    } else {
                        callback.onFailure(result);
                    }

                }
            }

        }.execute((Void) null);

    }

    @Override
    public void unregister(final Context context, final Callback<Void> callback) {
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {

                try {

                    if (gcm == null) {
                        gcm = gcmProvider.get(context);
                    }

                    gcm.unregister();
                    return upsUnregistration();


                } catch (Exception ex) {
                    return ex;
                }

            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onPostExecute(Exception result) {
                if (result == null) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure(result);
                }
            }

        }.execute((Void) null);
    }

    /**
     * Gets the current registration id for application on GCM service.
     * <p>
     * If result is empty, the registration has failed.
     * 
     * @param context the application context
     * 
     * @return registration id, or empty string if the registration is not
     *         complete.
     */
    public String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.length() == 0) {
            Log.v(TAG, "Registration not found.");
            return "";
        }
        // check if app was updated; if so, it must clear registration id to
        // avoid a race condition if GCM sends a message
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion
                || isRegistrationExpired(context)) {
            Log.v(TAG, "App version changed or registration expired.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(AeroGearGCMPushRegistrar.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Checks if the registration has expired.
     * 
     * To avoid the scenario where the device sends the registration to the
     * server but the server loses it, the app developer may choose to
     * re-register after REGISTRATION_EXPIRY_TIME_MS.
     * 
     * @return true if the registration has expired.
     */
    private boolean isRegistrationExpired(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        // checks if the information is not stale
        long expirationTime = prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
        return System.currentTimeMillis() > expirationTime;
    }

    /**
     * Stores the registration id, app versionCode, and expiration time in the
     * application's {@code SharedPreferences}.
     * 
     * @param context application's context.
     * @param regId registration id
     */
    private void setRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.v(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        long expirationTime = System.currentTimeMillis()
                + REGISTRATION_EXPIRY_TIME_MS;

        Log.v(TAG, "Setting registration expiry time to "
                + new Timestamp(expirationTime));
        editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
        editor.commit();
    }


}
