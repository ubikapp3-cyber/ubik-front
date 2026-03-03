import {
  Component,
  AfterViewInit,
  Input,
  inject,
  PLATFORM_ID,
  OnChanges,
  SimpleChanges,
  ElementRef,
  ViewChild,
} from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { getUserLocation } from './geolocation';

export interface MapPoint {
  lat: number;
  lng: number;
  name: string;
  id?: number;
}

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './map.html',
  styleUrls: ['./map.css'],
})
export class Map implements AfterViewInit, OnChanges {
  @Input() points: MapPoint[] = [];
  @Input() active: MapPoint | null = null;

  @ViewChild('mapContainer', { static: false })
  mapContainer!: ElementRef<HTMLDivElement>;

  private platformId = inject(PLATFORM_ID);

  private map!: import('leaflet').Map;
  private L!: typeof import('leaflet');
  private markerLayer!: import('leaflet').LayerGroup;
  private userLatLng?: [number, number];

  /* =========================
     INIT
  ========================== */

  async ngAfterViewInit() {
    if (!isPlatformBrowser(this.platformId)) return;

    // Import dinámico correcto para SSR
    const leaflet = await import('leaflet');
    this.L = (leaflet as any).default ?? leaflet;

    this.map = this.L.map(this.mapContainer.nativeElement, {
      center: [4.6, -74.1],
      zoom: 6,
    });

    delete (this.L.Icon.Default.prototype as any)._getIconUrl;

  this.L.Icon.Default.mergeOptions({
    iconRetinaUrl: 'assets/icons/leaflet/marker-icon-2x.png',
    iconUrl: 'assets/icons/leaflet/marker-icon.png',
    shadowUrl: 'assets/icons/leaflet/marker-shadow.png',
  });

    this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
    }).addTo(this.map);
    this.L.tileLayer(
      'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      {
        attribution: '© OpenStreetMap contributors',
      }
    ).addTo(this.map);

    this.markerLayer = this.L.layerGroup().addTo(this.map);

    setTimeout(() => {
      this.map.invalidateSize();
    });

    this.renderMarkers();
    this.initializeLocation();
  }

  /* =========================
     INPUT CHANGES
  ========================== */

  ngOnChanges(changes: SimpleChanges) {
    if (!this.map) return;

    if (changes['points']) {
      this.renderMarkers();
    }

    if (changes['active'] && this.active) {
      this.map.flyTo([this.active.lat, this.active.lng], 17);
    }
  }

  /* =========================
     USER LOCATION
  ========================== */

  private async initializeLocation() {
    try {
      const location = await getUserLocation();

      this.userLatLng = [location.latitude, location.longitude];

      this.L.marker(this.userLatLng)
        .addTo(this.map)
        .bindPopup('Tú estás aquí');

      this.adjustView();
    } catch {
      this.adjustView();
    }
  }

  /* =========================
     MARKERS
  ========================== */

  private renderMarkers() {
    if (!this.markerLayer) return;

    this.markerLayer.clearLayers();

    for (const p of this.points) {
      this.L.marker([p.lat, p.lng])
        .bindPopup(p.name)
        .addTo(this.markerLayer);
    }

    this.adjustView();
  }

  /* =========================
     VIEW ADJUSTMENT
  ========================== */

  private adjustView() {
    if (!this.map) return;

    const allPoints: [number, number][] = this.points.map(p => [p.lat, p.lng]);

    if (this.userLatLng) {
      allPoints.push(this.userLatLng);
    }

    if (allPoints.length === 0) {
      this.map.setView([4.6, -74.1], 6);
      return;
    }

    if (allPoints.length === 1) {
      this.map.setView(allPoints[0], 16);
      return;
    }

    const bounds = this.L.latLngBounds(allPoints);
    this.map.fitBounds(bounds, { padding: [50, 50] });
  }
}