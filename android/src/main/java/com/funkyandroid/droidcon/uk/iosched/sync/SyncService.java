/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.funkyandroid.droidcon.uk.iosched.sync;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import com.funkyandroid.droidcon.uk.iosched.Config;

import java.io.IOException;

import static com.funkyandroid.droidcon.uk.iosched.util.LogUtils.LOGE;
import static com.funkyandroid.droidcon.uk.iosched.util.LogUtils.LOGI;

/**
 * Service that handles sync. We're not requiring users to have any kind of account, so this needs to perform
 * the sync rather than just be a front end to a SyncAdapter.
 */
public class SyncService extends Service {

    private SyncHelper mSyncHelper;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        final boolean uploadOnly = extras.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, false);
        final boolean manualSync = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
        final boolean initialize = extras.getBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, false);

        if (uploadOnly) {
            return START_NOT_STICKY;
        }

        LOGI(Config.LOG_TAG, "Beginning sync ," +
                " uploadOnly=" + uploadOnly +
                " manualSync=" + manualSync +
                " initialize=" + initialize);

        /*
            TODO: Look at non-G+ dependant way of doing this
        String chosenAccountName = AccountUtils.getChosenAccountName(mContext);
        boolean isAccountSet = !TextUtils.isEmpty(chosenAccountName);
        boolean isChosenAccount = isAccountSet && chosenAccountName.equals(account.name);
        if (isAccountSet) {
            ContentResolver.setIsSyncable(account, authority, isChosenAccount ? 1 : 0);
        }
        if (!isChosenAccount) {
            LOGW(TAG, "Tried to sync account " + logSanitizedAccountName + " but the chosen " +
                    "account is actually " + chosenAccountName);
            ++syncResult.stats.numAuthExceptions;
            return;
        }
        */

        // Perform a sync using SyncHelper
        if (mSyncHelper == null) {
            mSyncHelper = new SyncHelper(this);
        }

        // Dummy SyncResult object, we can use this for detecting issues
        // at some point.
        SyncResult syncResult = new SyncResult();
        try {
            mSyncHelper.performSync(syncResult,
                    SyncHelper.FLAG_SYNC_LOCAL | SyncHelper.FLAG_SYNC_REMOTE);

        } catch (IOException e) {
            ++syncResult.stats.numIoExceptions;
            LOGE(Config.LOG_TAG, "Error syncing data for I/O 2013.", e);
        }

        return START_NOT_STICKY;
    }

    /**
     * The service is only invoked locally so we don't need to return a Binder.
     *
     * @param intent The bind intent.
     * @return null. This service should not be used via IPC.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}