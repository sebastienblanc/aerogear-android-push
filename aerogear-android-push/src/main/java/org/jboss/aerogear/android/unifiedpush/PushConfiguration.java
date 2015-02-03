/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.unifiedpush;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.jboss.aerogear.android.core.Config;

/**
 * The configuration builder for push registrars.
 * 
 * @param <CONFIGURATION> The concrete implementation of the PushConfiguration
 */
public abstract class PushConfiguration<CONFIGURATION extends PushConfiguration> implements Config<CONFIGURATION> {

    protected String deviceToken = "";
    protected String variantID;
    protected String secret;
    protected String deviceType = "ANDROID";
    protected String operatingSystem = "android";
    protected String osVersion = android.os.Build.VERSION.RELEASE;
    protected String alias;
    protected List<String> categories = new ArrayList<String>();
    protected URI pushServerURI;

    private String name;

    private Collection<OnPushRegistrarCreatedListener> listeners = new HashSet<OnPushRegistrarCreatedListener>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CONFIGURATION setName(String name) {
        this.name = name;
        return (CONFIGURATION) this;
    }

    /**
     * OnAuthenticationCreatedListeners are a collection of classes to be
     * notified when the configuration of the Pipe is complete.
     * 
     * @return the current collection.
     */
    public Collection<OnPushRegistrarCreatedListener> getOnAuthenticationCreatedListeners() {
        return listeners;
    }

    /**
     * OnAuthenticationCreatedListeners are a collection of classes to be
     * notified when the configuration of the Pipe is complete.
     * 
     * @param listener new listener to add to the collection
     * @return this configuration
     */
    public CONFIGURATION addOnPushRegistrarCreatedListener(OnPushRegistrarCreatedListener listener) {
        this.listeners.add(listener);
        return (CONFIGURATION) this;
    }

    /**
     * OnAuthenticationCreatedListeners are a collection of classes to be
     * notified when the configuration of the Pipe is complete.
     * 
     * @param listeners new collection to replace the current one
     * @return this configuration
     */
    public CONFIGURATION setOnPushRegistrarCreatedListeners(Collection<OnPushRegistrarCreatedListener> listeners) {
        listeners.addAll(listeners);
        return (CONFIGURATION) this;
    }

    /**
     * 
     * Creates a {@link PushRegistrar} based on the current configuration and
     * notifies all listeners
     * 
     * @return An {@link PushRegistrar} based on this configuration
     * 
     * @throws IllegalStateException if the {@link PushRegistrar} can not be
     *             constructed.
     * 
     */
    public final PushRegistrar asRegistrar() {

        PushRegistrar registrar = buildRegistrar();
        for (OnPushRegistrarCreatedListener listener : getOnAuthenticationCreatedListeners()) {
            listener.onPushRegistrarCreated(this, registrar);
        }
        return registrar;
    }

    /**
     * The device token Identifies the device within its Push Network. It is the
     * value = GoogleCloudMessaging.getInstance(context).register(SENDER_ID);
     *
     * @return the current device token
     *
     */
    public String getDeviceToken() {
        return deviceToken;
    }

    /**
     * The device token Identifies the device within its Push Network. It is the
     * value = GoogleCloudMessaging.getInstance(context).register(SENDER_ID);
     *
     * @param deviceToken the new device token
     * @return the current configuration
     *
     */
    public PushConfiguration setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
        return this;
    }

    /**
     * ID of the Variant from the AeroGear UnifiedPush Server.
     *
     * @return the current variant id
     */
    public String getVariantID() {
        return variantID;
    }

    /**
     * ID of the Variant from the AeroGear UnifiedPush Server.
     *
     * @param variantID the new variantID
     * @return the current configuration
     */
    public PushConfiguration setVariantID(String variantID) {
        this.variantID = variantID;
        return this;
    }

    /**
     * Secret of the Variant from the AeroGear UnifiedPush Server.
     *
     * @return the current Secret
     *
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Secret of the Variant from the AeroGear UnifiedPush Server.
     *
     * @param secret the new secret
     * @return the current configuration
     */
    public PushConfiguration setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    /**
     * Device type determines which cloud messaging system will be used by the
     * AeroGear Unified Push Server
     *
     * Defaults to ANDROID
     *
     * @return the device type
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * Device type determines which cloud messaging system will be used by the
     * AeroGear Unified Push Server.
     *
     * Defaults to ANDROID
     *
     * @param deviceType a new device type
     * @return the current configuration
     *
     */
    public PushConfiguration setDeviceType(String deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    /**
     * The name of the operating system. Defaults to Android
     *
     * @return the operating system
     */
    public String getOperatingSystem() {
        return operatingSystem;
    }

    /**
     * The name of the operating system. Defaults to Android
     *
     * @param operatingSystem the new operating system
     * @return the current configuration
     */
    public PushConfiguration setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
        return this;
    }

    /**
     * The version of the operating system running.
     *
     * Defaults to the value provided by android.os.Build.VERSION.RELEASE
     *
     * @return the current OSversion
     *
     */
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * The version of the operating system running.
     *
     * Defaults to the value provided by android.os.Build.VERSION.RELEASE
     *
     * @param osVersion the new osVersion
     * @return the current configuration
     *
     */
    public PushConfiguration setOsVersion(String osVersion) {
        this.osVersion = osVersion;
        return this;
    }

    /**
     * The Alias is an identifier of the user of the system.
     *
     * Examples are an email address or a username
     *
     * @return alias
     *
     */
    public String getAlias() {
        return alias;
    }

    /**
     * The Alias is an identifier of the user of the system.
     *
     * Examples are an email address or a username
     *
     * @param alias the new alias
     * @return the current configuration
     *
     */
    public PushConfiguration setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    /**
     * The categories specifies a channel which may be used to send messages
     *
     * @return the current categories
     *
     */
    public List<String> getCategories() {
        return categories;
    }

    /**
     * The categories specifies a channel which may be used to send messages
     *
     * @param categories the new categories
     * @return the current configuration
     *
     */
    public PushConfiguration setCategories(List<String> categories) {
        this.categories = new ArrayList<String>(categories);
        return this;
    }

    /**
     * The categories specifies a channel which may be used to send messages
     *
     * @param categories the new categories
     * @return the current configuration
     *
     */
    public PushConfiguration setCategories(String... categories) {
        this.categories = Arrays.asList(categories);
        return this;
    }

    /**
     * The categories specifies a channel which may be used to send messages
     *
     * @param category a new category to be added to the current list.
     * @return the current configuration
     *
     */
    public PushConfiguration addCategory(String category) {
        categories.add(category);
        return this;
    }

    /**
     * RegistryURL is the URL of the 3rd party application server
     *
     * @return the current pushServerURI
     */
    public URI getPushServerURI() {
        return pushServerURI;
    }

    /**
     * RegistryURL is the URL of the 3rd party application server
     *
     * @param pushServerURI a new URI
     * @return the current configuration
     *
     */
    public PushConfiguration setPushServerURI(URI pushServerURI) {
        this.pushServerURI = pushServerURI;
        return this;
    }

    /**
     * 
     * Validates configuration parameters and returns a PushRegistrar
     * instance.
     * 
     * @return An {@link PushRegistrar} based on this configuration
     * 
     */
    protected abstract PushRegistrar buildRegistrar();

}
