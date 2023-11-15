public class Result {
    String output;

    String input;

    String key;

    public Result(String output) {
        this.output = output;
    }

    public Result(String output, String input, String key) {
        this.output = output;
        this.input = input;
        this.key = key;
    }

    public String getOutput() {
        return output;
    }

    public String getInput() {
        return input;
    }

    public String getKey() {
        return key;
    }
}
