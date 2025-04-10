package com.example.goplaneticket;

import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SeatSelectionActivity extends AppCompatActivity {

    private GridLayout gridSeats;
    private Button btnConfirmSeats;
    private TextView tvSeatInfo;
    private ImageView backButton;

    private List<String> selectedSeats = new ArrayList<>();
    private List<String> alreadyTaken = new ArrayList<>();
    private List<String> currentSeats = new ArrayList<>();

    private int maxSelection = 1;
    private String flightId;
    private String documentId;
    private boolean isModifyMode = false;
    private final Random random = new Random();

    private final int[][] seatLayout = {
            {1, 1, 1, 0, 1, 1, 1},
            {1, 1, 1, 0, 1, 1, 1},
            {1, 1, 1, 0, 1, 1, 1},
            {1, 1, 1, 0, 1, 1, 1},
            {1, 1, 1, 0, 1, 1, 1},
            {1, 1, 1, 0, 1, 1, 1},
            {1, 1, 1, 0, 1, 1, 1},
            {1, 1, 1, 0, 1, 1, 1},
            {1, 1, 1, 0, 1, 1, 1},
            {1, 1, 1, 0, 1, 1, 1}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        gridSeats = findViewById(R.id.seatGrid);
        btnConfirmSeats = findViewById(R.id.btnConfirmSeats);
        tvSeatInfo = findViewById(R.id.tvSeatInfo);
        backButton = findViewById(R.id.backButtonSeats);

        if (getIntent() != null) {
            if ("modify".equals(getIntent().getStringExtra("mode"))) {
                isModifyMode = true;
                documentId = getIntent().getStringExtra("documentId");
                currentSeats = getIntent().getStringArrayListExtra("currentSeats");
            }
        }

        Flight flight = (Flight) getIntent().getSerializableExtra("flight");
        if (flight != null) {
            flightId = flight.getId();
        } else {
            flightId = getIntent().getStringExtra("flightId");
        }

        maxSelection = getIntent().getIntExtra("maxSeats", currentSeats != null ? currentSeats.size() : 1);

        loadReservations();

        btnConfirmSeats.setOnClickListener(v -> confirmReservation());

        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void loadReservations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("seat_reservations")
                .whereEqualTo("flightId", flightId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        alreadyTaken.clear();
                        List<String> tempTaken = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            List<String> seats = (List<String>) document.get("seats");
                            if (seats != null) {
                                tempTaken.addAll(seats);
                            }
                        }

                        if (tempTaken.isEmpty()) {
                            alreadyTaken = generateRandomReservedSeats();
                        } else {
                            alreadyTaken = new ArrayList<>(tempTaken);

                            if (isModifyMode && currentSeats != null) {
                                alreadyTaken.removeAll(currentSeats);
                            }
                        }

                        generateSeatButtons();
                    } else {
                        Toast.makeText(this, "Hiba történt a székek lekérdezésekor!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmReservation() {
        if (selectedSeats.size() != maxSelection) {
            Toast.makeText(this, "Pontosan " + maxSelection + " széket kell kiválasztanod!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Nincs bejelentkezett felhasználó!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (isModifyMode && documentId != null) {
            db.collection("seat_reservations")
                    .document(documentId)
                    .update("seats", selectedSeats)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Sikeres módosítás!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // 🔥 LÉNYEG: Létrehozunk egy ÚJ ID-t
            String newDocumentId = db.collection("seat_reservations").document().getId();

            Map<String, Object> booking = new HashMap<>();
            booking.put("user", auth.getCurrentUser().getUid());
            booking.put("flightId", flightId);
            booking.put("seats", selectedSeats);
            booking.put("timestamp", System.currentTimeMillis());
            booking.put("documentId", newDocumentId); // 🔥 EZ KELL A TICKET-HEZ!

            db.collection("seat_reservations")
                    .document(newDocumentId)
                    .set(booking) // 🔥
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Foglalás elmentve!", Toast.LENGTH_SHORT).show();
                        scheduleReminder();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void scheduleReminder() {
        long triggerTime = System.currentTimeMillis() + 60 * 1000;

        Intent intent = new Intent(this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    private void generateSeatButtons() {
        String[] seatLetters = {"A", "B", "C", "", "D", "E", "F"};
        gridSeats.removeAllViews();
        selectedSeats.clear();

        for (int row = 0; row < seatLayout.length; row++) {
            for (int col = 0; col < seatLayout[row].length; col++) {
                if (seatLayout[row][col] == 0 || seatLetters[col].isEmpty()) continue;

                String seatId = seatLetters[col] + (row + 1);

                Button seatButton = new Button(this);
                seatButton.setText(seatId);
                seatButton.setTextSize(10f);
                seatButton.setAllCaps(false);
                seatButton.setPadding(0, 0, 0, 0);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                params.width = 80;
                params.height = 80;

                if (col == 2) {
                    params.setMargins(4, 4, 100, 4);
                } else {
                    params.setMargins(4, 4, 4, 4);
                }

                seatButton.setLayoutParams(params);
                seatButton.setStateListAnimator(null);

                if (alreadyTaken.contains(seatId)) {
                    seatButton.setEnabled(false);
                    seatButton.setBackgroundColor(Color.RED);
                } else if (isModifyMode && currentSeats.contains(seatId)) {
                    selectedSeats.add(seatId);
                    seatButton.setBackgroundColor(Color.parseColor("#FFC107"));
                } else {
                    seatButton.setBackgroundColor(Color.parseColor("#00C853"));
                }

                final String finalSeatId = seatId;
                seatButton.setOnClickListener(v -> {
                    animateSeatClick(seatButton);

                    if (selectedSeats.contains(finalSeatId)) {
                        selectedSeats.remove(finalSeatId);
                        seatButton.setBackgroundColor(Color.parseColor("#00C853"));
                    } else if (selectedSeats.size() < maxSelection) {
                        selectedSeats.add(finalSeatId);
                        seatButton.setBackgroundColor(Color.parseColor("#FFC107"));
                    } else {
                        Toast.makeText(this, "Több széket nem választhatsz!", Toast.LENGTH_SHORT).show();
                    }
                    updateSeatInfo();
                });

                gridSeats.addView(seatButton);
            }
        }
        updateSeatInfo();
    }

    private void updateSeatInfo() {
        StringBuilder sb = new StringBuilder("Kiválasztva: ");
        if (!selectedSeats.isEmpty()) {
            Collections.sort(selectedSeats);
            for (int i = 0; i < selectedSeats.size(); i++) {
                sb.append(selectedSeats.get(i));
                if (i < selectedSeats.size() - 1) sb.append(", ");
            }
        }
        tvSeatInfo.setText(sb.toString());
    }

    private List<String> generateRandomReservedSeats() {
        List<String> randomSeats = new ArrayList<>();
        String[] seatLetters = {"A", "B", "C", "D", "E", "F"};
        int seatCount = 10 + random.nextInt(6);

        while (randomSeats.size() < seatCount) {
            String letter = seatLetters[random.nextInt(seatLetters.length)];
            int row = 1 + random.nextInt(10);

            int colIndex = "ABCDEF".indexOf(letter);
            if (colIndex >= 0 && seatLayout[row - 1][colIndex < 3 ? colIndex : colIndex + 1] == 1) {
                String seat = letter + row;
                if (!randomSeats.contains(seat)) {
                    randomSeats.add(seat);
                }
            }
        }
        return randomSeats;
    }

    private void animateSeatClick(Button button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.2f, 1f);
        scaleX.setDuration(150);
        scaleY.setDuration(150);
        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleY.setInterpolator(new DecelerateInterpolator());
        scaleX.start();
        scaleY.start();
    }

    @Override
    public void onBackPressed() {
        if (isModifyMode) {
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
