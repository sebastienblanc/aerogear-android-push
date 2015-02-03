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
package org.jboss.aerogear.android.unifiedpush.gcm;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.jboss.aerogear.android.unifiedpush.PushConfiguration;

/**
 * A Push Configuration which builds {@link AeroGearGCMPushRegistrar} instances.
 */
public class AeroGearGCMPushConfiguration extends PushConfiguration<AeroGearGCMPushConfiguration> {

    private static final long serialVersionUID = 1L;
    private Set<String> senderIds = new HashSet<String>();

    /**
     * SenderIds is a collection of all GCM sender Id elements registered for
     * this application.
     * 
     * @return a copy of the current set of senderIds.
     * 
     */
    public Set<String> getSenderIds() {
        return new HashSet<String>(senderIds);
    }

    /**
     * SenderIds is a collection of all GCM sender Id elements registered for
     * this application.
     * 
     * @param senderIds the new sender Ids to set.
     * @return the current configuration.
     */
    public AeroGearGCMPushConfiguration setSenderIds(String... senderIds) {
        Set<String> newSenderIds = new HashSet<String>(senderIds.length);
        Collections.addAll(newSenderIds, senderIds);
        this.senderIds = newSenderIds;
        return this;
    }

    /**
     * SenderIds is a collection of all GCM sender Id elements registered for
     * this application.
     * 
     * @param senderId a new sender Id to add to the current set of senderIds.
     * @return the current configuration.
     */
    public AeroGearGCMPushConfiguration addSenderId(String senderId) {
        this.senderIds.add(senderId);
        return this;
    }

    /**
     * 
     * Protected builder method.
     * 
     * @return A configured AeroGearGCMPushRegistrar
     * 
     * @throws IllegalStateException if pushServerURI is null or if senderIds is null or empty.
     */
    @Override
    protected final AeroGearGCMPushRegistrar buildRegistrar() {

        if (senderIds == null || senderIds.isEmpty()) {
            throw new IllegalStateException("SenderIds can't be null or empty");
        }

        if (pushServerURI == null) {
            throw new IllegalStateException("PushServerURI can't be null");
        }

        return new AeroGearGCMPushRegistrar(this);
    }

}
