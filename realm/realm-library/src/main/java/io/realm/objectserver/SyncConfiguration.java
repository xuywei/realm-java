/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.objectserver;

import android.content.Context;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.objectserver.session.Session;
import io.realm.objectserver.syncpolicy.AutomaticSyncPolicy;
import io.realm.objectserver.syncpolicy.SyncPolicy;
import io.realm.rx.RxObservableFactory;

/**
 * An {@link SyncConfiguration} is used to setup a replicated .
 * <p>
 * Instances of a RealmConfiguration can only created by using the {@link io.realm.RealmConfiguration.Builder} and calling
 * its {@link io.realm.RealmConfiguration.Builder#build()} method.
 * <p>
 * A commonly used RealmConfiguration can easily be accessed by first saving it as
 * {@link Realm#setDefaultConfiguration(RealmConfiguration)} and then using {@link io.realm.Realm#getDefaultInstance()}.
 * <p>
 * A minimal configuration can be created using:
 * <p>
 * {@code RealmConfiguration config = new RealmConfiguration.Builder(getContext()).build())}
 * <p>
 * This will create a RealmConfiguration with the following properties.
 * <ul>
 * <li>Realm file is called "default.realm"</li>
 * <li>It is saved in Context.getFilesDir()</li>
 * <li>It has its schema version set to 0.</li>
 * </ul>
 */
public final class SyncConfiguration extends RealmConfiguration {

    private final File realmFolder;
    private final String realmFileName;
    private final String canonicalPath;
    private final URI serverUrl;
    private final User user;
    private final boolean autoConnect;
    private final SyncPolicy syncPolicy;
    private final Session.ErrorHandler errorHandler;

    private SyncConfiguration(Builder builder) {
        super(builder);
        this.user = builder.user;
        try {
            this.serverUrl = new URI(builder.serverUrl.toString().replace("/~/", "/" + user.getIdentifier() + "/"));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not replace '/~/' with a valid user ID.", e);
        }
        this.autoConnect = builder.autoConnect;
        this.syncPolicy = builder.syncPolicy;
        this.errorHandler = builder.errorHandler;

        // Determine location on disk
        // Use the serverUrl + user to create a unique filepath unless it has been explicitly overridden.
        this.realmFolder = builder.defaultFolder; // TODO Add support for overriding default folder
        this.realmFileName = builder.overrideDefaultLocalFileName ? getRealmFileName() : builder.defaultLocalFileName;
        this.canonicalPath = getCanonicalPath(new File(realmFolder, realmFileName));

        // Create the folder on disk (if needed)
        realmFolder.mkdirs();

    }

    @Override
    public boolean equals(Object obj) {
        return true; // TODO;
    }

    @Override
    public int hashCode() {
        return 31; // TODO
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        // TODO
        return stringBuilder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getRealmFolder() {
        return this.realmFolder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRealmFileName() {
        return this.realmFileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return this.canonicalPath;
    }

    public SyncPolicy getSyncPolicy() {
        return syncPolicy;
    }

    public User getUser() {
        return user;
    }

    public boolean isAutoConnectEnabled() {
        return autoConnect;
    }

    public URI getServerUrl() {
        return serverUrl;
    }

    public Session.ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * ReplicationConfiguration.Builder used to construct instances of a ReplicationConfiguration in a fluent manner.
     */
    public static final class Builder extends RealmConfiguration.Builder {

        private Context context;
        private URI serverUrl;
        private User user = null;
        private boolean autoConnect = true;
        private SyncPolicy syncPolicy = new AutomaticSyncPolicy();
        private Session.ErrorHandler errorHandler = SyncManager.defaultSessionErrorHandler;
        private boolean overrideDefaultFolder = false;
        private boolean overrideDefaultLocalFileName = false;
        private File defaultFolder;
        private String defaultLocalFileName;

        /**
         * {@inheritDoc}
         */
        public Builder(Context context) {
            super(context);
            this.context = context;
        }

        /**
         * {@inheritDoc}
         */
        public Builder(Context context, File folder) {
            super(context, folder);
        }

        /**
         * Sets the local filename for the Realm.
         * This will override the default name defined by the {@link #serverUrl(String)}
         *
         * @param name name of the local file on disk.
         */
        @Override
        public Builder name(String name) {
            super.name(name);
            this.overrideDefaultLocalFileName = true;
            return this;
        }

        /**
         * Sets the local directory where the Realm file can be saved.
         * This will override the default location defined by the {@link #serverUrl(String)}
         * <p>
         * <b>WARNING:</b> Overriding the default location should be done with extreme care. If two users write
         * to the same locale Realm, it can no longer be synchronized with the remote Realm.
         *
         * @param dir directory on disk where the Realm file can be saved.
         * @throws IllegalArgumentException if the directory is not valid.
         */
        public Builder directory(File dir) {
            // TODO
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder encryptionKey(byte[] key) {
            super.encryptionKey(key);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder schemaVersion(long schemaVersion) {
            super.schemaVersion(schemaVersion);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder deleteRealmIfMigrationNeeded() {
            super.deleteRealmIfMigrationNeeded();
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SyncConfiguration.Builder inMemory() {
            super.inMemory();
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder modules(Object baseModule, Object... additionalModules) {
            super.modules(baseModule, additionalModules);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SyncConfiguration.Builder rxFactory(RxObservableFactory factory) {
            super.rxFactory(factory);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder initialData(Realm.Transaction transaction) {
            super.initialData(transaction);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder assetFile(Context context, String assetFile) {
            super.assetFile(context, assetFile);
            return this;
        }

        /**
         * Manual migrations are not supported (yet) for Realms that can be synced using the Realm Object Server
         * Only additive changes are allowed, and these will be detected and applied automatically.
         *
         * @throws IllegalArgumentException always.
         */
        @Override
        public Builder migration(RealmMigration migration) {
            throw new IllegalArgumentException("Migrations are not supported for Realms that can be synchronized using the Realm Mobile Platform");
        }

        /**
         * Enable server side synchronization for this Realm. The name should be a unique URL that identifies the Realm.
         * {@code /~/} can be used as a placeholder for a user ID in case the Realm should only be available to one
         * user, e.g. {@code "realm://objectserver.realm.io/~/default"}
         *
         * The `/~/` will automatically be replaced with the user ID when creating the {@link SyncConfiguration}.
         *
         * The URL also defines the local location on the device. The default location of a synchronized Realm file is
         * {@code /data/data/<packageName>/files/realm-object-server/<user-id>/<last-path-segment>}.
         *
         * This behaviour can be overwritten using {@link #name(String)} and {@link #directory(File}.
         *
         * @param url URL identifying the Realm.
         * @throws IllegalArgumentException if the URL is not valid.
         */
        public Builder serverUrl(String url) {
            try {
                this.serverUrl = new URI(url);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid url: " + url, e);
            }

            // Detect last path segment as it is the default file name
            String path = serverUrl.getPath();
            if (path == null) {
                throw new IllegalArgumentException("Invalid url: " + url);
            }

            String[] pathSegments = path.split("/");
            this.defaultLocalFileName = pathSegments[pathSegments.length - 1];

            // Validate filename
            if (defaultLocalFileName.endsWith(".realm")) {
                throw new IllegalArgumentException("The URL must not end with '.realm': " + url);
            }

            return this;
        }

        /**
         * Set the user for this Realm. An authenticated {@link User} is required to open any Realm managed by a
         * Realm Object Server.
         *
         * @param user {@link User} who wants to access this Realm.
         */
        public Builder user(User user) {
            if (user == null) {
                throw new IllegalArgumentException("Non-null `user` required.");
            }
            if (!user.isAuthenticated()) {
                throw new IllegalArgumentException("User not authenticated or authentication expired. User ID: " + user.getIdentifier());
            }

            String userId = user.getIdentifier();
            this.defaultFolder = new File(context.getFilesDir(), "realm-object-server/" + userId);
            this.user = user;
            return this;
        }

        /**
         * If this is set. Realm will automatically handle connections to the Realm Object Server as part of the normal
         * Realm lifecycle.
         *
         * Specifically this means that the connection will be established when the first local Realm is opened and
         * closed again after it has been closed and all changes locally have been sent to the server.
         *
         * If this is set to {@code false}, the connection must manually be established using
         * {@link SyncManager#connect(SyncConfiguration)}.
         *
         * The default value is {@code true}.
         */
        public Builder autoConnect(boolean autoConnect) {
            this.autoConnect = autoConnect;
            return this;
        }

        /**
         * Sets the sync policy used to control when changes should be synchronized with the remote Realm.
         * The {@link SyncPolicy} only takes effect after a Realm have been <i>bound</i> to the remote Realm.
         *
         * The default value policy is a {@link AutomaticSyncPolicy}.
         *
         * TODO Think about how a sync policy could also control if _any_ connection is made, not just sync changes.
         * TODO Does the core SyncClient support starting/stopping sync yet?
         *
         * @param syncPolicy
         * @return
         *
         * @see SyncConfiguration.Builder#autoConnect(boolean)
         * @see SyncManager#connect(SyncConfiguration)
         */
        public Builder syncPolicy(SyncPolicy syncPolicy) {
            this.syncPolicy = syncPolicy;
            return this;
        }

        /**
         * Sets the error handler used by this configuration.
         * This will override any handler set by calling {@link SyncManager#setDefaultSessionErrorHandler(Session.ErrorHandler)}.
         *
         * @param errorHandler error handler used to report back errors when communicating with the Realm Object Server.
         */
        public Builder errorHandler(Session.ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        /**
         * Creates the RealmConfiguration based on the builder parameters.
         *
         * @return the created {@link SyncConfiguration}.
         */
        public SyncConfiguration build() {
            return new SyncConfiguration(this);
        }
    }
}