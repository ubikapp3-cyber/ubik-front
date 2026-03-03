export interface Reservation {
  id: number;
  motel_id: number;
  room_id: number;
  user_id: number;
  total_price: number;
  start_date: Date;
  end_date: Date;
  status: 'pending' | 'confirmed' | 'cancelled';
  special_requests: string;
  created_at: Date;
  updated_at: Date;
}
