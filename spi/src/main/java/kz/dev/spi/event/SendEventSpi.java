package kz.dev.spi.event;

public interface SendEventSpi {
    void send(String topic, Object payload);
}
