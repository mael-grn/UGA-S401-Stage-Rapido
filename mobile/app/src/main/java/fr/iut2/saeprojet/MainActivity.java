package fr.iut2.saeprojet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import fr.iut2.saeprojet.api.APIClient;
import fr.iut2.saeprojet.api.APIService;
import fr.iut2.saeprojet.api.EtatCandidatureEnum;
import fr.iut2.saeprojet.api.ResultatAppel;
import fr.iut2.saeprojet.entity.Candidature;
import fr.iut2.saeprojet.entity.CandidatureRequest;
import fr.iut2.saeprojet.entity.CandidaturesResponse;
import fr.iut2.saeprojet.entity.CompteEtudiant;
import fr.iut2.saeprojet.entity.CompteEtudiantRequest;
import fr.iut2.saeprojet.entity.OffresResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends StageAppActivity {

    // View
    private LinearLayout offresView;
    private LinearLayout candidaturesView;

    private LinearLayout offreView;

    private Button logoutButton;
    private int nbRefuse;
    private int nbNonRefuse;

    private String derniereConnexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init View
        Button offresView = findViewById(R.id.btn_list_offre);
        Button candidaturesView = findViewById(R.id.btn_candidature_en_cour);
        Button candidatureRefuseView = findViewById(R.id.btn_candidature_refusé);
        logoutButton = findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Appeler la fonction de déconnexion

                APIClient.getCompteEtudiant(MainActivity.this, getCompteId(), new ResultatAppel<CompteEtudiant>() {
                    @Override
                    public void traiterResultat(CompteEtudiant compteEtudiant) {
                        updateCompteEtudiant(compteEtudiant);
                    }

                    @Override
                    public void traiterErreur() {
                        Log.e("ERROR", "Erreur lors de la récupération des données du compte étudiant");
                    }
                });

                deconnexion();
            }
        });

        candidaturesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ListCandidaturesActivity.class);
                ArrayList<String> listeCandidatures = new ArrayList<>();
                listeCandidatures.add("< Synthèse");
                intent.putStringArrayListExtra("candidatures", listeCandidatures);
                startActivity(intent);
            }
        });

        candidatureRefuseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ListCandidaturesActivityRefuse.class);
                ArrayList<String> listeCandidatures = new ArrayList<>();
                listeCandidatures.add("< Synthèse");
                intent.putStringArrayListExtra("candidatures", listeCandidatures);
                startActivity(intent);
            }
        });


        offresView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ListOffresActivity.class);
                ArrayList<String> listeCandidatures = new ArrayList<>();
                listeCandidatures.add("< Synthèse");
                intent.putStringArrayListExtra("candidatures", listeCandidatures);
                startActivity(intent);
            }
        });

        //
        refreshLogin();
        refreshNBOffres();
        refreshCandidatures();
    }


    private void deconnexion() {
        // Ajoutez ici la logique pour déconnecter l'utilisateur
        // Par exemple, vous pouvez effacer les informations d'identification stockées dans les préférences partagées
        SharedPreferences sharedPreferences = getSharedPreferences("nom_de_votre_preference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Efface toutes les valeurs de préférence partagée
        editor.apply();

        // Rediriger l'utilisateur vers l'écran de connexion ou toute autre destination appropriée
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Optionnel : fermer l'activité actuelle pour empêcher l'utilisateur de revenir à l'écran principal avec le bouton "Retour"
    }


    private void refreshLogin() {
        TextView loginView = findViewById(R.id.login);
        loginView.setText(getLogin());
        refreshMesInformations();
    }

    private void refreshNBOffres() {
        APIClient.getOffres(this, new ResultatAppel<OffresResponse>() {

            @Override
            public void traiterResultat(OffresResponse offres) {
                TextView nbOffresView = findViewById(R.id.textView8);
                nbOffresView.setText(String.valueOf(offres.offres.size()) + " " + nbOffresView.getText().toString());
            }

            @Override
            public void traiterErreur() {
            }
        });
    }

    private void refreshMesInformations() {
        nbRefuse = 0;
        nbNonRefuse = 0;

        APIClient.getCompteEtudiant(this, getCompteId(), new ResultatAppel<CompteEtudiant>() {
            @Override
            public void traiterResultat(CompteEtudiant compteEtudiant) {
                for (String candidature : compteEtudiant.candidatures) {
                    Long id_cand = Long.valueOf(candidature.replace("/api/candidatures/", ""));
                    APIClient.getCandidature(MainActivity.this, id_cand, new ResultatAppel<Candidature>() {
                        @Override
                        public void traiterResultat(Candidature response) {
                            if (response.etatCandidature.compareTo("/api/etat_candidatures/3") != 0) {
                                nbNonRefuse++;
                            } else {
                                nbRefuse++;
                            }

                            // Mettre à jour les TextView ici après avoir compté toutes les candidatures
                            updateTextViews();
                        }

                        @Override
                        public void traiterErreur() {
                            Log.e("ERROR", "Erreur lors de la récupération des données de la candidature");
                        }
                    });
                }



                if (compteEtudiant.derniereConnexion == null) {
                    long timestamp = System.currentTimeMillis();
                    java.util.Date date = new Date(timestamp);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    String dateString = dateFormat.format(date);
                    MainActivity.this.derniereConnexion = dateString;
                } else {
                    try {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("HH-mm-ss", Locale.getDefault());
                        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM 'à' HH'h'", Locale.getDefault());
                        Date parsedDate = inputFormat.parse(compteEtudiant.derniereConnexion);
                        String formattedDate = outputFormat.format(parsedDate);
                        MainActivity.this.derniereConnexion = "Dernière connexion le " + formattedDate;
                    } catch (ParseException e) {
                        // Gérer l'exception si la conversion échoue
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void traiterErreur() {
                Log.e("ERROR", "Erreur lors de la récupération des données du compte étudiant");
            }
        });
    }

    private void updateCompteEtudiant(CompteEtudiant compteEtudiant) {
        long timestamp = System.currentTimeMillis();
        Date date = new Date(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String dateString = dateFormat.format(date);

        compteEtudiant.derniereConnexion = dateString;

        CompteEtudiantRequest compteEtudiantReq = new CompteEtudiantRequest();
        compteEtudiantReq.login = getCompte_Id();
        compteEtudiantReq.roles = compteEtudiant.roles;
        compteEtudiantReq.password = compteEtudiant.password;
        compteEtudiantReq.parcours = compteEtudiant.parcours;
        compteEtudiantReq.derniereConnexion = dateString;
        compteEtudiantReq.etatRecherche = compteEtudiant.etatRecherche;
        compteEtudiantReq.etudiant = compteEtudiant.etudiant;


        // Affichez la date et l'heure formatées dans la console
        System.out.println("ICI AVEC LE COMPTE " + compteEtudiant.login + " dernier co : " + compteEtudiant.derniereConnexion);

        // Appelez la méthode pour mettre à jour la dernière connexion avec le nouveau timestamp
                APIClient.updateCompteEtudiant(MainActivity.this, compteEtudiant.id, compteEtudiantReq, new ResultatAppel<CompteEtudiant>() {
                    @Override
                    public void traiterResultat(CompteEtudiant response) {
                        System.out.println("RESULTAT APPEL : VALIDE  dernier co : " + compteEtudiant.derniereConnexion);
                    }

                    @Override
                    public void traiterErreur() {
                        System.out.println("RESULTAT APPEL : ERREUR  dernier co : " + compteEtudiant.derniereConnexion);
                    }
                });
    }

    private void updateTextViews() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView mesCandidaturesView = findViewById(R.id.btn_candidature_en_cour);
                TextView mescandidatureRefuseView = findViewById(R.id.btn_candidature_refusé);
                TextView derniereConnexionView = findViewById(R.id.textView6);

                mesCandidaturesView.setText(String.valueOf(nbNonRefuse + " CANDIDATURES EN COURS"));
                mescandidatureRefuseView.setText(String.valueOf(nbRefuse + " CANDIDATURES REFUSÉES"));
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy 'à' HH'h'mm'm'ss's'", Locale.getDefault());

                try {
                    if(derniereConnexion!=null) {
                        Date parsedDate = inputFormat.parse(derniereConnexion);
                        String formattedDate = outputFormat.format(parsedDate);
                        derniereConnexionView.setText("Dernière connexion le " + formattedDate);
                    }
                    else{
                        System.out.println("Dernière connexion nulle");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }            }
        });
    }





    private void refreshCandidatures() {
        APIClient.getCandidatures(this, new ResultatAppel<CandidaturesResponse>() {
            @Override
            public void traiterResultat(CandidaturesResponse candidatures) {
                TextView nbCandidaturesView = findViewById(R.id.btn_candidature_en_cour);
                TextView nbCandidaturesRefuseesView = findViewById(R.id.btn_candidature_refusé);

                int count = 0;
                for (Candidature c : candidatures.candidatures) {
                    if (c.etatCandidature.equals("/api/etat_candidature/3")) {
                        count++;
                    }
                }
                nbCandidaturesRefuseesView.setText(nbRefuse + " CANDIDATURES REFUSÉES");
                nbCandidaturesView.setText(nbNonRefuse + " CANDIDATURES EN COURS");
            }

            @Override
            public void traiterErreur() {

            }
        });
    }
}