package com.tradejob.pro.app;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.work.WorkManager;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertSame;

@RunWith(AndroidJUnit4.class)
public class WorkManagerInitializationTest {

    @Test
    public void workManagerUsesApplicationWorkerFactory() {
        TradeJobApplication application = (TradeJobApplication)
                InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();

        assertSame(application.getWorkerFactory(),
                WorkManager.getInstance(application).getConfiguration().getWorkerFactory());
    }
}
