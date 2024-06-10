package com.example.mad_assignment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddEditNoteActivity extends AppCompatActivity {
    private EditText editTextTitle, editTextContent;
    private DatabaseReference databaseNotes;
    private String noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        databaseNotes = FirebaseDatabase.getInstance().getReference("notes");

        if (getIntent() != null && getIntent().hasExtra("noteId")) {
            noteId = getIntent().getStringExtra("noteId");
            editTextTitle.setText(getIntent().getStringExtra("noteTitle"));
            editTextContent.setText(getIntent().getStringExtra("noteContent"));
        }

        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError("Title required");
            return;
        }

        if (TextUtils.isEmpty(content)) {
            editTextContent.setError("Content required");
            return;
        }

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (noteId == null) {
            // Add new note
            String id = databaseNotes.push().getKey();
            Note note = new Note(id, title, date, content);
            databaseNotes.child(id).setValue(note);
            Toast.makeText(this, "Note added", Toast.LENGTH_SHORT).show();
        } else {
            // Edit existing note
            Note note = new Note(noteId, title, date, content);
            databaseNotes.child(noteId).setValue(note);
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
