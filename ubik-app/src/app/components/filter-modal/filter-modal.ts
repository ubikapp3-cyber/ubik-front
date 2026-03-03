import { Component, inject, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogRef } from '@angular/cdk/dialog';

export interface ExploreFilters {
  priceMin: number | null;
  priceMax: number | null;
  roomTypes: string[];
  cities: string[];
  onlyAvailable: boolean;
  serviceIds: number[];
  sortBy: 'priceAsc' | 'priceDesc' | null;
}

@Component({
  selector: 'app-filter-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './filter-modal.html',
})
export class FilterModal {
  private dialog = inject(DialogRef<ExploreFilters>);

  @Input() roomTypes: string[] = [];
  @Input() cities: string[] = [];
  @Input() services: { id: number; name: string }[] = [];

  filters: ExploreFilters = {
    priceMin: null,
    priceMax: null,
    roomTypes: [],
    cities: [],
    onlyAvailable: false,
    serviceIds: [],
    sortBy: null,
  };

  close() {
    this.dialog.close();
  }

  toggleArrayValue(array: any[], value: any) {
    const index = array.indexOf(value);
    if (index >= 0) {
      array.splice(index, 1);
    } else {
      array.push(value);
    }
  }

  onRoomTypeChange(e: Event) {
    const value = (e.target as HTMLInputElement).value;
    this.toggleArrayValue(this.filters.roomTypes, value);
  }

  onCityChange(e: Event) {
    const value = (e.target as HTMLInputElement).value;
    this.toggleArrayValue(this.filters.cities, value);
  }

  onServiceChange(e: Event) {
    const value = Number((e.target as HTMLInputElement).value);
    this.toggleArrayValue(this.filters.serviceIds, value);
  }

  onPriceMinChange(e: Event) {
    const v = Number((e.target as HTMLInputElement).value);
    this.filters.priceMin = v || null;
  }

  onPriceMaxChange(e: Event) {
    const v = Number((e.target as HTMLInputElement).value);
    this.filters.priceMax = v || null;
  }

  onAvailabilityChange(e: Event) {
    this.filters.onlyAvailable = (e.target as HTMLInputElement).checked;
  }

  onSortChange(e: Event) {
    this.filters.sortBy =
      ((e.target as HTMLSelectElement).value as any) || null;
  }

  reset() {
    this.filters = {
      priceMin: null,
      priceMax: null,
      roomTypes: [],
      cities: [],
      onlyAvailable: false,
      serviceIds: [],
      sortBy: null,
    };
  }

  apply() {
    this.dialog.close(this.filters);
  }
}