/*
 * Copyright 2018 Filippo Beraldo. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.beraldo.playerlib

import android.app.IntentService
import android.app.Notification
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.beraldo.playerlib.player.PlayerHolder
import com.beraldo.playerlib.player.PlayerModule
import com.google.android.exoplayer2.ui.PlayerNotificationManager

/**
 * Created by Filippo Beraldo on 15/11/2018.
 * http://github.com/beraldofilippo
 */
class PlayerService : IntentService("playerlib"), PlayerNotificationManager.NotificationListener {

    companion object {
        const val NOTIFICATION_ID = 100
        const val NOTIFICATION_CHANNEL = "playerlib_channel"
        const val STREAM_URL = "stream_url"
    }

    private lateinit var playerHolder: PlayerHolder

    private lateinit var playerNotificationManager: PlayerNotificationManager

    override fun onBind(intent: Intent?): IBinder {
        intent?.getStringExtra(STREAM_URL)?.let {
            initialize(it)
            playerHolder.start()
        }

        return PlayerServiceBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerNotificationManager.setPlayer(null)
        playerHolder.release()
    }

    private fun initialize(streamUrl: String) {
        // Build a player holder
        playerHolder = PlayerModule.getPlayerHolder(this, streamUrl)

        /** Build a notification manager for our player, set a notification listener to this,
         * and assign the player just created.
         *
         * It is very important to note we need to get a [PlayerNotificationManager] instance
         * via the [PlayerNotificationManager.createWithNotificationChannel] because targeting Android O+
         * when building a notification we need to create a channel to which assign it.
         */
        playerNotificationManager = PlayerModule.getPlayerNotificationManager(this).also {
            it.setNotificationListener(this)
            it.setPlayer(playerHolder.audioFocusPlayer)
        }
    }

    /** NotificationListener callbacks, we get these calls when our [playerNotificationManager]
     * dispatches them, subsequently to our [PlayerNotificationManager.setPlayer] call.
     *
     * This way we can make our service a foreground service given a notification which was built
     * [playerNotificationManager].
     */
    override fun onNotificationCancelled(notificationId: Int) {}

    override fun onNotificationStarted(notificationId: Int, notification: Notification?) {
        startForeground(notificationId, notification)
    }

    inner class PlayerServiceBinder : Binder() {
        fun getPlayerHolderInstance() = playerHolder
    }

    override fun onHandleIntent(intent: Intent?) {}
}
