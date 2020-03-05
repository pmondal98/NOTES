package com.example.notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private RecyclerView notes_list;
    private GridLayoutManager gridLayoutManager;

    private DatabaseReference fNotesDatabase;

    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT>=21)
        {
            Window window=this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.mycolor));
        }

        notes_list=findViewById(R.id.notes_list);
        gridLayoutManager=new GridLayoutManager(this,3,GridLayoutManager.VERTICAL,false);

        notes_list.setHasFixedSize(true);
        notes_list.setLayoutManager(gridLayoutManager);

        firebaseAuth=FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null)
        {
            fNotesDatabase= FirebaseDatabase.getInstance().getReference().child("NOTES").child(firebaseAuth.getCurrentUser().getUid());
        }

        UpdateUI();

        loadData();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void loadData() {
        Query query = fNotesDatabase.orderByChild("TimeStamp");

        FirebaseRecyclerAdapter<NoteModel, NoteViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<NoteModel, NoteViewHolder>(
                NoteModel.class,
                R.layout.single_note_layout,
                NoteViewHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(final NoteViewHolder noteViewHolder, NoteModel noteModel, int position) {
                final String noteId=getRef(position).getKey();

                fNotesDatabase.child(noteId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("Title") && dataSnapshot.hasChild("TimeStamp"))
                        {
                            String title=dataSnapshot.child("Title").getValue().toString();
                            String time=dataSnapshot.child("TimeStamp").getValue().toString();

                            noteViewHolder.setNoteTitle(title);
                            GetTime getTime = new GetTime();
                            noteViewHolder.setNoteTime(getTime.getTime(Long.parseLong(time), getApplicationContext()));



                            noteViewHolder.note_card.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i=new Intent(MainActivity.this,NewNoteActivity.class);
                                    i.putExtra("noteId",noteId);
                                    startActivity(i);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };

        notes_list.setAdapter(firebaseRecyclerAdapter);
    }

    private void UpdateUI()
    {
        if (firebaseAuth.getCurrentUser() != null)
        {
            Log.i("MainActivity :","firebaseAuth != null");
        }
        else
        {
            startActivity(new Intent(MainActivity.this,NewNoteActivity.class));
            finish();
            Log.i("MainActivity :","firebaseAuth == null");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId())
        {
            case R.id.btn_newnote:
                startActivity(new Intent(MainActivity.this,NewNoteActivity.class));
                break;
            case R.id.btn_logout:
                LogOut();
                break;
        }

        return true;
    }

    private void LogOut()
    {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Log Out Succesfull...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    public void onBackPressed() {

        if(backPressedTime + 2000 > System.currentTimeMillis())
        {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        }
        else
        {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        backPressedTime=System.currentTimeMillis();
    }
}
