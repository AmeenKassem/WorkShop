package workshop.demo.DTOs;

public class UserDTO {
    public int id;
    //null for guest
    public String username;
    public int age;
    public Boolean isOnline; 
    public Boolean isAdmin; 

    public UserDTO(int id){
        this.id = id;
        this.username = null;
        this.isOnline = false;
        this.isAdmin = false;
        this.age = -1;
    }


    public UserDTO(int id , String username, int age , Boolean isOnline, Boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.age = age;
        this.isOnline = isOnline;
        this.isAdmin = isAdmin;
    }

}