package org.jboss.aerogear.android.unifiedpush;

import android.util.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.jboss.aerogear.android.core.Provider;
import org.jboss.aerogear.android.pipe.http.HttpException;
import org.jboss.aerogear.android.pipe.http.HttpProvider;
import org.jboss.aerogear.android.pipe.http.HttpRestProvider;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by sebastienblanc on 1/27/15.
 */
public abstract class AbstractAeroGearPushRegistrar implements PushRegistrar {

    protected final String secret;
    protected final String variantId;
    protected final String deviceType;
    protected final String alias;
    protected final String operatingSystem;
    protected final String osVersion;
    protected final List<String> categories;
    protected URL deviceRegistryURL;
    protected String deviceToken = "";

    protected final static String BASIC_HEADER = "Authorization";
    protected final static String AUTHORIZATION_METHOD = "Basic";

    protected static final Integer TIMEOUT = 30000;// 30 seconds
    /**
     * Default lifespan (7 days) of a registration until it is considered
     * expired.
     */
    public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;

    protected static final String TAG = AbstractAeroGearPushRegistrar.class.getSimpleName();
    public static final String PROPERTY_REG_ID = "registration_id";
    protected static final String PROPERTY_APP_VERSION = "appVersion";
    protected static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTimeMs";
    protected static final String registryDeviceEndpoint = "/rest/registry/device";



    public AbstractAeroGearPushRegistrar(PushConfiguration config) {
        this.variantId = config.getVariantID();
        this.osVersion = config.getOsVersion();
        this.categories = new ArrayList<String>(config.getCategories());
        this.secret = config.getSecret();
        this.alias = config.getAlias();
        this.operatingSystem = config.getOperatingSystem();
        this.deviceToken = config.getDeviceToken();
        this.deviceType = config.getDeviceType();
    }


    protected Exception upsRegistration(String deviceToken) {
        deviceToken = deviceToken;

        HttpProvider httpProvider = httpProviderProvider.get(deviceRegistryURL, TIMEOUT);
        setPasswordAuthentication(variantId, secret, httpProvider);

        try {
            JsonObject postData = new JsonObject();
            postData.addProperty("deviceType", deviceType);
            postData.addProperty("deviceToken", deviceToken);
            postData.addProperty("alias", alias);
            postData.addProperty("operatingSystem", operatingSystem);
            postData.addProperty("osVersion", osVersion);
            if (categories != null && !categories.isEmpty()) {
                JsonArray jsonCategories = new JsonArray();
                for (String category : categories) {
                    jsonCategories.add(new JsonPrimitive(category));
                }
                postData.add("categories", jsonCategories);
            }

            httpProvider.post(postData.toString());
            return null;
        } catch (HttpException ex) {
            return ex;
        }
    }

    protected Exception upsUnregistration() {
        HttpProvider provider = httpProviderProvider.get(deviceRegistryURL, TIMEOUT);
        setPasswordAuthentication(variantId, secret, provider);

        try {
            provider.delete(deviceToken);
            deviceToken = "";
            return null;
        } catch (HttpException ex) {
            return ex;
        }
    }

    private Provider<HttpProvider> httpProviderProvider = new Provider<HttpProvider>() {

        @Override
        public HttpProvider get(Object... in) {
            return new HttpRestProvider((URL) in[0], (Integer) in[1]);
        }
    };

    public void setPasswordAuthentication(final String username, final String password, final HttpProvider provider) {
        provider.setDefaultHeader(BASIC_HEADER, getHashedAuth(username, password.toCharArray()));
    }

    private String getHashedAuth(String username, char[] password) {
        StringBuilder headerValueBuilder = new StringBuilder(AUTHORIZATION_METHOD).append(" ");
        String unhashedCredentials = new StringBuilder(username).append(":").append(password).toString();
        String hashedCrentials = Base64.encodeToString(unhashedCredentials.getBytes(), Base64.DEFAULT | Base64.NO_WRAP);
        return headerValueBuilder.append(hashedCrentials).toString();
    }

}
