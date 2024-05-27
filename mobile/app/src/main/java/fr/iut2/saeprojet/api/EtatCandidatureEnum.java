package fr.iut2.saeprojet.api;

import fr.iut2.saeprojet.R;

public enum EtatCandidatureEnum {
    OFFRE_RETENUE("/api/etat_candidatures/1", "A Envoyer"),
    ATTENTE_REPONSE("/api/etat_candidatures/2", "Attente Réponse"),
    REFUSEE("/api/etat_candidatures/3", "Refusée"),
    ATTENTE_RDV("/api/etat_candidatures/4", "Attente RDV"),
    ATTENTE_ENTRETIEN("/api/etat_candidatures/5", "Attente Entretien"),
    ACCEPTEE("/api/etat_candidatures/6", "Acceptée");


    private String _id;
    private String label;
    private EtatCandidatureEnum(String _id , String label) {
        this._id = _id;
        this.label = label;
    }




    public String get_id() {
        return this._id;
    }

    public String toString() {
        return this.label;
    }
}
