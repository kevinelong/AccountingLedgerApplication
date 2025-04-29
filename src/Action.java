public class Action{
    String command;
    String description;
    Runnable runnable;
    Action(String command, String description, Runnable runnable){
        this.command = command;
        this.description = description;
        this.runnable = runnable;
    }
}
