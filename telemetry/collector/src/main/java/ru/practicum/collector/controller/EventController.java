package ru.practicum.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.collector.handler.HubEventHandler;
import ru.practicum.collector.handler.SensorEventHandler;
import ru.yandex.practicum.protobuf.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.protobuf.telemetry.event.HubEventProto;
import ru.yandex.practicum.protobuf.telemetry.event.SensorEventProto;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubEventHandlers;


    // Spring найдёт все реализации интерфейса SensorEventHandler
    // и внедрит в виде коллекции; здесь мы указали,
    // чтобы это было множество — Set (но можно и Collection, и List)
    public EventController(Set<SensorEventHandler> sensorEventHandlers, Set<HubEventHandler> hubEventHandlers) {
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(
                        SensorEventHandler::getMessageType,
                        Function.identity()
                ));
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getMessageType,
                        Function.identity()
                ));
    }

    /**
     * Метод для обработки событий от датчиков.
     * Вызывается при получении нового события от gRPC-клиента.
     *
     * @param sensorEvent           Событие от датчика
     * @param responseObserver  Ответ для клиента
     */
    @Override
    public void collectSensorEvent(SensorEventProto sensorEvent, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Эндпоинт для обработки событий от датчиков sensorEvent = {}", sensorEvent);
            // проверяем, есть ли обработчик для полученного события
            if (sensorEventHandlers.containsKey(sensorEvent.getPayloadCase())) {
                // если обработчик найден, передаём событие ему на обработку
                sensorEventHandlers.get(sensorEvent.getPayloadCase()).handle(sensorEvent);
            } else {
                throw new IllegalArgumentException("Не могу найти обработчик для события " + sensorEvent.getPayloadCase());
            }

            // после обработки события возвращаем ответ клиенту
            responseObserver.onNext(Empty.getDefaultInstance());
            // и завершаем обработку запроса
            responseObserver.onCompleted();
        } catch (Exception e) {
            // в случае исключения отправляем ошибку клиенту
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }

    /**
     * Метод для обработки событий от хаба.
     * Вызывается при получении нового события от gRPC-клиента.
     *
     * @param hubEvent           Событие от хаба
     * @param responseObserver  Ответ для хаба
     */
    @Override
    public void collectHubEvent(HubEventProto hubEvent, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Эндпоинт для обработки событий от хаба hubEvent = {}", hubEvent);
            if(hubEventHandlers.containsKey(hubEvent.getPayloadCase())) {
                hubEventHandlers.get(hubEvent.getPayloadCase()).handle(hubEvent);
            } else {
                throw new IllegalArgumentException("Не могу найти обработчик для события " + hubEvent.getPayloadCase());
            }
            // после обработки события возвращаем ответ клиенту
            responseObserver.onNext(Empty.getDefaultInstance());
            // и завершаем обработку запроса
            responseObserver.onCompleted();
        } catch (Exception e) {
            // в случае исключения отправляем ошибку клиенту
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }
}
