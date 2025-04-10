package com.example.goplaneticket;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TicketsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TicketAdapter ticketAdapter;
    private List<Ticket> ticketList;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ImageView backButton;
    private Button btnSetReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewTickets);
        progressBar = findViewById(R.id.progressBarTickets);
        tvEmpty = findViewById(R.id.tvEmptyTickets);
        btnSetReminder = findViewById(R.id.btnSetReminder);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ticketList = new ArrayList<>();
        ticketAdapter = new TicketAdapter(this, ticketList);
        recyclerView.setAdapter(ticketAdapter);

        btnSetReminder.setOnClickListener(v -> {
            scheduleReminder();
            Toast.makeText(this, "Értesítés beállítva!", Toast.LENGTH_SHORT).show();
        });

        loadUserTickets();
    }

    private void loadUserTickets() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Előbb be kell jelentkezned!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("seat_reservations")
                .whereEqualTo("user", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful() && task.getResult() != null) {
                        ticketList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Ticket ticket = document.toObject(Ticket.class);
                            ticket.setDocumentId(document.getId());
                            ticketList.add(ticket);
                        }

                        ticketAdapter.notifyDataSetChanged();

                        if (ticketList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            btnSetReminder.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            btnSetReminder.setVisibility(View.VISIBLE);
                        }

                    } else {
                        Toast.makeText(this, "Hiba a jegyek lekérdezésekor!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void scheduleReminder() {
        try {
            Intent intent = new Intent(this, ReminderBroadcast.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            long timeAtButtonClick = System.currentTimeMillis();
            long delayInMillis = 10 * 1000;

            if (alarmManager != null) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeAtButtonClick + delayInMillis, pendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Nem sikerült az értesítés időzítése.", Toast.LENGTH_SHORT).show();
        }
    }
}
