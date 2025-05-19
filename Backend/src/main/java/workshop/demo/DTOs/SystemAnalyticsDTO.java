package workshop.demo.DTOs;

import java.time.LocalDate;
import java.util.Map;

public class SystemAnalyticsDTO {
    private Map<LocalDate,Integer> loginsPerDay;
    private Map<LocalDate,Integer> logoutsPerDay;
    private Map<LocalDate,Integer> registerPerDay;
    private Map<LocalDate,Integer> purchasesPerDay;
    public SystemAnalyticsDTO(Map<LocalDate,Integer> loginsPerDay,Map<LocalDate,Integer> logoutsPerDay,
                              Map<LocalDate,Integer> registerPerDay,Map<LocalDate,Integer> purchasesPerDay){
        this.loginsPerDay = loginsPerDay;
        this.logoutsPerDay = logoutsPerDay;
        this.registerPerDay = registerPerDay;
        this.purchasesPerDay = purchasesPerDay;
    }

    public Map<LocalDate, Integer> getLoginsPerDay() {
        return loginsPerDay;
    }

    public void setLoginsPerDay(Map<LocalDate, Integer> loginsPerDay) {
        this.loginsPerDay = loginsPerDay;
    }

    public Map<LocalDate, Integer> getLogoutsPerDay() {
        return logoutsPerDay;
    }

    public void setLogoutsPerDay(Map<LocalDate, Integer> logoutsPerDay) {
        this.logoutsPerDay = logoutsPerDay;
    }

    public Map<LocalDate, Integer> getRegisterPerDay() {
        return registerPerDay;
    }

    public void setRegisterPerDay(Map<LocalDate, Integer> registerPerDay) {
        this.registerPerDay = registerPerDay;
    }

    public Map<LocalDate, Integer> getPurchasesPerDay() {
        return purchasesPerDay;
    }

    public void setPurchasesPerDay(Map<LocalDate, Integer> purchasesPerDay) {
        this.purchasesPerDay = purchasesPerDay;
    }
}
