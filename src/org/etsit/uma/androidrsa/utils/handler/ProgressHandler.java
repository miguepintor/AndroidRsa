
package org.etsit.uma.androidrsa.utils.handler;

public interface ProgressHandler {

    public void setTotal(int tot);

    public void increment(int inc);

    public void finished();
}
