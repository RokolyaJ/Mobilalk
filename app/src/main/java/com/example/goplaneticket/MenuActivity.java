package com.example.goplaneticket;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MenuActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;
    FrameLayout contentContainer;
    EditText activeDateField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        bottomNavigation = findViewById(R.id.bottomNavigation);
        contentContainer = findViewById(R.id.menuFragmentContainer);

        loadLayout(R.layout.activity_search_flight);
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                loadLayout(R.layout.activity_search_flight);
                return true;
            } else if (itemId == R.id.nav_favorite) {
                loadLayout(R.layout.activity_favorite);
                return true;
            } else if (itemId == R.id.nav_tickets) {
                Intent intent = new Intent(MenuActivity.this, TicketsActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadLayout(R.layout.activity_profile);
                setupProfileButtons();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bejelentkezés szükséges!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void loadLayout(int layoutResId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        contentContainer.removeAllViews();
        View newView = inflater.inflate(layoutResId, contentContainer, false);
        contentContainer.addView(newView);

        if (layoutResId == R.layout.activity_search_flight) {
            setupDatePickers(newView);
            setupSearchButton(newView);
        }
    }

    private void setupDatePickers(View view) {
        EditText editDeparture = view.findViewById(R.id.editDeparture);
        EditText editReturn = view.findViewById(R.id.editReturn);

        editDeparture.setOnClickListener(v -> {
            activeDateField = editDeparture;
            showMaterialDatePicker();
        });

        editReturn.setOnClickListener(v -> {
            activeDateField = editReturn;
            showMaterialDatePicker();
        });
    }

    private void showMaterialDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Válassz dátumot")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Date date = new Date(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String formattedDate = sdf.format(date);

            if (activeDateField != null) {
                activeDateField.setText(formattedDate);
            }
        });
    }

    private void setupSearchButton(View view) {
        EditText editFrom = view.findViewById(R.id.editFrom);
        EditText editTo = view.findViewById(R.id.editTo);
        EditText editDeparture = view.findViewById(R.id.editDeparture);
        EditText editReturn = view.findViewById(R.id.editReturn);
        EditText editClass = view.findViewById(R.id.editClass);
        EditText editAdults = view.findViewById(R.id.editAdults);
        EditText editChildren = view.findViewById(R.id.editChildren);
        Button btnSearchFlights = view.findViewById(R.id.btnSearchFlights);

        btnSearchFlights.setOnClickListener(v -> {
            String from = editFrom.getText().toString().trim();
            String to = editTo.getText().toString().trim();
            String departure = editDeparture.getText().toString().trim();
            String ret = editReturn.getText().toString().trim();
            String flightClass = editClass.getText().toString().trim();
            String adults = editAdults.getText().toString().trim();
            String children = editChildren.getText().toString().trim();

            if (from.isEmpty() || to.isEmpty() || departure.isEmpty() || flightClass.isEmpty()) {
                Toast.makeText(this, "Tölts ki minden mezőt!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (from.equalsIgnoreCase(to)) {
                Toast.makeText(this, "Az indulás és érkezés nem lehet ugyanaz!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (adults.isEmpty()) adults = "1";
            if (children.isEmpty()) children = "0";

            sendSearchNotification();

            Intent intent = new Intent(this, FlightResultsActivity.class);
            intent.putExtra("from", from);
            intent.putExtra("to", to);
            intent.putExtra("departure", departure);
            intent.putExtra("return", ret);
            intent.putExtra("flightClass", flightClass);
            intent.putExtra("adults", adults);
            intent.putExtra("children", children);
            startActivity(intent);
        });
    }

    private void setupProfileButtons() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Előbb be kell jelentkezni!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        contentContainer.post(() -> {
            View profileView = contentContainer.getChildAt(0);
            if (profileView == null) return;

            EditText editEmail = profileView.findViewById(R.id.editEmail);
            EditText editPassword = profileView.findViewById(R.id.editPassword);
            EditText editConfirmPassword = profileView.findViewById(R.id.editConfirmPassword);
            Button btnSaveChanges = profileView.findViewById(R.id.btnSaveChanges);
            Button btnLogout = profileView.findViewById(R.id.btnLogout);
            Button btnDeleteAccount = profileView.findViewById(R.id.btnDeleteAccount);

            if (editEmail != null) {
                editEmail.setText(currentUser.getEmail());
            }

            if (btnSaveChanges != null) {
                btnSaveChanges.setOnClickListener(v -> {
                    String newEmail = editEmail.getText().toString().trim();
                    String newPassword = editPassword.getText().toString().trim();
                    String confirmPassword = editConfirmPassword.getText().toString().trim();

                    if (!TextUtils.isEmpty(newPassword)) {
                        if (!newPassword.equals(confirmPassword)) {
                            Toast.makeText(this, "A jelszavak nem egyeznek!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        currentUser.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Jelszó módosítva!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }

                    if (!newEmail.equals(currentUser.getEmail())) {
                        currentUser.updateEmail(newEmail)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Email módosítva!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
            }

            if (btnLogout != null) {
                btnLogout.setOnClickListener(v -> {
                    mAuth.signOut();
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                });
            }

            if (btnDeleteAccount != null) {
                btnDeleteAccount.setOnClickListener(v -> {
                    currentUser.delete().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Fiók törölve!", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        Intent intent = new Intent(this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                });
            }
        });
    }

    private void sendSearchNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "flight_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Keresés folyamatban")
                .setContentText("Járatokat keresünk számodra!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        long triggerTime = System.currentTimeMillis() + 10000;
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "flight_channel",
                    "Flight Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Repülőjegy keresés értesítések");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
