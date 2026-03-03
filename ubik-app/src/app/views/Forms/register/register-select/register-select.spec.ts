import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterSelect } from './register-select';

describe('RegisterSelect', () => {
  let component: RegisterSelect;
  let fixture: ComponentFixture<RegisterSelect>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterSelect], // si es standalone
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterSelect);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

