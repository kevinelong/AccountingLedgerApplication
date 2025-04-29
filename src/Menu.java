import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
public class Menu {
    ArrayList<Action> list;
    HashMap<String, Action> map;
    String name;
    String exitCommand;
    Scanner in = new Scanner(System.in);
    Menu(String name, ArrayList<Action> list, String exitCommand){
        this.name = name;
        this.exitCommand = exitCommand;
        this.list = list;
        this.map = new HashMap<>();
        for(Action a : list){
            this.map.put(a.command.toUpperCase(), a);
        }
    }
    public void go(){
        String command = "";
        while(true){
            this.show();
            command = in.nextLine().trim().toUpperCase();
            if(command.equalsIgnoreCase(this.exitCommand)){
                break;
            }
            this.handle(command);
        }
    }
    protected void handle(String command){
        if(map.containsKey(command)) {
            map.get(command).runnable.run();
        }else{
            System.out.printf("\nNo such command: \"%s\".\n", command);
        }
    }
    protected void show(){
        System.out.printf("\n\t%s:\n", this.name);
        for(Action a: this.list){
            System.out.printf("\t\t%s) %s\n", a.command, a.description);
        }
        System.out.printf("\t%s to exit\n", this.exitCommand);
    }
}