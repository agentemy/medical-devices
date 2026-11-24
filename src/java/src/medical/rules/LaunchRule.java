package medical.rules;
public abstract class LaunchRule implements ReadinessRule {
    protected void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
