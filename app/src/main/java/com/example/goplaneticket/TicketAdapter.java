package com.example.goplaneticket;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private Context context;
    private List<Ticket> ticketList;

    public TicketAdapter(Context context, List<Ticket> ticketList) {
        this.context = context;
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ticket_item, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);

        holder.tvFlightId.setText("Járat azonosító: " + ticket.getFlightId());
        holder.tvSeats.setText("Székek: " + String.join(", ", ticket.getSeats()));
        holder.tvDate.setText("Foglalva: " + formatDate(ticket.getTimestamp()));

        holder.btnModify.setOnClickListener(v -> {
            Intent intent = new Intent(context, SeatSelectionActivity.class);
            intent.putExtra("flightId", ticket.getFlightId());
            intent.putStringArrayListExtra("currentSeats", new ArrayList<>(ticket.getSeats()));
            intent.putExtra("documentId", ticket.getDocumentId());
            intent.putExtra("mode", "modify");
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Foglalás törlése")
                    .setMessage("Biztosan törlöd ezt a foglalást?")
                    .setPositiveButton("Igen", (dialog, which) -> deleteTicket(ticket, position))
                    .setNegativeButton("Mégse", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvFlightId, tvSeats, tvDate;
        Button btnModify, btnDelete;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFlightId = itemView.findViewById(R.id.tvFlightId);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnModify = itemView.findViewById(R.id.btnModifyTicket);
            btnDelete = itemView.findViewById(R.id.btnDeleteTicket);
        }
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    private void deleteTicket(Ticket ticket, int position) {
        FirebaseFirestore.getInstance()
                .collection("seat_reservations")
                .document(ticket.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Foglalás törölve!", Toast.LENGTH_SHORT).show();
                    ticketList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, ticketList.size());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
