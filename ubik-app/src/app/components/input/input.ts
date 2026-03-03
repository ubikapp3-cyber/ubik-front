import { CommonModule } from '@angular/common';
import { Component, Input, forwardRef, Output, EventEmitter } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-input',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './input.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => Inputcomponent),
      multi: true,
    },
  ],
})
export class Inputcomponent implements ControlValueAccessor {
  @Input() label?: string;
  @Input() placeholder: string = '';
  @Input() type: string = 'text';
  @Input() hasToggle: boolean = false; // show eye toggle inside input
  @Input() visible: boolean = false; // visibility state for password
  @Output() toggle: EventEmitter<void> = new EventEmitter();

  value: string = '';
  disabled = false;

  // 🔥 DEBEN SER PÚBLICAS
  onChange: (_: any) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(value: any): void {
    this.value = value ?? '';
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }
}
