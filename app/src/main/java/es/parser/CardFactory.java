package es.parser;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class CardFactory {

    private final static String TAG_BEGIN_CARD = "<!-- Card -->";
    private final static String TAG_END_CARD = "<!-- /Card -->";

    private final static String TAG_BEGIN_CARD_INFO = "<!-- CARD INFO -->";
    private final static String TAG_END_CARD_INFO = "<!-- /CARD INFO -->";


    public static CardModel generateCardModel(SearchResult searchResult) throws IOException {
        String dataCode = searchResult.dataCode.replace(" ", "+");
        String url = String.format("http://db.fowtcg.us/?p=card&code=%s", dataCode);
        String html = Jsoup.connect(url).get().html();

        String[] results = StringUtils.substringsBetween(html, TAG_BEGIN_CARD_INFO, TAG_END_CARD_INFO);

        html = results[searchResult.isDoubleSide ? 1 : 0];

        String name = getContentOfTag(html, "Name");
        String cost = formatCosts(getContentOfTag(html, "Cost"), true);
        String attribute = getContentOfTag(html, "Attribute");
        String type = getContentOfTag(html, "Type");
        String race = getContentOfTag(html, "Race");
        String trait = getContentOfTag(html, "Trait");
        String text = formatText(getContentOfTag(html, "Text"));
        String set = getContentOfTag(html, "Set");
        String code = getContentOfTag(html, "Code");
        String rarity = getContentOfTag(html, "Rarity");
        String flavorText = getContentOfTag(html, "Flavor");
        if (flavorText != null && flavorText.contains("(none)"))
            flavorText = null;
        if (race != null && race.contains("(none)"))
            race = null;
        if (trait != null && trait.contains("(none)"))
            trait = null;


        String atkDef = getContentOfTag(html, "ATK/DEF");
        final String atk, def;
        if (atkDef != null) {
            String[] stats = atkDef.split("/");
            atk = stats[0].trim();
            def = stats[1].trim();
        } else {
            atk = null;
            def = null;
        }
        ArrayList<String> racesOrTraits = new ArrayList<String>();

        for (String str : new String[]{race, trait}) {
            if (str != null && !str.equals("(none)")) {
                if (str.contains("/")) {
                    Collections.addAll(racesOrTraits, str.split("/"));
                } else {
                    racesOrTraits.add(str);
                }
            }
        }


        String thumbnailImage = searchResult.thumbnail;
        String cardImage = searchResult.image;


        return new CardModel(name, cost, attribute, type, racesOrTraits, text, set, code, rarity, flavorText, atk, def, thumbnailImage, cardImage);
    }

    private static String getContentOfTag(String html, String tag) {
        String realTag = tag + "\n";
        if (!html.contains(realTag)) {
            return null;
        }
        String substring = html.substring(html.indexOf(realTag));
        String str = getStringBetweenTags(substring, "<div class=\"col-xs-9 col-sm-9 prop-value\">", "</div>").trim();
        //Document document = new Document(str);
        //str = document.text();
        return str;
    }


    private static SearchResult parseFromHtml(String html) {
        String src = getStringBetweenTags(html, "<img src=\"", "\"");
        String dataCode = getStringBetweenTags(html, "data-code=\"", "\"");
        return new SearchResult(src, dataCode);
    }

    public static ArrayList<SearchResult> parseCardsFromUrl(String url) throws IOException {
        return parseCardsFromHtml(Jsoup.connect(url).get().html());
    }

    private static ArrayList<SearchResult> parseCardsFromHtml(String html) {
        ArrayList<SearchResult> list = new ArrayList<SearchResult>();

        while (html.contains(TAG_BEGIN_CARD) && html.contains(TAG_END_CARD)) {
            String substring = getStringBetweenTags(html, TAG_BEGIN_CARD, TAG_END_CARD);
            list.add(parseFromHtml(substring));
            html = html.substring(html.indexOf(TAG_END_CARD) + 1);
        }
        return list;
    }

    public static ArrayList<CardModel> getModelFromCards(ArrayList<SearchResult> cardsFromList) throws IOException {
        ArrayList<CardModel> models = new ArrayList<CardModel>();


        for (int i = 0; i < cardsFromList.size(); i++) {
            SearchResult result = cardsFromList.get(i);
            models.add(generateCardModel(result));
            System.out.println((i + 1) + " out of " + cardsFromList.size());
        }

        return models;
    }

    private static String getStringBetweenTags(String str, String beginningTag, String endTag) {
        String string = StringUtils.substringBetween(str, beginningTag, endTag);
        if (StringUtils.equals(str, "(none)")) {
            return null;
        }
        return string;
    }


    private static String formatCosts(String costs, boolean removeLineBreaks) {
        if (costs == null) return null;

        costs = costs.replace("         ", "");
        costs = costs.replace(" />", "/>");
        costs = costs.replace(" >", ">");
        if (removeLineBreaks)
            costs = costs.replace("\n", "");
        costs = costs.replace(" alt='Dark' ", "");
        costs = costs.replace(" alt='Light' ", "");
        costs = costs.replace(" alt='Fire' ", "");
        costs = costs.replace(" alt='Water' ", "");
        costs = costs.replace(" alt='Wind' ", "");
        costs = costs.replace(" alt=\"Dark\" ", "");
        costs = costs.replace(" alt=\"Light\" ", "");
        costs = costs.replace(" alt=\"Fire\" ", "");
        costs = costs.replace(" alt=\"Water\" ", "");
        costs = costs.replace(" alt=\"Wind\" ", "");
        costs = costs.replace(" alt='Dark'", "");
        costs = costs.replace(" alt='Light'", "");
        costs = costs.replace(" alt='Fire'", "");
        costs = costs.replace(" alt='Water'", "");
        costs = costs.replace(" alt='Wind'", "");
        costs = costs.replace(" alt=\"Dark\"", "");
        costs = costs.replace(" alt=\"Light\"", "");
        costs = costs.replace(" alt=\"Fire\"", "");
        costs = costs.replace(" alt=\"Water\"", "");
        costs = costs.replace(" alt=\"Wind\"", "");

        costs = costs.replace(" class='mark' ", " ");
        costs = costs.replace(" class='costicons' ", " ");
        costs = costs.replace(" class=\"mark\" ", " ");
        costs = costs.replace(" class=\"costicons\" ", " ");

        String[] voids = new String[]{
                "<img src='_images/icons/free%s.png'/>\n ",
                "<img src=\"_images/icons/free%s.png\"/>\n ",
                "<img src='_images/icons/free%s.png'>\n ",
                "<img src=\"_images/icons/free%s.png\">\n ",

                "<img src='_images/icons/free%s.png'/>",
                "<img src=\"_images/icons/free%s.png\"/>",
                "<img src='_images/icons/free%s.png'>",
                "<img src=\"_images/icons/free%s.png\">"
        };
        String[] costArray = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "x"};
        for (String str : voids) {
            for (String cost : costArray) {
                String upper = cost.toUpperCase();
                String replacement = String.format(str, cost);
                costs = costs.replace(replacement, "$·" + upper);
            }
        }
        String[] colors = new String[]{
                "<img src='_images/icons/%s.png'/> ",
                "<img src=\"_images/icons/%s.png\"/> ",
                "<img src='_images/icons/%s.png'> ",
                "<img src=\"_images/icons/%s.png\"> ",

                "<img src='_images/icons/%s.png'/>",
                "<img src=\"_images/icons/%s.png\"/>",
                "<img src='_images/icons/%s.png'>",
                "<img src=\"_images/icons/%s.png\">"
        };

        String[] colorArray = new String[]{"w", "r", "u", "g", "b", "m", "v"};
        for (String phrase : colors) {
            for (String color : colorArray) {
                String upper = color.toUpperCase();
                String replacement = String.format(phrase, color);
                costs = costs.replace(replacement, "$·" + upper);
            }
        }

        while (costs.contains("  ")) {
            costs = costs.replace("  ", " ");
        }

        costs = costs.replace("\n $·", "");
        costs = costs.replace("\n$·", "");
        costs = costs.replace(" $·", "");
        costs = costs.replace("$·", "");

        return costs;
    }

    private static String formatText(String text) {
        if (text == null) return null;
        text = cleanup(text);
        text = text.replace("<span class='mark_abilities'>Continuous</span>", "Continuous:");
        text = text.replace("<span class='mark_abilities'>Enter</span>", "Enter:");
        text = text.replace("<span class='mark_abilities'>Activate</span>", "Activate.");
        text = text.replace("<span class=\"mark_abilities\">Continuous</span>", "Continuous:");
        text = text.replace("<span class=\"mark_abilities\">Enter</span>", "Enter:");
        text = text.replace("<span class=\"mark_abilities\">Activate</span>", "Activate.");
        text = text.replace("<span class='mark_break'>Break</span>", "Break:");
        text = text.replace("<span class=\"mark_break\">Break</span>", "Break:");
        text = text.replace("<br/>", "\n");
        text = text.replace("<br>", "\n");
        text = text.replace("<hr class=\"card-hr\">", "\n");
        text = text.replace("<hr class='card-hr'>", "\n");
        text = text.replace("<img class=\"mark\" src=\"_images/icons/rest.png\">", " Rest ");
        text = text.replace("<img class='mark' src='_images/icons/rest.png'>", " Rest ");
        text = text.replace("<span class='mark_abilities'>", "");
        text = text.replace("<span class=\"mark_abilities\">", "");
        text = text.replace("<span class='mark_skills'>", "");
        text = text.replace("<span class=\"mark_skills\">", "");
        text = text.replace("<span class='mark_errata'>", "");
        text = text.replace("<span class=\"mark_errata\">", "");
        text = text.replace("<span class='mark_breaktext'>", "");
        text = text.replace("<span class=\"mark_breaktext\">", "");
        text = text.replace("<j esonators>", "J/Resonators");
        text = text.replace("</j>", "");
        text = text.replace("</span>", "");

        text = cleanup(text);

        text = formatCosts(text, false);

        text = text.replace("\n Rest ", " Rest ");
        text = text.replace("\nRest ", " Rest ");
        text = text.replace("Break: \n", "Break: ");
        text = text.replace(" :", ":");
        text = text.replace("Pay", "Pay ");
        text = text.replace("pay", "pay ");
        text = text.replace("pay s", "pays ");
        text = text.replace("plays1", "pays 1");
        text = text.replace("Incarnation", "Incarnation ");

        String[] colors = {"W", "R", "U", "G", "B", "M", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "X"};
        for (String color1 : colors) {
            for (String color2 : colors) {
                String toReplace = String.format("%sor%s", color1, color2);
                String replacement = String.format(" %s or %s ", color1, color2);
                text = text.replace(toReplace, replacement);
            }
            text = text.replace("Barrier" + color1, "Barrier " + color1 + " ");
            text = text.replace("Energize" + color1, "Energize " + color1);
            text = text.replace("Judgment" + color1, "Judgment " + color1);
            text = text.replace("J-Activate" + color1, "J-Activate " + color1);
            text = text.replace("Produce" + color1, "Produce " + color1);
            text = text.replace("Awakening" + color1, "Awakening " + color1);
            text = text.replace(color1 + "less ", color1 + " less ");
            text = text.replace(color1 + ":", " " + color1 + ":");
        }

        text = cleanup(text);

        text = text.replace("W,R,U,G, orB.", "W, R, U, G or B.");

        text = cleanup(text);

        return text;
    }

    private static String cleanup(String text) {
        while (text.contains("\n \n") || text.contains("\n\n") || text.contains("  ") || text.contains("\n ") || text.contains(" ,") || text.contains(" .")) {
            text = text.replace("\n \n", "\n").replace("\n\n", "\n").replace("  ", " ").replace("\n ", "\n").replace(" ,", ",").replace(" .", ".");
        }
        text = text.replace(" \n", " ");
        text = text.trim();
        return text;
    }


}
