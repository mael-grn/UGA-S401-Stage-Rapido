package fr.iut2.saeprojet;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import fr.iut2.saeprojet.api.APIClient;
import fr.iut2.saeprojet.api.APIService;
import fr.iut2.saeprojet.api.EtatCandidatureEnum;
import fr.iut2.saeprojet.entity.Candidature;
import fr.iut2.saeprojet.entity.EtatsCandidatures;
import fr.iut2.saeprojet.entity.Offre;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CandidatureAdapter extends ArrayAdapter<Candidature> {

    // API
    private APIService apiInterface;

    private Context mContext; // Ajout du contexte

    public CandidatureAdapter(Context mCtx, List<Candidature> candidatures) {
        super(mCtx, R.layout.template_candidature, candidatures);
        this.mContext = mCtx; // Initialisation du contexte
        // Chargement de l'API
        apiInterface = APIClient.getAPIService();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Récupération de la candidature
        final Candidature candidature = getItem(position);

        // Si l'activité appelante est une instance de ListCandidaturesActivityRefuse
        if (mContext instanceof ListCandidaturesActivityRefuse) {
            // Si la candidature est refusée, retourner une vue vide
            if (candidature.getEtatCandidatureId() != 3) {
                return new View(mContext); // Retourner une vue vide
            }
        } else {
            // Si la candidature est refusée, passer à la candidature suivante
            if (candidature.getEtatCandidatureId() == 3) {
                if (convertView != null) {
                    return convertView;
                } else {
                    return new View(mContext); // Retourner une vue vide
                }
            }
        }

        // Charge le template XML
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View rowView = inflater.inflate(R.layout.template_candidature, parent, false);

        // Récupération des objets graphiques dans le template
        TextView intituleView = rowView.findViewById(R.id.intitule);
        TextView etatCandidatureView = rowView.findViewById(R.id.etatCandidature);

        // Actualisation des informations de la candidature
        refreshMesInformations(candidature, intituleView, etatCandidatureView);

        return rowView;
    }


    private void refreshMesInformations(Candidature candidature, TextView intituleView, TextView etatCandidatureView) {
        SharedPreferences sharedPref = getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        String token = sharedPref.getString(getContext().getString(R.string.token_key), "no token");

        Call<Offre> call = apiInterface.doGetOffre("Bearer " + token, candidature.getOffreId());
        call.enqueue(new Callback<Offre>() {
            @Override
            public void onResponse(Call<Offre> call, Response<Offre> response) {
                // Récupérer l'intitulé de l'offre
                String intitule = response.body().intitule;
                String etatOffre = response.body().etatOffre;

                etatCandidatureView.setText(EtatsCandidatures.etatsCandidatureInverse.get(candidature.getEtatCandidatureId()));

                if (intitule.length() > 10) {
                    intitule = intitule.substring(0, 9) + "...";
                }

                // Mettre à jour l'intitulé de la candidature
                intituleView.setText(intitule);
            }

            @Override
            public void onFailure(Call<Offre> call, Throwable t) {
                call.cancel();
                Log.e("TAG", t.getMessage());
            }
        });
    }

}
