package com.example.apptnhchuvidientichhcn;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    private EditText lengthInput, widthInput;
    private TextView resultText;
    private WebView webView;
    private static final String CHANNEL_ID = "calculation_channel";

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ các thành phần giao diện
        lengthInput = findViewById(R.id.lengthInput);
        widthInput = findViewById(R.id.widthInput);
        resultText = findViewById(R.id.resultText);
        Button calculateButton = findViewById(R.id.calculateButton);
        webView = findViewById(R.id.webView);

        // Xử lý sự kiện nút bấm
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateRectangle();
            }
        });

        // Cấu hình WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("file:///android_asset/index.html");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Tạo kênh thông báo cho Android 8.0+
        createNotificationChannel();
    }

    private void calculateRectangle() {
        try {
            double length = Double.parseDouble(lengthInput.getText().toString());
            double width = Double.parseDouble(widthInput.getText().toString());

            double perimeter = RectangleCalculator.calculatePerimeter(length, width);
            double area = RectangleCalculator.calculateArea(length, width);

            resultText.setText("Chu vi: " + perimeter + "\nDiện tích: " + area);

            // Hiển thị Toast
            Toast.makeText(this, "Chu vi: " + perimeter + ", Diện tích: " + area, Toast.LENGTH_LONG).show();

            // Hiển thị Notification
            showNotification("Kết quả tính toán", "Chu vi: " + perimeter + ", Diện tích: " + area);
        } catch (NumberFormatException e) {
            resultText.setText("Vui lòng nhập số hợp lệ!");
            Toast.makeText(this, "Lỗi: Hãy nhập số hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }

    // Hiển thị thông báo khi gọi từ WebView
    public class WebAppInterface {
        @JavascriptInterface
        public String calculateRectangle(double length, double width) {
            double perimeter = RectangleCalculator.calculatePerimeter(length, width);
            double area = RectangleCalculator.calculateArea(length, width);

            // Hiển thị Toast từ WebView
            runOnUiThread(() ->
                    Toast.makeText(MainActivity.this, "Chu vi: " + perimeter + ", Diện tích: " + area, Toast.LENGTH_LONG).show()
            );

            // Hiển thị Notification từ WebView
            showNotification("Kết quả từ WebView", "Chu vi: " + perimeter + ", Diện tích: " + area);

            return "Chu vi: " + perimeter + " - Diện tích: " + area;
        }
    }

    // Tạo kênh thông báo (chỉ cần gọi 1 lần khi app khởi chạy)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Kênh thông báo tính toán";
            String description = "Kênh này hiển thị kết quả tính toán";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền thông báo đã được cấp!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để nhận thông báo!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Hiển thị thông báo trên thanh trạng thái
    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Thay bằng icon thực tế của app
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());
    }
}
