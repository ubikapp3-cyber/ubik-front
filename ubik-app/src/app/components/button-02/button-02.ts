import { CommonModule } from '@angular/common';
import { Component, Input, Output } from '@angular/core';
import { Router } from '@angular/router';
import { EventEmitter } from '@angular/core';

@Component({
  selector: 'app-button-02',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './button-02.html',
})
export class Button02 {
  @Output() clicked = new EventEmitter<void>();

  @Input() text!: string;
  @Input() routerLink?: string;
  @Input() queryParams?: { [key: string]: any } | null;
  @Input() iconLeft?: string;
  @Input() iconRight?: string;

  // si es true el botón ocupará el 100%
  @Input() fullWidth: boolean = false;

  constructor(private router: Router) {}

  navigate() {
    if (this.routerLink) {
      const link = Array.isArray(this.routerLink) ? this.routerLink : [this.routerLink];
      const extras = this.queryParams ? { queryParams: this.queryParams } : undefined;
      if (extras) this.router.navigate(link, extras);
      else this.router.navigate(link);
      return;
    }

    this.clicked.emit();
  }
}
