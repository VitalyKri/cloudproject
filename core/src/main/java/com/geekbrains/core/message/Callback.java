package com.geekbrains.core.message;

@FunctionalInterface
public interface Callback<E extends Message> {

        void onReceive(E message);

}
