package es.parser;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class CardModel {

    @Expose
    public final String name;
    @Expose
    public final String cost;
    @Expose
    public final String attribute;
    @Expose
    public final String type;
    @Expose
    public final ArrayList<String> racesOrTraits;
    @Expose
    public final String text;
    @Expose
    public final String set;
    @Expose
    public final String code;
    @Expose
    public final String rarity;
    @Expose
    public final String flavorText;
    @Expose
    public final String atk;
    @Expose
    public final String def;
    @Expose
    public final String thumbnailImage;
    @Expose
    public final String cardImage;

    public CardModel(String name, String cost, String attribute, String type, ArrayList<String> racesOrTraits, String text, String set, String code, String rarity, String flavorText, String atk, String def, String thumbnailImage, String cardImage) {
        this.name = name;
        this.cost = cost;
        this.attribute = attribute;
        this.type = type;
        this.racesOrTraits = racesOrTraits;
        this.text = text;
        this.set = set;
        this.code = code;
        this.rarity = rarity;
        this.flavorText = flavorText;
        this.atk = atk;
        this.def = def;
        this.thumbnailImage = thumbnailImage;
        this.cardImage = cardImage;
    }
}
