package fr.iut2.saeprojet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.iut2.saeprojet.api.APIClient;
import fr.iut2.saeprojet.api.APIService;
import fr.iut2.saeprojet.api.EtatCandidatureEnum;
import fr.iut2.saeprojet.api.ResultatAppel;
import fr.iut2.saeprojet.entity.Candidature;
import fr.iut2.saeprojet.entity.CandidaturesResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListCandidaturesActivity extends StageAppActivity {
    // View
    private TextView retourSyntheseView;
    private ListView candidaturesView;

    private ArrayAdapter<EtatCandidatureEnum> adapterCandidature;

    // data
    private CandidatureAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_candidatures);

        // Init view
        retourSyntheseView = findViewById(R.id.retourSynthese);
        candidaturesView = findViewById(R.id.candidatures);


        Intent intent = getIntent();
        if(intent != null) {
            // Récupérer la liste de chaînes de caractères de l'intention
            ArrayList<String> candidaturesListe = intent.getStringArrayListExtra("candidatures");

            // Vérifier si la liste est non nulle et non vide
            if (candidaturesListe != null && !candidaturesListe.isEmpty()) {
                // Afficher le premier élément de la liste dans retourSyntheseView
                retourSyntheseView.setText(candidaturesListe.get(0));
            }
        }

        // Lier l'adapter au listView
        adapter = new CandidatureAdapter(this, new ArrayList<Candidature>());
        candidaturesView.setAdapter(adapter);

        //
        retourSyntheseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListCandidaturesActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        candidaturesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Récupération de la tâche cliquée à l'aide de l'adapter
                Candidature candidature = adapter.getItem(position);

                //
                Intent intent = new Intent(ListCandidaturesActivity.this, CandidatureActivity.class);
                intent.putExtra(CandidatureActivity.CANDIDADURE_KEY, candidature);
                startActivity(intent);
            }
        });

        //
        refreshMesInformations();
    }

    private void refreshMesInformations() {
        APIClient.getCandidatures(this, new ResultatAppel<CandidaturesResponse>() {
            @Override
            public void traiterResultat(CandidaturesResponse candidatures) {
                // Mettre à jour l'adapter avec la liste de taches
                adapter.clear();
                adapter.addAll(candidatures.candidatures);

                // Now, notify the adapter of the change in source
                adapter.notifyDataSetChanged();
            }

            @Override
            public void traiterErreur() {

            }
        });
    }
}