import { Room } from "./room.model";
import { Service } from "./services.model";

export interface RoomService {
  room_id?: Room[]; 
  service_id: Service[];
}