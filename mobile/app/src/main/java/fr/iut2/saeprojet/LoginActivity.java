package fr.iut2.saeprojet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

import fr.iut2.saeprojet.api.APIClient;
import fr.iut2.saeprojet.api.APIService;
import fr.iut2.saeprojet.api.EtatCandidatureEnum;
import fr.iut2.saeprojet.api.ResultatAppel;
import fr.iut2.saeprojet.entity.Auth;
import fr.iut2.saeprojet.entity.CompteEtudiant;
import fr.iut2.saeprojet.entity.CompteEtudiantRequest;
import fr.iut2.saeprojet.entity.ComptesEtudiantsResponse;
import fr.iut2.saeprojet.entity.EtatCandidature;
import fr.iut2.saeprojet.entity.EtatCandidaturesResponse;
import fr.iut2.saeprojet.entity.EtatOffre;
import fr.iut2.saeprojet.entity.EtatOffresResponse;
import fr.iut2.saeprojet.entity.EtatRecherche;
import fr.iut2.saeprojet.entity.EtatRecherchesResponse;
import fr.iut2.saeprojet.entity.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends StageAppActivity {

    // View
    private EditText usernameView;
    private EditText passwordView;
    private Button loginView;

    private boolean estAdmin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Init view
        usernameView = findViewById(R.id.username);
        passwordView = findViewById(R.id.password);
        loginView = findViewById(R.id.login);
        estAdmin = false;
        //
        loginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void login() {

        String login = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        Auth auth = new Auth(login, password);

        Call<LoginResponse> call = apiInterface.login(auth);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                if (response.isSuccessful()) {
                    setAuthData(login, response.body().token);
                    getInformationEtudiant(login);
                } else {
                    Log.i("Reponse non successful", response.message());
                    Toast.makeText(LoginActivity.this, "Identifiants incorrects", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Un problème est survenu", Toast.LENGTH_SHORT).show();
                call.cancel();
                Log.e("TAG",t.getMessage());
            }
        });
    }

    private void getInformationEtudiant(String login) {
        String token = getToken();

        //
        Call<ComptesEtudiantsResponse> call = apiInterface.doGetCompteEtudiants("Bearer " + token);
        call.enqueue(new Callback<ComptesEtudiantsResponse>() {
            @Override
            public void onResponse(Call<ComptesEtudiantsResponse> call, Response<ComptesEtudiantsResponse> response) {

                if (response.isSuccessful()) {

                    for (CompteEtudiant compteEtudiant : response.body().compteEtudiants) {


                        if (compteEtudiant.login.equals(login)) {
                            List<String> roles = (compteEtudiant.roles);
                            System.out.println("roles size : " + roles.size() + " roles 1 : " + roles.get(0) + " " + roles.get(1));
                            if (roles.contains("ROLE_ETUDIANT") && roles.size() == 2) {
                                // Le compte a uniquement le rôle "ROLE_ETUDIANT" ( et ROLE_USER comme tous )
                                setCompteId(compteEtudiant.id, compteEtudiant._id);
                                updateDerniereConnexion(compteEtudiant);
                            } else {
                                estAdmin = true;
                                Toast.makeText(LoginActivity.this, "Désolé, un compte Admin ne peut pas se connecter.", Toast.LENGTH_SHORT).show();

                            }
                            break;
                        }
                    }

                    if(!estAdmin) {
                        //
                        // Rediriger l'utilisateur vers l'écran principal et effacer la pile d'activités précédentes
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        Toast.makeText(LoginActivity.this, "Bienvenue " + login + "!", Toast.LENGTH_SHORT).show();
                        finish(); // Fin de l'activité actuelle pour empêcher le retour en arrière
                    }

                } else {
                    Toast.makeText(LoginActivity.this, "Un problème est survenu.", Toast.LENGTH_SHORT).show();
                }
            }

            private void updateDerniereConnexion(CompteEtudiant compteEtudiant) {
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
//                APIClient.updateDerniereConnexion(LoginActivity.this, compteEtudiant.id, compteEtudiantReq, new ResultatAppel<CompteEtudiant>() {
//                    @Override
//                    public void traiterResultat(CompteEtudiant response) {
//                        System.out.println("RESULTAT APPEL : VALIDE " + " dernier co : " + compteEtudiant.derniereConnexion);
//                    }
//
//                    @Override
//                    public void traiterErreur() {
//                        System.out.println("RESULTAT APPEL : ERREUR " + " dernier co : " + compteEtudiant.derniereConnexion);
//                    }
//                });
            }

            @Override
            public void onFailure(Call<ComptesEtudiantsResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Un problème est survenu", Toast.LENGTH_SHORT).show();
                call.cancel();
                Log.e("TAG",t.getMessage());

            }
        });

        APIClient.getEtatRecherches(this, new ResultatAppel<EtatRecherchesResponse>() {
            @Override
            public void traiterResultat(EtatRecherchesResponse liste) {
                HashMap<String, String> entries = new HashMap<>();
                for(EtatRecherche etat : liste.etatRecherches) {
                    entries.put(etat._id, etat.descriptif);
                }
                LoginActivity.this.setEnumValues(entries);
            }

            @Override
            public void traiterErreur() {

            }
        });

        APIClient.getEtatOffres(this, new ResultatAppel<EtatOffresResponse>() {
            @Override
            public void traiterResultat(EtatOffresResponse liste) {
                HashMap<String, String> entries = new HashMap<>();
                for(EtatOffre etat : liste.etatOffres) {
                    entries.put(etat._id, etat.descriptif);
                }
                LoginActivity.this.setEnumValues(entries);
            }

            @Override
            public void traiterErreur() {

            }
        });

        APIClient.getEtatCandidatures(this, new ResultatAppel<EtatCandidaturesResponse>() {
            @Override
            public void traiterResultat(EtatCandidaturesResponse liste) {
                HashMap<String, String> entries = new HashMap<>();
                for(EtatCandidature etat : liste.etatCandidatures) {
                    entries.put(etat._id, etat.descriptif);
                }
                LoginActivity.this.setEnumValues(entries);
            }

            @Override
            public void traiterErreur() {

            }
        });
    }
}