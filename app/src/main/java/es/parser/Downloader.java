package es.parser;

import com.google.gson.Gson;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;

public class Downloader {

    public static void main(String... args) throws IOException {
        ArrayList<SearchResult> searchResults = CardFactory.parseCardsFromUrl("http://db.fowtcg.us/?do=search&exact=yes&orderby=setnum&format=New+Frontiers");
        ArrayList<CardModel> models = CardFactory.getModelFromCards(searchResults);

        ArrayList<String> traits = new ArrayList<String>();
        ArrayList<String> types = new ArrayList<String>();
        ArrayList<String> sets = new ArrayList<String>();

        for (CardModel model : models) {
            if (model.racesOrTraits != null) {
                for (String str : model.racesOrTraits) {
                    if (!traits.contains(str)) {
                        traits.add(str);
                    }
                }
            }
            if (!types.contains(model.type)) {
                types.add(model.type);
            }
            if (!sets.contains(model.set)) {
                sets.add(model.set);
            }
        }
        Gson gson = new Gson();
        String modelsAsString = gson.toJson(models);
        String typesAsString = gson.toJson(types);
        String traitsAsString = gson.toJson(traits);
        String setsAsString = gson.toJson(sets);


        File allCardsFile = new File("allCards.json");
        File traitsFile = new File("traits.json");
        File typesFile = new File("types.json");
        File setsFile = new File("sets.json");

        writeToFile(modelsAsString, allCardsFile);
        writeToFile(traitsAsString, traitsFile);
        writeToFile(typesAsString, typesFile);
        writeToFile(setsAsString, setsFile);
    }


    private static void writeToFile(String data, File file) {
        try {
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(data);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
