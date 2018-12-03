package Main;

import java.util.*;

public class Param {
    private List<String> rawArgs = new ArrayList();
    private Map<String, String> namedParams = new HashMap();
    private List<String> unnamedParams = new ArrayList();
    private Map<String, String> readonlyNamedParams = null;

    public Param(String[] args) {
        if (args != null) {
            this.init(Arrays.asList(args));
        }
    }

    private void init(List<String> args) {
        Iterator var2 = args.iterator();

        while(var2.hasNext()) {
            String arg = (String)var2.next();
            if (arg != null) {
                this.rawArgs.add(arg);
            }
        }

        this.computeNamedParams();
        this.computeUnnamedParams();
    }


    private boolean validFirstChar(char c) {
        return Character.isLetter(c) || c == '_';
    }


    private boolean isNamedParam(String arg) {
        if (!arg.startsWith("--")) {
            return false;
        } else {
            return arg.indexOf(61) > 2 && this.validFirstChar(arg.charAt(2));
        }
    }

    private void computeUnnamedParams() {
        Iterator var1 = this.rawArgs.iterator();

        while(var1.hasNext()) {
            String arg = (String)var1.next();
            if (!this.isNamedParam(arg)) {
                this.unnamedParams.add(arg);
            }
        }

    }

    private void computeNamedParams() {
        Iterator var1 = this.rawArgs.iterator();

        while(var1.hasNext()) {
            String arg = (String)var1.next();
            if (this.isNamedParam(arg)) {
                int eqIdx = arg.indexOf(61);
                String key = arg.substring(2, eqIdx);
                String value = arg.substring(eqIdx + 1);
                this.namedParams.put(key, value);
            }
        }

    }

    public Map<String, String> getNamed() {
        if (this.readonlyNamedParams == null) {
            this.readonlyNamedParams = Collections.unmodifiableMap(this.namedParams);
        }

        return this.readonlyNamedParams;
    }
}
