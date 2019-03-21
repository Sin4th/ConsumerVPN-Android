package com.wlvpn.slider.whitelabelvpn;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.multidex.MultiDex;

import com.evernote.android.job.JobManager;
import com.gentlebreeze.vpn.sdk.IVpnSdk;
import com.gentlebreeze.vpn.sdk.VpnSdk;
import com.gentlebreeze.vpn.sdk.config.SdkConfig;
import com.squareup.leakcanary.LeakCanary;
import com.wlvpn.slider.whitelabelvpn.di.AppComponent;
import com.wlvpn.slider.whitelabelvpn.di.AppModule;
import com.wlvpn.slider.whitelabelvpn.di.DaggerAppComponent;
import com.wlvpn.slider.whitelabelvpn.jobs.ConsumerVpnJobCreator;

import javax.inject.Inject;

import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ConsumerVpnApplication extends Application {

    private static IVpnSdk vpnSdk;

    private static AppComponent COMPONENT;

    private static final String LOG_TAG_SDK = "VPNSDK-";

    public static AppComponent component() {
        return COMPONENT;
    }

    public static IVpnSdk getVpnSdk() {
        return vpnSdk;
    }

    @Inject
    ConsumerVpnJobCreator consumerVpnJobCreator;

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        LeakCanary.install(this);

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
        COMPONENT = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        COMPONENT.inject(this);

        JobManager.create(this).addJobCreator(consumerVpnJobCreator);

        vpnSdk = VpnSdk.init(this, new SdkConfig(
                BuildConfig.ACCOUNT_NAME,
                BuildConfig.API_KEY,
                BuildConfig.AUTH_SUFFIX,
                BuildConfig.CLIENT,
                BuildConfig.ENDPOINT,
                BuildConfig.IP_GEO,
                BuildConfig.LOGIN_API,
                BuildConfig.REFRESH_API,
                BuildConfig.PROTOCOL_LIST_API,
                BuildConfig.SERVER_LIST_API,
                LOG_TAG_SDK
        ));

        Timber.plant(new Timber.DebugTree());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        CalligraphyContextWrapper.wrap(base);
    }

}