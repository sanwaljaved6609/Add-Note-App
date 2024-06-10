package com.example.mad_assignment;

package com.example.mad_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private List<Note> noteList, filteredList;
    private DatabaseReference databaseNotes;
    private androidx.appcompat.widget.SearchView searchView;
    private Spinner spinnerSort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new NoteAdapter(filteredList, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddEditNoteActivity.class));
            }
        });

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterNotes(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotes(newText);
                return false;
            }
        });

        spinnerSort = findViewById(R.id.spinnerSort);
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortNotes(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        databaseNotes = FirebaseDatabase.getInstance().getReference("notes");

        databaseNotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                noteList.clear();
                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                    Note note = noteSnapshot.getValue(Note.class);
                    noteList.add(note);
                }
                filterNotes(searchView.getQuery().toString());
                sortNotes(spinnerSort.getSelectedItemPosition());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load notes.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterNotes(String query) {
        filteredList.clear();
        for (Note note : noteList) {
            if (note.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    note.getContent().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(note);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void sortNotes(int sortOption) {
        Comparator<Note> comparator;
        switch (sortOption) {
            case 0:
                comparator = new Comparator<Note>() {
                    @Override
                    public int compare(Note n1, Note n2) {
                        return n1.getTitle().compareToIgnoreCase(n2.getTitle());
                    }
                };
                break;
            case 1:
                comparator = new Comparator<Note>() {
                    @Override
                    public int compare(Note n1, Note n2) {
                        return n1.getDate().compareTo(n2.getDate());
                    }
                };
                break;
            default:
                return;
        }
        Collections.sort(filteredList, comparator);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onNoteClick(Note note) {
        Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
        intent.putExtra("noteId", note.getId());
        intent.putExtra("noteTitle", note.getTitle());
        intent.putExtra("noteDate", note.getDate());
        intent.putExtra("noteContent", note.getContent());
        startActivity(intent);
    }

    @Override
    public void onNoteLongClick(Note note) {
        DatabaseReference noteRef = databaseNotes.child(note.getId());
        noteRef.removeValue();
        Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
    }
}

