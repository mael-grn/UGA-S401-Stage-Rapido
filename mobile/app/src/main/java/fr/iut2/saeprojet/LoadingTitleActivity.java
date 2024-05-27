package fr.iut2.saeprojet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

public class LoadingTitleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_title);

        // Créer un compte à rebours de 3 secondes
        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Ne rien faire pendant le compte à rebours
            }

            public void onFinish() {
                // Après 3 secondes, lancer MainActivity
                Intent intent = new Intent(LoadingTitleActivity.this, LoginActivity.class);
                // Effacer les flags de l'intent
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Terminer cette activité pour ne pas revenir ici en arrière
            }
        }.start();
    }
}