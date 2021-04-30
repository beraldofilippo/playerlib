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

package com.beraldo.playerlib.media

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.beraldo.playerlib.R
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

/**
 * Created by Filippo Beraldo on 15/11/2018.
 * http://github.com/beraldofilippo
 */
class DescriptionAdapter(private val context: Context) :
    PlayerNotificationManager.MediaDescriptionAdapter {

    override fun getCurrentContentTitle(player: Player): String =
        context.getString(R.string.notification_title)

    override fun getCurrentContentText(player: Player): String? = null

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? = null

    /**
     * Specify the PendingIntent which is fired whenever there's a click on the notification.
     * This pending intent will fire a broadcast, which will be captured by a BroadcastReceiver
     * we have to put in the app module.
     * The broadcast receiver will intercept this intent and start the player activity.
     *
     * Since I want this component to be decoupled from the PlayerActivity implementation
     * here I have to do a workaround, using polling and forging an explicit Intent (as I do not know which
     * is the name of the player activity I would have to start) because of the security
     * limitations introduced on O+.
     *
     * https://commonsware.com/blog/2017/04/11/android-o-implicit-broadcast-ban.html
     * */
    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        val intent = Intent().apply {
            action = "com.beraldo.playerlib.LAUNCH_PLAYER_ACTIVITY"
        }
        val matches = context.packageManager.queryBroadcastReceivers(intent, 0)

        val explicit = Intent(intent)
        for (resolveInfo in matches) {
            val componentName = ComponentName(
                resolveInfo.activityInfo.applicationInfo.packageName,
                resolveInfo.activityInfo.name
            )

            explicit.component = componentName
        }

        return PendingIntent.getBroadcast(context, 0, explicit, 0)
    }
}