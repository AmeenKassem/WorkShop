package workshop.demo.DTOs;

public class UserDTO {
    public int id;
    //null for guest
    public String username;
    private String password;
    public int age;
    private Boolean isOnline; 
    private Boolean isAdmin; 

    public UserDTO(int id){
        this.id = id;
        this.username = null;
        this.password = null;
        this.isOnline = false;
        this.isAdmin = false;
        this.age = -1;
    }


    public UserDTO(int id , String username, String password, int age , Boolean isOnline, Boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.age = age;
        this.isOnline = isOnline;
        this.isAdmin = isAdmin;
    }

}