package cf.tinkerit.generator.grpc.impl;

import cf.tinkerit.generator.grpc.logging.Logger;

public class DummyLogger implements Logger {
    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(CharSequence var1) {

    }

    @Override
    public void debug(CharSequence var1, Throwable var2) {

    }

    @Override
    public void debug(Throwable var1) {

    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(CharSequence var1) {

    }

    @Override
    public void info(CharSequence var1, Throwable var2) {

    }

    @Override
    public void info(Throwable var1) {

    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(CharSequence var1) {

    }

    @Override
    public void warn(CharSequence var1, Throwable var2) {

    }

    @Override
    public void warn(Throwable var1) {

    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(CharSequence var1) {

    }

    @Override
    public void error(CharSequence var1, Throwable var2) {

    }

    @Override
    public void error(Throwable var1) {

    }
}
