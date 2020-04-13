package nl.underkoen.browniebot;

import com.google.gson.Gson;
import net.dv8tion.jda.api.entities.User;
import nl.underkoen.browniebot.utils.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Under_Koen on 01/04/2020.
 */
public class BrowniePointLedger {
    private static final Gson GSON = new Gson();

    private Ledger ledger = new Ledger();
    private File saveFile;

    public BrowniePointLedger(File saveFile) {
        this.saveFile = saveFile;
    }

    public void read() {
        try {
            String content = FileUtil.getAllContent(saveFile);
            ledger = GSON.fromJson(content, Ledger.class);
        } catch (FileNotFoundException ignored) {
            //Should not happen
        }
    }

    public int getPoints(User user) {
        if (ledger.points.containsKey(user.getId())) return ledger.points.get(user.getId());
        ledger.points.put(user.getId(), 10);
        save();
        return 10;
    }

    public int addPoints(User user, int points) {
        int i = ledger.points.getOrDefault(user.getId(), 10) + points;
        ledger.points.put(user.getId(), i);
        save();
        return i;
    }

    public int removePoints(User user, int points) {
        return addPoints(user, -points);
    }

    public void setPoints(User user, int points) {
        ledger.points.put(user.getId(), points);
        save();
    }

    Ledger getLedger() {
        return ledger;
    }

    public void save() {
        try {
            FileUtil.writeContent(saveFile, GSON.toJson(ledger, Ledger.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class Ledger {
        Map<String, Integer> points = new HashMap<>();
    }
}
