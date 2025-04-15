package com.example.sobadguys;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CallScreening extends CallScreeningService {


    private static final int NOTIFICATION_ID = 101;

    private static final String CHANNEL_ID = "incoming_call_channel";
    public static boolean STOP_WORKING = true;


    @Override
    public void onScreenCall(Call.Details callDetails) {
        if (!STOP_WORKING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (callDetails.getCallDirection() == Call.Details.DIRECTION_INCOMING) {
                    String phoneNumber = callDetails.getHandle().toString().
                            replace("tel:%2B", "");

                    String telephoneAccountData = fetchPhoneData(phoneNumber);
                    showNotification(this, phoneNumber, telephoneAccountData);
                }
            }
        }
        else {
            return;
        }
    }

    private void showNotification(Context context, String number, String telephoneAccountData) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.sym_action_call)
                .setContentTitle("Входящий звонок")
                .setContentText("Звонит: " + number)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(telephoneAccountData))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Уведомления о звонках";
            String description = "Канал для уведомлений о входящих звонках";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            notificationManager.createNotificationChannel(channel);
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }


    }

    private String fetchPhoneData(String phoneNumber) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(() -> {
            HttpParse handler = new HttpParse();
            return handler.executeGetRequest(phoneNumber);
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {

            return  e.toString();
        } finally {
            executorService.shutdown();
        }
    }

}
