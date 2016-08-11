package es.parser;

import com.google.gson.annotations.Expose;

public class SearchResult {


    @Expose
    public final String thumbnail;
    @Expose
    public final String image;
    @Expose
    public final String dataCode;
    @Expose
    public final boolean isDoubleSide;


    public SearchResult(String src, String dataCode) {
        this.thumbnail = "http://db.fowtcg.us/" + src;
        this.image = thumbnail.replace("thumbs", "cards");
        this.dataCode = dataCode;
        isDoubleSide = src.contains("j.") || src.contains("sh.");
    }
}
