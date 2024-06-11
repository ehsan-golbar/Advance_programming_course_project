import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        File information = new File("G:\\Ehsan\\Ehsan\\sources\\term_2\\ap\\finalProj\\information");//address of project + \\information
        if (!information.exists()) {
            information.mkdir();
        }
        File [] userInformation = information.listFiles();

        ArrayList<User> users = new ArrayList<User>();
        if (userInformation != null){
            for (int i = 0; i < userInformation.length; i++) {
                try{
                    FileInputStream fin = new FileInputStream(userInformation[i].getPath());
                    ObjectInputStream in = new ObjectInputStream(fin);
                    users.add((User)in.readObject());

                    fin.close();
                    in.close();
                }catch (FileNotFoundException f){
                    System.out.println(f);
                }catch (IOException e){
                    System.out.println(e);
                }catch (ClassNotFoundException c){
                    System.out.println(c);
                }
            }
        }

        System.out.println(users.size());

        for (int i = 0; i < users.size(); i++) {
            for (int j = 0; j < users.get(i).getUserProf().getUserAd().size(); j++) {
                System.out.println(users.get(i).getUserProf().getUserAd().get(j).getAdId());
            }
        }

        UserAction[] userActions = new UserAction[2];// same to Socket numbers , choose a desiered number
        try {
            ServerSocket server = new ServerSocket(8888 );
            Socket[] sockets = new Socket[2];
            for (int i = 0; i < sockets.length; i++) {
                sockets[i] = server.accept();
                DataOutputStream dou = new DataOutputStream(sockets[i].getOutputStream());
                DataInputStream din = new DataInputStream(sockets[i].getInputStream());

                userActions[i] = new UserAction(sockets[i],users);
                userActions[i].start();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        for (int i = 0; i < userActions.length; i++) {
            try {
                userActions[i].join();
            }catch (InterruptedException e){
                System.out.println(e);
            }
        }
    if (userInformation != null){
        for (int i = 0; i < userInformation.length; i++) {
           // System.out.println(userInformation[i].getName());
            userInformation[i].delete();

        }
    }



        for (int i = 0; i < users.size() ; i++) {
            File f = new File(information.getPath() + "\\" + i + ".txt");
            try {
                f.createNewFile();
            }catch (IOException e){
                System.out.println(e );
            }

        }

        userInformation = information.listFiles();

        if(userInformation != null){
            for (int i = 0 ; i < userInformation.length ; i++){
                try{
                    FileOutputStream fos = new FileOutputStream(userInformation[i].getPath());
                    ObjectOutputStream out = new ObjectOutputStream(fos);

                    out.writeObject(users.get(i));
                    out.flush();
                    out.close();


                    File temp = new File(information.getPath() + "\\" + users.get(i).getUserProf().getUserName() + ".txt");

                    userInformation[i].renameTo(temp);
                    userInformation[i] = temp;


                }catch (FileNotFoundException ferror){
                    System.out.println(ferror);
                }catch (IOException e){
                    System.out.println(e);
                }
            }
        }



    }



}


class UserAction extends Thread{
    private Socket socket ;
    private ArrayList<User> users = new ArrayList<User>();

    public UserAction(Socket socket, ArrayList<User> users) {
        this.socket = socket;
        this.users = users;
    }

    @Override
    public void run() {
        User thisUser;
        Show.setUsers(users);
        String client , server ;

        try{

            DataOutputStream dou = new DataOutputStream(socket.getOutputStream());
            DataInputStream din = new DataInputStream(socket.getInputStream());



            server = "1) Register\n" +
                    "2) Log in\n" +
                    "3) Exit\n"+
                    "choose a number : ";
            while (true){
                try {

                    dou.writeUTF(server);
                    dou.flush();


                    client = din.readUTF();





                    switch (client){
                        case "1" :  thisUser = Show.Register(socket) ; break;
                        case "2" : {
                            thisUser = Show.logIn(socket);
                            if (thisUser == null ){
                              //  din.readUTF();//test
                                continue;
                            }
                        }break;
                        case "3" : {

                            dou.writeUTF("Exit");

                            dou.flush();
                            din.close();


                            dou.close();
                            socket.close();
                            return;
                        }
                        default: throw new IllegalEntry("invalied entry, try again");

                    }
                    break;
                }catch (IllegalEntry i){
                   // System.out.println(i.getMessage());
                    dou.writeUTF(i.getMessage());
                    dou.flush();
                    din.readUTF();
                }
            }

            server = "1) Edit Profile\n" +
                    "2) Advertisements\n" +
                    "3) Add Advertising\n" +
                    "4) Exit\n"+

                    "choose a number : ";
            while (true){
                try{

                    dou.writeUTF(server);
                    dou.flush();


                    client = din.readUTF();

                    switch (client){
                        case "1" :  Show.ProfilePage(socket,thisUser) ; break;
                        case "2" :  Show.AdPage(socket,thisUser) ; break;
                        case "3" : Show.AdRegisterPage(socket,thisUser);break;
                        case "4" : {
                            dou.writeUTF("Exit");
                            dou.flush();
                            din.close();
                            dou.close();
                            socket.close();
                            return;}
                        default: throw new IllegalEntry("invalied entry, try again");

                    }
                }catch (IllegalEntry i){
                    dou.writeUTF(i.getMessage());
                    dou.flush();
                    din.readUTF();
                }
            }




        }catch (IOException e){
            System.out.println(e);
        }




    }
}


class User implements Serializable{
    private Profile userProf = new Profile();

    public Profile getUserProf() {
        return userProf;
    }

    public void setUserProf(Profile userProf) {
        this.userProf = userProf;
    }


}
class Profile implements Serializable{
    private String userName;
 //   private ArrayList<User> users = new ArrayList<User>();
    private String password;
    private String email;
    private String firstAndLastName;
    private String pictureAddress;

    private String birthYear ;
    private String birthMonth;

    private ArrayList<Advertising> userAd = new ArrayList<Advertising>();
    private ArrayList<Advertising> userFavoriteAd = new ArrayList<Advertising>();
    private String birthDay ;
    private  String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) throws  PhoneException {
        if(Pattern.matches("(\\+\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}" , phone)) {
            this.phone = phone;
        }
        else {
            throw new PhoneException("Invalid number ! please try again");
        }
    }

    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYear) throws YearException{
        int digit = Integer.parseInt(birthYear);
        if(digit>= 1950 && digit <= 2012) {
            this.birthYear = birthYear;
        }
        else {
            throw new YearException("your birth year must be between 1950 and 2012");
        }
    }

    public String getBirthMonth() {
        return birthMonth;
    }

    public void setBirthMonth(String birthMonth) throws MonthException {
        int month = Integer.parseInt(birthMonth);
        if(month>=1 && month<=12 ) {
            this.birthMonth = birthMonth;
        }else{
            throw new MonthException("your birth month most be between 1 and 12");
        }
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) throws DayException{
        int day = Integer.parseInt(birthDay);
        if(day>=1 && day<=31) {
            this.birthDay = birthDay;
        }
        else{
            throw new DayException("your birthday must be between 1 and 31 !");
        }
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName , ArrayList<User> users) throws IdException, DuplicateIdException {

        if(Pattern.matches("([a-zA-Z]{1,}[ ]?)+" , userName) && !Pattern.matches("(?=.*[!@#&()–[{}]:;',?/*~$^+=<>])" , userName)){

            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getUserProf().getUserName().equals(userName)){
                    throw new DuplicateIdException("Duplicate username");
                }

            }
            this.userName = userName;
        } else {
            throw  new IdException("your user name must include just characters, it can also have numbers. and your user name must'nt include ! , @, #, $, %, ^, &, *, (, ), _, +, ?, >, <");
        }






    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) throws PasswordException {
        if(Pattern.matches(  "(?=.*[0-9])(?=.*[a-z]).{8,}" , password)) {
            if (Pattern.matches(".*[a].*[a].*", password)  ){

                Pattern pattern = Pattern.compile("[0-9]{1,}");
                Matcher matcher = pattern.matcher(password);

                double temp , number;
                while (matcher.find()){

                    number = Double.parseDouble(matcher.group());

                    temp = Math.log(number) / Math.log(2);


                    if ( temp == (int)temp){
                        this.password = password;
                        return;
                    }

                }
                throw new PasswordException("your password must have atleast one number that is power of two");
            }else {
                throw new PasswordException("your password must includes at least two {a} ");
            }
        }
        else{
            throw new PasswordException("your password must include at least 8 lowercase letters and numbers");
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) throws EmailException {
        if(Pattern.matches("[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+" , email)){
            this.email = email;
            //throw new EmailException("invalid Email form ! please try again");
        }
        else{
            throw new EmailException("invalid Email form ! please try again");
        }
    }

    public String getFirstAndLastName() {
        return firstAndLastName;
    }

    public void setFirstAndLastName(String firstAndLastName) {
        this.firstAndLastName = firstAndLastName;
    }

    public String getPictureAddress() throws PhoneException {
        return pictureAddress;
    }

    public void setPictureAddress(String pictureAddress) throws PhotoException, FileNotFoundException {
        File f = new File(pictureAddress);
        if ( f.exists() ){
            this.pictureAddress = pictureAddress;
        }else {
            throw new FileNotFoundException("File not found");
        }
        String[] temp =  pictureAddress.split("/");
        String fileName = temp[temp.length - 1 ];
        if (Pattern.matches("[a-zA-Z]*[\\.]((jpeg)|(png)|(jpg))", fileName) ){
            this.pictureAddress = pictureAddress;
        }else {
            throw new PhotoException("Invalid File Format");
        }
    }


    public ArrayList<Advertising> getUserAd() {
        return userAd;
    }

    public void setUserAd(ArrayList<Advertising> userAd) {
        this.userAd = userAd;
    }


    public ArrayList<Advertising> getUserFavoriteAd() {
        return userFavoriteAd;
    }

    public void setUserFavoriteAd(ArrayList<Advertising> userFavoriteAd) {
        this.userFavoriteAd = userFavoriteAd;
    }
    public void setUserFavoriteAd(Advertising ad){
        userFavoriteAd.add(ad);
    }
}
class Advertising implements Serializable, Comparable{
    private String name;
    private String explaination;
    private String cost ;
    private String city;
    private String absoluteAddress;
    private String telephone;
    private  int adId  ;

    @Override
    public int hashCode() {
        adId = super.hashCode();
        return super.hashCode();
    }

    private  static int   adNumbers = 0 ;

    Advertising(){
        hashCode();

    }

    public int getAdId() {
        return adId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExplaination() {
        return explaination;
    }

    public void setExplaination(String explaination) {
        this.explaination = explaination;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAbsoluteAddress() {
        return absoluteAddress;
    }

    public void setAbsoluteAddress(String absoluteAddress) {
        this.absoluteAddress = absoluteAddress;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public int compareTo(Object o) {
        Advertising temp = (Advertising) o;
        int thisObject = Integer.parseInt(this.getCost()) , entryObject = Integer.parseInt(temp.getCost()) ;
        return thisObject - entryObject ;
    }

}


class Show {
    private static ArrayList<User> users = new ArrayList<User>();

    public static void setUsers(ArrayList<User> users) {
        Show.users = users;
    }

    public static User  Register(Socket socket){
        String client , server ;
        User newUser = new User();
       // Profile prof = new Profile();

        try{
            DataOutputStream dou = new DataOutputStream(socket.getOutputStream());
            DataInputStream din = new DataInputStream(socket.getInputStream());
            while (true){
                try {
                    server = "Enter your User Name : ";
                    dou.writeUTF(server);
                    dou.flush();
                    client = din.readUTF();
                    newUser.getUserProf().setUserName(client, users);
                    break;
                }catch (IdException id){
                    dou.writeUTF(id.getMessage());
                    dou.flush();
                    din.readUTF();
                }catch (DuplicateIdException dId){
                    dou.writeUTF(dId.getMessage());
                    dou.flush();
                    din.readUTF();
                }
            }

            while (true){
                try {
                    server = "Enter your Password : ";
                    dou.writeUTF(server);
                    dou.flush();
                    client = din.readUTF();
                    newUser.getUserProf().setPassword(client);
                    break;
                }catch (PasswordException p){
                    dou.writeUTF(p.getMessage());
                    dou.flush();
                    din.readUTF();
                }
            }

            while (true){
                try {
                    server = "Enter your Email : ";
                    dou.writeUTF(server);
                    dou.flush();
                    client = din.readUTF();
                    newUser.getUserProf().setEmail(client);
                    break;
                }catch (EmailException e){
                    dou.writeUTF(e.getMessage());
                    dou.flush();
                    din.readUTF();
                }
            }


        }catch (IOException i){
            System.out.println(i);
        }

        users.add(newUser);
        return newUser;
    }

    public static User logIn (Socket socket){

        String userName , password  , server ;
        User newUser = new User();

        try {
            DataOutputStream dou = new DataOutputStream(socket.getOutputStream());
            DataInputStream din = new DataInputStream(socket.getInputStream());

            while (true){
                try {
                    server = "Enter your User Name : ";
                    dou.writeUTF(server);
                    dou.flush();
                    userName =din.readUTF();


                    System.out.println();
                    server = "Enter your Password : ";
                    dou.writeUTF(server);
                    dou.flush();
                    password =din.readUTF();
                    for (int i = 0; i < users.size(); i++) {
                        if ( users.get(i).getUserProf().getUserName().equals(userName)){
                            if ( users.get(i).getUserProf().getPassword().equals(password)){
                                return  users.get(i);
                            }else{
                                throw new IncorrectPassword("Password is incorrect");
                            }
                        }
                    }


                    break;
                }catch (IncorrectPassword a){
                    dou.writeUTF(a.getMessage());
                    dou.flush();
                    din.readUTF();
                }
            }
            server = "user name doesn't exist\n";
            dou.writeUTF(server);
            dou.flush();
            din.readUTF();


        }catch (IOException i ){
            System.out.println(i);
        }
        return null;
    }

    public static  void ProfilePage(Socket socket, User user){
        String client , server  ;
        String[] temp ;
        User newUser = new User();



        try {
            DataOutputStream dou = new DataOutputStream(socket.getOutputStream());
            DataInputStream din = new DataInputStream(socket.getInputStream());

            while (true){
                try {
                    server = "Enter new User Name : ";
                    dou.writeUTF(server);
                    dou.flush();
                    client = din.readUTF();
                    newUser.getUserProf().setUserName(client, users);
                    break;
                }catch (IdException id){
                    dou.writeUTF(id.getMessage());
                    dou.flush();
                    din.readUTF();
                }catch (DuplicateIdException dId){
                    dou.writeUTF(dId.getMessage());
                    dou.flush();
                    din.readUTF();
                }
            }


            while (true){
                try {
                    server = "Enter new Password : ";
                    dou.writeUTF(server);
                    dou.flush();
                    client = din.readUTF();
                    newUser.getUserProf().setPassword(client);
                    break;
                }catch (PasswordException p){
                    dou.writeUTF(p.getMessage());
                    dou.flush();
                    din.readUTF();
                }
            }

            while (true){
                try {
                    server = "Enter new Email : ";
                    dou.writeUTF(server);
                    dou.flush();
                    client = din.readUTF();
                    newUser.getUserProf().setEmail(client);
                    break;
                }catch (EmailException e){
                    dou.writeUTF(e.getMessage());
                    dou.flush();
                    din.readUTF();
                }
            }

            while (true){

                server = "Enter first and last name : ";
                dou.writeUTF(server);
                dou.flush();
                client = din.readUTF();
                newUser.getUserProf().setFirstAndLastName(client);
                break;

            }

            while (true){
                try {
                    server = "Enter Image Address : ";
                    dou.writeUTF(server);
                    dou.flush();
                    client = din.readUTF();
                    newUser.getUserProf().setPictureAddress(client);
                    break;
                }catch (PhotoException e){
                    dou.writeUTF(e.getMessage());
                    dou.flush();
                    din.readUTF();
                }catch (FileNotFoundException p){
                    dou.writeUTF(p.getMessage());
                    dou.flush();
                    din.readUTF();
                }
            }

            while (true){
                try {
                    server = "Enter your Birth Date : (Year/Month/Day) ";

                    dou.writeUTF(server);
                    dou.flush();
                    client = din.readUTF();
                    temp = client.split("/");

                    newUser.getUserProf().setBirthYear(temp[0]);
                    newUser.getUserProf().setBirthMonth(temp[1]);
                    newUser.getUserProf().setBirthDay(temp[2]);
                    break;
                }catch (YearException e){
                    dou.writeUTF(e.getMessage());
                    dou.flush();
                    din.readUTF();
                }catch (MonthException m){
                    dou.writeUTF(m.getMessage());
                    dou.flush();
                    din.readUTF();
                }catch (DayException d ){
                    dou.writeUTF(d.getMessage());
                    dou.flush();
                    din.readUTF();
                }
            }


            while (true){
                try {
                    server = "Enter new Phone Number : ";
                    dou.writeUTF(server);
                    dou.flush();
                    client = din.readUTF();
                    newUser.getUserProf().setPhone(client);
                    break;
                }catch (PhoneException e){
                    dou.writeUTF(e.getMessage());
                    dou.flush();
                    din.readUTF();
                }
            }




        }catch (IOException e){
            System.out.println(e);
        }

    }


    public static  void AdRegisterPage(Socket socket, User user){

        String server , client ;

        Advertising newAd  = new Advertising();
        try{
            DataOutputStream dou = new DataOutputStream(socket.getOutputStream());
            DataInputStream din = new DataInputStream(socket.getInputStream());



            server = "Enter the ad Name (House, Car, Furniture, Digital Devices, Travel Accessories) : ";

            dou.writeUTF(server);
            dou.flush();
            client = din.readUTF();
            newAd.setName(client);



            server = "Enter the ad explaination : ";

            dou.writeUTF(server);
            dou.flush();
            client = din.readUTF();
            newAd.setExplaination(client);

            server = "Enter the ad city : ";

            dou.writeUTF(server);
            dou.flush();
            client = din.readUTF();
            newAd.setCity(client);

            server = "Enter the ad absoluteAddress : ";

            dou.writeUTF(server);
            dou.flush();
            client = din.readUTF();
            newAd.setAbsoluteAddress(client);

            server = "Enter the ad cost : ";

            dou.writeUTF(server);
            dou.flush();
            client = din.readUTF();
            newAd.setCost(client);



            server = "Enter the ad Telephone : ";

            dou.writeUTF(server);
            dou.flush();
            client = din.readUTF();
            newAd.setTelephone(client);



            user.getUserProf().getUserAd().add(newAd);


        }catch (IOException g){
            System.out.println(g);
        }

    }

    public static void AdPage(Socket socket, User user){


        String server , client ;
        int adIdChoosen;
        ArrayList<Advertising> ads = new ArrayList<Advertising>();
        for (int i = 0; i < users.size(); i++) {
            //sortUsers.add(users.get(i))  ;
            for (int j = 0; j < users.get(i).getUserProf().getUserAd().size(); j++) {
                ads.add(users.get(i).getUserProf().getUserAd().get(j));
            }

        }
        try {

            DataOutputStream dou = new DataOutputStream(socket.getOutputStream());
            DataInputStream din = new DataInputStream(socket.getInputStream());
            server = "choose filter :\n" +
                    "1) Default\n" +
                    "2) cost\n" +
                    "3) city\n" +
                    "4) whsh list\n" +
                    "5) search by name\n";
            try {
                while (true){

                    dou.writeUTF(server);
                    dou.flush();
                    client = din.readUTF();

                    switch (client){
                        case "1" :/*method*/ defaultShow(socket);break;
                        case "2" :costFilter(socket);break;
                        case "3" :CitySort(socket);break;
                        case "4" : {/*not developed yet*/

                            dou.writeUTF("not developed yet\n");
                            dou.flush();
                            din.readUTF(); //test
                            continue;
                        }
                        case "5" :SearchSort(socket);break;
                        default: throw new IllegalEntry("invalid entry");
                    }
                    break;
                }

            }catch (IllegalEntry i){
                dou.writeUTF(i.getMessage());
                dou.flush();
                din.readUTF();

            }
            server = "Enter your favorite Advertising Id (if you don't want anything , Enter a negative number): ";

           outer: while (true){

                dou.writeUTF(server);
                dou.flush();
                adIdChoosen = Integer.parseInt( din.readUTF());
                if (adIdChoosen < 0 ){
                    break;
                }
                for (int i = 0; i < ads.size(); i++) {
                    if ( adIdChoosen == ads.get(i).getAdId()){
                        user.getUserProf().getUserFavoriteAd().add(ads.get(i));
                        continue outer;
                    }

                }

                dou.writeUTF("Id does not found\n");
                dou.flush();
                din.readUTF(); //test

            }

        }catch (IOException e){
            System.out.println(e);
        }


    }

    public static void defaultShow (Socket socket){

        String server , client ;
        try{

            DataOutputStream dou = new DataOutputStream(socket.getOutputStream());
            DataInputStream din = new DataInputStream(socket.getInputStream());
            for (int i = 0; i < users.size(); i++) {
                for (int j = 0; j < users.get(i).getUserProf().getUserAd().size(); j++) {
                    server = "name : " + users.get(i).getUserProf().getUserAd().get(j).getName() + "  explanation : "
                            + users.get(i).getUserProf().getUserAd().get(j).getExplaination() + "\n";
                    server += "city : " + users.get(i).getUserProf().getUserAd().get(j).getCity() + "  cost : " + users.get(i).getUserProf().getUserAd().get(j).getCost() +
                            "\nId : " + users.get(i).getUserProf().getUserAd().get(j).getAdId()  ;


                    dou.writeUTF(server );
                    dou.flush();
                    din.readUTF(); //test
                }

            }

        }catch (IOException e){
            System.out.println(e);
        }



    }

    public static void costFilter(Socket socket){


        String server , client ;

        ArrayList<Advertising> ads = new ArrayList<Advertising>();
        for (int i = 0; i < users.size(); i++) {

            for (int j = 0; j < users.get(i).getUserProf().getUserAd().size(); j++) {
                ads.add(users.get(i).getUserProf().getUserAd().get(j));
            }

        }

        try {
            DataOutputStream dou = new DataOutputStream(socket.getOutputStream());
            DataInputStream din = new DataInputStream(socket.getInputStream());
            server = "1)descending \n" +
                    "2) ascending\n";
            dou.writeUTF(server);
            dou.flush();
            client = din.readUTF();
            switch (client){
                case "1": Collections.sort(ads); ;break;
                case "2":{
                    Collections.sort(ads);
                    Collections.reverse(ads);
                }break;
            }

        }catch (IOException e){
            System.out.println(e);
        }

        try{

            DataOutputStream dou = new DataOutputStream(socket.getOutputStream());
            DataInputStream din = new DataInputStream(socket.getInputStream());
            for (int i = 0; i < ads.size(); i++) {

                server = "name : " + ads.get(i).getName() +
                        "  explanation : " + ads.get(i).getExplaination() + "\n";
                server += "city : " + ads.get(i).getCity() +
                        "  cost : " + ads.get(i).getCost() + " Id : "+ ads.get(i).getAdId() ;

                dou.writeUTF(server );
                dou.flush();
                din.readUTF(); //test


            }

        }catch (IOException e){
            System.out.println(e);
        }

    }




    public static void CitySort(Socket socket){

        String server , client ;
        ArrayList<User> newsort = new ArrayList<User>();

        ArrayList<Advertising> ads = new ArrayList<Advertising>();
//        for (int i = 0; i < users.size(); i++) {
//
//            for (int j = 0; j < users.get(i).getUserProf().getUserAd().size(); j++) {
//                ads.add(users.get(i).getUserProf().getUserAd().get(j));
//            }
//
//        }

        User temp;
        try{
            DataOutputStream dou = new DataOutputStream(socket.getOutputStream());
            DataInputStream din = new DataInputStream(socket.getInputStream());
            server = "Enter city name : ";
            dou.writeUTF(server);
            dou.flush();
            String mycity =din.readUTF();
            for (int i = 0; i < users.size(); i++) {
                for (int j = 0; j < users.get(i).getUserProf().getUserAd().size(); j++) {
                    if (users.get(i).getUserProf().getUserAd().get(j).getCity().equals(mycity)){
                        ads.add(users.get(i).getUserProf().getUserAd().get(j));
                    }
                }
            }
//            for (int i = 0; i < ads.size(); i++) {
//                if (ads.get(i).getCity().equals(mycity)){
//                    newsort.add()
//                }
//            }

          //  DataOutputStream dou = new DataOutputStream(socket.getOutputStream());
//            for (int i = 0; i < newsort.size(); i++) {
//                for (int j = 0; j < newsort.get(i).getUserProf().getUserAd().size(); j++) {
//                    server = "name : " + newsort.get(i).getUserProf().getUserAd().get(j).getName() + "  explanation : " + newsort.get(i).getUserProf().getUserAd().get(j).getExplaination() + "\n";
//                    server += "city : " + newsort.get(i).getUserProf().getUserAd().get(j).getCity() + "  cost : " + newsort.get(i).getUserProf().getUserAd().get(j).getCost()  + " Id : " + newsort.get(i).getUserProf().getUserAd().get(j).getAdId()  ;
//
//
//                    dou.writeUTF(server );
//                    dou.flush();
//                    din.readUTF(); //test
//                }
//
//            }

            for (int i = 0; i < ads.size(); i++) {

                server = "name : " + ads.get(i).getName() +
                        "  explanation : " + ads.get(i).getExplaination() + "\n";
                server += "city : " + ads.get(i).getCity() +
                        "  cost : " + ads.get(i).getCost() + " Id : "+ ads.get(i).getAdId() ;

                dou.writeUTF(server );
                dou.flush();
                din.readUTF(); //test


            }

        }catch (IOException e){
            System.out.println(e);
        }
    }




    public static void SearchSort(Socket socket) {

        String server, client;
        ArrayList<Advertising> mysearch = new ArrayList<Advertising>();
        try{
            DataOutputStream dou = new DataOutputStream(socket.getOutputStream());
            DataInputStream din = new DataInputStream(socket.getInputStream());


            server = "specify your shopping area :\n" +
                    "1) House\n" +
                    "2) Car\n" +
                    "3) Furniture\n" +
                    "4) Digital Devices \n" +
                    "5) Travel Accessories\n";
            dou.writeUTF(server);
            dou.flush();
            try{
                while (true) {

                    client = din.readUTF();
                    switch (client) {
                        case "1":mysearch =Search("House");
                            ;
                            break;
                        case "2":mysearch =Search("Car");
                            ;
                            break;
                        case "3":mysearch =Search(" Furniture");
                            ;
                            break;
                        case "4":mysearch =Search("Digital Devices");
                            ;
                            break;
                        case "5":mysearch =Search("Travel Accessories");
                            ;
                            break;
                        default:
                            throw  new IllegalEntry("invalid entry");
                    }
                    break;
                }
            }catch (IllegalEntry i){
                dou.writeUTF(i.getMessage());
                dou.flush();
                din.readUTF();
            }



                for (int j = 0; j < mysearch.size(); j++) {
                    server = "name : " + mysearch.get(j).getName() + "  explanation : " + mysearch.get(j).getExplaination() + "\n";
                    server += "city : " + mysearch.get(j).getCity() + "  cost : " + mysearch.get(j).getCost() + " Id : " +  mysearch.get(j).getAdId() ;


                    dou.writeUTF(server );
                    dou.flush();
                    din.readUTF(); //test
                }



        } catch (IOException e) {
            System.out.println(e);
        }



    }
    public static ArrayList<Advertising> Search(String myclient){
        String server;

        ArrayList<Advertising> ads = new ArrayList<Advertising>();

        //ArrayList<User> Searchsort = new ArrayList<User>();

        for (int i = 0; i < users.size(); i++) {
            for (int j = 0; j < users.get(i).getUserProf().getUserAd().size(); j++) {
                if (users.get(i).getUserProf().getUserAd().get(j).getName().equals(myclient)){
                    ads.add(users.get(i).getUserProf().getUserAd().get(j));
                }
            }
        }

        return ads;
    }


}







class PhoneException extends Exception{
    PhoneException(String  s){
        super(s);
    }

}
class EmailException extends Exception{

    EmailException(String  s){
        super(s);
    }

}
class DayException extends Exception{

    DayException(String  s){
        super(s);
    }

}
class MonthException extends Exception{


    MonthException(String  s){
        super(s);
    }

}
class YearException extends Exception{

    YearException(String  s){
        super(s);
    }

}
class IdException extends Exception{

    IdException(String  s){
        super(s);
    }

}

class DuplicateIdException extends Exception{

    DuplicateIdException(String  s){
        super(s);
    }

}
class PasswordException extends Exception{


    PasswordException(String s){
        super(s);
    }

}


class IllegalEntry extends Exception{


    IllegalEntry(String s){
        super(s);
    }
}

class IncorrectPassword extends Exception{


    IncorrectPassword(String s){
        super(s);
    }
}


class PhotoException extends Exception{


    public PhotoException(String message) {
        super(message);
    }
}

