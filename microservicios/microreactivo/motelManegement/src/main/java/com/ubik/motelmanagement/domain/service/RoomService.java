package com.ubik.motelmanagement.domain.service;

import com.ubik.motelmanagement.domain.model.Room;
import com.ubik.motelmanagement.domain.port.in.RoomUseCasePort;
import com.ubik.motelmanagement.domain.port.out.MotelRepositoryPort;
import com.ubik.motelmanagement.domain.port.out.NotificationPort;
import com.ubik.motelmanagement.domain.port.out.RoomRepositoryPort;
import com.ubik.motelmanagement.domain.port.out.UserPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RoomService implements RoomUseCasePort {

    private final RoomRepositoryPort roomRepositoryPort;
    private final MotelRepositoryPort motelRepositoryPort;
    private final NotificationPort notificationPort;
    private final UserPort userPort;

    public RoomService(RoomRepositoryPort roomRepositoryPort, MotelRepositoryPort motelRepositoryPort, NotificationPort notificationPort, UserPort userPort) {
        this.roomRepositoryPort = roomRepositoryPort;
        this.motelRepositoryPort = motelRepositoryPort;
        this.notificationPort = notificationPort;
        this.userPort = userPort;
    }

    @Override
    public Mono<Room> createRoom(Room room) {
        return motelRepositoryPort.findById(room.motelId())
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Motel no encontrado con ID: " + room.motelId())
                ))
                .flatMap(motel -> {
                    if (motel.approvalStatus() != com.ubik.motelmanagement.domain.model.ApprovalStatus.APPROVED) {
                        return Mono.error(new IllegalStateException("El motel no está aprobado. Solo se pueden crear o editar habitaciones en moteles aprobados."));
                    }
                    return validateRoom(room)
                            .then(roomRepositoryPort.save(room))
                            .flatMap(savedRoom ->
                                userPort.getUserById(motel.propertyId())
                                        .flatMap(user -> {
                                            String priceFormatted = String.format("%,.2f", savedRoom.price());
                                            String createdAt = java.time.LocalDateTime.now()
                                                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

                                            return notificationPort.sendRoomCreationNotification(
                                                            user.email(),
                                                            motel.name(),
                                                            savedRoom.roomType(),
                                                            savedRoom.number(),
                                                            priceFormatted,
                                                            createdAt
                                                    )
                                                    .thenReturn(savedRoom);
                                        })
                            );
                });
    }

    @Override
    public Mono<Room> getRoomById(Long id) {
        return roomRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Habitación no encontrada con ID: " + id)));
    }

    @Override
    public Flux<Room> getAllRooms() {
        return roomRepositoryPort.findAll();
    }

    @Override
    public Flux<Room> getRoomsByMotelId(Long motelId) {
        return roomRepositoryPort.findByMotelId(motelId);
    }

    @Override
    public Flux<Room> getAvailableRoomsByMotelId(Long motelId) {
        return roomRepositoryPort.findAvailableByMotelId(motelId);
    }

    @Override
    public Mono<Room> updateRoom(Long id, Room room) {
        return roomRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Habitación no encontrada con ID: " + id)))
                .flatMap(existingRoom -> 
                    motelRepositoryPort.findById(existingRoom.motelId())
                            .flatMap(motel -> {
                                if (motel.approvalStatus() != com.ubik.motelmanagement.domain.model.ApprovalStatus.APPROVED) {
                                    return Mono.error(new IllegalStateException("El motel no está aprobado. Solo se pueden crear o editar habitaciones en moteles aprobados."));
                                }
                                Room updatedRoom = new Room(
                                        id,
                                        existingRoom.motelId(),
                                        room.number(),
                                        room.roomType(),
                                        room.price(),
                                        room.description(),
                                        room.isAvailable(),
                                        room.imageUrls(),
                                        room.latitude(),
                                        room.longitude(),
                                        existingRoom.motelName(),
                                        existingRoom.motelAddress(),
                                        existingRoom.motelCity(),
                                        existingRoom.motelPhoneNumber(),
                                        room.serviceIds()
                                );
                                return validateRoom(updatedRoom).then(roomRepositoryPort.update(updatedRoom));
                            })
                );
    }

    @Override
    public Mono<Void> deleteRoom(Long id) {
        return roomRepositoryPort.existsById(id)
                .flatMap(exists -> {
                    if (!exists) return Mono.error(new RuntimeException("Habitación no encontrada con ID: " + id));
                    return roomRepositoryPort.deleteById(id);
                });
    }

    private Mono<Void> validateRoom(Room room) {
        if (room.number() == null || room.number().trim().isEmpty())
            return Mono.error(new IllegalArgumentException("El número de habitación es requerido"));
        if (room.roomType() == null || room.roomType().trim().isEmpty())
            return Mono.error(new IllegalArgumentException("El tipo de habitación es requerido"));
        if (room.price() == null || room.price() <= 0)
            return Mono.error(new IllegalArgumentException("El precio debe ser mayor que cero"));
        if (room.imageUrls() != null && room.imageUrls().size() > 15)
            return Mono.error(new IllegalArgumentException("No se pueden agregar más de 15 imágenes"));
        if (room.latitude() != null && (room.latitude() < -90.0 || room.latitude() > 90.0))
            return Mono.error(new IllegalArgumentException("La latitud debe estar entre -90 y 90"));
        if (room.longitude() != null && (room.longitude() < -180.0 || room.longitude() > 180.0))
            return Mono.error(new IllegalArgumentException("La longitud debe estar entre -180 y 180"));
        return Mono.empty();
    }
}