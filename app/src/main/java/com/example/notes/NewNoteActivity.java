package com.example.notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class NewNoteActivity extends AppCompatActivity {

    private Button btnnnote;
    private EditText etnntitle,etnndesc;

    FirebaseAuth firebaseAuth;
    DatabaseReference firebaseDatabaseNotes;

    private ProgressDialog progressDialog;

    private Menu mainmenu;

    private String noteId;

    private boolean isExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        try
        {
            noteId=getIntent().getStringExtra("noteId");

            if (!noteId.trim().equals(""))
            {
                isExists=true;
            }
            else
            {
                isExists=false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        btnnnote=findViewById(R.id.btnnnote);
        etnntitle=findViewById(R.id.etnntitle);
        etnndesc=findViewById(R.id.etnndesc);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabaseNotes=FirebaseDatabase.getInstance().getReference().child("NOTES").child(firebaseAuth.getCurrentUser().getUid());

        btnnnote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title=etnntitle.getText().toString().trim();
                String content=etnndesc.getText().toString().trim();

                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content))
                {
                    CreateNote(title,content);
                }
                else
                {
                    Toast.makeText(NewNoteActivity.this, "Fill the empty fields...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        PutData();
    }

    private void PutData()
    {
        if (isExists) {
            firebaseDatabaseNotes.child(noteId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("Title") && dataSnapshot.hasChild("TimeStamp")) {
                        String title = dataSnapshot.child("Title").getValue().toString();
                        String content = dataSnapshot.child("Content").getValue().toString();

                        etnntitle.setText(title);
                        etnndesc.setText(content);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void CreateNote(String title, String content)
    {
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Processing your request....please wait...");
        progressDialog.show();

        if (firebaseAuth.getCurrentUser() != null) {
            if (isExists) {
                // UPDATE A NOTE
                Map updateMap=new HashMap();

                updateMap.put("Title",etnntitle.getText().toString().trim());
                updateMap.put("Content",etnndesc.getText().toString().trim());
                updateMap.put("TimeStamp",ServerValue.TIMESTAMP);

                firebaseDatabaseNotes.child(noteId).updateChildren(updateMap);

                progressDialog.dismiss();

                Toast.makeText(this, "Note Updated...", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(NewNoteActivity.this,MainActivity.class));
                finish();
            }
            else {
                // CREATE A NEW NOTE
                final DatabaseReference newnoteref = firebaseDatabaseNotes.push();

                final Map notemap = new HashMap();
                notemap.put("Title", title);
                notemap.put("Content", content);
                notemap.put("TimeStamp", ServerValue.TIMESTAMP);

                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        newnoteref.setValue(notemap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    Toast.makeText(NewNoteActivity.this, "Note added to database...", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(NewNoteActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(NewNoteActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                mainThread.start();
            }
        }
        else
        {
            Toast.makeText(this, "USERS IS NOT SIGNED IN...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.new_note_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;
            case R.id.new_note_delete_btn:
                if (isExists)
                {
                    new AlertDialog.Builder(this)
                            .setTitle("").setMessage("Are you sure to delete the note?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DeleteNote();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                }
                else
                {
                    Toast.makeText(this, "Nothing to delete...", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        return true;
    }

    private void DeleteNote()
    {
        firebaseDatabaseNotes.child(noteId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(NewNoteActivity.this, "Note deleted...", Toast.LENGTH_SHORT).show();
                    noteId="no";
                    finish();
                }
                else
                {
                    Toast.makeText(NewNoteActivity.this, "ERROR: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
