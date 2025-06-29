package workshop.demo.ApplicationLayer.DataInitilizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DomainLayer.AppSettings.AppSettingsEntity;

@Service
public class InitDataService extends ManagerDataInit {

    @Autowired
    private AdminParser adminParser;
    @Autowired
    private StoreParser storeParser;
    @Autowired
    private UserParser userParser;

    public String readFileAsString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    public String init(String key, String userName, String password) throws Exception {
        String data = readFileAsString("Backend\\src\\main\\resources\\dataToInit.txt");
        List<List<String>> cons = createCons(data);
        line = 1;
        error = false;
        output = "";
        for (List<String> construction : cons) {
            try {
                excute(construction);
                checkErrors();
            } catch (Exception e) {
                error = true;
                log("line " + line + " :" + e.getMessage());
            }
            line++;
        }
        if (error) {
            clearData();
            shutDown();
        }
        return output;
    }

    private void checkErrors() {
        try {
            Thread.currentThread().sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void clearData() {
        dataBase.wipeDatabase();
        log("all data cleared!");
    }

    protected void shutDown() {
        AppSettingsEntity settings = appSettingsRepository.findById(1L)
                .orElse(new AppSettingsEntity());
        settings.setInitialized(false);
        appSettingsRepository.save(settings);
    }

    protected void excute(List<String> construction) throws Exception {
        if (error)
            return;
        List<String> toSend = construction.subList(1, construction.size());
        switch (construction.get(0).toLowerCase()) {
            case "clear-all":
                clearData();
                break;
            case "admin":
                adminParser.admin(toSend);
                break;
            case "user":
                userParser.user(toSend);
                break;
            case "store":
                storeParser.store(toSend);
                break;
            case "wait":
                justWait(toSend);
                break;
            default:
                log("syntax error on line " + line + " :Unkown (" + construction.get(0) + ")");
                error = true;
                break;
        }
    }

    private void justWait(List<String> toSend) {
        long ms = Long.parseLong(toSend.get(0));
        try {
            Thread.currentThread().sleep(ms);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected static List<List<String>> createCons(String data) {
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("Input data is null or empty.");
        }

        List<List<String>> result = new ArrayList<>();
        String[] lines = data.split("\\r?\\n");

        for (String line : lines) {
            line = line.trim();

            // Skip commented lines
            if (line.startsWith("//") || line.isEmpty()) {
                continue;
            }

            // Check if line ends with ";"
            if (!line.endsWith(";")) {
                throw new IllegalArgumentException("Line does not end with ';': " + line);
            }

            // Remove the ending ';' and split by whitespace
            String content = line.substring(0, line.length() - 1).trim();
            if (!content.isEmpty()) {
                String[] words = content.split("\\s+");

                result.add(Arrays.asList(words));
            }
        }

        return result;
    }

}
