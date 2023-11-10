// public so it can be used for exceptions
public class ReturnType {
    enum ReturnKind {
        Normal, Break, Continue, Return
    }

    private Optional<String> returnValue = Optional.empty();
    private ReturnKind returnKind;

    public Optional<String> getReturnValue() {
        return returnValue;
    }

    public ReturnKind getReturnKind() {
        return returnKind;
    }

    public ReturnType(String retunrValue, ReturnType.ReturnKind returnKind) {
        this.returnValue = Optional.of(retunrValue);
        this.returnKind = returnKind;
    }

    public ReturnType(ReturnType.ReturnKind returnKind) {
        this.returnKind = returnKind;
    }
}