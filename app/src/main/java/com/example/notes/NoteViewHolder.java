package com.example.notes;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class NoteViewHolder extends RecyclerView.ViewHolder {

    View mView;
    TextView TextTitle,TextTime;
    CardView note_card;

    public NoteViewHolder(@NonNull View itemView) {
        super(itemView);

        mView=itemView;
        TextTitle=mView.findViewById(R.id.note_title);
        TextTime=mView.findViewById(R.id.note_time);
        note_card=mView.findViewById(R.id.note_card);
    }

    public void setNoteTitle(String title)
    {
        TextTitle.setText(title);
    }

    public void setNoteTime(String time)
    {
        TextTime.setText(time);
    }
}
