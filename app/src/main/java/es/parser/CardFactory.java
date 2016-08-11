package es.parser;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

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
        String cost = formatCosts(getContentOfTag(html, "Cost"));
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

        if (race != null && !race.equals("(none)")) {
            if (race.contains("/")) {
                for (String str : race.split("/")) {
                    racesOrTraits.add(str);
                }
            } else {
                racesOrTraits.add(race);
            }
        }
        if (trait != null && !trait.equals("(none)")) {
            if (trait.contains("/")) {
                for (String str : trait.split("/")) {
                    racesOrTraits.add(str);
                }
            } else {
                racesOrTraits.add(trait);
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
            System.out.println(i * 100f / cardsFromList.size() + "%");
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


    private static String formatCosts(String costs) {
        if (costs == null) return null;

        costs = costs.replace("         ", "");
        costs = costs.replace(" />", "/>");
        costs = costs.replace(" >", ">");
        costs = costs.replace("\n", "");

        costs = costs.replace(" alt='Dark'", "");
        costs = costs.replace(" alt='Light'", "");
        costs = costs.replace(" alt='Fire'", "");
        costs = costs.replace(" alt='Water'", "");
        costs = costs.replace(" alt='Wind'", "");
        costs = costs.replace(" class='mark' ", " ");
        costs = costs.replace(" class='costicons' ", " ");
        costs = costs.replace(" alt=\"Dark\"", "");
        costs = costs.replace(" alt=\"Light\"", "");
        costs = costs.replace(" alt=\"Fire\"", "");
        costs = costs.replace(" alt=\"Water\"", "");
        costs = costs.replace(" alt=\"Wind\"", "");
        costs = costs.replace(" class=\"mark\" ", " ");
        costs = costs.replace(" class=\"costicons\" ", " ");

        String[] voids = new String[]{
                "<img class='costicons' src='_images/icons/free%s.png'/>",
                "<img class=\"costicons\" src=\"_images/icons/free%s.png\"/>",
                "<img class='costicons' src='_images/icons/free%s.png'>",
                "<img class=\"costicons\" src=\"_images/icons/free%s.png\">",
                "<img class='mark' src='_images/icons/free%s.png'/>",
                "<img class=\"mark\" src=\"_images/icons/free%s.png\"/>",
                "<img class='mark' src='_images/icons/free%s.png'>",
                "<img class=\"mark\" src=\"_images/icons/free%s.png\">",
                "\n<img class='costicons' src='_images/icons/free%s.png'/>",
                "\n<img class=\"costicons\" src=\"_images/icons/free%s.png\"/>",
                "\n<img class='costicons' src='_images/icons/free%s.png'>",
                "\n<img class=\"costicons\" src=\"_images/icons/free%s.png\">",
                "\n<img class='mark' src='_images/icons/free%s.png'/>",
                "\n<img class=\"mark\" src=\"_images/icons/free%s.png\"/>",
                "\n<img class='mark' src='_images/icons/free%s.png'>",
                "\n<img class=\"mark\" src=\"_images/icons/free%s.png\">",
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
                costs = costs.replace(replacement, upper);
            }
        }
        for (String str : voids) {
            costs = costs.replace("\n" + str, str);
        }
        String[] colors = new String[]{
                "<img class='costicons' src='_images/icons/%s.png'/>",
                "<img class=\"costicons\" src=\"_images/icons/%s.png\"/>",
                "<img class='costicons' src='_images/icons/%s.png'>",
                "<img class=\"costicons\" src=\"_images/icons/%s.png\">",
                "<img class='mark' src='_images/icons/%s.png'/>",
                "<img class=\"mark\" src=\"_images/icons/%s.png\"/>",
                "<img class='mark' src='_images/icons/%s.png'>",
                "<img class=\"mark\" src=\"_images/icons/%s.png\">",
                "\n<img class='costicons' src='_images/icons/%s.png'/>",
                "\n<img class=\"costicons\" src=\"_images/icons/%s.png\"/>",
                "\n<img class='costicons' src='_images/icons/%s.png'>",
                "\n<img class=\"costicons\" src=\"_images/icons/%s.png\">",
                "\n<img class='mark' src='_images/icons/%s.png'/>",
                "\n<img class=\"mark\" src=\"_images/icons/%s.png\"/>",
                "\n<img class='mark' src='_images/icons/%s.png'>",
                "\n<img class=\"mark\" src=\"_images/icons/%s.png\">",
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
                costs = costs.replace(replacement, upper);
            }
        }

        for (String str : colors) {
            costs = costs.replace("\n" + str, str);
        }

        return costs;
    }

    private static String formatText(String text) {
        if (text == null) return null;

        text = text.replace("<span class='mark_abilities'>Continuous</span>", "Continuous:");
        text = text.replace("<span class='mark_abilities'>Enter</span>", "Enter:");
        text = text.replace("<span class='mark_abilities'>Activate</span>", "Activate.");
        text = text.replace("<span class=\"mark_abilities\">Continuous</span>", "Continuous:");
        text = text.replace("<span class=\"mark_abilities\">Enter</span>", "Enter:");
        text = text.replace("<span class=\"mark_abilities\">Activate</span>", "Activate.");
        text = formatCosts(text);
        text = text.replace("<br/>", "\n");
        text = text.replace("<br>", "\n");
        text = text.replace("<hr class=\"card-hr\">", "\n");
        text = text.replace("<img src=\"_images/icons/rest.png\">", "Rest");
        text = text.replace("<span class='mark_abilities'>", "");
        text = text.replace("<span class=\"mark_abilities\">", "");
        text = text.replace("<span class='mark_skills'>", "");
        text = text.replace("<span class=\"mark_skills\">", "");
        text = text.replace("<span class='mark_errata'>", "");
        text = text.replace("<span class=\"mark_errata\">", "");
        text = text.replace("<j esonators>", "J/Resonators");
        text = text.replace("</j>", "");
        text = text.replace("</span>", "");
        while (text.contains("  ")) {
            text = text.replace("  ", " ");
        }
        return text;
    }


}
