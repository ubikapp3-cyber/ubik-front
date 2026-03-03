  import { RoomService } from './room_services.model';
  import { RoomImage } from './room_images.model';

  export interface Room {
  id: number;
  motelId: number;
  motelName: string;
  motelCity: string;
  motelAddress: string;
  motelPhoneNumber: string;
  number: string;
  roomType: string;
  price: number;
  description: string;
  imageUrls: string[];
  serviceIds: number[];
  latitude: number;
  longitude: number;
  isAvailable: boolean;
}
