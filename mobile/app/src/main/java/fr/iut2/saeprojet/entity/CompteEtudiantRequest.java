package fr.iut2.saeprojet.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CompteEtudiantRequest  {


        @SerializedName("login")
        public String login;
        @SerializedName("roles")
        public List<String> roles;
        @SerializedName("password")
        public String password;
        @SerializedName("parcours")
        public String parcours;
        @SerializedName("derniereConnexion")
        @Nullable
        public String derniereConnexion;
        @SerializedName("etatRecherche")
        public String etatRecherche;
        @SerializedName("etudiant")
        public String etudiant;




}
