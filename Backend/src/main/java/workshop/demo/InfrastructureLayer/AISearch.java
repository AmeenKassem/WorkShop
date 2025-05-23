package workshop.demo.InfrastructureLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;

import workshop.demo.ApplicationLayer.OrderService;

@Component
public class AISearch {

    private final String apiBaseUrl = "http://localhost:5000";
    private final RestTemplate restTemplate = new RestTemplate();
    private Thread worker = new Thread();
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private Object lock = new Object(); 

    private ConcurrentLinkedDeque<Runnable> tasks = new ConcurrentLinkedDeque<>();
    public AISearch(){
        worker = new Thread(()->{
            synchronized(worker){

                while (true) {
                    // while(tasks.isEmpty())
                        // try {
                        //     logger.info("There is no new pairs to train AI with");
                        //     worker.wait();
                        // } catch (InterruptedException e) {
                        //     // TODO Auto-generated catch block
                        //     e.printStackTrace();
                        // }
                    // logger.info("AI trainer woke up");
                    if(!tasks.isEmpty()) tasks.remove().run();
                }
            }
        });
        worker.start();
    }


    public void addPair(String string1, String string2, boolean same) {
        String url = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/addPair")
                .queryParam("string1", string1)
                .queryParam("string2", string2)
                .queryParam("same", same ? 1 : 0)
                .toUriString();

        try {
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
                    logger.info("new pair are added to the AI trainer: " + string1 + "," + string2 + " . got :"
                            + response.getBody());
                }

            };

            tasks.add(task);
            // synchronized(lock){
            //     worker.notifyAll();
            // }
        } catch (Exception e) {
            logger.info("something went wrong with training AI .\n" + e.getMessage());
        }
    }

    private void notifyWorker() {
        
    }

    public String checkPair(String input, String actualName) {
        String url = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/checkPair")
                .queryParam("input", input)
                .queryParam("actualName", actualName)
                .toUriString();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            return response.getBody();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public double getConfidence(String input, String actualString) {
        String jsonRes = checkPair(input, actualString);
        JsonObject obj = JsonParser.parseString(jsonRes).getAsJsonObject();
        return obj.get("confidence").getAsDouble();
    }

    public static void main(String[] args) {
        AISearch ai = new AISearch();
        String res = ai.checkPair("samsung", "android");
        System.out.println(res);

    }
}
