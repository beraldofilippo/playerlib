# playerlib - ExoPlayer HTTP audio streaming with foreground service, Android O+ compliant
This is a simple bare-bone module which allows background playing from a HTTP stream, using ExoPlayer. It is compliant with all Android O+ requirements as foreground services are concerned.

## Features
- One hundred percent Kotlin.
- Uses foreground service to ensure playback is preserved when in background.
- Uses audio focus wrapping, to perform handling of audio focus in Android O+ style.
- Shows an ongoing notification for the user to control playback, using PlayerNotificationManager form ExoPlayer.
- It is completely decoupled from whichever implementation any users wants for the player activity.
- This is untested.

## How to get it
- Set up a main `app` project as usual
- Clone into a separate folder from the main project
- Use `Import module...` function from AS
- Go to `Project structure` and add playerlib as a dependency

## How to use it
Simple and self explanatory, just an actvity which launches the service, and a broadcast receiver to handle the launch of the activity from the player notification (when it gets clicked).
Change the source of the stream changing the `Player.kt` file, in the `start()` method.

*AndroidManifest.xml*
```
<application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        tools:ignore="GoogleAppIndexingWarning">
        <activity
            android:name="com.beraldo.myapp.PlayerActivity"
            android:configChanges="keyboard|keyboardHidden|orientation|screenSize|screenLayout|smallestScreenSize|uiMode"
            android:label="PlayerActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service android:name="com.beraldo.playerlib.PlayerService" />

        <receiver
            android:name=".LaunchPlayerBroadcastReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="com.beraldo.playerlib.LAUNCH_PLAYER_ACTIVITY" />
            </intent-filter>
        </receiver>
    </application>
```

*LaunchPlayerBroadcastReceiver.kt*
```
class LaunchPlayerBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notifyIntent = Intent(context, PlayerActivity::class.java)
        context.startActivity(notifyIntent)
    }
}
```

*PlayerActivity.kt*
```
class PlayerActivity : AppCompatActivity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        //Start the service
        val intent = Intent(this, PlayerService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    /**
     * Create our connection to the service to be used in our bindService call.
     */
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {}

        /**
         * Called after a successful bind with our PlayerService.
         */
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is PlayerService.PlayerServiceBinder) {
                // Get the instance of the player from the service and set it as player to our playerView
                player_view.player = service.getPlayerHolderInstance().audioFocusPlayer
            }
        }
    }
}
```
*activity_player.xml*
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.beraldo.radiouci.PlayerActivity"
    tools:showIn="@layout/activity_player">

    <com.google.android.exoplayer2.ui.PlayerView
        android:id="@+id/player_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</FrameLayout>
```
*build.gradle* of module `app` after importing the module (see there's `implementation project(path: ':playerlib')`)
```
apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-android-extensions'

android {
    compileSdkVersion 28
    buildToolsVersion "28.0.3"
    defaultConfig {
        applicationId "com.beraldo.myapp"
        minSdkVersion 26
        targetSdkVersion 28
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'com.android.support:appcompat-v7:28.0.0'
    implementation 'com.android.support:support-v4:28.0.0'
    implementation 'com.google.android.exoplayer:exoplayer-ui:2.9.1'
    implementation 'com.android.support.constraint:constraint-layout:1.1.3'
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
    implementation 'com.android.support:design:28.0.0'
    implementation 'org.jetbrains.anko:anko:0.10.8'
    implementation project(path: ':playerlib')
}
repositories {
    mavenCentral()
}

```

## Acknowledgments
I took inspiration from the *Google codelabs for ExoPlayer* https://codelabs.developers.google.com/codelabs/exoplayer-intro/#0 and mixed it up with *Architecting Video Playback through a Service* https://proandroiddev.com/architecting-video-playback-through-a-service-501c7bd158fa and other stuff I don't remember.


